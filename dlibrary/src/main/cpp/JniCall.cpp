#include "JniCall.h"
JniCall::JniCall(JavaVM *javaVM, JNIEnv *jniEnv, jobject jDetectorObj) {
    this->javaVM = javaVM;
    this->jniEnv = jniEnv;
    this->jDetectorObj = jniEnv->NewGlobalRef(jDetectorObj);
    jclass jdetectoerClsss = jniEnv->GetObjectClass(jDetectorObj);
    this->jRecognitionMid = jniEnv->GetMethodID(jdetectoerClsss, "onRecognitionResult", "(ILjava/lang/String;)V");
    this->jSamplePiackMid = jniEnv->GetMethodID(jdetectoerClsss, "onSampleCallBack","(I)V");
}

JniCall::~JniCall() {
    this->jniEnv->DeleteGlobalRef(jDetectorObj);
}

//回调java
void JniCall::callRecognitionResult(ThreadMode threadMode, int code, char *msg) {
    if (threadMode == THREAD_MAIN) {
        jstring  jMsg =  this->jniEnv->NewStringUTF(msg);
        this->jniEnv->CallVoidMethod(this->jDetectorObj,this->jRecognitionMid,code,jMsg);
        this->jniEnv->DeleteLocalRef(jMsg);
    } else {
        // 获取当前线程的 JNIEnv， 通过 JavaVM
        JNIEnv *env;
        if (this->javaVM->AttachCurrentThread(&env,0) !=JNI_OK){
            LOGE("get child thread env error!")
            return;
        }
        jstring  jMsg =  env->NewStringUTF(msg);
        env->CallVoidMethod(this->jDetectorObj,this->jRecognitionMid,code,jMsg);
        env->DeleteLocalRef(jMsg);

        this->javaVM->DetachCurrentThread();
    }
}
//回调java
void JniCall::callSamplePick(ThreadMode threadMode, int num) {
    if (threadMode == THREAD_MAIN) {
            this->jniEnv->CallVoidMethod(this->jDetectorObj,this->jSamplePiackMid,num);
    } else {
        // 获取当前线程的 JNIEnv， 通过 JavaVM
        JNIEnv *env;
        if (this->javaVM->AttachCurrentThread(&env,0) !=JNI_OK){
            LOGE("get child thread env error!")
            return;
        }
       env ->CallVoidMethod(this->jDetectorObj,this->jSamplePiackMid,num);

        this->javaVM->DetachCurrentThread();
    }
}




