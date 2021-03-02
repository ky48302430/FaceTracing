package me.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import me.service.FaceService;
import org.springframework.web.multipart.MultipartFile;
import utils.ResponseUtil;
import utils.Utils;

import javax.rmi.CORBA.Util;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;



@RestController
@RequestMapping("/faceDetect")
public class FaceDetectController {
    public static final String root = "E:/faceTrain/";  //win/linux

    //文件下载地址
    public static final String downLoadFilePath = root + "%s/download/sample.zip";



    @Autowired
    FaceService service;


    //http://localhost:9529/API/faceDetect/uploadZipAndTrain?userId=8888
    //zip文件上传 +解压+ 训练样本
    @PostMapping("/uploadZipAndTrain")
    public Object uploadZipAndTrain(@RequestParam String userId,
                                    @RequestParam("sample") MultipartFile sample) {

        Utils.deleteDirs(root + userId);
        String fPath = service.receiveFile(String.format(downLoadFilePath,userId), sample);

        String unzipDir = root + userId + "/img/";
        service.unZip(fPath, unzipDir);
        String errmsg = service.trainSample(userId, root, unzipDir);
        if (errmsg != null) {
            return ResponseUtil.error(errmsg);
        }
        return ResponseUtil.ok();
    }



    //http://localhost:9529/API/faceDetect/downLoadTrainMoel/8888
    //model下载
    @GetMapping("/downLoadTrainMoel/{userId}")
    public void downLoadTrainMoel(@PathVariable(name = "userId") String userId, HttpServletResponse resp) throws NoSuchFieldException {
        String xml = String.format("%s%s/data/cascade.xml", root, userId);
        System.out.println(xml);
        service.downLoadTrainMoel(xml, resp);
    }
}
