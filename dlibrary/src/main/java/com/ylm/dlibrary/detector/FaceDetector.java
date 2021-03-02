package com.ylm.dlibrary.detector;

import android.content.Context;
import android.view.Surface;

import com.ylm.dlibrary.Utils;
import com.ylm.dlibrary.zip.ZipUtil;

import java.io.File;

public class FaceDetector {
    public static String ROOT_PATH = null;
    public static String ROOT_FACEIMG_PATH = null;
    public static String ROOT_SAMPLE_FILE_PATH = null;

    public static final String RECOGNITION_CASCADE_XML = "cascade.xml";
    public static final String LBPCASCADE_FRONTALFACE_XML = "lbpcascade_frontalface.xml";

    static {
        System.loadLibrary("native-lib");
    }

    private ISamplePickCallBack iSamplePickCallBack;
    private IRecognitionCallBack iRecognitionCallBack;

    /**
     * @param context
     * @param sourcePath /sdcard/face
     */
    public FaceDetector(Context context, String sourcePath) {
        ROOT_PATH = sourcePath;
        Utils.isSureExitDir(ROOT_PATH);

        ROOT_FACEIMG_PATH = sourcePath + File.separator + "faceImg";
        Utils.isSureExitDir(ROOT_FACEIMG_PATH);

        ROOT_SAMPLE_FILE_PATH = ROOT_PATH + "/sample.zip";

        Utils.copyAssets(context, FaceDetector.LBPCASCADE_FRONTALFACE_XML, ROOT_PATH);
    }


    /**
     * @param needPickSampleNumber
     */
    public void  pickInit(int needPickSampleNumber) {
        npickInit(
                ROOT_PATH + File.separator + FaceDetector.LBPCASCADE_FRONTALFACE_XML,
                ROOT_FACEIMG_PATH + "/%d.jpg",
                needPickSampleNumber);
    }

    public void reginInit() {
        nreginInit(
                ROOT_PATH + File.separator + FaceDetector.RECOGNITION_CASCADE_XML
        );
    }

    public String zipFaceImg() {
        boolean isOk = ZipUtil.zip(ROOT_FACEIMG_PATH, new File(ROOT_FACEIMG_PATH).list(), ROOT_SAMPLE_FILE_PATH);
        return isOk ? ROOT_SAMPLE_FILE_PATH : null;
    }


    // called from jni
    public void onSampleCallBack(int num) {
        if (iSamplePickCallBack != null)
            iSamplePickCallBack.samplePickCallBack(num);
    }

    public void onRecognitionResult(int errorCode, String errorMsg) {
        if (iRecognitionCallBack != null)
            iRecognitionCallBack.recognitionResult(errorCode, errorMsg);
    }


    public native void nreginInit(String model);

    /**
     * 采集样本初始化
     *
     * @param model
     */
    public native void npickInit(String model, String path, int sampleNumTotal);

    /**
     * 识别初始化
     * @param model ----  传特定的某个model
     */
//    public native void nrecongnizeInit(String model);


    /**
     * 设置画布
     * ANativeWindow
     *
     * @param surface
     */
    public native void nsetSurface(Surface surface);

    /**
     * 处理摄像头数据
     *
     * @param data
     * @param w
     * @param h
     * @param cameraId
     */
    public native void npostData(byte[] data, int w, int h, int cameraId);

    /**
     * 释放
     */
    public native void nrelease();

    public interface ISamplePickCallBack {
        void samplePickCallBack(int num);
    }

    public interface IRecognitionCallBack {
        void recognitionResult(int errorCode, String errorMsg);
    }

    public ISamplePickCallBack getiSamplePickCallBack() {
        return iSamplePickCallBack;
    }

    public void setiSamplePickCallBack(ISamplePickCallBack iSamplePickCallBack) {
        this.iSamplePickCallBack = iSamplePickCallBack;
    }

    public IRecognitionCallBack getiRecognitionCallBack() {
        return iRecognitionCallBack;
    }

    public void setiRecognitionCallBack(IRecognitionCallBack iRecognitionCallBack) {
        this.iRecognitionCallBack = iRecognitionCallBack;
    }
}
