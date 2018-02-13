#include <jni.h>
#include <android/log.h>
#include <sys/types.h>
#include <cstdio>
#include <unistd.h>
#include <array>
#include <pthread.h>
#include <memory>
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
#include "multy_core/internal/account.h"
#include "multy_core/sha3.h"


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

void throw_java_exception_str(JNIEnv *env, const char *message) {
    jclass c = env->FindClass("io/multy/util/JniException");
//    __android_log_print(ANDROID_LOG_INFO, "log message", "!!! Throwing an Error: %s", message);
    env->ThrowNew(c, message);
}

void throw_java_exception(JNIEnv *env, const Error &error) {
    throw_java_exception_str(env, error.message);
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
                                                       jint walletIndex, jint addressIndex,
                                                       jint currency) {

    using namespace wallet_core::internal;

    size_t len = (size_t) env->GetArrayLength(array);
    unsigned char *buf = new unsigned char[len];
    env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));

    ExtendedKeyPtr rootKey;

    BinaryData seed{buf, len};
    ERSOR(make_master_key(&seed, reset_sp(rootKey)), jstring());

    HDAccountPtr hdAccount;
    ERSOR(make_hd_account(rootKey.get(), static_cast<Currency >((int) currency), walletIndex,
                          reset_sp(hdAccount)), jstring());

    AccountPtr account;
    ERSOR(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, addressIndex, reset_sp(account)),
          jstring());

    ConstCharPtr address;
    ERSOR(get_account_address_string(account.get(), reset_sp(address)), jstring());

    return env->NewStringUTF(address.get());
}

//JNIEXPORT jstring JNICALL
//Java_io_multy_util_NativeDataHelper_makePrivateKey

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_getEstimate(JNIEnv *env, jclass type, jstring feePerByte_,
                                                jint inputs, jint outputs) {

    using namespace wallet_core::internal;
    using namespace multy_transaction::internal;

    const char *feePerByte = env->GetStringUTFChars(feePerByte_, 0);

    size_t in = inputs;
    size_t out = outputs;
    Amount fee_per_byte(feePerByte);

    ErrorPtr error;
    AccountPtr baseAccount;
    error.reset(
            make_account(CURRENCY_BITCOIN, "cQeGKosJjWPn9GkB7QmvmotmBbVg1hm8UjdN6yLXEWZ5HAcRwam7",
                         reset_sp(baseAccount)));

    TransactionPtr transaction;
    error.reset(make_transaction(baseAccount.get(), reset_sp(transaction)));

    {
        Properties &fee = transaction->get_fee();
        fee.set_property("amount_per_byte", fee_per_byte);
    }

    Amount total_fee = transaction->estimate_total_fee(out, in);

    //  const char* ret = fee_per_byte.get_value();

    env->ReleaseStringUTFChars(feePerByte_, feePerByte);

    return env->NewStringUTF(total_fee.get_value().c_str());
}

JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_makeTransaction(JNIEnv *jniEnv, jobject obj, jbyteArray jSeed,
                                                    jint jWalletIndex, jstring amountToSpend,
                                                    jstring jFeePerByte, jstring jDonation,
                                                    jstring jDestinationAddress,
                                                    jstring jChangeAddress) {

    using namespace wallet_core::internal;
    using namespace multy_transaction::internal;

    JNIEnv *env = getEnv();
    size_t len = (size_t) env->GetArrayLength(jSeed);
    unsigned char *seedBuf = new unsigned char[len];
    env->GetByteArrayRegion(jSeed, 0, len, reinterpret_cast<jbyte *>(seedBuf));

    jclass jTransaction = env->FindClass("io/multy/util/SendTransactionModel");
    jmethodID jMethodInit = env->GetMethodID(jTransaction, "<init>", "(ILjava/lang/String;)V");
    jobject jObjectTransaction = env->NewObject(jTransaction, jMethodInit, jWalletIndex,
                                                amountToSpend);

    jmethodID jMidSetup = env->GetMethodID(jTransaction, "setupFields", "(I)V");
    jmethodID jMidAddrIdes = env->GetMethodID(jTransaction, "getAddressesIndexes", "()[I");
    jmethodID jMidIds = env->GetMethodID(jTransaction, "getOutIds", "()[I");
    jmethodID jMidHashes = env->GetMethodID(jTransaction, "getHashes", "()[Ljava/lang/String;");
    jmethodID jMidKeys = env->GetMethodID(jTransaction, "getPubKeys", "()[Ljava/lang/String;");
    jmethodID jMidAmounts = env->GetMethodID(jTransaction, "getAmounts", "()[Ljava/lang/String;");

    jintArray addrIds = (jintArray) env->CallObjectMethod(jObjectTransaction, jMidAddrIdes);
    jint *addressIds = env->GetIntArrayElements(addrIds, NULL);
    int length = env->GetArrayLength(addrIds);

    ErrorPtr error;
    ExtendedKeyPtr rootKey;

    BinaryData seed{seedBuf, len};
    error.reset(make_master_key(&seed, reset_sp(rootKey)));

    HDAccountPtr hdAccount;
    error.reset(
            make_hd_account(rootKey.get(), CURRENCY_BITCOIN, jWalletIndex, reset_sp(hdAccount)));

    __android_log_print(ANDROID_LOG_INFO, "log message", "JWalletIndex %d",  jWalletIndex);

    AccountPtr baseAccount;
    error.reset(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, 0, reset_sp(baseAccount)));

    TransactionPtr transaction;
    error.reset(make_transaction(baseAccount.get(), reset_sp(transaction)));

    Amount sum(0);

    const char *donationAmountStr = env->GetStringUTFChars(jDonation, nullptr);
    const char *donationAddressStr = "mzNZBhim9XGy66FkdzrehHwdWNgbiTYXCQ";
    const char *feePerByteStr = env->GetStringUTFChars(jFeePerByte, nullptr);

    const char *destinationAddressStr = env->GetStringUTFChars(jDestinationAddress, nullptr);
    const char *destinationAmountStr = env->GetStringUTFChars(amountToSpend, nullptr);
    const char *changeAddressStr = env->GetStringUTFChars(jChangeAddress, nullptr);

    const Amount destinationAmount(destinationAmountStr);
    const Amount feePerByte(feePerByteStr);
    const Amount donationAmount(donationAmountStr);
    size_t outputsCount = 0;
    Amount total_fee;

    try {

        for (int i = 0; i < length; i++) {
            //SET ADDRESS INDEX
            //GET DATA FROM SET ADDRESS
            jint addressId = addressIds[i];

            env->CallVoidMethod(jObjectTransaction, jMidSetup, addressId);

            jintArray outIds = (jintArray) env->CallObjectMethod(jObjectTransaction, jMidIds);
            jobjectArray hashes = (jobjectArray) env->CallObjectMethod(jObjectTransaction,
                                                                       jMidHashes);
            jobjectArray keys = (jobjectArray) env->CallObjectMethod(jObjectTransaction, jMidKeys);
            jobjectArray amounts = (jobjectArray) env->CallObjectMethod(jObjectTransaction,
                                                                        jMidAmounts);

            int stringCount = env->GetArrayLength(hashes);
            jint *outIdArr = env->GetIntArrayElements(outIds, nullptr);

            AccountPtr account;
            error.reset(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, addressId,
                                             reset_sp(account)));

            for (int k = 0; k < stringCount; k++) {
                jstring jHash = (jstring) (env->GetObjectArrayElement(hashes, k));
                jstring jKey = (jstring) (env->GetObjectArrayElement(keys, k));
                jstring jAmount = (jstring) (env->GetObjectArrayElement(amounts, k));
                const char *hashString = env->GetStringUTFChars(jHash, 0);
                const char *keyString = env->GetStringUTFChars(jKey, 0);
                const char *amountString = env->GetStringUTFChars(jAmount, 0);
                jint outId = outIdArr[k];

                __android_log_print(ANDROID_LOG_INFO, "log message", "!!! address id  %d",
                                    addressId);
                __android_log_print(ANDROID_LOG_INFO, "log message", "!!! Pubkey %s",
                                    keyString);
                __android_log_print(ANDROID_LOG_INFO, "log message", "!!! Hash %s",
                                    hashString);
                __android_log_print(ANDROID_LOG_INFO, "log message", "!!! amount %s",
                                    amountString);
                __android_log_print(ANDROID_LOG_INFO, "log message", "!!! outid %s",
                                    outId);


                ConstCharPtr address;
                ERSOR(get_account_address_string(account.get(), reset_sp(address)) , jbyteArray());
                __android_log_print(ANDROID_LOG_INFO, "log message", "!!! outid %s",
                                    address.get());



                BinaryDataPtr binaryDataTxHash;
                BinaryDataPtr binaryDataPubKey;
                make_binary_data_from_hex(hashString, reset_sp(binaryDataTxHash));
                make_binary_data_from_hex(keyString, reset_sp(binaryDataPubKey));

                sum += amountString;

                Properties &source = transaction->add_source();
                source.set_property("amount", Amount(amountString));
                source.set_property("prev_tx_hash", *binaryDataTxHash);
                source.set_property("prev_tx_out_index", outId);
                source.set_property("prev_tx_out_script_pubkey", *binaryDataPubKey);
                source.set_property("private_key", *account->get_private_key());

                outputsCount++;

                env->ReleaseStringUTFChars(jHash, hashString);
                env->ReleaseStringUTFChars(jKey, keyString);
                env->ReleaseStringUTFChars(jAmount, amountString);
            }
            env->ReleaseIntArrayElements(outIds, outIdArr, 0);
        }

        {
            Properties &fee = transaction->get_fee();
            fee.set_property("amount_per_byte", feePerByte);
            fee.set_property("min_amount_per_byte", Amount(0));
            fee.set_property("max_amount_per_byte", feePerByte + "1000");
        }

        total_fee = transaction->estimate_total_fee(outputsCount, donationAmount == "0" ? 2 : 3);

        {
            Properties &destination = transaction->add_destination();
            destination.set_property("address", destinationAddressStr);
            destination.set_property("amount", destinationAmount);
        }

        if (donationAmount != "0") {
            Properties &donation = transaction->add_destination();
            donation.set_property("address", donationAddressStr);
            donation.set_property("amount", donationAmount);
        }

        {
            Properties &change = transaction->add_destination();
            change.set_property("address", changeAddressStr);
            change.set_property("amount", sum - destinationAmount - donationAmount - total_fee);
        }

        transaction->update_state();
        transaction->sign();
        BinaryDataPtr serialized = transaction->serialize();

        jbyteArray resultArray = env->NewByteArray(serialized.get()->len);
        env->SetByteArrayRegion(resultArray, 0, serialized.get()->len,
                                reinterpret_cast<const jbyte *>(serialized->data));
        return resultArray;

    } catch (std::exception const &e) {
        throw_java_exception_str(env, e.what());
    } catch (...) {
        throw_java_exception_str(env, "something went wrong");
    }
    env->ReleaseIntArrayElements(addrIds, addressIds, 0);
    return jbyteArray();
}

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

struct FinallyWrapper {
    ~FinallyWrapper() {
        finally();
    }

    const std::function<void(void)> finally;
};

#define FINALLY(statement) FinallyWrapper finally_wrapper ## __LINE__{[&](){statement;}}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_digestSha3256(JNIEnv *env, jclass type, jbyteArray s_) {
    using namespace wallet_core::internal;
    jbyte *data = env->GetByteArrayElements(s_, nullptr);
    FINALLY(env->ReleaseByteArrayElements(s_, data, 0));

    std::array<uint8_t, SHA3_256> hash_buffer;
    BinaryData output{hash_buffer.data(), hash_buffer.size()};

    const BinaryData input{reinterpret_cast<unsigned char *>(data),
                           static_cast<size_t>(env->GetArrayLength(s_))};
    ERSOR(sha3(&input, &output), jbyteArray());

    jbyteArray resultArray = env->NewByteArray(output.len);
    env->SetByteArrayRegion(resultArray, 0, output.len,
                            reinterpret_cast<const jbyte *>(output.data));
    return resultArray;
}