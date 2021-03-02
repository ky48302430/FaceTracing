#include "FaceBase.h"


FaceBase::FaceBase(JniCall *jniCall, const char *model){
    this->model = (char *) malloc(strlen(model) + 1);
    strcpy(this->model, model);
    this->jniCall = jniCall;
    createTracker();
}

FaceBase::~FaceBase(){
    release();
}
void FaceBase::release() {
    if (model) {
        free(model);
        model = 0;
    }

    if (tracker) {
        tracker->stop();
        delete tracker;
        tracker = 0;
    }
}

void FaceBase::createTracker() {
    if (tracker!=NULL) {
        tracker->stop();
        delete tracker;
        tracker = 0;
    }
    //智能指针
    Ptr<CascadeClassifier> classifier = makePtr<CascadeClassifier>(model);

    //创建一个跟踪适配器
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(classifier);

    Ptr<CascadeClassifier> classifier1 = makePtr<CascadeClassifier>(model);
    //创建一个跟踪适配器
    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(classifier1);
    //拿去用的跟踪器
    DetectionBasedTracker::Parameters DetectorParams;
    tracker = new DetectionBasedTracker(mainDetector, trackingDetector, DetectorParams);
    //开启跟踪器
    tracker->run();
}

void FaceBase::setSurface(ANativeWindow *pWindow) {
    this->pWindow = pWindow;
}

void FaceBase::postData(JNIEnv *env, jobject instance, jbyteArray data_, jint w, jint h, jint cameraId) {
// nv21的数据
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    //mat  data-》Mat
    //1、高 2、宽
    Mat src(h + h / 2, w, CV_8UC1, data);
    //颜色格式的转换 nv21->RGBA
    //将 nv21的yuv数据转成了rgba
    cvtColor(src, src, COLOR_YUV2RGBA_NV21);
    // 正在写的过程 退出了，导致文件丢失数据
    //imwrite("/sdcard/src.jpg",src);
    if (cameraId == 1) {
        //前置摄像头，需要逆时针旋转90度
        rotate(src, src, ROTATE_90_COUNTERCLOCKWISE);
        //水平翻转 镜像
        flip(src, src, 1);
    } else {
        //顺时针旋转90度
        rotate(src, src, ROTATE_90_CLOCKWISE);
    }
    Mat gray;
    //灰色
    cvtColor(src, gray, COLOR_RGBA2GRAY);
    //增强对比度 (直方图均衡)
    equalizeHist(gray, gray);
    std::vector<Rect> faces;
    //定位人脸 N个
    tracker->process(gray);
    tracker->getObjects(faces);

    for (Rect face : faces) {
        handlerTracker(src,face);
    }
    //显示
    if (this->pWindow) {
        //设置windows的属性
        // 因为旋转了 所以宽、高需要交换
        //这里使用 cols 和rows 代表 宽、高 就不用关心上面是否旋转了
        ANativeWindow_setBuffersGeometry(this->pWindow, src.cols, src.rows,
                                         WINDOW_FORMAT_RGBA_8888);
        ANativeWindow_Buffer buffer;
        do {
            //lock失败 直接brek出去
            if (ANativeWindow_lock(pWindow, &buffer, 0)) {
                ANativeWindow_release(pWindow);
                pWindow = 0;
                break;
            }
            //src.data ： rgba的数据
            //把src.data 拷贝到 buffer.bits 里去
            // 一行一行的拷贝
//            memcpy(buffer.bits, src.data, buffer.stride*buffer.height*4);
            uint8_t *dst_data = static_cast<uint8_t *>(buffer.bits);
            int det_linesize = buffer.stride * 4;

            for (int i = 0; i < buffer.height; ++i) {
                memcpy(dst_data + i * det_linesize, src.data + i * src.cols * 4, det_linesize);
            }
            //提交刷新
            ANativeWindow_unlockAndPost(pWindow);
        } while (0);
    }
    //释放Mat
    //内部采用的 引用计数
    src.release();
    gray.release();
    env->ReleaseByteArrayElements(data_, data, 0);
}

