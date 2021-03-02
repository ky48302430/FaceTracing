#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/native_window_jni.h>
#include "../cpp/lm-androidLog.h"
#include "../cpp/JniCall.h"

using namespace cv;

ANativeWindow *window = 0;
int sample_num = 0;
JavaVM *_vm;
JniCall *jniCall;

class CascadeDetectorAdapter : public DetectionBasedTracker::IDetector {
public:
    CascadeDetectorAdapter(cv::Ptr<cv::CascadeClassifier> detector) :
            IDetector(),
            Detector(detector) {

        CV_Assert(detector);
    }

    void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects) {
        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize,
                                   maxObjSize);
    }

    virtual ~CascadeDetectorAdapter() {

    }

private:
    CascadeDetectorAdapter();

    cv::Ptr<cv::CascadeClassifier> Detector;
};

DetectionBasedTracker *tracker = 0;
extern "C"
JNIEXPORT void JNICALL
setSurface(JNIEnv *env, jobject instance, jobject surface) {
    if (window) {
        ANativeWindow_release(window);
        window = 0;
    }
    window = ANativeWindow_fromSurface(env, surface);
}



extern "C"
JNIEXPORT void JNICALL
postData(JNIEnv *env, jobject instance,
         jbyteArray data_, jint w, jint h, jint cameraId) {
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
    Mat sample; //采集脸部样本

    for (Rect face : faces) {
        //画矩形
        //分别指定 bgra
        rectangle(src, face, Scalar(255, 0, 255));

        if (sample_num < 100) {
            //存储到本地地址
            src(face).copyTo(sample);
            resize(sample, sample, Size(24, 24));
            cvtColor(sample, sample, COLOR_BGR2GRAY);
            char path[100];
            sprintf(path, "/sdcard/face/%d.jpg", sample_num++);
            imwrite(path, sample);
        }
    }
    //显示
    if (window) {
        //设置windows的属性
        // 因为旋转了 所以宽、高需要交换
        //这里使用 cols 和rows 代表 宽、高 就不用关心上面是否旋转了
        ANativeWindow_setBuffersGeometry(window, src.cols, src.rows, WINDOW_FORMAT_RGBA_8888);
        ANativeWindow_Buffer buffer;
        do {
            //lock失败 直接brek出去
            if (ANativeWindow_lock(window, &buffer, 0)) {
                ANativeWindow_release(window);
                window = 0;
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
            ANativeWindow_unlockAndPost(window);
        } while (0);
    }
    //释放Mat
    //内部采用的 引用计数
    src.release();
    gray.release();
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
init(JNIEnv *env, jobject instance, jstring model_) {
    const char *model = env->GetStringUTFChars(model_, 0);
    if (tracker) {
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

    //初始化回调接口类
    jniCall = new JniCall(_vm, env, instance);


    env->ReleaseStringUTFChars(model_, model);
}

extern "C"
JNIEXPORT void JNICALL
release(JNIEnv *env, jobject instance) {
    if (tracker) {
        tracker->stop();
        delete tracker;
        tracker = nullptr;
    }

    if (jniCall){
        delete jniCall;
        jniCall = nullptr;
    }
}


//类名  // com.ylm.dlibrary.detector
static const char *mClassName = "com/ylm/dlibrary/detector/FaceDetector";
//静态的Jni native 方法数组
static const JNINativeMethod method[] = {
        //方法名    签名    本地方法
        {"ninit",       "(Ljava/lang/String;)V",     (void *) init},
        {"nsetSurface", "(Landroid/view/Surface;)V", (void *) setSurface},
        {"npostData",   "([BIII)V",                  (void *) postData},
        {"nrelease",    "()V",                       (void *) release}
};

//返回Jni 版本
int JNI_OnLoad(JavaVM *vm, void *r) {
    LOGE("JNI_OnLoad");
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








