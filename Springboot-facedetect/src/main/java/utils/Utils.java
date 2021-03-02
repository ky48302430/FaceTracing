package utils;

import java.io.File;

/**
 * Created by yanlm on 2020/12/4.
 */
public class Utils {
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
}
