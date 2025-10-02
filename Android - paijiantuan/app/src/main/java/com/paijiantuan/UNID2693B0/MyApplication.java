package com.paijiantuan.UNID2693B0;

import android.app.Application;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.webkit.WebView;
import com.paijiantuan.UNID2693B0.util.AdManager;
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
            // 在初始化Taku SDK之前设置穿山甲广告的直接下载选项
            // 这对于穿山甲广告的正常填充非常重要
            Class<?> ttatInitManagerClass = Class.forName("com.anythink.china.common.TTATInitManager");
            Method getInstanceMethod = ttatInitManagerClass.getDeclaredMethod("getInstance");
            Object ttatInitManager = getInstanceMethod.invoke(null);
            Method setIsOpenDirectDownloadMethod = ttatInitManagerClass.getDeclaredMethod("setIsOpenDirectDownload", boolean.class);
            setIsOpenDirectDownloadMethod.invoke(ttatInitManager, false);
            Log.d(TAG, "穿山甲广告配置已设置");
        } catch (Exception e) {
            Log.e(TAG, "设置穿山甲广告配置失败: " + e.getMessage());
        }
        
        AdManager.getInstance().init(this);
        Log.d(TAG, "广告SDK初始化完成");
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