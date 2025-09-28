package com.fortunequizking.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * 设备信息工具类，用于获取设备唯一标识符和其他硬件信息
 */
public class DeviceUtils {
    private static final String TAG = "DeviceUtils";

    /**
     * 获取设备唯一标识符
     * @param context 上下文对象
     * @return 设备唯一标识符
     */
    public static String getDeviceId(Context context) {
        // 优先从设置中获取
        String deviceId = getPersistentDeviceId(context);
        if (!TextUtils.isEmpty(deviceId)) {
            return deviceId;
        }

        // 如果没有，生成一个基于硬件信息的标识符
        deviceId = generateHardwareBasedIdentifier(context);
        if (!TextUtils.isEmpty(deviceId)) {
            return deviceId;
        }

        // 作为最后的备用方案，使用随机生成的UUID
        deviceId = UUID.randomUUID().toString();
        // 保存这个生成的ID以便后续使用
        savePersistentDeviceId(context, deviceId);
        return deviceId;
    }

    /**
     * 生成基于硬件信息的唯一标识符
     * @param context 上下文对象
     * @return 硬件标识符
     */
    private static String generateHardwareBasedIdentifier(Context context) {
        StringBuilder hardwareInfo = new StringBuilder();
        
        // 收集各种硬件信息
        try {
            // 添加Android ID
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (!TextUtils.isEmpty(androidId)) {
                hardwareInfo.append(androidId);
            }

            // 添加设备型号和制造商
            hardwareInfo.append(Build.MODEL).append(Build.MANUFACTURER);

            // 添加设备序列号（如果有）
            if (!TextUtils.isEmpty(Build.SERIAL)) {
                hardwareInfo.append(Build.SERIAL);
            }

            // 尝试获取IMEI（需要权限）
            if (context.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            String imei = telephonyManager.getImei();
                            if (!TextUtils.isEmpty(imei)) {
                                hardwareInfo.append(imei);
                            }
                        } else {
                            // 旧版本Android
                            String imei = telephonyManager.getDeviceId();
                            if (!TextUtils.isEmpty(imei)) {
                                hardwareInfo.append(imei);
                            }
                        }
                    }
                } catch (SecurityException e) {
                    Log.w(TAG, "无法获取IMEI，权限被拒绝: " + e.getMessage());
                }
            }

            // 添加主板信息
            hardwareInfo.append(Build.BOARD);

            // 添加处理器信息（通过反射获取）
            try {
                Class<?> systemProperties = Class.forName("android.os.SystemProperties");
                Method getMethod = systemProperties.getMethod("get", String.class, String.class);
                String cpuAbi = (String) getMethod.invoke(null, "ro.product.cpu.abi", "");
                if (!TextUtils.isEmpty(cpuAbi)) {
                    hardwareInfo.append(cpuAbi);
                }
            } catch (Exception e) {
                Log.w(TAG, "无法获取CPU信息: " + e.getMessage());
            }

            // 对收集的信息进行哈希处理，生成唯一标识
            return hashString(hardwareInfo.toString());
        } catch (Exception e) {
            Log.e(TAG, "生成硬件标识符失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从持久化存储中获取设备ID
     * @param context 上下文对象
     * @return 设备ID
     */
    private static String getPersistentDeviceId(Context context) {
        return SharedPreferenceUtil.getString(context, "persistent_device_id", "");
    }

    /**
     * 保存设备ID到持久化存储
     * @param context 上下文对象
     * @param deviceId 设备ID
     */
    private static void savePersistentDeviceId(Context context, String deviceId) {
        SharedPreferenceUtil.putString(context, "persistent_device_id", deviceId);
    }

    /**
     * 对字符串进行哈希处理
     * @param input 输入字符串
     * @return 哈希后的字符串
     */
    private static String hashString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "哈希算法不可用: " + e.getMessage());
            return null;
        }
    }
}