#ifndef FACETRACING_FACEDETECT_H
#define FACETRACING_FACEDETECT_H

#include "FaceBase.h"
using namespace cv;

class FaceDetect : public FaceBase {
public:
    FaceDetect(JniCall *jniCall, const char *model);
    ~FaceDetect();
    void handlerTracker(Mat &src,Rect &face);
};

#endif //FACETRACING_FACEDETECT_H
