package com.paijiantuan.UNID2693B0.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.paijiantuan.UNID2693B0.MyApplication;
import com.paijiantuan.UNID2693B0.api.ApiManager;
import com.paijiantuan.UNID2693B0.api.ApiCallback;
import com.paijiantuan.UNID2693B0.model.ApiResponse;
import com.qq.e.comm.constants.BiddingLossReason;
import com.qq.e.comm.pi.IBidding;
import com.qq.e.comm.pi.IBiddingLoss;
import com.qq.e.comm.util.AdError;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BiddingC2SUtils {

  private static final String TAG = "BiddingC2SUtils";
  private static int reportBiddingWinLoss = -1;

  public static final int REPORT_BIDDING_DISABLE = -1;
  public static final int REPORT_BIDDING_WIN = 0;
  public static final int REPORT_BIDDING_LOSS_LOW_PRICE = BiddingLossReason.LOW_PRICE; // 有广告回包，竞败
  public static final int REPORT_BIDDING_LOSS_NO_AD = BiddingLossReason.NO_AD; // 无广告回包
  public static final int REPORT_BIDDING_LOSS_NOT_COMPETITION = BiddingLossReason.NOT_COMPETITION; // 有广告回包但未参竞价
  public static final int REPORT_BIDDING_LOSS_OTHER = BiddingLossReason.OTHER; // 其他
  
  // 广告类型常量
  public static final int AD_TYPE_BANNER = 1; // 横幅广告
  public static final int AD_TYPE_INTERSTITIAL = 2; // 插屏广告
  public static final int AD_TYPE_REWARD_VIDEO = 3; // 激励视频广告
  public static final int AD_TYPE_NATIVE = 4; // 原生广告
  public static final int AD_TYPE_SPLASH = 5; // 开屏广告

  public static void setReportBiddingWinLoss(int reportBiddingWinLoss) {
    BiddingC2SUtils.reportBiddingWinLoss = reportBiddingWinLoss;
  }

  /**
   * 上报C2S竞价结果到优量汇SDK
   */
  public static void reportBiddingWinLoss(Object ad) {
    if (ad == null) {
      Log.w(TAG, "reportBiddingWinLoss: Ad object is null");
      return;
    }
    
    // Check if the ad object implements IBidding interface
    if (!(ad instanceof IBidding)) {
      Log.w(TAG, "Ad object does not implement IBidding interface: " + ad.getClass().getName());
      return;
    }
    
    IBidding biddingAd = (IBidding) ad;
    
    try {
      switch (reportBiddingWinLoss) {
        case REPORT_BIDDING_WIN:
          HashMap<String, Object> hashMap = new HashMap<>();
          hashMap.put(IBidding.EXPECT_COST_PRICE, 200);
          hashMap.put(IBidding.HIGHEST_LOSS_PRICE, 199);
          Log.d(TAG, "Reporting bidding win for ad: " + ad.getClass().getSimpleName());
          biddingAd.sendWinNotification(hashMap);
          // 同时上报到FastAdmin服务器
          reportToFastAdmin(ad, REPORT_BIDDING_WIN, 200, 199);
          break;
        case REPORT_BIDDING_LOSS_LOW_PRICE:
          Log.d(TAG, "Reporting bidding loss (low price) for ad: " + ad.getClass().getSimpleName());
          reportLoss(biddingAd, REPORT_BIDDING_LOSS_LOW_PRICE);
          // 同时上报到FastAdmin服务器
          reportToFastAdmin(ad, REPORT_BIDDING_LOSS_LOW_PRICE, 300, 200);
          break;
        case REPORT_BIDDING_LOSS_NO_AD:
          Log.d(TAG, "Reporting bidding loss (no ad) for ad: " + ad.getClass().getSimpleName());
          reportLoss(biddingAd, REPORT_BIDDING_LOSS_NO_AD);
          // 同时上报到FastAdmin服务器
          reportToFastAdmin(ad, REPORT_BIDDING_LOSS_NO_AD, 0, 0);
          break;
        case REPORT_BIDDING_LOSS_NOT_COMPETITION:
          Log.d(TAG, "Reporting bidding loss (not competition) for ad: " + ad.getClass().getSimpleName());
          reportLoss(biddingAd, REPORT_BIDDING_LOSS_NOT_COMPETITION);
          // 同时上报到FastAdmin服务器
          reportToFastAdmin(ad, REPORT_BIDDING_LOSS_NOT_COMPETITION, 0, 0);
          break;
        case REPORT_BIDDING_LOSS_OTHER:
          Log.d(TAG, "Reporting bidding loss (other) for ad: " + ad.getClass().getSimpleName());
          reportLoss(biddingAd, REPORT_BIDDING_LOSS_OTHER);
          // 同时上报到FastAdmin服务器
          reportToFastAdmin(ad, REPORT_BIDDING_LOSS_OTHER, 0, 0);
          break;
        default:
          Log.d(TAG, "Bidding reporting is disabled for ad: " + ad.getClass().getSimpleName());
          break;
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to report bidding result: " + e.getMessage(), e);
    }
  }
  
  /**
   * Helper method to report loss notification
   */
  private static void reportLoss(IBidding ad, int lossReason) {
    try {
      HashMap<String, Object> hashMapLoss = new HashMap<>();
      hashMapLoss.put(IBidding.WIN_PRICE, 300);
      hashMapLoss.put(IBidding.LOSS_REASON, lossReason);
      hashMapLoss.put(IBidding.ADN_ID, "WinAdnID");
      ad.sendLossNotification(hashMapLoss);
    } catch (Exception e) {
      Log.e(TAG, "Failed to report bidding loss: " + e.getMessage(), e);
      throw e;
    }
  }

  public static void reportBiddingNoAd(Object ad) {
    if (ad == null) {
      Log.w(TAG, "reportBiddingNoAd: Ad object is null");
      return;
    }
    
    // Check if the ad object implements IBiddingLoss interface
    if (!(ad instanceof IBiddingLoss)) {
      Log.w(TAG, "Ad object does not implement IBiddingLoss interface: " + ad.getClass().getName());
      return;
    }
    
    IBiddingLoss biddingLossAd = (IBiddingLoss) ad;
    
    try {
      HashMap<String, Object> hashMap = new HashMap<>();
      hashMap.put(IBidding.WIN_PRICE, 300);
      hashMap.put(IBidding.LOSS_REASON, BiddingLossReason.NO_AD);
      hashMap.put(IBidding.ADN_ID, "WinAdnID");
      Log.d(TAG, "Reporting no ad for: " + ad.getClass().getSimpleName());
      biddingLossAd.sendLossNotification(hashMap);
      
      // 同时上报到FastAdmin服务器
      reportToFastAdmin(ad, REPORT_BIDDING_LOSS_NO_AD, 300, 0);
    } catch (Exception e) {
      Log.e(TAG, "Failed to report no ad: " + e.getMessage(), e);
    }
  }
  
  /**
   * 上报C2S竞价结果到FastAdmin服务器
   */
  private static void reportToFastAdmin(Object ad, int reportType, int winPrice, int expectPrice) {
    try {
      // 构建参数
      final java.util.Map<String, String> params = new java.util.HashMap<>();
      
      // 广告类型
      int adType = getAdTypeFromObject(ad);
      params.put("ad_type", String.valueOf(adType));
      
      // 上报类型
      params.put("report_type", String.valueOf(reportType));
      
      // 竞价价格信息
      params.put("win_price", String.valueOf(winPrice));
      params.put("expect_price", String.valueOf(expectPrice));
      
      // 设备信息
      params.put("device_model", Build.MODEL);
      params.put("os_version", String.valueOf(Build.VERSION.SDK_INT));
      params.put("device_id", getDeviceId());
      
      // 应用信息
      Context context = MyApplication.getInstance();
      params.put("app_version", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
      
      // 用户信息 - 如果有用户ID可以添加
      // String userId = getUserID();
      // if (userId != null && !userId.isEmpty()) {
      //     params.put("user_id", userId);
      // }
      
      Log.d(TAG, "上报C2S竞价结果到FastAdmin: " + params.toString());
      
      // 调用不需要回调的重载方法
      ApiManager.getInstance().reportBiddingResult(params);
    } catch (Exception e) {
      Log.e(TAG, "上报C2S竞价结果到FastAdmin异常: " + e.getMessage(), e);
    }
  }
  
  /**
   * 根据广告对象获取广告类型
   */
  private static int getAdTypeFromObject(Object ad) {
    if (ad == null) {
      return 0;
    }
    
    String className = ad.getClass().getSimpleName();
    Log.d(TAG, "广告对象类名: " + className);
    
    if (className.contains("Banner")) {
      return AD_TYPE_BANNER;
    } else if (className.contains("Interstitial")) {
      return AD_TYPE_INTERSTITIAL;
    } else if (className.contains("Reward")) {
      return AD_TYPE_REWARD_VIDEO;
    } else if (className.contains("Native")) {
      return AD_TYPE_NATIVE;
    } else if (className.contains("Splash")) {
      return AD_TYPE_SPLASH;
    } else {
      return 0;
    }
  }
  
  /**
   * 获取设备唯一标识（简化处理，实际应用中需要考虑隐私政策）
   */
  private static String getDeviceId() {
    try {
      Context context = MyApplication.getInstance();
      // 这里使用UUID作为示例，实际应用中应该使用更合适的设备标识方法
      String deviceId = UUID.randomUUID().toString();
      return deviceId;
    } catch (Exception e) {
      Log.e(TAG, "获取设备ID失败: " + e.getMessage());
      return "unknown_device";
    }
  }
  
  /**
   * 上报广告加载错误信息
   */
  public static void reportAdError(Object ad, AdError error) {
    if (error == null) {
      return;
    }
    
    try {
      int errorCode = error.getErrorCode();
      String errorMsg = error.getErrorMsg();
      
      Log.e(TAG, "广告加载错误: code=" + errorCode + ", msg=" + errorMsg);
      
      // 构建参数
      Map<String, String> params = new HashMap<>();
      
      // 广告类型
      int adType = getAdTypeFromObject(ad);
      params.put("ad_type", String.valueOf(adType));
      
      // 错误信息
      params.put("error_code", String.valueOf(errorCode));
      params.put("error_msg", errorMsg);
      
      // 设备信息
      params.put("device_model", Build.MODEL);
      params.put("os_version", String.valueOf(Build.VERSION.SDK_INT));
      params.put("device_id", getDeviceId());
      
      // 调用API接口上报错误信息
      ApiManager.getInstance().reportBiddingResult(params, new ApiCallback<Object>() {
          @Override
          public void onSuccess(Object data) {
              Log.d(TAG, "广告错误信息上报成功");
          }
          
          @Override
          public void onFailure(String errorMessage) {
              // 上报失败不影响主流程，只记录日志
              Log.w(TAG, "广告错误信息上报失败: " + errorMessage);
          }
      });
    } catch (Exception e) {
      Log.e(TAG, "上报广告错误信息异常: " + e.getMessage(), e);
    }
  }
}