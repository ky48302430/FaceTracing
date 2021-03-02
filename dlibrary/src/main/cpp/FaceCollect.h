#ifndef FACETRACING_FACECOLLECT_H
#define FACETRACING_FACECOLLECT_H

#include "FaceBase.h"
using namespace cv;

class FaceCollect : public FaceBase{
public :
    char *path= NULL;
    int sampleNumTotal = 100;
    int sampleNum = 0;
public:
    FaceCollect(JniCall *jniCall,const char *model,const char *path,int sampleNumTotal);
    ~FaceCollect();

public:
    void handlerTracker(Mat &src,Rect &face);
};


#endif //FACETRACING_FACECOLLECT_H
