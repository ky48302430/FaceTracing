package me.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import utils.Utils;
import utils.ZipUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Service
public class FaceService {
    public void unZip(String zipFilePath, String fileDirPath) {
        ZipUtil.unzip(zipFilePath, fileDirPath);
    }

    public String trainSample(String userId, String root, String unzipDir) {
        RuntimeCmd cmd = new RuntimeCmd();
        cmd.doParpare(userId, root, unzipDir);
        cmd.exe();
        return cmd.getErrmsg();
    }

    public void downLoadTrainMoel(String xml, HttpServletResponse resp) throws NoSuchFieldException {
        File file = new File(xml);
        if (!file.exists()) {
            throw new NoSuchFieldException("没有此文件!");
        }

        resp.setHeader("content-type", "application/octet-stream");
        resp.setHeader("Content-Length", file.length()+"");
        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment;filename=cascade.xml");
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {
            os = resp.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(file));
            int length = bis.read(buff);
            while (length != -1) {
                os.write(buff, 0, length);
                os.flush();
                length = bis.read(buff);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String receiveFile(String downLoadFilePath, MultipartFile sample) {
        Utils.isSureExitFile(downLoadFilePath);

        try {
            if (!sample.isEmpty()) {
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(downLoadFilePath));
                out.write(sample.getBytes());
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return downLoadFilePath;
    }
}
