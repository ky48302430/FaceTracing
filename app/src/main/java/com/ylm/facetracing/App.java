package com.ylm.facetracing;
/*
 * Created by Yanlm on 2020/12/8.
 */

import android.app.Application;

import com.ylm.facetracing.http.NetWorkManager;
import com.ylm.dlibrary.detector.FaceDetectorManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NetWorkManager.getInstance().init();
        FaceDetectorManager.getInstance().init(getBaseContext(),"7777");
    }
}
