#include "FaceDetect.h"

FaceDetect::FaceDetect(JniCall *jniCall, const char *model)
        :FaceBase(jniCall,model)
{

}


void FaceDetect::handlerTracker(Mat &src,Rect &face) {
     //画矩形
     rectangle(src, face, Scalar(255, 255, 0));
     jniCall->callRecognitionResult(THREAD_MAIN,1,"识别成功");
}

FaceDetect::~FaceDetect(){
     release();
}


