package com.ylm.dlibrary.detector;
/*
 * Created by Yanlm on 2020/12/9.
 */

import android.content.Context;
import android.os.Environment;

public class FaceDetectorManager {
    private static FaceDetectorManager mInstance;
    private FaceDetector mFaceDetector;

    public static FaceDetectorManager getInstance() {
        if (mInstance == null) {
            synchronized (FaceDetectorManager.class) {
                if (mInstance == null) {
                    mInstance = new FaceDetectorManager();
                }
            }
        }
        return mInstance;
    }
    public void init(Context context,String userId){
        mFaceDetector = new FaceDetector(context,
                Environment.getExternalStorageDirectory().getPath() + "/FACE_DETECTOR/" + userId);
    }

    public FaceDetector getFaceDetector() {
        return mFaceDetector;
    }
}
