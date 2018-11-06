#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_csc472_depaul_edu_csc472finalproject_GameMainUi_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
