package com.ylm.dlibrary;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;


public final class Utils {
    public static void copyAssets(Context context, String path,String toPath) {
        File model = new File(path);
        File file = new File(toPath, model.getName());
        if (file.exists()) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            InputStream is = context.getAssets().open(path);
            int len;
            byte[] b = new byte[2048];
            while ((len = is.read(b)) != -1) {
                fos.write(b, 0, len);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean isSureExitDir(String dirPath) {
        File file = new File(dirPath);
        try {
            if (file.exists()) {
                String[] list = file.list();
                for (String xml : list) {
                    File fxml = new File(xml);
                    fxml.delete();
                }
            } else {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static boolean isSureExitFile(String filePath) {
        File file = new File(filePath);
        try {
            if (file.exists()) {
                file.delete();
            } else {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void deleteDirs(String dir) {
        File file = new File(dir);
        System.out.println(file.exists());
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            String[] list = file.list();
            for (String path : list) {
                deleteDirs(dir + "/" + path);
            }
        } else {
            file.delete();
        }
    }

    public static String getPercent(long completed, long total) {
        if (total == -1) return  "";
        NumberFormat numberFormat= NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(1);
        return numberFormat.format(((double)completed)/((double)total));
    }
}
