package com.ylm.facetracing;

import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ylm.dlibrary.camera.CameraHelper;
import com.ylm.dlibrary.detector.FaceDetector;
import com.ylm.dlibrary.detector.FaceDetectorManager;

public class RecognitionActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera
        .PreviewCallback {
    private static final String TAG = "RecognitionActivity";

    private CameraHelper cameraHelper;
    int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    FaceDetector mFaceDetector;
    String userId = "7777";

    boolean isRecognition = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regmain);
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);
        cameraHelper = new CameraHelper(cameraId);
        cameraHelper.setPreviewCallback(this);
        mFaceDetector = FaceDetectorManager.getInstance().getFaceDetector();
        mFaceDetector.setiRecognitionCallBack(new FaceDetector.IRecognitionCallBack() {
            @Override
            public void recognitionResult(int errorCode, String errorMsg) {
                Log.d(TAG, ">>>>>>> " + errorCode + ", " + errorMsg);
                isRecognition = true;

                showDialog_("识别成功!");
            }
        });
    }

    private void showDialog_(String msg) {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(this);
        normalDialog.setTitle("提示");
        normalDialog.setMessage(msg);
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         finish();
                    }
                });
        // 显示
        normalDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //初始化跟踪器
        mFaceDetector.reginInit();
        cameraHelper.startPreview();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //释放跟踪器
        mFaceDetector.nrelease();
        cameraHelper.stopPreview();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //设置surface 用于显示
        mFaceDetector.nsetSurface(holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //传输数据
        if (!isRecognition)
            mFaceDetector.npostData(data, CameraHelper.WIDTH, CameraHelper.HEIGHT, cameraId);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            cameraHelper.switchCamera();
            cameraId = cameraHelper.getCameraId();
        }
        return super.onTouchEvent(event);
    }
}
