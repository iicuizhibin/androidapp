package com.hengcai88.hengcai;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpDown {

    /**
     * @param strurl   文件url地址
     * @param path     需要保存文件的路径
     * @param fileName 保存到本地的文件名
     * @return
     */
    public String download(String strurl, String path, String fileName) {

        InputStream is = null;
        OutputStream os = null;
        URL url = null;
        try {
            //创建文件夹
            File f = new File(path);
            if (!f.exists()) {
                f.mkdir();
            }
            //创建文件
            File file = new File(path + fileName);
            System.out.println(file);
            //判断是否存在文件
            if (file.exists()) {
                //创建新文件
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            //创建并打开连接
            url = new URL(strurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //文件输入流
            is = conn.getInputStream();
            //输出流
            os = new FileOutputStream(file);
            byte buffer[] = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
            return "success";
        } catch (IOException e) {
            e.printStackTrace();
            return "failure";
        } finally {
            try {
                os.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void deleteAllFiles(File root) {
        System.out.println(root);
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
    }
}