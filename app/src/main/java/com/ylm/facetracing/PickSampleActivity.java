package com.ylm.facetracing;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ylm.facetracing.http.ApiService;
import com.ylm.facetracing.http.BaseResponse;
import com.ylm.facetracing.http.NetWorkManager;
import com.ylm.dlibrary.Utils;
import com.ylm.dlibrary.camera.CameraHelper;
import com.ylm.dlibrary.detector.FaceDetector;
import com.ylm.dlibrary.detector.FaceDetectorManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import cn.bingoogolapple.progressbar.BGAProgressBar;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class PickSampleActivity extends AppCompatActivity
        implements Consumer<Disposable>, SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = "PickSampleActivity";
    private CameraHelper cameraHelper;
    int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    FaceDetector mFaceDetector;
    String userId = "7777";
    ApiService mApiService;

    LinearLayout pickLayout;
    BGAProgressBar bgaProgressBar;

    LinearLayout netWorkLayout;
    ProgressBar progressBar;
    TextView netWorkTextView;

    boolean isPickFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);
        cameraHelper = new CameraHelper(cameraId);
        cameraHelper.setPreviewCallback(this);
        mFaceDetector = FaceDetectorManager.getInstance().getFaceDetector();
//        mFaceDetector = new FaceDetector(this,
//                Environment.getExternalStorageDirectory().getPath() + "/FACE_DETECTOR/" + userId);
        mFaceDetector.setiSamplePickCallBack(new FaceDetector.ISamplePickCallBack() {
            @Override
            public void samplePickCallBack(int num) {
                Log.d("MainActivity", ">>>>>>> " + num);
                int realNum = num + 1;
                if (realNum < 100) {
                    updatePickProgressBar(realNum);
                } else if (realNum == 100) {
                    isPickFinish = true;
                    updatePickProgressBar(realNum);
                    postFile();
                }
            }
        });
        mApiService = NetWorkManager.getInstance().getRequest();
    }

    private void initView() {
        pickLayout = findViewById(R.id.pick_layout);
        bgaProgressBar = findViewById(R.id.progressbar);

        netWorkLayout = findViewById(R.id.network_layout);
        progressBar = findViewById(R.id.work_progress);
        netWorkTextView = findViewById(R.id.network_text);
        netWorkLayout.setVisibility(View.GONE);
    }


    private void updatePickProgressBar(int num) {
        bgaProgressBar.setProgress(num);
        if (num == 100) {
            hidePickLayout();
        }
    }

    private void hidePickLayout() {
        pickLayout.setVisibility(View.GONE);
    }

    private void postFile() {
        String zipPath = mFaceDetector.zipFaceImg();
        if (zipPath == null) {
            Toast.makeText(PickSampleActivity.this, "样本压缩失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(zipPath);
        if (!file.exists()) {
            Toast.makeText(PickSampleActivity.this, "没有样本文件提交", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        MultipartBody.Part sample = MultipartBody.Part.createFormData("sample", file.getName(), body);
        mApiService.uploadFile(userId, sample)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        showNetWorkLayout();
                        setNetWorkText("开始提交样本数据....");
                    }

                    @Override
                    public void onNext(@NonNull BaseResponse<String> response) {
                        if (response.getErrno() == 0) {
                            hidePickLayout();
                            downloadXmlfile(userId);
                        } else {
                            setNetWorkText("网络出错 >>>>" + response.getErrmsg());
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable throwable) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void showNetWorkLayout() {
        netWorkLayout.setVisibility(View.VISIBLE);
    }

    private void setNetWorkText(String txt) {
        netWorkTextView.setText(txt);
    }

    @SuppressLint("CheckResult")
    private void downloadXmlfile(String userId) {
        setNetWorkText("下载特征文件....");

        mApiService.download(userId)
                .subscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, String>() {
                    @Override
                    public String apply(@NonNull ResponseBody response) throws Exception {
                        long total = response.contentLength();//需要下载的总大小
                        long current = 0;
                        //拿到字节流
                        InputStream is = response.byteStream();
                        int len = 0;
                        File file = new File(FaceDetector.ROOT_PATH, FaceDetector.RECOGNITION_CASCADE_XML);
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buf = new byte[128];
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                            current = current + len;
                            final String percent = Utils.getPercent(current, total);
                            Log.e(TAG, "已经下载=" + current + " 需要下载=" + total);

                            netWorkTextView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    setNetWorkText("特征文件已下载 " + percent);
                                }
                            }, 1000);
                        }
                        fos.flush();
                        //关闭流
                        fos.close();
                        is.close();
                        return "文件保存完毕";
                    }
                })
//                .doOnSubscribe(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        setNetWorkText(response);

                        finish();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //初始化跟踪器
        mFaceDetector.pickInit(100);
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
        if (!isPickFinish)
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

    @Override
    public void accept(@NonNull Disposable disposable) throws Exception {

    }
}
