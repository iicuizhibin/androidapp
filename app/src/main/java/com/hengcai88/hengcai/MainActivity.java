package com.hengcai88.hengcai;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static Handler handler;
    private WebView webView;
    private long exitTime = 0;
    private static String SHARE_APP_TAG;
    private String h5Url;
    String sdCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 是否第一次进入app
        SharedPreferences setting = getSharedPreferences(SHARE_APP_TAG, 0);
        Boolean user_first = setting.getBoolean("FIRST", true);
        if (user_first) {//第一次
            setting.edit().putBoolean("FIRST", false).commit();
            h5Url = "file:///android_asset/dist/index.html";
            FileUtils.getInstance(this).copyAssetsToSD("dist", "hc");
        } else {
            // 判断当前是否有h5资源文件
            if (!isFileExist("index.html", Environment.getExternalStorageDirectory() + "/hc/")) {
                h5Url = "file:///android_asset/dist/index.html";
            } else {
                h5Url = "file:///mnt/sdcard/hc/index.html";
            }
        }

        webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {
            //设置在webView点击打开的新网页在当前界面显示,而不跳转到新的浏览器中
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);  //设置WebView属性,运行执行js脚本
        webView.getSettings().setDomStorageEnabled(true);//设置WebView属性,开启h5 storage 本地存储
        // Set cache size to 8 mb by default. should be more than enough
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);

        // This next one is crazy. It's the DEFAULT location for your app's cache
        // But it didn't work for me without this line.
        // UPDATE: no hardcoded path. Thanks to Kevin Hawkins
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        //java与js回调，自定义方法
        //1.java调用js
        //2.js调用java
        //首先java暴露接口，供js调用
        /**
         * obj:暴露的要调用的对象
         * interfaceName：对象的映射名称 ,object的对象名，在js中可以直接调用
         * 在html的js中：JSTest.showToast(msg)
         * 可以直接访问JSTest，这是因为JSTest挂载到js的window对象下了
         */
        webView.addJavascriptInterface(new Object() {
            //定义要调用的方法
            //msg由js调用的时候传递
            @JavascriptInterface
            public void showToast(String msg) {
                System.out.println(msg);
                Toast.makeText(getApplicationContext(),
                        msg, Toast.LENGTH_SHORT).show();
            }

        }, "JSBridge");
        webView.addJavascriptInterface(new Object() {
            //定义要调用的方法
            //msg由js调用的时候传递
            @JavascriptInterface
            public void goDownLoad() {
                HttpDown http = new HttpDown();
                http.deleteAllFiles(new File(Environment.getExternalStorageDirectory() + "/hc/"));
                Intent mainIntent = new Intent(MainActivity.this,
                        DownLoad.class);
                MainActivity.this.startActivity(mainIntent);
                MainActivity.this.finish();
            }
        }, "JSBridge");
        webView.loadUrl(h5Url);          //调用loadUrl方法为WebView加入链接
//        webView.loadUrl("http://10.63.34.33:8091/");          //开发
        setContentView(webView);                           //调用Activity提供的setContentView将webView显示出来
    }

    //我们需要重写回退按钮的时间,当用户点击回退按钮：
    //1.webView.canGoBack()判断网页是否能后退,可以则goback()
    //2.如果不可以连续点击两次退出App,否则弹出提示Toast
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }
        }
    }

    /**
     * 判断SD卡上文件是否存在
     */
    public boolean isFileExist(String fileName, String path) {
        File file = new File(path + fileName);
        return file.exists();
    }

    /**
     * 复制asset文件到指定目录
     *
     * @param oldPath asset下的路径
     * @param newPath SD卡下保存路径
     */
    public static void CopyAssets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(newPath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    CopyAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
