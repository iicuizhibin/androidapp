package com.hengcai88.hengcai;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class DownLoad extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     */
    private Button down;
    private HttpDown http;
    private String msg;
    //下载进度条
    private ProgressDialog mSaveDialog = null;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load);
        down = (Button) findViewById(R.id.down);
        down.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                mSaveDialog = ProgressDialog.show(DownLoad.this, "下载文件", "文件正在保存中，请稍等...", true);
                //开启一个新的线程执行下载任务
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                http = new HttpDown();
                                //获得SD卡路径
                                String sdpath = Environment.getExternalStorageDirectory() + "/hc/";
                                String httpStr = "http://10.63.15.242/feed/downH5/hc.zip";
                                try {
                                    //检测是否有写的权限
                                    int permission = ActivityCompat.checkSelfPermission(DownLoad.this,
                                            "android.permission.WRITE_EXTERNAL_STORAGE");
                                    if (permission != PackageManager.PERMISSION_GRANTED) {
                                        Toast.makeText(getApplicationContext(),
                                                "下载失败，请开启手机权限！", Toast.LENGTH_SHORT).show();
                                        // 没有写的权限，去申请写的权限，会弹出对话框
                                        ActivityCompat.requestPermissions(DownLoad.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                msg = http.download(httpStr, sdpath, "hc.zip");
                                if (msg.equals("success")) {
                                    try {
                                        ZipUtils.UnZipFolder(sdpath + "hc.zip", sdpath);
                                        Intent mainIntent = new Intent(DownLoad.this,
                                                MainActivity.class);
                                        DownLoad.this.startActivity(mainIntent);
                                        DownLoad.this.finish();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                messageHandler.sendMessage(messageHandler.obtainMessage());
                            }
                        }
                ).start();
            }
        });
    }

    private Handler messageHandler = new Handler() {
        public void handleMessage(Message me) {
            mSaveDialog.dismiss();
            Toast.makeText(DownLoad.this, msg, Toast.LENGTH_SHORT).show();
        }
    };
}
