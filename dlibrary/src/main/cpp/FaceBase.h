//
// Created by MS on 2020/12/9.
//

#ifndef FACETRACING_FACEBASE_H
#define FACETRACING_FACEBASE_H

#include "JniCall.h"
#include "CascadeDetectorAdapter.cpp"
#include <android/native_window_jni.h>

using namespace cv;

class FaceBase {
public:
    DetectionBasedTracker *tracker = NULL;
    ANativeWindow *pWindow = NULL;
    JniCall *jniCall = NULL;
    char *model = NULL;
public:
    FaceBase(JniCall *jniCall, const char *model);
    ~FaceBase();

public:
    void setSurface(ANativeWindow *pWindow);

    void postData(JNIEnv *pEnv, jobject pJobject, jbyteArray pArray, jint w, jint h, jint cameraId);

    virtual void handlerTracker(Mat &src,Rect &face) = 0;

    void createTracker();

    void release();
};

#endif //FACETRACING_FACEBASE_H
