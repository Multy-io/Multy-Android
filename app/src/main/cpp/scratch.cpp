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


JavaVM* gJvm = nullptr;
static jobject gClassLoader;
static jmethodID gFindClassMethod;

JNIEnv* getEnv() {
    JNIEnv *env;
    int status = gJvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if(status < 0) {
        status = gJvm->AttachCurrentThread(&env, NULL);
        if(status < 0) {
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

static void *thread_func(void*);

int start_logger(const char *app_name)
{
    tag = app_name;

    /* make stdout line-buffered and stderr unbuffered */
    setvbuf(stdout, 0, _IOLBF, 0);
    setvbuf(stderr, 0, _IONBF, 0);

    /* create the pipe and redirect stdout and stderr */
    pipe(pfd);
    dup2(pfd[1], 1);
    dup2(pfd[1], 2);

    /* spawn the logging thread */
    if(pthread_create(&thr, 0, thread_func, 0) == -1)
        return -1;
    pthread_detach(thr);
    return 0;
}

static void *thread_func(void*)
{
    ssize_t rdsz;
    char buf[128];
    while((rdsz = read(pfd[0], buf, sizeof buf - 1)) > 0) {
        if(buf[rdsz - 1] == '\n') --rdsz;
        buf[rdsz] = 0;  /* add null-terminator */
        __android_log_write(ANDROID_LOG_DEBUG, tag, buf);
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_io_multy_util_NativeDataHelper_runTest(JNIEnv *jenv, jclass jcls) {
    jint jresult = 0;

    start_logger("hallow");
    __android_log_print(ANDROID_LOG_INFO, "foo", "Error: %s", "Hallow");
    char* foo = (char *) "foo";
    jresult = run_tests(1, &foo);
    return jresult;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *pjvm, void *reserved) {
    using namespace wallet_core::internal;

    gJvm = pjvm;  // cache the JavaVM pointer
    auto env = getEnv();
    //replace with one of your classes in the line below
    auto randomClass = env->FindClass("io/multy/util/EntropyProvider");
    jclass classClass = env->GetObjectClass(randomClass);
    auto classLoaderClass = env->FindClass("java/lang/ClassLoader");
    auto getClassLoaderMethod = env->GetMethodID(classClass, "getClassLoader", "()Ljava/lang/ClassLoader;");
    gClassLoader = env->CallObjectMethod(randomClass, getClassLoaderMethod);
    gFindClassMethod = env->GetMethodID(classLoaderClass, "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");

    return JNI_VERSION_1_6;
}

jclass findClass(const char* name) {
    return static_cast<jclass>(getEnv()->CallObjectMethod(gClassLoader, gFindClassMethod, getEnv()->NewStringUTF(name)));
}

JNIEXPORT jbyteArray JNICALL
Java_io_multy_util_NativeDataHelper_makeSeed(JNIEnv *env, jobject obj, jstring string) {
    using namespace wallet_core::internal;

    const char *mnemonic = env->GetStringUTFChars(string, JNI_FALSE);

    BinaryDataPtr data;
    ErrorPtr error;
    error.reset(make_seed(mnemonic, "", reset_sp(data)));

    env->ReleaseStringUTFChars(string, mnemonic);

    if (error) {
        __android_log_print(ANDROID_LOG_INFO, "makeSeed", "Error: %s", error->message);
        return NULL;
    } else {
        jbyteArray array = env->NewByteArray (data.get()->len);
        env->SetByteArrayRegion (array, 0, data.get()->len, reinterpret_cast<const jbyte*>(data->data));
        return array;
    }
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_makeMnemonic(JNIEnv *jniEnv, jobject obj) {

    using namespace wallet_core::internal;

    auto env = getEnv();
    auto randomClass = env->FindClass("io/multy/util/EntropyProvider");

    jmethodID mid = env->GetStaticMethodID(randomClass, "generateKey", "(I)[B");
    jbyteArray result = (jbyteArray) env->CallStaticObjectMethod(randomClass, mid, 160);

    typedef std::vector<unsigned char> bytes;
    int len = env->GetArrayLength (result);
    bytes buf(len, 0);
    //unsigned char* buf = new unsigned char[len];
    env->GetByteArrayRegion (result, 0, len, reinterpret_cast<jbyte*>(buf.data()));

    auto fill_entropy = [](void* data, ::size_t size, void* dest) -> ::size_t {
        const bytes* entropy = (const bytes*)(data);
        const size_t result_size = std::min(size, entropy->size());
        memcpy(dest, entropy->data(), result_size);
        return result_size;
    };
    auto entropy_source = EntropySource{(void*)&buf, fill_entropy};
    ConstCharPtr mnemonic_str;
    ErrorPtr error;
    error.reset(make_mnemonic(entropy_source, reset_sp(mnemonic_str)));

    if (error) {
        __android_log_print(ANDROID_LOG_INFO, "makeMnemonic", "Error: %s", error->message);
        return jniEnv->NewStringUTF(error->message);
    }
    else {
        __android_log_print(ANDROID_LOG_INFO, "makeMnemonic", "SUCCESS: %s", mnemonic_str.get());
        return jniEnv->NewStringUTF(mnemonic_str.get());
    }
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_makeAccountId(JNIEnv *env, jobject obj, jbyteArray array) {

    using namespace wallet_core::internal;

    size_t len = (size_t) env->GetArrayLength (array);
    unsigned char* buf = new unsigned char[len];
    env->GetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(buf));

    ErrorPtr error;
    ExtendedKeyPtr rootKey;

    BinaryData seed{buf, len};
    error.reset(make_master_key(&seed, reset_sp(rootKey)));

    const char *id = nullptr;

    error.reset(make_key_id(rootKey.get(), &id));

    if (error) {
        __android_log_print(ANDROID_LOG_INFO, "makeAccountId", "Error: %s", error->message);
        return env->NewStringUTF(error->message);
    }
    else {
        __android_log_print(ANDROID_LOG_INFO, "makeAccountId", "SUCCESS: %s", id);
        return env->NewStringUTF(id);
    }
}

JNIEXPORT jstring JNICALL
Java_io_multy_util_NativeDataHelper_makeAccountAddress(JNIEnv *env, jobject obj, jbyteArray array) {

    using namespace wallet_core::internal;

    size_t len = (size_t) env->GetArrayLength (array);
    unsigned char* buf = new unsigned char[len];
    env->GetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(buf));

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

    if (error) {
        __android_log_print(ANDROID_LOG_INFO, "accountAddress", "Error: %s", error->message);
        return env->NewStringUTF(error->message);
    }
    else {
        __android_log_print(ANDROID_LOG_INFO, "accountAddress", "SUCCESS: %s", address.get());
        return env->NewStringUTF(address.get());
    }
}

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
MULTY_CORE_API Error* make_seed(
        const char* mnemonic, const char* password, BinaryData** seed);
MULTY_CORE_API Error* seed_to_string(const BinaryData* seed, const char** str);

/** Frees mnemonic, can take null */
MULTY_CORE_API void free_mnemonic(const char* mnemonic);



#ifdef __cplusplus
} // extern "C"
#endif
