#include <jni.h>

#include "lm-androidLog.h"
#include "JniCall.h"
#include "FaceCollect.h"
#include "FaceDetect.h"
#include "FaceDetect.cpp"
#include "FaceBase.cpp"

ANativeWindow *window = 0;
JavaVM *_vm = 0;
JniCall *jniCall = 0;
FaceCollect *collect = 0;
FaceDetect *detect = 0;

extern "C"
JNIEXPORT void JNICALL
setSurface(JNIEnv *env, jobject instance, jobject surface) {
//    if (window) {
//        delete window;
//        window = 0;
//    }
    window = ANativeWindow_fromSurface(env, surface);

    if (collect) {
        collect->setSurface(window);
    }

    if (detect) {
        detect->setSurface(window);
    }
}



extern "C"
JNIEXPORT void JNICALL
postData(JNIEnv *env, jobject instance,jbyteArray data_, jint w, jint h, jint cameraId) {
    if (collect) {
        collect->postData(env, instance, data_, w, h, cameraId);
    }
    if (detect) {
        detect->postData(env, instance, data_, w, h, cameraId);
    }
}

extern "C"
JNIEXPORT void JNICALL
pickInit(JNIEnv *env, jobject instance, jstring model_, jstring path_, int sampleNum) {
    const char *model = env->GetStringUTFChars(model_, 0);
    const char *path = env->GetStringUTFChars(path_, 0);
    //初始化回调接口类
    jniCall = new JniCall(_vm, env, instance);
    collect = new FaceCollect(jniCall, model, path, sampleNum);
    if (detect) {
        delete detect;
        detect = 0;
    }
    env->ReleaseStringUTFChars(model_, model);
    env->ReleaseStringUTFChars(path_, path);
}

extern "C"
JNIEXPORT void JNICALL
reginInit(JNIEnv *env, jobject instance, jstring model_) {
    const char *model = env->GetStringUTFChars(model_, 0);
    //初始化回调接口类
    jniCall = new JniCall(_vm, env, instance);
    detect = new FaceDetect(jniCall, model);
    if (collect) {
        delete collect;
        collect = 0;
    }
    env->ReleaseStringUTFChars(model_, model);
}

extern "C"
JNIEXPORT void JNICALL
release(JNIEnv *env, jobject instance) {
    if (collect) {
        delete collect;
        collect = 0;
    }

    if (detect) {
        delete detect;
        detect = 0;
    }

    if (jniCall) {
        delete jniCall;
        jniCall = 0;
    }
}


//类名   com.ylm.dlibrary.detector
static const char *mClassName = "com/ylm/dlibrary/detector/FaceDetector";
//静态的Jni native 方法数组
static const JNINativeMethod method[] = {
        //方法名    签名    本地方法
        {"npickInit",   "(Ljava/lang/String;Ljava/lang/String;I)V", (void *) pickInit},
        {"nreginInit",  "(Ljava/lang/String;)V",                    (void *) reginInit},
        {"nsetSurface", "(Landroid/view/Surface;)V",                (void *) setSurface},
        {"npostData",   "([BIII)V",                                 (void *) postData},
        {"nrelease",    "()V",                                      (void *) release}
};

//返回Jni 版本
int JNI_OnLoad(JavaVM *vm, void *r) {
    _vm = vm;
    JNIEnv *env = 0;
    //获得JNIEnv 这里会返回一个值 小于0 代表失败
    jint res = vm->GetEnv((void **) (&env), JNI_VERSION_1_6);

    //判断返回结果
    if (res != JNI_OK) {
        return -1;
    }
    //根据类名找到类，注意有native的类不能被混淆
    jclass jcls = env->FindClass(mClassName);
    //动态注册  第一个参数 类  第二个参数 方法数组  第三个参数 注册多少个方法
    env->RegisterNatives(jcls, method, sizeof(method) / sizeof(JNINativeMethod));
    return JNI_VERSION_1_6;
}








