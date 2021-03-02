package com.ylm.facetracing.http;
/*
 * Created by Yanlm on 2020/12/8.
 */



import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public  interface ApiService {
    public static String HOST = "http://192.168.1.111:9529/API/";

    //http://localhost:9529/API/faceDetect/uploadZipAndTrain?userId=8888
    //文件上传
    @POST("faceDetect/uploadZipAndTrain")
    @Multipart
    Observable<BaseResponse<String>> uploadFile(@Query("userId")  String userId, @Part MultipartBody.Part part);

    //http://localhost:9529/API/faceDetect/downLoadTrainMoel/8888
    //文件下载
    @GET("faceDetect/downLoadTrainMoel/{userId}")
    @Streaming
    //如果下载较大的文件必须添加该注解
    Flowable<ResponseBody> download(@Path("userId") String userId);
}
