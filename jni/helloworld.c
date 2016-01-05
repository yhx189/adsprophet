#include <stdio.h>
#include <jni.h>
JNIEXPORT jstring JNICALL Java_com_mytest_JNIActivity_getMessage
          (JNIEnv *env, jobject thisObj) {
                 return (*env)->NewStringUTF(env, "Hello from native code!");
}
