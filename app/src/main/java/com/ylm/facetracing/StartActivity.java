package com.ylm.facetracing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ylm.facetracing.http.ApiService;
import com.ylm.facetracing.http.BaseResponse;
import com.ylm.facetracing.http.NetWorkManager;
import com.ylm.dlibrary.detector.FaceDetector;
import com.ylm.dlibrary.zip.ZipUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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

public class StartActivity extends AppCompatActivity {
    private static final String TAG = "StartActivity";
    String userId = "7777";
    ApiService mApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mApiService = NetWorkManager.getInstance().getRequest();
    }

    //采集
    public void onPick(View view) {
        Intent intent = new Intent(this, PickSampleActivity.class);
        startActivity(intent);
    }

    //识别
    public void onRecognition(View view) {
        Intent intent = new Intent(this, RecognitionActivity.class);
        startActivity(intent);
    }

    //上传文件
    public void onUploadFile(View view) {
        postFile();
    }

    public void  onDownloadFile(View view){
        downloadXmlfile(userId);
    }

    @SuppressLint("CheckResult")
    private void downloadXmlfile(String userId) {
        final  String root = Environment.getExternalStorageDirectory().getPath() + "/FACE_DETECTOR/" + userId;
        mApiService.download(userId)
                .subscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, String>() {
                    @Override
                    public String apply(@NonNull ResponseBody response) throws Exception {
                        //需要下载的总大小 需要后台添加  resp.setHeader("Content-Length", file.length()+"");
                        long total = response.contentLength();
                        long current = 0;
                        //拿到字节流
                        InputStream is = response.byteStream();
                        int len = 0;
                        File file  = new File(root, FaceDetector.RECOGNITION_CASCADE_XML);
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buf = new byte[128];
                        while ((len = is.read(buf)) != -1){
                            fos.write(buf, 0, len);
                            current = current + len;
                            Log.e(TAG, "已经下载=" + current + " 需要下载=" + total);
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
                        Toast.makeText(StartActivity.this, response, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void postFile() {
        String root = Environment.getExternalStorageDirectory().getPath() + "/FACE_DETECTOR/" + userId;
        ZipUtil.zip(root+"/faceImg", new File(root+"/faceImg").list(), root + "/sample.zip");

        File file = new File(root + "/sample.zip");
        if (!file.exists()) {
            Toast.makeText(this, "没有样本文件提交", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        MultipartBody.Part sample = MultipartBody.Part.createFormData("sample", file.getName(), body);
        mApiService.uploadFile(userId, sample)
                .subscribeOn(Schedulers.io())
//                .doOnSubscribe(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        Toast.makeText(StartActivity.this, "开始提交样本数据", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(@NonNull BaseResponse<String> response) {
                        if (response.getErrno() == 0) {
//                            downloadXmlfile(userId);
                            Toast.makeText(StartActivity.this, "开始下载xml", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(StartActivity.this, response.getErrmsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable throwable) {
                        Toast.makeText(StartActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }
}