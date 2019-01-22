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
#include <vector>
#include <multy_core/account.h>
#include <cstring>
#include <functional>
#include <multy_core/src/utility.h>
#include "multy_core/key.h"
#include <multy_core/transaction.h>
#include <multy_core/src/api/properties_impl.h>
#include <multy_core/src/api/big_int_impl.h>
#include <multy_core/big_int.h>
#include <multy_core/src/ethereum/ethereum_transaction.h>
#include <multy_core/json_api.h>
#include "multy_core/sha3.h"
#include "multy_core/src/transaction_base.h"
#include "multy_core/properties.h"
#include "multy_core/src/api/account_impl.h"
#include "multy_core/ethereum.h"
#include "multy_core/blockchain.h"
#include "multy_core/transaction_builder.h"
#include "multy_core/ethereum.h"

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

class JniString {
    JNIEnv *m_env;
    jstring m_jstring;
    const char *m_object;
public:
    JniString(JNIEnv *env, jstring input_str) :
            m_env(env),
            m_jstring(input_str),
            m_object(env->GetStringUTFChars(m_jstring, nullptr)) {
    }

    const char *c_str() const {
        return m_object;
    }

    ~JniString() {
        m_env->ReleaseStringUTFChars(m_jstring, m_object);
    }
};


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

void throw_java_exception(JNIEnv *env, const Error &error, const char *context) {
    std::string full_message =
            "Error: " + std::to_string(error.code) + " : \"" + error.message + "\""
            + " @ " + error.location.file + ":" + std::to_string(error.location.line)
            + "\n" + (error.backtrace ? error.backtrace : "!NO BACKTRACE!")
            + "\nContext: " + context;
    throw_java_exception_str(env, full_message.c_str());
}

#define HANDLE_ERROR(statement)                                                                     \
    do {                                                                                            \
        ErrorPtr error(statement);                                                                  \
        if (error)                                                                                  \
        {                                                                                           \
            __android_log_print(ANDROID_LOG_INFO, "Multy-core error", "In file %s, \n in line: %d, jni : %d ", error->location.file , error->location.line, __LINE__ );    \
            throw_java_exception(env, *error, #statement);                                                      \
            return (0);                                                                         \
        }                                                                                           \
    } while(false)

//#define HANDLE_ERROR(statement) ERSOR(statement, 0)

JNIEXPORT jint JNICALL
Java_io_multy_util_NativeDataHelper_runTest(JNIEnv *jenv, jclass jcls) {
    jint jresult = 0;

    start_logger("Tests");
    __android_log_print(ANDROID_LOG_INFO, "Multy", "Running tests");
    char *foo = (char *) "foo";
    jresult = run_tests(1, &foo);
    return jresult;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *pjvm, void *reserved) {

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
    using namespace multy_core::internal;
    const jbyteArray defaultResult{};

    JniString mnemonic(env, string);

    BinaryDataPtr data;

    HANDLE_ERROR(make_seed(mnemonic.c_str(), "", reset_sp(data)));

    jbyteArray array = env->NewByteArray(data.get()->len);
    env->SetByteArrayRegion(array, 0, data.get()->len, reinterpret_cast<const jbyte *>(data->data));
    return array;
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_makeMnemonic(JNIEnv *jniEnv, jobject obj) {

    using namespace multy_core::internal;
    const jstring defaultResult{};

    auto env = getEnv();
    auto randomClass = env->FindClass("io/multy/util/EntropyProvider");

    jmethodID mid = env->GetStaticMethodID(randomClass, "generateKey", "(I)[B");
    //160 is for 15 mnemonic words
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

    HANDLE_ERROR(make_mnemonic(entropy_source, reset_sp(mnemonic_str)));
    return jniEnv->NewStringUTF(mnemonic_str.get());
}


JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_makeAccountId(JNIEnv *env, jobject obj, jbyteArray array) {

    using namespace multy_core::internal;
    const jstring defaultResult{};

    size_t len = (size_t) env->GetArrayLength(array);
    unsigned char *buf = new unsigned char[len];
    env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));

    ExtendedKeyPtr rootKey;

    BinaryData seed{buf, len};
    HANDLE_ERROR(make_master_key(&seed, reset_sp(rootKey)));

    const char *id = nullptr;
    HANDLE_ERROR(make_user_id_from_master_key(rootKey.get(), &id));

    return env->NewStringUTF(id);
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_makeAccountAddress(JNIEnv *env, jobject obj, jbyteArray array,
                                                       jint walletIndex, jint addressIndex,
                                                       jint blockchain, jint type) {

    using namespace multy_core::internal;
    const jstring defaultResult{};

    size_t len = (size_t) env->GetArrayLength(array);
    unsigned char *buf = new unsigned char[len];
    env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));
    ExtendedKeyPtr rootKey;

    BinaryData seed{buf, len};
    HANDLE_ERROR(make_master_key(&seed, reset_sp(rootKey)));

    HDAccountPtr hdAccount;
    HANDLE_ERROR(make_hd_account(rootKey.get(),
                                 BlockchainType{(Blockchain) blockchain, (size_t) type},
                                 BITCOIN_ADDRESS_P2PKH,
                                 walletIndex,
                                 reset_sp(hdAccount)));

    AccountPtr account;
    HANDLE_ERROR(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, addressIndex,
                                      reset_sp(account)));

    ConstCharPtr address;
    HANDLE_ERROR(account_get_address_string(account.get(), reset_sp(address)));

    return env->NewStringUTF(address.get());
}

//extern "C"
JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_getMyPrivateKey(JNIEnv *env, jclass type_, jbyteArray array,
                                                    jint walletIndex, jint addressIndex,
                                                    jint blockchain, jint netType) {

    using namespace multy_core::internal;
    const jstring defaultResult{};

    size_t len = (size_t) env->GetArrayLength(array);
    unsigned char *buf = new unsigned char[len];
    env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));

    ExtendedKeyPtr rootKey;

    BinaryData seed{buf, len};
    HANDLE_ERROR(make_master_key(&seed, reset_sp(rootKey)));

    HDAccountPtr hdAccount;
    HANDLE_ERROR(make_hd_account(rootKey.get(),
                                 BlockchainType{(Blockchain) blockchain, (size_t) netType},
                                 BITCOIN_ADDRESS_P2PKH,
                                 walletIndex,
                                 reset_sp(hdAccount)));

    AccountPtr account;
    HANDLE_ERROR(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, addressIndex,
                                      reset_sp(account)));

    KeyPtr keyPtr;
    HANDLE_ERROR(account_get_key(account.get(), KEY_TYPE_PRIVATE, reset_sp(keyPtr)));

    ConstCharPtr privKeyStr;
    HANDLE_ERROR(key_to_string(keyPtr.get(), reset_sp(privKeyStr)));

    return env->NewStringUTF(privKeyStr.get());
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_ethereumPersonalSign(JNIEnv *env, jclass type_, jstring key,
                                                         jstring message) {

    using namespace multy_core::internal;
    JniString keyStr(env, key);
    JniString messageStr(env, message);


    CharPtr signature;

//    HANDLE_ERROR(ethereum_personal_sign(keyStr.c_str(), messageStr.c_str(), reset_sp(signature)));


    return env->NewStringUTF(signature.get());;
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_bruteForceAddress(JNIEnv *jniEnv, jobject obj,
                                                      jbyteArray jSeed,
                                                      jint jWalletIndex, jint jAddressIndex,
                                                      jint blockchain, jint netType,
                                                      jstring jAddress) {
    const jstring defaultResult{};
    using namespace multy_core::internal;

    JNIEnv *env = getEnv();
    size_t len = (size_t) env->GetArrayLength(jSeed);
    unsigned char *seedBuf = new unsigned char[len];
    env->GetByteArrayRegion(jSeed, 0, len, reinterpret_cast<jbyte *>(seedBuf));

    JniString address(env, jAddress);
    std::string addres_back = std::string(address.c_str());

    try {
        ExtendedKeyPtr rootKey;

        BinaryData seed{seedBuf, len};
        HANDLE_ERROR(make_master_key(&seed, reset_sp(rootKey)));

        HDAccountPtr hdAccount;
        HANDLE_ERROR(make_hd_account(rootKey.get(),
                                     BlockchainType{(Blockchain) blockchain, (size_t) netType},
                                     BITCOIN_ADDRESS_P2PKH,
                                     jWalletIndex,
                                     reset_sp(hdAccount)));

        AccountPtr account;
        HANDLE_ERROR(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, jAddressIndex,
                                          reset_sp(account)));

        ConstCharPtr modified_private_key_str;

        for (unsigned char b = 0; b <= 0xFF; ++b) {
            HANDLE_ERROR(account_change_private_key(account.get(), -1, b));
            ConstCharPtr modified_address;
            HANDLE_ERROR(account_get_address_string(account.get(), reset_sp(modified_address)));
            if (std::string(modified_address.get()) == addres_back) {
                KeyPtr modified_private_key;
                HANDLE_ERROR(account_get_key(account.get(), KEY_TYPE_PRIVATE,
                                             reset_sp(modified_private_key)));
                HANDLE_ERROR(key_to_string(modified_private_key.get(),
                                           reset_sp(modified_private_key_str)));
                break;
            }
        }

        jstring key = env->NewStringUTF(modified_private_key_str.get());
        return key;
    } catch (std::exception const &e) {
        throw_java_exception_str(env, e.what());
    } catch (...) {
        throw_java_exception_str(env, "something went wrong");
    }

    return jstring();
}

Transaction* makeTransaction(JNIEnv *jniEnv, jobject obj, jlong jWalletId,
            jint jNetworkId, jbyteArray jSeed,
            jint jWalletIndex, jstring amountToSpend,
            jstring jFeePerByte, jstring jDonation,
            jstring jDestinationAddress,
                    jstring jChangeAddress,
            jstring jDonationAddress, jboolean payFee)
{
    const jbyteArray defaultResult{};
    bool fladPayFee = (bool) (payFee == JNI_TRUE);
    using namespace multy_core::internal;

    JNIEnv *env = getEnv();
    size_t len = (size_t) env->GetArrayLength(jSeed);
    unsigned char *seedBuf = new unsigned char[len];
    env->GetByteArrayRegion(jSeed, 0, len, reinterpret_cast<jbyte *>(seedBuf));

    jclass jTransaction = env->FindClass("io/multy/util/SendTransactionModel");
    jmethodID jMethodInit = env->GetMethodID(jTransaction, "<init>", "(JLjava/lang/String;)V");
    jobject jObjectTransaction = env->NewObject(jTransaction, jMethodInit, jWalletId,
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

    ExtendedKeyPtr rootKey;

    BinaryData seed{seedBuf, len};
    HANDLE_ERROR(make_master_key(&seed, reset_sp(rootKey)));


    __android_log_print(ANDROID_LOG_INFO, "networkid address", "%d", jNetworkId);

    HDAccountPtr hdAccount;
    HANDLE_ERROR(
            make_hd_account(rootKey.get(),
                            BlockchainType{BLOCKCHAIN_BITCOIN, (size_t) jNetworkId},
                            BITCOIN_ADDRESS_P2PKH,
                            jWalletIndex, reset_sp(hdAccount)));

    AccountPtr baseAccount;
    HANDLE_ERROR(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, 0, reset_sp(baseAccount)));

    TransactionPtr transaction;
    HANDLE_ERROR(make_transaction(baseAccount.get(), reset_sp(transaction)));

    BigInt sum(0);

    JniString donationAmountStr(env, jDonation);
    JniString feePerByteStr(env, jFeePerByte);

    JniString destinationAddressStr(env, jDestinationAddress);
    JniString destinationAmountStr(env, amountToSpend);
    JniString changeAddressStr(env, jChangeAddress);
    JniString donationAddressStr(env, jDonationAddress);

    BigInt destinationAmount(destinationAmountStr.c_str());
    const BigInt feePerByte(feePerByteStr.c_str());
    const BigInt donationAmount(donationAmountStr.c_str());
    size_t outputsCount = 0;
    BigInt total_fee;

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
        HANDLE_ERROR(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, addressId,
                                          reset_sp(account)));
        // START SET INPUTS
        for (int k = 0; k < stringCount; k++) {
            jstring jHash = (jstring) (env->GetObjectArrayElement(hashes, k));
            jstring jKey = (jstring) (env->GetObjectArrayElement(keys, k));
            jstring jAmount = (jstring) (env->GetObjectArrayElement(amounts, k));
            JniString hashString(env, jHash);
            JniString keyString(env, jKey);
            JniString amountString(env, jAmount);

            jint outId = outIdArr[k];

            BinaryDataPtr binaryDataTxHash;
            BinaryDataPtr binaryDataPubKey;
            BigIntPtr amount;
            PrivateKey *private_key_ptr;
            HANDLE_ERROR(account_get_key(account.get(), KEY_TYPE_PRIVATE,
                                         reinterpret_cast<Key **>(&private_key_ptr)));

            PrivateKeyPtr private_key(private_key_ptr);
            HANDLE_ERROR(
                    make_binary_data_from_hex(hashString.c_str(), reset_sp(binaryDataTxHash)));
            HANDLE_ERROR(
                    make_binary_data_from_hex(keyString.c_str(), reset_sp(binaryDataPubKey)));
            HANDLE_ERROR(make_big_int(amountString.c_str(), reset_sp(amount)));

            HANDLE_ERROR(big_int_add(&sum, amount.get()));

            Properties *source = nullptr;
            HANDLE_ERROR(transaction_add_source(transaction.get(), &source));
            HANDLE_ERROR(properties_set_big_int_value(source, "amount", amount.get()));
            HANDLE_ERROR(properties_set_binary_data_value(source, "prev_tx_hash",
                                                          binaryDataTxHash.get()));
            HANDLE_ERROR(properties_set_int32_value(source, "prev_tx_out_index", outId));
            HANDLE_ERROR(properties_set_binary_data_value(source, "prev_tx_out_script_pubkey",
                                                          binaryDataPubKey.get()));
            HANDLE_ERROR(
                    properties_set_private_key_value(source, "private_key", private_key.get()));


            outputsCount++;
        }
        env->ReleaseIntArrayElements(outIds, outIdArr, 0);
    }
    // END SET INPUTS

    // set fee per byte
    {
        Properties *fee = nullptr;
        HANDLE_ERROR(transaction_get_fee(transaction.get(), &fee));
        HANDLE_ERROR(properties_set_big_int_value(fee, "amount_per_byte", &feePerByte));
    }
    // set donation
    if (donationAmount != "0") {
        // donation
        Properties *donation = nullptr;
        HANDLE_ERROR(transaction_add_destination(transaction.get(), &donation));
        HANDLE_ERROR(properties_set_big_int_value(donation, "amount", &donationAmount));
        HANDLE_ERROR(
                properties_set_string_value(donation, "address", donationAddressStr.c_str()));
    }

    // SET DESTINATION AND CHANGE ADDRESS
    Properties *destination = nullptr;
    Properties *change = nullptr;
    HANDLE_ERROR(transaction_add_destination(transaction.get(), &destination));
    HANDLE_ERROR(properties_set_string_value(destination, "address",
                                             destinationAddressStr.c_str()));
    if (sum == destinationAmount) {
        HANDLE_ERROR(properties_set_int32_value(destination, "is_change", 1));
    } else {
        if (!fladPayFee) {
        destinationAmount = destinationAmount - donationAmount;
    }
    // set destination amount
    HANDLE_ERROR(properties_set_big_int_value(destination, "amount", &destinationAmount));

    //set change destination
    HANDLE_ERROR(transaction_add_destination(transaction.get(), &change));
    HANDLE_ERROR(properties_set_int32_value(change, "is_change", 1));
    HANDLE_ERROR(properties_set_string_value(change, "address", changeAddressStr.c_str()));

    if (!fladPayFee) {
        HANDLE_ERROR(transaction_update(transaction.get()));
        BigIntPtr fee_transaction;
        HANDLE_ERROR(
                transaction_get_total_fee(transaction.get(), reset_sp(fee_transaction)));
        BigInt noPtrFeeTransaction(*fee_transaction);
        destinationAmount -= noPtrFeeTransaction;
        HANDLE_ERROR(
                properties_set_big_int_value(destination, "amount", &destinationAmount));
        }
    }
    HANDLE_ERROR(transaction_update(transaction.get()));

    BinaryDataPtr serialized;
    HANDLE_ERROR(transaction_serialize(transaction.get(), reset_sp(serialized)));

    // Clear memory after using addrIds
    jniEnv->ReleaseIntArrayElements(addrIds, addressIds, 0);
    return transaction.release();
}


JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_estimateTransactionFee(JNIEnv *jniEnv, jobject obj, jlong jWalletId,
        jint jNetworkId, jbyteArray jSeed,
        jint jWalletIndex, jstring amountToSpend,
        jstring jFeePerByte, jstring jDonation,
        jstring jDestinationAddress,
                jstring jChangeAddress,
        jstring jDonationAddress, jboolean payFee)
{
    using namespace multy_core::internal;
    auto env = jniEnv;
    try {
        TransactionPtr transaction(makeTransaction(jniEnv, obj, jWalletId,
             jNetworkId,  jSeed,
             jWalletIndex,  amountToSpend,
             jFeePerByte,  jDonation,
             jDestinationAddress,
             jChangeAddress,
             jDonationAddress,  payFee));
        if (transaction) {

            BigIntPtr fee_transaction;
            HANDLE_ERROR(transaction_get_total_fee(transaction.get(), reset_sp(fee_transaction)));
            return jniEnv->NewStringUTF(fee_transaction->get_value().c_str());
        }
    } catch (std::exception const &e) {
        throw_java_exception_str(jniEnv, e.what());
    } catch (...) {
        throw_java_exception_str(jniEnv, "something went wrong");
    }
    return jstring();
}

JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_makeTransaction(JNIEnv *jniEnv, jobject obj, jlong jWalletId,
                                                    jint jNetworkId, jbyteArray jSeed,
                                                    jint jWalletIndex, jstring amountToSpend,
                                                    jstring jFeePerByte, jstring jDonation,
                                                    jstring jDestinationAddress,
                                                    jstring jChangeAddress,
                                                    jstring jDonationAddress, jboolean payFee) {
    using namespace multy_core::internal;
    try{
        TransactionPtr transaction(makeTransaction(jniEnv, obj, jWalletId,
                                                     jNetworkId,  jSeed,
                                                     jWalletIndex,  amountToSpend,
                                                     jFeePerByte,  jDonation,
                                                     jDestinationAddress,
                                                     jChangeAddress,
                                                     jDonationAddress,  payFee));
        if(transaction) {
            auto env =jniEnv;

            BinaryDataPtr serialized;
            HANDLE_ERROR(transaction_serialize(transaction.get(), reset_sp(serialized)));

            BigIntPtr fee_transaction;
            HANDLE_ERROR(transaction_get_total_fee(transaction.get(), reset_sp(fee_transaction)));

            //auto env = getEnv();
            auto randomClass = jniEnv->FindClass("io/multy/viewmodels/AssetSendViewModel");

            jstring jstrBuf = jniEnv->NewStringUTF(fee_transaction->get_value().c_str());

            jmethodID mid = jniEnv->GetStaticMethodID(randomClass, "setTransactionPrice",
                                                   "(Ljava/lang/String;)V");
            jniEnv->CallStaticVoidMethod(randomClass, mid, jstrBuf);


            //let java side know about tx fee


            jbyteArray resultArray = jniEnv->NewByteArray(serialized.get()->len);
            jniEnv->SetByteArrayRegion(resultArray, 0, serialized.get()->len,
                                    reinterpret_cast<const jbyte *>(serialized->data));
            return resultArray;
        }

    } catch (std::exception const &e) {
        throw_java_exception_str(jniEnv, e.what());
    } catch (...) {
        throw_java_exception_str(jniEnv, "something went wrong");
    }

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

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_getDictionary(JNIEnv *env, jclass type) {

    const jstring defaultResult{};
    using namespace multy_core::internal;

    ConstCharPtr dict;
    HANDLE_ERROR(mnemonic_get_dictionary(reset_sp(dict)));

    return env->NewStringUTF(dict.get());
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_getLibraryVersion(JNIEnv *env, jclass type) {

    const jstring defaultResult{};
    using namespace multy_core::internal;

    ConstCharPtr version;
    HANDLE_ERROR(make_version_string(reset_sp(version)));

    return env->NewStringUTF(version.get());
}

JNIEXPORT void JNICALL
Java_io_multy_util_NativeDataHelper_isValidAddress(JNIEnv *env, jclass type_, jstring address_,
                                                   jint blockchain, jint netType) {
    using namespace multy_core::internal;

    JniString address(env, address_);


    ErrorPtr error(
            validate_address(BlockchainType{(Blockchain) blockchain, (size_t) netType},
                             address.c_str()));
    if (error) {
        throw_java_exception(env, *error, "validate_address");
        return;
    }

    return;
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_getPublicKey(JNIEnv *env, jclass type_,
                                                 jint jChainId, jint jNetType, jstring key) {

    using namespace multy_core::internal;
    const jstring defaultResult{};

    JniString keyStr(env, key);

    AccountPtr account;

    HANDLE_ERROR(make_account(
            BlockchainType{(Blockchain) jChainId, (size_t) jNetType}, ACCOUNT_TYPE_DEFAULT,
            keyStr.c_str(), reset_sp(account)));

    KeyPtr keyPtr;
    HANDLE_ERROR(account_get_key(account.get(), KEY_TYPE_PUBLIC, reset_sp(keyPtr)));

    ConstCharPtr pubKeyStr;
    HANDLE_ERROR(key_to_string(keyPtr.get(), reset_sp(pubKeyStr)));


    return env->NewStringUTF(pubKeyStr.get());
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_makeTransactionJSONAPI(JNIEnv *env, jclass type_,
                                                           jstring jTransactionData) {

    using namespace multy_core::internal;
    const jstring defaultResult{};

    JniString txDataStr(env, jTransactionData);
    ConstCharPtr result;

    HANDLE_ERROR(make_transaction_from_json(txDataStr.c_str(), reset_sp(result)));

    return env->NewStringUTF(result.get());
}

JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_makeTransactionEthPayload(JNIEnv *env, jclass type,
                                                              jbyteArray jSeed,
                                                              jint jWalletIndex,
                                                              jint jAddressIndex, jint jChainId,
                                                              jint jNetType, jstring jBalance,
                                                              jstring jAmount,
                                                              jstring jDestinationAddress,
                                                              jstring jGasLimit, jstring jGasPrice,
                                                              jstring jNonce, jstring payload) {

    using namespace multy_core::internal;
    const jbyteArray defaultResult{};

    size_t len = (size_t) env->GetArrayLength(jSeed);
    unsigned char *seedBuf = new unsigned char[len];
    env->GetByteArrayRegion(jSeed, 0, len, reinterpret_cast<jbyte *>(seedBuf));

    JniString balanceStr(env, jBalance);
    JniString amountStr(env, jAmount);
    JniString destinationAddressStr(env, jDestinationAddress);
    JniString gasLimitStr(env, jGasLimit);
    JniString gasPriceStr(env, jGasPrice);
    JniString nonceStr(env, jNonce);
    JniString payloadStr(env, payload);


    try {
        ExtendedKeyPtr rootKey;

        BinaryData seed{seedBuf, len};
        HANDLE_ERROR(make_master_key(&seed, reset_sp(rootKey)));

        HDAccountPtr hdAccount;
        HANDLE_ERROR(make_hd_account(rootKey.get(),
                                     BlockchainType{(Blockchain) jChainId, (size_t) jNetType},
                                     ACCOUNT_TYPE_DEFAULT,
                                     jWalletIndex,
                                     reset_sp(hdAccount)));

        AccountPtr account;
        HANDLE_ERROR(
                make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, jAddressIndex,
                                     reset_sp(account)));


        TransactionPtr transaction;
        HANDLE_ERROR(make_transaction(account.get(), reset_sp(transaction)));

        {
            Properties *properties = nullptr;
            BigIntPtr nonce;
            HANDLE_ERROR(make_big_int(nonceStr.c_str(), reset_sp(nonce)));
            HANDLE_ERROR(transaction_get_properties(transaction.get(), &properties));
            HANDLE_ERROR(properties_set_big_int_value(properties, "nonce", nonce.get()));
//            HANDLE_ERROR(properties_set_int32_value(properties, "chain_id", ETHEREUM_CHAIN_ID_RINKEBY));
        }

        {
            Properties *source = nullptr;
            HANDLE_ERROR(transaction_add_source(transaction.get(), &source));
            // Address balance
            BigIntPtr balance;
            HANDLE_ERROR(make_big_int(balanceStr.c_str(), reset_sp(balance)));
            HANDLE_ERROR(properties_set_big_int_value(source, "amount", balance.get()));
        }

        {
            Properties *destination = nullptr;
            HANDLE_ERROR(transaction_add_destination(transaction.get(), &destination));

            BigIntPtr amount;
            HANDLE_ERROR(make_big_int(amountStr.c_str(), reset_sp(amount)));
            HANDLE_ERROR(properties_set_big_int_value(destination, "amount", amount.get()));

//            BinaryDataPtr address;
//            HANDLE_ERROR(make_binary_data_from_hex(destinationAddressStr, reset_sp(address)));
            HANDLE_ERROR(
                    properties_set_string_value(destination, "address",
                                                destinationAddressStr.c_str()));
        }

        {
            BinaryDataPtr data;
            HANDLE_ERROR(make_binary_data_from_hex(payloadStr.c_str(), reset_sp(data)));
            HANDLE_ERROR(transaction_set_message(transaction.get(), data.get()));
        }

        {
            Properties *fee = nullptr;
            HANDLE_ERROR(transaction_get_fee(transaction.get(), &fee));

            BigIntPtr amount_gas_price;
            HANDLE_ERROR(make_big_int(gasPriceStr.c_str(), reset_sp(amount_gas_price)));
            HANDLE_ERROR(properties_set_big_int_value(fee, "gas_price", amount_gas_price.get()));

            BigIntPtr amount_gas_limit;
            HANDLE_ERROR(make_big_int(gasLimitStr.c_str(), reset_sp(amount_gas_limit)));
            HANDLE_ERROR(properties_set_big_int_value(fee, "gas_limit", amount_gas_limit.get()));
        }

        BinaryDataPtr serialized;
        HANDLE_ERROR(transaction_serialize(transaction.get(), reset_sp(serialized)));

//        env->ReleaseStringUTFChars(jBalance, balanceStr);
//        env->ReleaseStringUTFChars(jAmount, amountStr);
//        env->ReleaseStringUTFChars(jDestinationAddress, destinationAddressStr);
//        env->ReleaseStringUTFChars(jGasLimit, gasLimitStr);
//        env->ReleaseStringUTFChars(jGasPrice, gasPriceStr);
//        env->ReleaseStringUTFChars(jNonce, nonceStr);

        jbyteArray resultArray = env->NewByteArray(serialized.get()->len);
        env->SetByteArrayRegion(resultArray, 0, serialized.get()->len,
                                reinterpret_cast<const jbyte *>(serialized->data));
        return resultArray;
    } catch (std::exception const &e) {
        throw_java_exception_str(env, e.what());
    } catch (...) {
        throw_java_exception_str(env, "something went wrong");
    }

    return jbyteArray();
}

JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_makeTransactionETHFromKey(JNIEnv *env, jclass type,
                                                              jstring jPrivateKey,
                                                              jint jChainId,
                                                              jint jNetType,
                                                              jstring jBalance,
                                                              jstring jAmount,
                                                              jstring jDestinationAddress,
                                                              jstring jGasLimit,
                                                              jstring jGasPrice,
                                                              jstring jNonce) {

    using namespace multy_core::internal;
    const jbyteArray defaultResult{};

    JniString privateKey(env, jPrivateKey);
    JniString balanceStr(env, jBalance);
    JniString amountStr(env, jAmount);
    JniString destinationAddressStr(env, jDestinationAddress);
    JniString gasLimitStr(env, jGasLimit);
    JniString gasPriceStr(env, jGasPrice);
    JniString nonceStr(env, jNonce);


//    const char *payloadStr = env->GetStringUTFChars(payload, nullptr);

    try {
        AccountPtr account;
        HANDLE_ERROR(make_account(BlockchainType{(Blockchain) jChainId, (size_t) jNetType},
                                  ACCOUNT_TYPE_DEFAULT,
                                  privateKey.c_str(),
                                  reset_sp(account)));

        TransactionPtr transaction;
        HANDLE_ERROR(make_transaction(account.get(), reset_sp(transaction)));

        {
            Properties *properties = nullptr;
            BigIntPtr nonce;
            HANDLE_ERROR(make_big_int(nonceStr.c_str(), reset_sp(nonce)));
            HANDLE_ERROR(transaction_get_properties(transaction.get(), &properties));
            HANDLE_ERROR(properties_set_big_int_value(properties, "nonce", nonce.get()));
        }

        {
            Properties *source = nullptr;
            HANDLE_ERROR(transaction_add_source(transaction.get(), &source));
            // Address balance
            BigIntPtr balance;
            HANDLE_ERROR(make_big_int(balanceStr.c_str(), reset_sp(balance)));
            HANDLE_ERROR(properties_set_big_int_value(source, "amount", balance.get()));
        }

        {
            Properties *destination = nullptr;
            HANDLE_ERROR(transaction_add_destination(transaction.get(), &destination));

            BigIntPtr amount;
            HANDLE_ERROR(make_big_int(amountStr.c_str(), reset_sp(amount)));
            HANDLE_ERROR(properties_set_big_int_value(destination, "amount", amount.get()));

            HANDLE_ERROR(
                    properties_set_string_value(destination, "address",
                                                destinationAddressStr.c_str()));
        }

//        {
//            BinaryDataPtr data;
//            HANDLE_ERROR(make_binary_data_from_hex(payloadStr, reset_sp(data)));
//            HANDLE_ERROR(transaction_set_message(transaction.get(), data.get()));
//        }

        {
            Properties *fee = nullptr;
            HANDLE_ERROR(transaction_get_fee(transaction.get(), &fee));

            BigIntPtr amount_gas_price;
            HANDLE_ERROR(make_big_int(gasPriceStr.c_str(), reset_sp(amount_gas_price)));
            HANDLE_ERROR(properties_set_big_int_value(fee, "gas_price", amount_gas_price.get()));

            BigIntPtr amount_gas_limit;
            HANDLE_ERROR(make_big_int(gasLimitStr.c_str(), reset_sp(amount_gas_limit)));
            HANDLE_ERROR(properties_set_big_int_value(fee, "gas_limit", amount_gas_limit.get()));
        }

        BinaryDataPtr serialized;
        HANDLE_ERROR(transaction_serialize(transaction.get(), reset_sp(serialized)));

        jbyteArray resultArray = env->NewByteArray(serialized.get()->len);
        env->SetByteArrayRegion(resultArray, 0, serialized.get()->len,
                                reinterpret_cast<const jbyte *>(serialized->data));
        return resultArray;
    } catch (std::exception const &e) {
        throw_java_exception_str(env, e.what());
    } catch (...) {
        throw_java_exception_str(env, "something went wrong");
    }

    return jbyteArray();
}

JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_makeTransactionETH(JNIEnv *env, jclass type, jbyteArray jSeed,
                                                       jint jWalletIndex,
                                                       jint jAddressIndex, jint jChainId,
                                                       jint jNetType, jstring jBalance,
                                                       jstring jAmount, jstring jDestinationAddress,
                                                       jstring jGasLimit, jstring jGasPrice,
                                                       jstring jNonce) {

    using namespace multy_core::internal;
    const jstring defaultResult{};

    size_t len = (size_t) env->GetArrayLength(jSeed);
    unsigned char *buf = new unsigned char[len];
    env->GetByteArrayRegion(jSeed, 0, len, reinterpret_cast<jbyte *>(buf));

    ExtendedKeyPtr rootKey;

    BinaryData seed{buf, len};
    HANDLE_ERROR(make_master_key(&seed, reset_sp(rootKey)));

    HDAccountPtr hdAccount;
    HANDLE_ERROR(make_hd_account(rootKey.get(),
                                 BlockchainType{(Blockchain) jChainId, (size_t) jNetType},
                                 BITCOIN_ADDRESS_P2PKH,
                                 jWalletIndex,
                                 reset_sp(hdAccount)));

    AccountPtr account;
    HANDLE_ERROR(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, jAddressIndex,
                                      reset_sp(account)));

    KeyPtr keyPtr;
    HANDLE_ERROR(account_get_key(account.get(), KEY_TYPE_PRIVATE, reset_sp(keyPtr)));

    ConstCharPtr privKeyStr;
    HANDLE_ERROR(key_to_string(keyPtr.get(), reset_sp(privKeyStr)));

    jstring jPrivateKey = env->NewStringUTF(privKeyStr.get());


    return Java_io_multy_util_NativeDataHelper_makeTransactionETHFromKey(env, type, jPrivateKey,
                                                                         jChainId,
                                                                         jNetType,
                                                                         jBalance, jAmount,
                                                                         jDestinationAddress,
                                                                         jGasLimit,
                                                                         jGasPrice, jNonce);
}

//JNIEXPORT jbyteArray JNICALL
//Java_io_multy_util_NativeDataHelper_makeTransactionETH(JNIEnv *env, jclass type, jbyteArray jSeed,
//                                                       jint jWalletIndex,
//                                                       jint jAddressIndex, jint jChainId,
//                                                       jint jNetType, jstring jBalance,
//                                                       jstring jAmount, jstring jDestinationAddress,
//                                                       jstring jGasLimit, jstring jGasPrice,
//                                                       jstring jNonce) {
//
//    using namespace multy_core::internal;
//    const jbyteArray defaultResult{};
//
//    size_t len = (size_t) env->GetArrayLength(jSeed);
//    unsigned char *seedBuf = new unsigned char[len];
//    env->GetByteArrayRegion(jSeed, 0, len, reinterpret_cast<jbyte *>(seedBuf));
//
//    const char *balanceStr = env->GetStringUTFChars(jBalance, nullptr);
//    const char *amountStr = env->GetStringUTFChars(jAmount, nullptr);
//    const char *destinationAddressStr = env->GetStringUTFChars(jDestinationAddress, nullptr);
//    const char *gasLimitStr = env->GetStringUTFChars(jGasLimit, nullptr);
//    const char *gasPriceStr = env->GetStringUTFChars(jGasPrice, nullptr);
//    const char *nonceStr = env->GetStringUTFChars(jNonce, nullptr);
////    const char *payloadStr = env->GetStringUTFChars(payload, nullptr);
//
//    try {
//        ExtendedKeyPtr rootKey;
//
//        BinaryData seed{seedBuf, len};
//        HANDLE_ERROR(make_master_key(&seed, reset_sp(rootKey)));
//
//        HDAccountPtr hdAccount;
//        HANDLE_ERROR(make_hd_account(rootKey.get(),
//                                     BlockchainType{(Blockchain) jChainId, (size_t) jNetType},
//                                     ACCOUNT_TYPE_DEFAULT,
//                                     jWalletIndex,
//                                     reset_sp(hdAccount)));
//
//        AccountPtr account;
//        HANDLE_ERROR(
//                make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, jAddressIndex,
//                                     reset_sp(account)));
//
//
//        TransactionPtr transaction;
//        HANDLE_ERROR(make_transaction(account.get(), reset_sp(transaction)));
//
//        {
//            Properties *properties = nullptr;
//            BigIntPtr nonce;
//            HANDLE_ERROR(make_big_int(nonceStr, reset_sp(nonce)));
//            HANDLE_ERROR(transaction_get_properties(transaction.get(), &properties));
//            HANDLE_ERROR(properties_set_big_int_value(properties, "nonce", nonce.get()));
////            HANDLE_ERROR(properties_set_int32_value(properties, "chain_id", ETHEREUM_CHAIN_ID_RINKEBY));
//        }
//
//        {
//            Properties *source = nullptr;
//            HANDLE_ERROR(transaction_add_source(transaction.get(), &source));
//            // Address balance
//            BigIntPtr balance;
//            HANDLE_ERROR(make_big_int(balanceStr, reset_sp(balance)));
//            HANDLE_ERROR(properties_set_big_int_value(source, "amount", balance.get()));
//        }
//
//        {
//            Properties *destination = nullptr;
//            HANDLE_ERROR(transaction_add_destination(transaction.get(), &destination));
//
//            BigIntPtr amount;
//            HANDLE_ERROR(make_big_int(amountStr, reset_sp(amount)));
//            HANDLE_ERROR(properties_set_big_int_value(destination, "amount", amount.get()));
//
////            BinaryDataPtr address;
////            HANDLE_ERROR(make_binary_data_from_hex(destinationAddressStr, reset_sp(address)));
//            HANDLE_ERROR(
//                    properties_set_string_value(destination, "address", destinationAddressStr));
//        }
//
////        {
////            BinaryDataPtr data;
////            HANDLE_ERROR(make_binary_data_from_hex(payloadStr, reset_sp(data)));
////            HANDLE_ERROR(transaction_set_message(transaction.get(), data.get()));
////        }
//
//        {
//            Properties *fee = nullptr;
//            HANDLE_ERROR(transaction_get_fee(transaction.get(), &fee));
//
//            BigIntPtr amount_gas_price;
//            HANDLE_ERROR(make_big_int(gasPriceStr, reset_sp(amount_gas_price)));
//            HANDLE_ERROR(properties_set_big_int_value(fee, "gas_price", amount_gas_price.get()));
//
//            BigIntPtr amount_gas_limit;
//            HANDLE_ERROR(make_big_int(gasLimitStr, reset_sp(amount_gas_limit)));
//            HANDLE_ERROR(properties_set_big_int_value(fee, "gas_limit", amount_gas_limit.get()));
//        }
//
//        BinaryDataPtr serialized;
//        HANDLE_ERROR(transaction_serialize(transaction.get(), reset_sp(serialized)));
//
//        env->ReleaseStringUTFChars(jBalance, balanceStr);
//        env->ReleaseStringUTFChars(jAmount, amountStr);
//        env->ReleaseStringUTFChars(jDestinationAddress, destinationAddressStr);
//        env->ReleaseStringUTFChars(jGasLimit, gasLimitStr);
//        env->ReleaseStringUTFChars(jGasPrice, gasPriceStr);
//        env->ReleaseStringUTFChars(jNonce, nonceStr);
//
//        jbyteArray resultArray = env->NewByteArray(serialized.get()->len);
//        env->SetByteArrayRegion(resultArray, 0, serialized.get()->len,
//                                reinterpret_cast<const jbyte *>(serialized->data));
//        return resultArray;
//    } catch (std::exception const &e) {
//        throw_java_exception_str(env, e.what());
//    } catch (...) {
//        throw_java_exception_str(env, "something went wrong");
//    }
//
//    return jbyteArray();
//}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_sendEOS(JNIEnv *env, jclass type_,
                                            jint jChainId, jint jNetType, jstring key,
                                            jint blockNumber, jstring refBlockPrefix,
                                            jstring time, jstring balance, jstring senderAddress,
                                            jstring sendAmount, jstring receiverAddress) {

    using namespace multy_core::internal;
    const jbyteArray defaultResult{};

    JniString blockPrefixStr(env, refBlockPrefix);
    JniString timeStr(env, time);
    JniString balanceStr(env, balance);
    JniString senderAddressStr(env, senderAddress);
    JniString sendAmountStr(env, sendAmount);
    JniString receiverAddressStr(env, receiverAddress);
    JniString keyStr(env, key);

    AccountPtr account;


    HANDLE_ERROR(make_account(
            BlockchainType{(Blockchain) jChainId, (size_t) jNetType}, ACCOUNT_TYPE_DEFAULT,
            keyStr.c_str(), reset_sp(account)));

    TransactionPtr transaction;
    HANDLE_ERROR(make_transaction(account.get(), reset_sp(transaction)));

    {
        Properties *properties = nullptr;
        const int32_t block_num = static_cast<int32_t>(blockNumber);

        BigIntPtr ref_block_prefix;
        HANDLE_ERROR(make_big_int(blockPrefixStr.c_str(), reset_sp(ref_block_prefix)));

        HANDLE_ERROR(properties_set_int32_value(properties, "block_num", block_num));
        HANDLE_ERROR(properties_set_big_int_value(properties, "ref_block_prefix",
                                                  ref_block_prefix.get()));
        HANDLE_ERROR(properties_set_string_value(properties, "expiration", timeStr.c_str()));
    }

    {
        Properties *source = nullptr;
        HANDLE_ERROR(transaction_add_source(transaction.get(), &source));


        BigIntPtr balance;
        HANDLE_ERROR(make_big_int(balanceStr.c_str(), reset_sp(balance)));

        HANDLE_ERROR(properties_set_big_int_value(source, "amount", balance.get()));
        HANDLE_ERROR(properties_set_string_value(source, "address", senderAddressStr.c_str()));
    }

    {
        Properties *destination = nullptr;
        HANDLE_ERROR(transaction_add_destination(transaction.get(), &destination));

        BigIntPtr amount;
        HANDLE_ERROR(make_big_int(sendAmountStr.c_str(), reset_sp(amount)));
        HANDLE_ERROR(properties_set_big_int_value(destination, "amount", amount.get()));
        HANDLE_ERROR(
                properties_set_string_value(destination, "address", receiverAddressStr.c_str()));
    }

    ConstCharPtr signatures;
    HANDLE_ERROR(transaction_serialize_encoded(transaction.get(), reset_sp(signatures)));


    return env->NewStringUTF(signatures.get());;
}

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
    using namespace multy_core::internal;
    const jbyteArray defaultResult{};

    jbyte *data = env->GetByteArrayElements(s_, nullptr);
    FINALLY(env->ReleaseByteArrayElements(s_, data, 0));

    std::array<uint8_t, SHA3_256> hash_buffer;
    BinaryData output{hash_buffer.data(), hash_buffer.size()};

    const BinaryData input{reinterpret_cast<unsigned char *>(data),
                           static_cast<size_t>(env->GetArrayLength(s_))};
    HANDLE_ERROR(sha3(&input, &output));

    jbyteArray resultArray = env->NewByteArray(output.len);
    env->SetByteArrayRegion(resultArray, 0, output.len,
                            reinterpret_cast<const jbyte *>(output.data));
    return resultArray;
}

//func createPublicInfo(binaryData: inout BinaryData, blockchainType: BlockchainType, privateKey: String) -> Result<Dictionary<String, String>, String> {
//    let binaryDataPointer = UnsafeMutablePointer(mutating: &binaryData)
//    let privateKeyPointer = privateKey.UTF8CStringPointer
//    var walletDict = Dictionary<String, String>()
//
//    //HD Account
//    let newAccountPointer: UnsafeMutablePointer<OpaquePointer?> = allocateUnsafeMutableObject()
//
//    //New address
//    let newAddressPointer: UnsafeMutablePointer<OpaquePointer?> = allocateUnsafeMutableObject()
//    let newAddressStringPointer: UnsafeMutablePointer<UnsafePointer<Int8>?> = allocateUnsafeMutableObject()
//
//    //Private
//    let addressPrivateKeyPointer: UnsafeMutablePointer<OpaquePointer?> = allocateUnsafeMutableObject()
//    let privateKeyStringPointer: UnsafeMutablePointer<UnsafePointer<Int8>?> = allocateUnsafeMutableObject()
//
//    //Public
//    let addressPublicKeyPointer: UnsafeMutablePointer<OpaquePointer?> = allocateUnsafeMutableObject()
//    let publicKeyStringPointer: UnsafeMutablePointer<UnsafePointer<Int8>?> = allocateUnsafeMutableObject()
//
//    //placed here since we have multiple returns
//    defer {
//            free_account(newAccountPointer.pointee)
//
//            free_key(addressPrivateKeyPointer.pointee)
//            free_string(privateKeyStringPointer.pointee)
//
//            free_key(addressPublicKeyPointer.pointee)
//            free_string(publicKeyStringPointer.pointee)
//
//            newAccountPointer.deallocate()
//
//            newAddressPointer.deallocate()
//            newAddressStringPointer.deallocate()
//
//            addressPrivateKeyPointer.deallocate()
//            privateKeyStringPointer.deallocate()
//
//            addressPublicKeyPointer.deallocate()
//            publicKeyStringPointer.deallocate()
//    }
//
//    let ma = make_account(blockchainType, ACCOUNT_TYPE_DEFAULT.rawValue, privateKeyPointer, newAccountPointer)
//
//    if ma != nil {
//                let error = errorString(from: ma, mask: "make_account")
//
//                return Result.failure(error!)
//        }
//
//
//    let gakPRIV = account_get_key(newAccountPointer.pointee, KEY_TYPE_PRIVATE, addressPrivateKeyPointer)
//    _ = errorString(from: gakPRIV, mask: "account_get_key:KEY_TYPE_PRIVATE")
//    let gakPUBL = account_get_key(newAccountPointer.pointee, KEY_TYPE_PUBLIC, addressPublicKeyPointer)
//    _ = errorString(from: gakPUBL, mask: "account_get_key:KEY_TYPE_PUBLIC")
//
//    let ktsPRIV = key_to_string(addressPrivateKeyPointer.pointee, privateKeyStringPointer)
//    _ = errorString(from: ktsPRIV, mask: "key_to_string:KEY_TYPE_PRIVATE")
//    let ktsPUBL = key_to_string(addressPublicKeyPointer.pointee, publicKeyStringPointer)
//    _ = errorString(from: ktsPUBL, mask: "key_to_string:KEY_TYPE_PUBLIC")
//
//    var privateKeyString : String?
//                           var publicKeyString : String?
//
//    if ktsPRIV != nil {
//                let error = errorString(from: ktsPRIV, mask: "key_to_string:KEY_TYPE_PRIVATE")
//
//                return Result.failure(error!)
//        } else {
//        privateKeyString = String(cString: privateKeyStringPointer.pointee!)
//        walletDict["privateKey"] = privateKeyString
//    }
//
//    if ktsPUBL != nil {
//                let error = errorString(from: ktsPUBL, mask: "key_to_string:KEY_TYPE_PUBLIC")
//
//                return Result.failure(error!)
//        } else {
//        publicKeyString = String(cString: publicKeyStringPointer.pointee!)
//        walletDict["publicKey"] = publicKeyString
//    }
//
//    return Result.success(walletDict)
//}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_createEthMultisigWalletFromKey(JNIEnv *env, jclass type,
                                                                   jstring privateKey_,
                                                                   jint chainId, jint netType,
                                                                   jstring balance_,
                                                                   jstring gasLimit_,
                                                                   jstring gasPrice_,
                                                                   jstring nonce_,
                                                                   jstring factoryAddress_,
                                                                   jstring ownerAddress_,
                                                                   jint confirmation,
                                                                   jstring price_) {

    using namespace multy_core::internal;
    const jbyteArray defaultResult{};

    JniString privateKey(env, privateKey_);
    JniString balanceStr(env, balance_);
    JniString gasLimitStr(env, gasLimit_);
    JniString gasPriceStr(env, gasPrice_);
    JniString nonceStr(env, nonce_);
    JniString factoryAddressStr(env, factoryAddress_);
    JniString ownerAddressesStr(env, ownerAddress_);
    JniString priceStr(env, price_);


    try {

        AccountPtr account;
        HANDLE_ERROR(make_account(BlockchainType{(Blockchain) chainId, (size_t) netType},
                                  ACCOUNT_TYPE_DEFAULT,
                                  privateKey.c_str(),
                                  reset_sp(account)));
        TransactionBuilderPtr builder;
        HANDLE_ERROR(
                make_transaction_builder(
                        account.get(),
                        ETHEREUM_TRANSACTION_BUILDER_MULTISIG,
                        "new_wallet",
                        reset_sp(builder)));

        {
            Properties *builder_propertie;
            BigIntPtr balance;
            BigIntPtr price;
            HANDLE_ERROR(make_big_int(balanceStr.c_str(),
                                      reset_sp(balance)));  // constr char* "1000000000000000000"
            HANDLE_ERROR(make_big_int(priceStr.c_str(), reset_sp(price))); // sample: "0"
            HANDLE_ERROR(transaction_builder_get_properties(builder.get(), &builder_propertie));
            HANDLE_ERROR(properties_set_big_int_value(builder_propertie, "price", price.get()));
            HANDLE_ERROR(properties_set_big_int_value(builder_propertie, "balance", balance.get()));
            HANDLE_ERROR(properties_set_string_value(builder_propertie, "factory_address",
                                                     factoryAddressStr.c_str())); // sample: "0x116ffa11dd8829524767f561da5d33d3d170e17d"
            HANDLE_ERROR(properties_set_string_value(builder_propertie, "owners",
                                                     ownerAddressesStr.c_str()));// sample: "[0x6b4be1fc5fa05c5d959d27155694643b8af72fd8, 0x2b74679d2a190fd679a85ce7767c05605237f030, 0xbc11d8f8d741515d2696e34333a0671adb6aee34]"));
            HANDLE_ERROR(properties_set_int32_value(builder_propertie, "confirmations",
                                                    confirmation));  //sample: int32 2
        }

        TransactionPtr transaction;
        transaction_builder_make_transaction(builder.get(), reset_sp(transaction));
        {
            Properties *properties = nullptr;
            BigIntPtr nonce;
            HANDLE_ERROR(make_big_int(nonceStr.c_str(), reset_sp(nonce)));
            HANDLE_ERROR(transaction_get_properties(transaction.get(), &properties));
            HANDLE_ERROR(properties_set_big_int_value(properties, "nonce", nonce.get()));
        }

        {
            Properties *fee = nullptr;
            HANDLE_ERROR(transaction_get_fee(transaction.get(), &fee));

            BigIntPtr amount_gas_price;

            HANDLE_ERROR(make_big_int(gasPriceStr.c_str(), reset_sp(amount_gas_price)));
            HANDLE_ERROR(properties_set_big_int_value(fee, "gas_price", amount_gas_price.get()));

            BigIntPtr amount_gas_limit;
            HANDLE_ERROR(make_big_int(gasLimitStr.c_str(), reset_sp(amount_gas_limit)));
            HANDLE_ERROR(properties_set_big_int_value(fee, "gas_limit", amount_gas_limit.get()));
        }

        BinaryDataPtr serialized;
        HANDLE_ERROR(transaction_serialize(transaction.get(), reset_sp(serialized)));

        jbyteArray resultArray = env->NewByteArray(serialized.get()->len);
        env->SetByteArrayRegion(resultArray, 0, serialized.get()->len,
                                reinterpret_cast<const jbyte *>(serialized->data));
        return resultArray;
    } catch (std::exception const &e) {
        throw_java_exception_str(env, e.what());
    } catch (...) {
        throw_java_exception_str(env, "something went wrong");
    }

    return jbyteArray();
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_createEthMultisigWallet(JNIEnv *env, jclass type,
                                                            jbyteArray jSeed,
                                                            jint jWalletIndex,
                                                            jint jAddressIndex, jint jChainId,
                                                            jint jNetType, jstring jBalance,
                                                            jstring jGasLimit, jstring jGasPrice,
                                                            jstring jNonce, jstring factoryAddress,
                                                            jstring ownerAddresses,
                                                            jint confirmation,
                                                            jstring price) {

    using namespace multy_core::internal;
    const jstring defaultResult{};

    size_t len = (size_t) env->GetArrayLength(jSeed);
    unsigned char *buf = new unsigned char[len];
    env->GetByteArrayRegion(jSeed, 0, len, reinterpret_cast<jbyte *>(buf));

    ExtendedKeyPtr rootKey;

    BinaryData seed{buf, len};
    HANDLE_ERROR(make_master_key(&seed, reset_sp(rootKey)));

    HDAccountPtr hdAccount;
    HANDLE_ERROR(make_hd_account(rootKey.get(),
                                 BlockchainType{(Blockchain) jChainId, (size_t) jNetType},
                                 BITCOIN_ADDRESS_P2PKH,
                                 jWalletIndex,
                                 reset_sp(hdAccount)));

    AccountPtr account;
    HANDLE_ERROR(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, jAddressIndex,
                                      reset_sp(account)));

    KeyPtr keyPtr;
    HANDLE_ERROR(account_get_key(account.get(), KEY_TYPE_PRIVATE, reset_sp(keyPtr)));

    ConstCharPtr privKeyStr;
    HANDLE_ERROR(key_to_string(keyPtr.get(), reset_sp(privKeyStr)));

    jstring key = env->NewStringUTF(privKeyStr.get());


    return Java_io_multy_util_NativeDataHelper_createEthMultisigWalletFromKey(env, type,
                                                                              key,
                                                                              jChainId,
                                                                              jNetType,
                                                                              jBalance,
                                                                              jGasLimit,
                                                                              jGasPrice,
                                                                              jNonce,
                                                                              factoryAddress,
                                                                              ownerAddresses,
                                                                              confirmation,
                                                                              price);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_makeAccountAddressFromKey(JNIEnv *env, jclass type,
                                                              jstring privateKey_, jint chainId,
                                                              jint networkId) {
    using namespace multy_core::internal;
    const jstring defaultResult{};

    JniString privateKey(env, privateKey_);

    AccountPtr account;
    HANDLE_ERROR(make_account(BlockchainType{(Blockchain) chainId, (size_t) networkId},
                              ACCOUNT_TYPE_DEFAULT,
                              privateKey.c_str(),
                              reset_sp(account)));
    ConstCharPtr address;
    HANDLE_ERROR(account_get_address_string(account.get(), reset_sp(address)));

    return env->NewStringUTF(address.get());
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_makeTransactionMultisigETHFromKey(JNIEnv *env, jclass type,
                                                                      jstring privatKey_,
                                                                      jint chainId, jint networkId,
                                                                      jstring linkedBalance_,
                                                                      jstring multisigWalletAddress_,
                                                                      jstring amount_,
                                                                      jstring destionationAddress_,
                                                                      jstring gasLimit_,
                                                                      jstring gasPrice_,
                                                                      jstring nonce_) {
    using namespace multy_core::internal;
    const jbyteArray defaultResult{};

    JniString privateKey(env, privatKey_);
    JniString balanceStr(env, linkedBalance_);
    JniString msWalletAddressStr(env, multisigWalletAddress_);
    JniString amountStr(env, amount_);
    JniString destionationAddressStr(env, destionationAddress_);
    JniString gasLimitStr(env, gasLimit_);
    JniString gasPriceStr(env, gasPrice_);
    JniString nonceStr(env, nonce_);


    try {
        AccountPtr account;
        HANDLE_ERROR(make_account(BlockchainType{(Blockchain) chainId, (size_t) networkId},
                                  ACCOUNT_TYPE_DEFAULT,
                                  privateKey.c_str(),
                                  reset_sp(account)));

        TransactionBuilderPtr builder;
        HANDLE_ERROR(
                make_transaction_builder(
                        account.get(),
                        ETHEREUM_TRANSACTION_BUILDER_MULTISIG,
                        "new_request",
                        reset_sp(builder)));
        {
            Properties *builder_propertie;
            BigIntPtr amount;
            BigIntPtr balance;
            HANDLE_ERROR(make_big_int(balanceStr.c_str(), reset_sp(balance)));
            HANDLE_ERROR(make_big_int(amountStr.c_str(), reset_sp(amount)));
            HANDLE_ERROR(transaction_builder_get_properties(builder.get(), &builder_propertie));
            HANDLE_ERROR(transaction_builder_get_properties(builder.get(), &builder_propertie));
            HANDLE_ERROR(properties_set_big_int_value(builder_propertie, "balance", balance.get()));
            HANDLE_ERROR(properties_set_string_value(builder_propertie, "wallet_address",
                                                     msWalletAddressStr.c_str()));
            HANDLE_ERROR(properties_set_string_value(builder_propertie, "dest_address",
                                                     destionationAddressStr.c_str()));
            HANDLE_ERROR(properties_set_big_int_value(builder_propertie, "amount", amount.get()));
        }

        TransactionPtr transaction;
        transaction_builder_make_transaction(builder.get(), reset_sp(transaction));

        {
            Properties *properties = nullptr;
            BigIntPtr nonce;
            HANDLE_ERROR(make_big_int(nonceStr.c_str(), reset_sp(nonce)));
            HANDLE_ERROR(transaction_get_properties(transaction.get(), &properties));
            HANDLE_ERROR(properties_set_big_int_value(properties, "nonce", nonce.get()));
        }

        {
            Properties *fee = nullptr;
            HANDLE_ERROR(transaction_get_fee(transaction.get(), &fee));

            BigIntPtr amount_gas_price;
            HANDLE_ERROR(make_big_int(gasPriceStr.c_str(), reset_sp(amount_gas_price)));
            HANDLE_ERROR(properties_set_big_int_value(fee, "gas_price", amount_gas_price.get()));

            BigIntPtr amount_gas_limit;
            HANDLE_ERROR(make_big_int(gasLimitStr.c_str(), reset_sp(amount_gas_limit)));
            HANDLE_ERROR(properties_set_big_int_value(fee, "gas_limit", amount_gas_limit.get()));
        }

        BinaryDataPtr serialized;

        HANDLE_ERROR(transaction_serialize(transaction.get(), reset_sp(serialized)));

        //  env->ReleaseByteArrayElements(seed_, seedBuf, 0);

        jbyteArray resultArray = env->NewByteArray(serialized.get()->len);
        env->SetByteArrayRegion(resultArray, 0, serialized.get()->len,
                                reinterpret_cast<const jbyte *>(serialized->data));

        return resultArray;
    } catch (std::exception const &e) {
        throw_java_exception_str(env, e.what());
    } catch (...) {
        throw_java_exception_str(env, "something went wrong");
    }
    return jbyteArray();
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_makeTransactionMultisigETH(JNIEnv *env, jclass type,
                                                               jbyteArray seed_,
                                                               jint walletIndex, jint addressIndex,
                                                               jint chainId, jint networkId,
                                                               jstring balance_,
                                                               jstring msWalletAddress_,
                                                               jstring amount_,
                                                               jstring destionationAddress_,
                                                               jstring gasLimit_, jstring gasPrice_,
                                                               jstring nonce_) {

    using namespace multy_core::internal;
    const jstring defaultResult{};

    size_t len = (size_t) env->GetArrayLength(seed_);
    unsigned char *buf = new unsigned char[len];
    env->GetByteArrayRegion(seed_, 0, len, reinterpret_cast<jbyte *>(buf));

    ExtendedKeyPtr rootKey;

    BinaryData seed{buf, len};
    HANDLE_ERROR(make_master_key(&seed, reset_sp(rootKey)));

    HDAccountPtr hdAccount;
    HANDLE_ERROR(make_hd_account(rootKey.get(),
                                 BlockchainType{(Blockchain) chainId, (size_t) networkId},
                                 BITCOIN_ADDRESS_P2PKH,
                                 walletIndex,
                                 reset_sp(hdAccount)));

    AccountPtr account;
    HANDLE_ERROR(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, addressIndex,
                                      reset_sp(account)));

    KeyPtr keyPtr;
    HANDLE_ERROR(account_get_key(account.get(), KEY_TYPE_PRIVATE, reset_sp(keyPtr)));

    ConstCharPtr privKeyStr;
    HANDLE_ERROR(key_to_string(keyPtr.get(), reset_sp(privKeyStr)));

    jstring key = env->NewStringUTF(privKeyStr.get());


    return Java_io_multy_util_NativeDataHelper_makeTransactionMultisigETHFromKey(env, type,
                                                                                 key,
                                                                                 chainId,
                                                                                 networkId,
                                                                                 balance_,
                                                                                 msWalletAddress_,
                                                                                 amount_,
                                                                                 destionationAddress_,
                                                                                 gasLimit_,
                                                                                 gasPrice_,
                                                                                 nonce_);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_confirmTransactionMultisigETHFromKey(JNIEnv *env, jclass type,
                                                                         jstring privateKey_,
                                                                         jint chainId,
                                                                         jint networkId,
                                                                         jstring linkedBalance_,
                                                                         jstring multisigWalletAddress_,
                                                                         jstring requestId_,
                                                                         jstring gasLimit_,
                                                                         jstring gasPrice_,
                                                                         jstring nonce_) {
    using namespace multy_core::internal;
    const jbyteArray defaultResult{};

    JniString privateKey(env, privateKey_);
    JniString linkedBalanceStr(env, linkedBalance_);
    JniString multisigWalletAddressStr(env, multisigWalletAddress_);
    JniString requestIdStr(env, requestId_);
    JniString gasLimitStr(env, gasLimit_);
    JniString gasPriceStr(env, gasPrice_);
    JniString nonceStr(env, nonce_);

    try {
        AccountPtr account;
        HANDLE_ERROR(make_account(BlockchainType{(Blockchain) chainId, (size_t) networkId},
                                  ACCOUNT_TYPE_DEFAULT,
                                  privateKey.c_str(),
                                  reset_sp(account)));

        TransactionBuilderPtr builder;
        HANDLE_ERROR(
                make_transaction_builder(
                        account.get(),
                        ETHEREUM_TRANSACTION_BUILDER_MULTISIG,
                        "request",
                        reset_sp(builder)));

        {
            Properties *builder_propertie;
            BigIntPtr request;
            BigIntPtr balance;
            HANDLE_ERROR(make_big_int(linkedBalanceStr.c_str(), reset_sp(balance)));
            HANDLE_ERROR(make_big_int(requestIdStr.c_str(), reset_sp(request)));
            HANDLE_ERROR(transaction_builder_get_properties(builder.get(), &builder_propertie));
            HANDLE_ERROR(properties_set_big_int_value(builder_propertie, "balance", balance.get()));
            HANDLE_ERROR(properties_set_string_value(builder_propertie, "wallet_address",
                                                     multisigWalletAddressStr.c_str()));
            HANDLE_ERROR(properties_set_string_value(builder_propertie, "action", "confirm"));
            HANDLE_ERROR(
                    properties_set_big_int_value(builder_propertie, "request_id", request.get()));

        }

        TransactionPtr transaction;
        transaction_builder_make_transaction(builder.get(), reset_sp(transaction));

        {
            Properties *properties = nullptr;
            BigIntPtr nonce;
            HANDLE_ERROR(make_big_int(nonceStr.c_str(), reset_sp(nonce)));
            HANDLE_ERROR(transaction_get_properties(transaction.get(), &properties));
            HANDLE_ERROR(properties_set_big_int_value(properties, "nonce", nonce.get()));
        }

        {
            Properties *fee = nullptr;
            HANDLE_ERROR(transaction_get_fee(transaction.get(), &fee));

            BigIntPtr amount_gas_price;

            HANDLE_ERROR(make_big_int(gasPriceStr.c_str(), reset_sp(amount_gas_price)));
            HANDLE_ERROR(properties_set_big_int_value(fee, "gas_price", amount_gas_price.get()));

            BigIntPtr amount_gas_limit;
            HANDLE_ERROR(make_big_int(gasLimitStr.c_str(), reset_sp(amount_gas_limit)));
            HANDLE_ERROR(properties_set_big_int_value(fee, "gas_limit", amount_gas_limit.get()));
        }

        BinaryDataPtr serialized;

        HANDLE_ERROR(transaction_serialize(transaction.get(), reset_sp(serialized)));

//        env->ReleaseByteArrayElements(seed_, seed, 0);

        jbyteArray resultArray = env->NewByteArray(serialized.get()->len);
        env->SetByteArrayRegion(resultArray, 0, serialized.get()->len,
                                reinterpret_cast<const jbyte *>(serialized->data));


        return resultArray;
    } catch (std::exception const &e) {
        throw_java_exception_str(env, e.what());
    } catch (...) {
        throw_java_exception_str(env, "something went wrong");
    }
    return jbyteArray();
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_confirmTransactionMultisigETH(JNIEnv *env, jclass type,
                                                                  jbyteArray seed_,
                                                                  jint walletIndex,
                                                                  jint addressIndex, jint chainId,
                                                                  jint networkId,
                                                                  jstring linkedBalance_,
                                                                  jstring multisigWalletAddress_,
                                                                  jstring requestId_,
                                                                  jstring gasLimit_,
                                                                  jstring gasPrice_,
                                                                  jstring nonce_) {

    using namespace multy_core::internal;
    const jstring defaultResult{};

    size_t len = (size_t) env->GetArrayLength(seed_);
    unsigned char *buf = new unsigned char[len];
    env->GetByteArrayRegion(seed_, 0, len, reinterpret_cast<jbyte *>(buf));

    ExtendedKeyPtr rootKey;

    BinaryData seed{buf, len};
    HANDLE_ERROR(make_master_key(&seed, reset_sp(rootKey)));

    HDAccountPtr hdAccount;
    HANDLE_ERROR(make_hd_account(rootKey.get(),
                                 BlockchainType{(Blockchain) chainId, (size_t) networkId},
                                 BITCOIN_ADDRESS_P2PKH,
                                 walletIndex,
                                 reset_sp(hdAccount)));

    AccountPtr account;
    HANDLE_ERROR(make_hd_leaf_account(hdAccount.get(), ADDRESS_EXTERNAL, addressIndex,
                                      reset_sp(account)));

    KeyPtr keyPtr;
    HANDLE_ERROR(account_get_key(account.get(), KEY_TYPE_PRIVATE, reset_sp(keyPtr)));

    ConstCharPtr privKeyStr;
    HANDLE_ERROR(key_to_string(keyPtr.get(), reset_sp(privKeyStr)));

    jstring key = env->NewStringUTF(privKeyStr.get());


    return Java_io_multy_util_NativeDataHelper_confirmTransactionMultisigETHFromKey(env, type,
                                                                                    key,
                                                                                    chainId,
                                                                                    networkId,
                                                                                    linkedBalance_,
                                                                                    multisigWalletAddress_,
                                                                                    requestId_,
                                                                                    gasLimit_,
                                                                                    gasPrice_,
                                                                                    nonce_);
}
