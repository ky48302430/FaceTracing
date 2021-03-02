#ifndef FACETRACING_JNICALL_H
#define FACETRACING_JNICALL_H

#include "jni.h"
#include "lm-androidLog.h"

enum ThreadMode {
    THREAD_CHILD, THREAD_MAIN
};

class JniCall {
private:
    JavaVM *javaVM = NULL;
    JNIEnv *jniEnv = NULL;
    jmethodID jSamplePiackMid;
    jmethodID jRecognitionMid;
    jobject jDetectorObj;

public:
    JniCall(JavaVM *javaVM, JNIEnv *jniEnv, jobject jDetectorObj);
    ~JniCall();

public:
    void callRecognitionResult(ThreadMode threadMode, int code, char *msg);
    void callSamplePick(ThreadMode mode, int num);
};

#endif //FACETRACING_JNICALL_H
