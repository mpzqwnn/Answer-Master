package com.paijiantuan.UNID2693B0.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import android.os.Handler;

/**
 * 网络连接检查工具类
 */
public class NetworkUtils {
    
    private static Handler networkCheckHandler;
    private static Runnable networkCheckRunnable;
    private static boolean isMonitoring = false;
    private static final long CHECK_INTERVAL = 2000; // 2秒检测一次
    
    /**
     * 开始持续网络检测
     * @param context 上下文
     */
    public static void startContinuousNetworkMonitoring(Context context) {
        if (isMonitoring) {
            return; // 已经在监控中
        }
        
        isMonitoring = true;
        networkCheckHandler = new Handler();
        networkCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isNetworkConnected(context)) {
                    // 网络断开，退出应用
                    Toast.makeText(context, "网络连接已断开，应用将退出", Toast.LENGTH_LONG).show();
                    
                    // 延迟1秒后退出应用，让用户看到提示信息
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(0);
                        }
                    }, 1000);
                    return;
                }
                
                // 继续下一次检测
                if (isMonitoring && networkCheckHandler != null) {
                    networkCheckHandler.postDelayed(this, CHECK_INTERVAL);
                }
            }
        };
        
        // 开始第一次检测
        networkCheckHandler.post(networkCheckRunnable);
    }
    
    /**
     * 停止持续网络检测
     */
    public static void stopContinuousNetworkMonitoring() {
        isMonitoring = false;
        if (networkCheckHandler != null && networkCheckRunnable != null) {
            networkCheckHandler.removeCallbacks(networkCheckRunnable);
        }
        networkCheckHandler = null;
        networkCheckRunnable = null;
    }

    /**
     * 检查网络是否连接
     * @param context 上下文
     * @return true表示网络已连接，false表示网络未连接
     */
    public static boolean isNetworkConnected(Context context) {
        if (context == null) {
            return false;
        }
        
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * 检查网络连接，如果未连接则显示提示并退出应用
     * @param context 上下文
     * @return true表示网络已连接，false表示网络未连接并已退出应用
     */
    public static boolean checkNetworkAndExitIfDisconnected(Context context) {
        if (!isNetworkConnected(context)) {
            // 显示网络未连接提示
            Toast.makeText(context, "网络未连接，请检查网络设置后重试", Toast.LENGTH_LONG).show();
            
            // 延迟1秒后退出应用，让用户看到提示信息
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 退出应用
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            }, 1000);
            
            return false;
        }
        return true;
    }

    /**
     * 检查网络连接，如果未连接则显示提示并返回false
     * @param context 上下文
     * @return true表示网络已连接，false表示网络未连接
     */
    public static boolean checkNetworkAndShowToast(Context context) {
        if (!isNetworkConnected(context)) {
            Toast.makeText(context, "网络未连接，请检查网络设置", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}