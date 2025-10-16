package com.fortunequizking;

import android.app.Application;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.webkit.WebView;
import com.fortunequizking.util.TakuAdManager;
import java.io.File;
import java.lang.reflect.Method;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    
    private static MyApplication instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        instance = this;
        
        // Android 9及以上必须设置，解决多进程WebView兼容问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getCurrentProcessName();
            if (!getPackageName().equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
        
        // 初始化广告SDK
        initAdSDK();
    }
    
    private void initAdSDK() {
        try {
            // 初始化Taku广告SDK，而不是AdManager
            TakuAdManager.getInstance().init(this);
            Log.d(TAG, "Taku广告SDK初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "Taku广告SDK初始化失败: " + e.getMessage(), e);
        }
    }
    
    private String getCurrentProcessName() {
        try {
            File file = new File("/proc/" + Process.myPid() + "/cmdline");
            java.io.BufferedReader mBufferedReader = new java.io.BufferedReader(
                    new java.io.FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static MyApplication getInstance() {
        return instance;
    }
}