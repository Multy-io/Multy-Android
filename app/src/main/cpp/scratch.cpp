#include <jni.h>
#include <android/log.h>
#include <sys/types.h>
#include <cstdio>
#include <unistd.h>
#include <pthread.h>
#include <multy_test/run_tests.h>
#include <multy_core/common.h>
#include <multy_core/error.h>
#include <multy_core/mnemonic.h>
#include <multy_core/internal/u_ptr.h>
#include <vector>
#include <multy_core/internal/utility.h>
#include <multy_core/keys.h>
#include <multy_core/account.h>
#include <cstring>
#include "multy_transaction/transaction.h"
#include <multy_transaction/internal/u_ptr.h>
#include <multy_transaction/internal/properties.h>
#include <multy_transaction/properties.h>
#include "multy_transaction/internal/amount.h"
#include "multy_transaction/internal/transaction.h"
#include "multy_test/utility.h"


JavaVM *gJvm = nullptr;
static jobject gClassLoader;
static jmethodID gFindClassMethod;

JNIEnv *getEnv() {
    JNIEnv *env;
    int status = gJvm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (status < 0) {
        status = gJvm->AttachCurrentThread(&env, NULL);
        if (status < 0) {
            return nullptr;
        }
    }
    return env;
}

#ifdef __cplusplus
extern "C" {
#endif

static int pfd[2];
static pthread_t thr;
static const char *tag = "myapp";

static void *thread_func(void *);

int start_logger(const char *app_name) {
    tag = app_name;

    /* make stdout line-buffered and stderr unbuffered */
    setvbuf(stdout, 0, _IOLBF, 0);
    setvbuf(stderr, 0, _IONBF, 0);

    /* create the pipe and redirect stdout and stderr */
    pipe(pfd);
    dup2(pfd[1], 1);
    dup2(pfd[1], 2);

    /* spawn the logging thread */
    if (pthread_create(&thr, 0, thread_func, 0) == -1)
        return -1;
    pthread_detach(thr);
    return 0;
}

static void *thread_func(void *) {
    ssize_t rdsz;
    char buf[128];
    while ((rdsz = read(pfd[0], buf, sizeof buf - 1)) > 0) {
        if (buf[rdsz - 1] == '\n') --rdsz;
        buf[rdsz] = 0;  /* add null-terminator */
        __android_log_write(ANDROID_LOG_DEBUG, tag, buf);
    }
    return 0;
}

void throw_java_exception(JNIEnv *env, const Error &error) {
    jclass c = env->FindClass("io/multy/util/JniException");
//    __android_log_print(ANDROID_LOG_INFO, "log message", "Error: %s", error->message);
    env->ThrowNew(c, error.message);
}

#define ERSOR(statement, value) do { ErrorPtr error(statement); if (error) {throw_java_exception(env, *error); return (value);} } while(false)

JNIEXPORT jint JNICALL
Java_io_multy_util_NativeDataHelper_runTest(JNIEnv *jenv, jclass jcls) {
    jint jresult = 0;

    start_logger("hallow");
    __android_log_print(ANDROID_LOG_INFO, "foo", "Error: %s", "Hallow");
    char *foo = (char *) "foo";
    jresult = run_tests(1, &foo);
    return jresult;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *pjvm, void *reserved) {
    using namespace wallet_core::internal;

    gJvm = pjvm;  // cache the JavaVM pointer
    auto env = getEnv();
    auto randomClass = env->FindClass("io/multy/util/EntropyProvider");
    jclass classClass = env->GetObjectClass(randomClass);
    auto classLoaderClass = env->FindClass("java/lang/ClassLoader");
    auto getClassLoaderMethod = env->GetMethodID(classClass, "getClassLoader",
                                                 "()Ljava/lang/ClassLoader;");
    gClassLoader = env->CallObjectMethod(randomClass, getClassLoaderMethod);
    gFindClassMethod = env->GetMethodID(classLoaderClass, "findClass",
                                        "(Ljava/lang/String;)Ljava/lang/Class;");

    return JNI_VERSION_1_6;
}

jclass findClass(const char *name) {
    return static_cast<jclass>(getEnv()->CallObjectMethod(gClassLoader, gFindClassMethod,
                                                          getEnv()->NewStringUTF(name)));
}

JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_makeSeed(JNIEnv *env, jobject obj, jstring string) {
    using namespace wallet_core::internal;
    const char *mnemonic = env->GetStringUTFChars(string, JNI_FALSE);

    BinaryDataPtr data;
    ErrorPtr error;

    ERSOR(make_seed(mnemonic, "", reset_sp(data)), jbyteArray());
    env->ReleaseStringUTFChars(string, mnemonic);

    jbyteArray array = env->NewByteArray(data.get()->len);
    env->SetByteArrayRegion(array, 0, data.get()->len, reinterpret_cast<const jbyte *>(data->data));
    return array;
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_makeMnemonic(JNIEnv *jniEnv, jobject obj) {

    using namespace wallet_core::internal;

    auto env = getEnv();
    auto randomClass = env->FindClass("io/multy/util/EntropyProvider");

    jmethodID mid = env->GetStaticMethodID(randomClass, "generateKey", "(I)[B");
    jbyteArray result = (jbyteArray) env->CallStaticObjectMethod(randomClass, mid, 160);

    typedef std::vector<unsigned char> bytes;
    int len = env->GetArrayLength(result);
    bytes buf(len, 0);
    env->GetByteArrayRegion(result, 0, len, reinterpret_cast<jbyte *>(buf.data()));

    auto fill_entropy = [](void *data, ::size_t size, void *dest) -> ::size_t {
        const bytes *entropy = (const bytes *) (data);
        const size_t result_size = std::min(size, entropy->size());
        memcpy(dest, entropy->data(), result_size);
        return result_size;
    };
    auto entropy_source = EntropySource{(void *) &buf, fill_entropy};

    ConstCharPtr mnemonic_str;

    ERSOR(make_mnemonic(entropy_source, reset_sp(mnemonic_str)), jstring());
    return jniEnv->NewStringUTF(mnemonic_str.get());
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_makeAccountId(JNIEnv *env, jobject obj, jbyteArray array) {

    using namespace wallet_core::internal;

    size_t len = (size_t) env->GetArrayLength(array);
    unsigned char *buf = new unsigned char[len];
    env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));

    ExtendedKeyPtr rootKey;

    BinaryData seed{buf, len};
    ERSOR(make_master_key(&seed, reset_sp(rootKey)), jstring());

    const char *id = nullptr;
    ERSOR(make_key_id(rootKey.get(), &id), jstring());

    return env->NewStringUTF(id);
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_makeAccountAddress(JNIEnv *env, jobject obj, jbyteArray array,
                                                       jint index, jint currency) {

    using namespace wallet_core::internal;

    size_t len = (size_t) env->GetArrayLength(array);
    unsigned char *buf = new unsigned char[len];
    env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));

    ExtendedKeyPtr rootKey;

    BinaryData seed{buf, len};
    ERSOR(make_master_key(&seed, reset_sp(rootKey)), jstring());

    HDAccountPtr hdAccount;
    ERSOR(make_hd_account(rootKey.get(), static_cast<Currency >((int) currency), (int) index,
                          reset_sp(hdAccount)), jstring());

    AccountPtr account;
    ERSOR(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, 0, reset_sp(account)), jstring());

    ConstCharPtr address;
    ERSOR(get_account_address_string(account.get(), reset_sp(address)), jstring());

    return env->NewStringUTF(address.get());
}

//BinaryData to_binary_data(JNIEnv *env, jbyteArray array)
//{
//    return BinaryData{(const unsigned char*)env->GetByteArrayElements(array, nullptr), (size_t) env->GetArrayLength(array)};
//}

JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_makeTransaction(JNIEnv *env, jobject obj, jbyteArray array,
                                                    jstring tx_hash_bytes, jstring tx_pub_key,
                                                    jint tx_out_index, jstring sum, jstring amount,
                                                    jstring fee, jstring destination_address,
                                                    jstring send_address) {

    using namespace wallet_core::internal;
    using namespace multy_transaction::internal;

    size_t len = (size_t) env->GetArrayLength(array);
    unsigned char *buf = new unsigned char[len];
    env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));

    const char *amountStr = env->GetStringUTFChars(amount, nullptr);
    const char *feeStr = env->GetStringUTFChars(fee, nullptr);
    const char *sumStr = env->GetStringUTFChars(sum, nullptr);
    const char *txPubKeyStr = env->GetStringUTFChars(tx_pub_key, nullptr);
    const char *txHashStr = env->GetStringUTFChars(tx_hash_bytes, nullptr);

    __android_log_print(ANDROID_LOG_INFO, "foo", "Amount: %s", amountStr);
    __android_log_print(ANDROID_LOG_INFO, "foo", "Fee: %s", feeStr);

    ErrorPtr error;
    ExtendedKeyPtr rootKey;

    BinaryData seed{buf, len};
    error.reset(make_master_key(&seed, reset_sp(rootKey)));

    HDAccountPtr hdAccount;
    error.reset(make_hd_account(rootKey.get(), CURRENCY_BITCOIN, 0, reset_sp(hdAccount)));

    AccountPtr account;
    error.reset(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, 0, reset_sp(account)));

    ConstCharPtr address;
    error.reset(get_account_address_string(account.get(), reset_sp(address)));

    __android_log_print(ANDROID_LOG_INFO, "foo", "address: %s", address.get());

    TransactionPtr transaction;
    error.reset(make_transaction(account.get(), reset_sp(transaction)));

    Amount one_BTC(amountStr);
    Amount fee_value(feeStr);
    Amount sumAmount(sumStr);
//    Amount one_BTC(Amount(1000) * 1000 * 1000 * 1000 * 1000);
//    Amount fee_value(Amount(1000) * 1000 * 1000 * 1000);
    {
        Properties &source = transaction->add_source();
        source.set_property("amount", sumAmount);
        source.set_property("prev_tx_hash", test_utility::to_binary_data(test_utility::from_hex(txHashStr)));
        // source.set_property("prev_tx_hash", binaryTxHash);
        source.set_property("prev_tx_out_index", tx_out_index);
        source.set_property("prev_tx_out_script_pubkey", test_utility::to_binary_data(test_utility::from_hex(txPubKeyStr)));
    }

    {
        Properties &destination = transaction->add_destination();
        destination.set_property("address", env->GetStringUTFChars(destination_address, nullptr));
        destination.set_property("amount", one_BTC);
    }

    {
        Properties &change = transaction->add_destination();
        change.set_property("address", env->GetStringUTFChars(send_address, nullptr));
        change.set_property("amount", sumAmount - one_BTC - fee_value);
    }

    transaction->update_state();
    transaction->sign();
    BinaryDataPtr serialized = transaction->serialize();

    jbyteArray resultArray = env->NewByteArray(serialized.get()->len);
    env->SetByteArrayRegion(resultArray, 0, serialized.get()->len, reinterpret_cast<const jbyte *>(serialized->data));
    return resultArray;
}

//JNIEXPORT jobjectArray  JNICALL
//Java_io_multy_util_NativeDataHelper_estimateFee(JNIEnv *env, jobject obj, jbyteArray array,
//                                                jstring tx_hash_bytes, jstring tx_pub_key,
//                                                jint tx_out_index, jstring sum, jstring amount,
//                                                jstring fee, jstring destination_address,
//                                                jstring send_address, jobjectArray fees) {
//
////    using namespace wallet_core::internal;
////    using namespace multy_transaction::internal;
////
////    size_t len = (size_t) env->GetArrayLength(array);
////    unsigned char *buf = new unsigned char[len];
////    env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));
////
////    const char *amountStr = env->GetStringUTFChars(amount, nullptr);
////    const char *feeStr = env->GetStringUTFChars(fee, nullptr);
////    const char *sumStr = env->GetStringUTFChars(sum, nullptr);
////    const char *txPubKeyStr = env->GetStringUTFChars(tx_pub_key, nullptr);
////    const char *txHashStr = env->GetStringUTFChars(tx_hash_bytes, nullptr);
////
////    __android_log_print(ANDROID_LOG_INFO, "foo", "Amount: %s", amountStr);
////    __android_log_print(ANDROID_LOG_INFO, "foo", "Fee: %s", feeStr);
////
////    ErrorPtr error;
////    ExtendedKeyPtr rootKey;
////
////    BinaryData seed{buf, len};
////    error.reset(make_master_key(&seed, reset_sp(rootKey)));
////
////    HDAccountPtr hdAccount;
////    error.reset(make_hd_account(rootKey.get(), CURRENCY_BITCOIN, 0, reset_sp(hdAccount)));
////
////    AccountPtr account;
////    error.reset(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, 0, reset_sp(account)));
////
////    ConstCharPtr address;
////    error.reset(get_account_address_string(account.get(), reset_sp(address)));
////
////    __android_log_print(ANDROID_LOG_INFO, "foo", "address: %s", address.get());
////
////    TransactionPtr transaction;
////    error.reset(make_transaction(account.get(), reset_sp(transaction)));
////
////    int stringCount = env->GetArrayLength(fees);
////    for (int i = 0; i < stringCount; i++) {
////        jstring string = (jstring) (env->GetObjectArrayElement(fees, i));
////        const char *rawString = env->GetStringUTFChars(string, 0);
////    }
////
////
////    for (int i = 0; i < stringCount; i++) {
////        Amount one_BTC(amountStr);
////        Amount fee_value(feeStr);
////        Amount sumAmount(sumStr);
////        {
////            Properties &source = transaction->add_source();
////            source.set_property("amount", sumAmount);
////            source.set_property("prev_tx_hash",
////                                test_utility::to_binary_data(test_utility::from_hex(txHashStr)));
////            source.set_property("prev_tx_out_index", tx_out_index);
////            source.set_property("prev_tx_out_script_pubkey",
////                                test_utility::to_binary_data(test_utility::from_hex(txPubKeyStr)));
////        }
////
////        {
////            Properties &destination = transaction->add_destination();
////            destination.set_property("address",
////                                     env->GetStringUTFChars(destination_address, nullptr));
////            destination.set_property("amount", one_BTC);
////        }
////
////        {
////            Properties &change = transaction->add_destination();
////            change.set_property("address", env->GetStringUTFChars(send_address, nullptr));
////            change.set_property("amount", sumAmount - one_BTC - fee_value);
////        }
////
////        transaction->update_state();
////        transaction->sign();
////
////        estimatedFees[i] = transaction->get_total_fee().get_value_as_int64();
////    }
////
//////    env->ReleaseIntArrayElements(fees, feeArray, NULL);
//////    env->ReleaseIntArrayElements(estimatedFeeArray, estimatedFeeArray, NULL);
////
////
////    jobjectArray ret;
////    int i;
////
////    char *data[5]= {"A", "B", "C", "D", "E"};
////
////    ret = (jobjectArray)env->NewObjectArray(5,env->FindClass("java/lang/String"),env->NewStringUTF(""));
////    for (i = 0; i < stringCount; i ++) {
////        env->SetObjectArrayElement(ret, i, env->NewStringUTF())
////    }
////
////    for(i=0;i<5;i++) env->SetObjectArrayElement(ret,i,env->NewStringUTF(data[i]));
//
//    return nullptr;
//}

//void foo()
//{
//    ConstCharPtr mnemonic_str;
//    ErrorPtr error;
//    error.reset(make_mnemonic(dummy_entropy_source, reset_sp(mnemonic_str)));
//    ASSERT_EQ(nullptr, error);
//    ASSERT_NE(nullptr, mnemonic_str);
//
//    BinaryDataPtr seed;
//    error.reset(make_seed(mnemonic_str.get(), "", reset_sp(seed)));
//    ASSERT_EQ(nullptr, error);
//    ASSERT_NE(nullptr, seed);
//
//    ExtendedKeyPtr root_key;
//    error.reset(make_master_key(seed.get(), reset_sp(root_key)));
//    ASSERT_EQ(nullptr, error);
//    ASSERT_NE(nullptr, root_key);
//
//    HDAccountPtr root_account;
//    error.reset(make_hd_account(root_key.get(), CURRENCY_BITCOIN, 0, reset_sp(root_account)));
//    ASSERT_EQ(nullptr, error);
//    ASSERT_NE(nullptr, root_account);
//
//    AccountPtr leaf_account;
//    error.reset(
//            make_hd_leaf_account(
//                    root_account.get(), ADDRESS_EXTERNAL, 0,
//                    reset_sp(leaf_account)));
//    ASSERT_EQ(nullptr, error);
//    ASSERT_NE(nullptr, leaf_account);
//
//    ConstCharPtr address;
//    error.reset(
//            get_account_address_string(leaf_account.get(), reset_sp(address)));
//    ASSERT_EQ(nullptr, error);
//    ASSERT_NE(nullptr, address);
//    ASSERT_LT(0, strlen(address.get()));
//    printf("final address: %s\n", address.get());
//}

/** Generates a pseudo-random seed from given mnemonic and password.
 * @param mnemonic - space-separated list of mnemonic words.
 * @param password - password, optional, can be null if not set.
 * @param [out]seed - resulting pseudo-random seed.
 */
MULTY_CORE_API Error *make_seed(
        const char *mnemonic, const char *password, BinaryData **seed);
MULTY_CORE_API Error *seed_to_string(const BinaryData *seed, const char **str);


#ifdef __cplusplus
} // extern "C"
#endif
