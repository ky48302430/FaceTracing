package me.service;

import utils.Utils;

import java.io.*;

/**
 * <p>
 * cmd /c dir 是执行完dir命令后关闭命令窗口。
 * cmd /k dir 是执行完dir命令后不关闭命令窗口。
 * cmd /c start dir 会打开一个新窗口后执行dir指令，原窗口会关闭。
 * cmd /k start dir 会打开一个新窗口后执行dir指令，原窗口不会关闭。
 */
public class RuntimeCmd {
    //opencv_createsamples -info userid.data -vec userid.vec  -num 100  -w 24 -h 24
    public static final String CMD_CREATESAMPLES = "opencv_createsamples -info %s -vec %s  -num 100  -w 24 -h 24";
    //opencv_traincascade -data data  -vec userid.vec -bg bg.txt -numPos 100 -numNeg 300  -numStages 15  -featureType LBP -w 24 -h 24 -mode ALL
    public static final String CMD_TRAINCASCADE = "opencv_traincascade -data %s  -vec %s -bg bg.txt -numPos 100 -numNeg 300  -numStages 15  -featureType LBP -w 24 -h 24 -mode ALL";


    private String dataDir = "/data";      //训练之后的xml存放处
    private String imgData = "/img.data"; //用户照片数据路径及参数 userid/img/0.jpg 1 0 0 24 24
    private String dataVec = "/data.vec"; //向量


    private String cmd;
    private String errmsg;



    public void exe() {
        System.out.println(cmd);
        Runtime run = Runtime.getRuntime();
        try {
            Process p = run.exec(cmd);
            InputStream ins = p.getInputStream();
            InputStream ers = p.getErrorStream();
            new Thread(new inputStreamThread(ins)).start();
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            errmsg = e.getMessage();
        }
    }

    public void doParpare(String userId, String root, String imgDir) {
        boolean isOk = false;
        String datatxt = userId + imgData; //用户照片数据路径及参数 userid/img/0.jpg 1 0 0 24 24
        isOk = doCreateImgData( userId,  root,  imgDir,datatxt);
        if (!isOk) {
            errmsg = "创建用户照片参数失败";
        }
        String userVec = userId + dataVec;  //向量

        //样本配置
        String dataPath = userId + dataDir;
        Utils.isSureExitDir(root + "/" + dataPath);

        StringBuffer command = new StringBuffer("cmd /c  E: && ");
        command.append(" cd %s && "); //E:\faceTrain
        command.append(String.format(CMD_CREATESAMPLES, datatxt, userVec)).append(" && ");
        command.append(String.format(CMD_TRAINCASCADE, dataPath, userVec));
        cmd = String.format(command.toString(), root);
    }

/*
    *不同系统的换行符不一样：
11  *        windows：\r\n
12  *        linux:\n
13  *        Mac：\r
*/
    private boolean  doCreateImgData(String userId, String root, String imgDirPath,String datatxt) {
            Utils.isSureExitFile(root + "/" + datatxt);
            File imgDir = new File(imgDirPath);
            File dataFile = new File(root + "/" + datatxt);

        try {
            FileOutputStream  outputStream = new FileOutputStream(dataFile);
            String[] list = imgDir.list();
            for (String imgPath:list) {
                File img = new File(imgPath);
                String info =String.format("img/%s 1 0 0 24 24",img.getName()) ;  //userid/img/0.jpg 1 0 0 24 24
                outputStream.write(info.getBytes());
                outputStream.write("\n".getBytes()); //
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    class inputStreamThread implements Runnable {
        private InputStream ins = null;
        private BufferedReader bfr = null;

        public inputStreamThread(InputStream ins) {
            this.ins = ins;
            this.bfr = new BufferedReader(new InputStreamReader(ins));
        }

        @Override
        public void run() {
            String line = null;
            byte[] b = new byte[100];
            int num = 0;
            try {
                while ((num = ins.read(b)) != -1) {
                    System.out.println(new String(b, "gb2312"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


//    public static void main(String[] args) {
//        RuntimeCmd s = new RuntimeCmd();
//        s.exe("E:\\faceTrain", "lance");
//    }


    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}

