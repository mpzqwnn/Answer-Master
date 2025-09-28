package com.fortunequizking.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 导入优量汇SDK相关类
import com.fortunequizking.QuizActivity;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.managers.GDTAdSdk;
import com.qq.e.comm.managers.setting.GlobalSetting;
import com.qq.e.comm.constants.LoadAdParams;
import com.qq.e.comm.util.AdError;
import com.anythink.core.api.ATAdInfo;

// 导入R类以使用布局资源
import com.fortunequizking.R;
// 导入竞价相关工具类
import com.fortunequizking.util.BiddingC2SUtils;

// 导入API管理类
import com.fortunequizking.api.ApiManager;
// 导入Application类
import com.fortunequizking.MyApplication;

/**
 * 广告管理类，负责优量汇SDK的初始化和各种广告的展示管理
 */
public class AdManager {
    private static final String TAG = "AdManager";
    private static final String GDT_APP_ID = "1211147611"; // 优量汇App ID
    private static boolean isInitialized = false;

    // 广告位ID - 使用优量汇Demo中的广告位ID
    private static final String SPLASH_PLACEMENT_ID = "9093517612222759"; // 开屏广告
    private static final String BANNER_PLACEMENT_ID = "4200669226896781"; // 横幅广告
    private static final String INTERSTITIAL_PLACEMENT_ID = "6230063296996435"; // 插屏广告（全屏）
    private static final String REWARD_PLACEMENT_ID = "9250444775074318"; // 激励视频广告
    private static final String NATIVE_PLACEMENT_ID = "9220243712063434"; // 原生广告

    private static AdManager instance;

    // 广告对象
    private SplashAD splashAd;
    private UnifiedBannerView bannerView;
    private UnifiedInterstitialAD interstitialAd;
    private RewardVideoAD rewardVideoAd;
    private NativeExpressAD nativeAd;

    // 回调接口
    private RewardAdListener rewardAdListener;
    private Runnable splashAdDismissCallback;
    private BannerAdListener bannerAdListener;
    private InterstitialAdListener interstitialAdListener;
    private NativeAdListener nativeAdListener;
    // 横幅广告相关
    private static final long MIN_BANNER_REFRESH_INTERVAL = 30000; // 最小刷新间隔30秒
    private long lastBannerLoadTime = 0; // 上次加载时间
    private int bannerRetryCount = 0; // 重试次数
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRetryRunnable;
    // 插屏广告相关
    private static final long MIN_INTERSTITIAL_REFRESH_INTERVAL = 30000; // 最小刷新间隔30秒
    private long lastInterstitialLoadTime = 0; // 上次加载时间
    private int interstitialRetryCount = 0; // 重试次数
    private Handler interstitialHandler = new Handler(Looper.getMainLooper());
    private Runnable interstitialRetryRunnable;
    private UnifiedInterstitialMediaListener mediaListener;
    private boolean isInterstitialLoading = false;
    private boolean isRewardAdPlaying = false; // 标记激励广告是否正在播放

    // 原生广告相关
    private static final long MIN_NATIVE_REFRESH_INTERVAL = 30000; // 最小刷新间隔30秒
    private long lastNativeLoadTime = 0; // 上次加载时间
    private int nativeRetryCount = 0; // 重试次数
    private Handler nativeHandler = new Handler(Looper.getMainLooper());
    private Runnable nativeRetryRunnable;

    // 用户ID，用于广告请求参数
    private static String userId = "";

    // 单例模式
    public static synchronized AdManager getInstance() {
        if (instance == null) {
            instance = new AdManager();
        }
        return instance;
    }

    /**
     * 初始化SDK
     */
    public void init(Context context) {
        initSDK(context);
    }

    /**
     * 设置用户ID，用于广告请求参数
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取用户ID，用于广告SDK中的userid参数
     */
    public static String getUserId() {
        if (TextUtils.isEmpty(userId)) {
            userId = "";
            try {
                // 从Android ID生成用户ID，避免使用敏感信息
                userId = UUID.randomUUID().toString();
            } catch (Exception e) {
                Log.e(TAG, "获取用户ID失败: " + e.getMessage(), e);
                userId = UUID.randomUUID().toString();
            }
        }
        return userId;
    }

    /**
     * 在Application的onCreate中初始化SDK的方法
     */
    public static void initSDK(Context context) {
        if (isInitialized) {
            return;
        }

        // 检查是否在主进程
        if (!isMainProcess(context)) {
            Log.d(TAG, "不在主进程，不初始化SDK");
            return;
        }

        try {
            // 检查设备Android版本，为特定版本添加兼容性处理
            int currentApiVersion = android.os.Build.VERSION.SDK_INT;
            Log.d(TAG, "当前Android版本: " + currentApiVersion);
            Log.d(TAG, "设备型号: " + android.os.Build.MODEL);
            Log.d(TAG, "设备厂商: " + android.os.Build.MANUFACTURER);

            // 设置用户隐私配置
            setupPrivacySettings();
            
            // 配置优量汇SDK
            configureGDTAdSDK();

            // 初始化优量汇SDK
            GDTAdSdk.initWithoutStart(context.getApplicationContext(), GDT_APP_ID);
            
            // 等待SDK初始化完成
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d(TAG, "优量汇 SDK 初始化完成，广告实例准备就绪");
            }, 1000);

            Log.d(TAG, "优量汇 SDK 初始化完成");
            isInitialized = true;
        } catch (Throwable t) {
            Log.e(TAG, "优量汇 SDK 初始化异常: " + t.getMessage(), t);
        }
    }

    /**
     * 配置优量汇SDK
     */
    private static void configureGDTAdSDK() {
        // 建议在初始化 SDK 前进行此设置
        GlobalSetting.setChannel(1);
        GlobalSetting.setEnableMediationTool(true);
        GlobalSetting.setEnableCollectAppInstallStatus(true);
    }

    /**
     * 设置用户隐私相关配置
     */
    private static void setupPrivacySettings() {
        try {
            // 设置用户隐私信息同意状态
            Map<String, Boolean> params = new HashMap<>();
            params.put("android_id", true);
            params.put("device_id", true);
            GlobalSetting.setAgreeReadPrivacyInfo(params);
            
            Log.d(TAG, "已设置完整用户隐私配置，同意读取设备信息");
        } catch (Exception e) {
            Log.e(TAG, "设置用户隐私配置异常: " + e.getMessage());
        }
    }

    /**
     * 检查是否在主进程
     */
    private static boolean isMainProcess(Context context) {
        try {
            String packageName = context.getPackageName();
            String processName = getProcessName();
            return packageName.equals(processName);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取当前进程名
     */
    private static String getProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            Log.e(TAG, "获取进程名失败", e);
            return null;
        }
    }

    /**
     * 根据优量汇错误码处理不同类型的错误
     */
    private long getDelayTimeByErrorCode(String errorCode, int retryCount) {
        if (errorCode == null) {
            return Math.min(10000 * (long) Math.pow(2, retryCount - 1), 120000);
        }

        switch (errorCode) {
            case "4001":
                return -1; // 参数错误，不重试
            case "4004":
                return Math.min(20000 * (long) Math.pow(2, retryCount - 1), 180000);
            case "2008":
                return 60000;
            default:
                return Math.min(10000 * (long) Math.pow(2, retryCount - 1), 120000);
        }
    }

    /**
     * 创建通用广告请求参数
     */
    private Map<String, Object> createCommonAdParams() {
        Map<String, Object> params = new HashMap<>();

        params.put("user_id", getUserId());
        params.put("app_scene", "normal");
        params.put("device_type", Build.MODEL);
        params.put("os_version", Build.VERSION.SDK_INT);
        params.put("network_timeout", 15000);

        return params;
    }

    /**
     * 开屏广告相关参数
     */
    private int minSplashTimeWhenNoAD = 2000;
    private long fetchSplashADTime = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean canJump = false;

    /**
     * 显示开屏广告
     */
    public void showSplashAd(Activity activity, Runnable onAdDismissed) {
        this.splashAdDismissCallback = onAdDismissed;

        try {
            if (!isInitialized) {
                Log.w(TAG, "SDK尚未初始化，尝试重新初始化");
                initSDK(activity.getApplicationContext());
            }

            ViewGroup container = activity.findViewById(android.R.id.content);
            if (container == null) {
                Log.e(TAG, "无法获取内容容器，开屏广告无法展示");
                if (splashAdDismissCallback != null) {
                    splashAdDismissCallback.run();
                }
                return;
            }

            fetchSplashADTime = System.currentTimeMillis();
            canJump = false;

            splashAd = new SplashAD(activity, SPLASH_PLACEMENT_ID, new SplashADListener() {
                @Override
                public void onADDismissed() {
                    Log.d(TAG, "开屏广告关闭");
                    if (splashAdDismissCallback != null) {
                        if (canJump) {
                            splashAdDismissCallback.run();
                        } else {
                            handler.postDelayed(() -> {
                                if (splashAdDismissCallback != null) {
                                    splashAdDismissCallback.run();
                                }
                            }, minSplashTimeWhenNoAD - (System.currentTimeMillis() - fetchSplashADTime));
                        }
                    }
                }

                @Override
                public void onNoAD(AdError error) {
                    Log.e(TAG, "开屏广告加载失败: " + error.getErrorMsg());

                    String errorCode = String.valueOf(error.getErrorCode());
                    if ("4001".equals(errorCode)) {
                        Log.e(TAG, "优量汇4001错误：广告请求参数错误，请检查配置");
                    } else if ("4004".equals(errorCode)) {
                        Log.e(TAG, "优量汇4004错误：当前没有合适的广告填充");
                    }

                    if (splashAdDismissCallback != null) {
                        long alreadyDelayMills = System.currentTimeMillis() - fetchSplashADTime;
                        if (alreadyDelayMills < minSplashTimeWhenNoAD) {
                            handler.postDelayed(() -> {
                                if (splashAdDismissCallback != null) {
                                    splashAdDismissCallback.run();
                                }
                            }, minSplashTimeWhenNoAD - alreadyDelayMills);
                        } else {
                            splashAdDismissCallback.run();
                        }
                    }
                }

                @Override
                public void onADPresent() {
                    Log.d(TAG, "开屏广告显示");
                }

                @Override
                public void onADClicked() {
                    Log.d(TAG, "开屏广告点击");
                    canJump = true;
                }

                @Override
                public void onADTick(long millisUntilFinished) {
                    Log.d(TAG, "开屏广告倒计时: " + millisUntilFinished + "ms");
                }

                @Override
                public void onADLoaded(long l) {
                    Log.d(TAG, "开屏广告加载完成");
                    splashAd.showAd(container);
                    // 处理并发送eCPM数据
                    handleAdEcpm(SPLASH_PLACEMENT_ID);
                }

                @Override
                public void onADExposure() {
                    Log.d(TAG, "开屏广告曝光");
                }
            }, 5000);

        } catch (Exception e) {
            Log.e(TAG, "开屏广告加载异常: " + e.getMessage());
            if (splashAdDismissCallback != null) {
                splashAdDismissCallback.run();
            }
        }
    }

    /**
     * 设置激励广告播放状态
     */
    public void setRewardAdPlaying(boolean playing) {
        this.isRewardAdPlaying = playing;
    }

    /**
     * 获取激励广告播放状态
     */
    public boolean isRewardAdPlaying() {
        return isRewardAdPlaying;
    }

    /**
     * 显示横幅广告
     */
    public void showBannerAd(Activity activity, ViewGroup container) {
        try {
            int width = activity.getResources().getDisplayMetrics().widthPixels;
            int height = (int) (width / 6.4f);

            UnifiedBannerADListener adListener = new UnifiedBannerADListener() {
                @Override
                public void onADReceive() {
                    Log.d(TAG, "横幅广告加载成功");
                    lastBannerLoadTime = System.currentTimeMillis();
                    bannerRetryCount = 0;
                    if (bannerAdListener != null) {
                        bannerAdListener.onBannerAdLoaded();
                    }
                    reportBiddingResult(bannerView);
                    // 处理并发送eCPM数据
                    handleAdEcpm(BANNER_PLACEMENT_ID);
                }

                @Override
                public void onADExposure() {
                    Log.d(TAG, "横幅广告显示");
                    if (bannerAdListener != null) {
                        bannerAdListener.onBannerAdExposure();
                    }
                }

                @Override
                public void onADClosed() {
                    Log.d(TAG, "横幅广告关闭");
                    if (bannerAdListener != null) {
                        bannerAdListener.onBannerAdClosed();
                    }
                }

                @Override
                public void onADClicked() {
                    Log.d(TAG, "横幅广告点击");
                    if (bannerAdListener != null) {
                        bannerAdListener.onBannerAdClicked();
                    }
                }

                @Override
                public void onNoAD(AdError adError) {
                    Log.e(TAG, "横幅广告加载失败: " + adError.getErrorMsg());
                    if (bannerAdListener != null) {
                        bannerAdListener.onBannerAdFailedToShow(adError.getErrorMsg());
                    }
                    reportBiddingNoAd(bannerView);
                    try {
                        BiddingC2SUtils.reportAdError(bannerView, adError);
                    } catch (Exception e) {
                        Log.e(TAG, "调用BiddingC2SUtils.reportAdError异常: " + e.getMessage());
                    }

                    if (bannerRetryRunnable != null) {
                        bannerHandler.removeCallbacks(bannerRetryRunnable);
                    }

                    String errorCode = String.valueOf(adError.getErrorCode());
                    long delayTime = getDelayTimeByErrorCode(errorCode, bannerRetryCount);

                    if (delayTime == -1) {
                        Log.e(TAG, "根据优量汇规则，此错误不应重试");
                        return;
                    }

                    Log.d(TAG, "根据错误码调整延迟时间: " + delayTime + "ms");
                    bannerHandler.postDelayed(() -> {
                        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                            scheduleBannerRetry(activity);
                        }
                    }, delayTime);
                }

                @Override
                public void onADLeftApplication() {
                    Log.d(TAG, "用户离开应用");
                }
            };

            bannerView = new UnifiedBannerView(activity, BANNER_PLACEMENT_ID, adListener);

            container.removeAllViews();
            container.addView(bannerView);

            bannerView.loadAD();

        } catch (Exception e) {
            Log.e(TAG, "横幅广告加载异常: " + e.getMessage());
        }
    }

    /**
     * 带频率控制的横幅广告加载方法
     */
    private void loadBannerAdWithFrequencyControl(Activity activity) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastBannerLoadTime;

        if (elapsedTime >= MIN_BANNER_REFRESH_INTERVAL || lastBannerLoadTime == 0) {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && bannerView != null) {
                bannerView.loadAD();
                lastBannerLoadTime = currentTime;
            }
        } else {
            long delayTime = MIN_BANNER_REFRESH_INTERVAL - elapsedTime;
            Log.d(TAG, "未达到最小加载间隔，延迟 " + delayTime + "ms 后加载");
            bannerHandler.postDelayed(() -> {
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && bannerView != null) {
                    bannerView.loadAD();
                    lastBannerLoadTime = System.currentTimeMillis();
                }
            }, delayTime);
        }
    }

    /**
     * 安排横幅广告重试加载
     */
    private void scheduleBannerRetry(Activity activity) {
        bannerRetryCount++;
        long retryDelay = Math.min(10000 * (long) Math.pow(2, bannerRetryCount - 1), 120000);

        Log.d(TAG, "安排横幅广告重试，第" + bannerRetryCount + "次，延迟 " + retryDelay + "ms");

        bannerRetryRunnable = () -> {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && bannerView != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBannerLoadTime >= MIN_BANNER_REFRESH_INTERVAL * 1.5) {
                    loadBannerAdWithFrequencyControl(activity);
                } else {
                    long safeDelay = (long)(MIN_BANNER_REFRESH_INTERVAL * 1.5) - (currentTime - lastBannerLoadTime);
                    Log.d(TAG, "为避免频率过高，额外延迟 " + safeDelay + "ms");
                    bannerHandler.postDelayed(() -> {
                        loadBannerAdWithFrequencyControl(activity);
                    }, safeDelay);
                }
            }
        };

        bannerHandler.postDelayed(bannerRetryRunnable, retryDelay);
    }

    /**
     * 安排原生广告重试加载
     */
    private void scheduleNativeRetry(Activity activity, ViewGroup container) {
        nativeRetryCount++;
        long retryDelay = Math.min(10000 * (long) Math.pow(2, nativeRetryCount - 1), 120000);

        Log.d(TAG, "安排原生广告重试，第" + nativeRetryCount + "次，延迟 " + retryDelay + "ms");

        nativeRetryRunnable = () -> {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastNativeLoadTime >= MIN_NATIVE_REFRESH_INTERVAL * 1.5) {
                    loadNativeAdWithFrequencyControl(activity, container);
                } else {
                    long safeDelay = (long)(MIN_NATIVE_REFRESH_INTERVAL * 1.5) - (currentTime - lastNativeLoadTime);
                    Log.d(TAG, "为避免频率过高，额外延迟 " + safeDelay + "ms");
                    nativeHandler.postDelayed(() -> {
                        loadNativeAdWithFrequencyControl(activity, container);
                    }, safeDelay);
                }
            }
        };

        nativeHandler.postDelayed(nativeRetryRunnable, retryDelay);
    }
    
    /**
     * 带频率控制的原生广告加载方法
     */
    private void loadNativeAdWithFrequencyControl(Activity activity, ViewGroup container) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastNativeLoadTime;

        if (elapsedTime >= MIN_NATIVE_REFRESH_INTERVAL || lastNativeLoadTime == 0) {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                final int width = activity.getResources().getDisplayMetrics().widthPixels;
                
                int adjustedWidth = width;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    DisplayMetrics metrics = new DisplayMetrics();
                    activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                    adjustedWidth = metrics.widthPixels;
                }
                
                final int height = (int) (adjustedWidth / 2.5f);
                
                NativeExpressAD.NativeExpressADListener adListener = new NativeExpressAD.NativeExpressADListener() {
                    @Override
                    public void onADLoaded(List<NativeExpressADView> list) {
                        Log.d(TAG, "原生广告加载成功，返回" + list.size() + "个广告");
                        if (list != null && list.size() > 0) {
                            NativeExpressADView adView = list.get(0);
                            // 保存引用以便后续获取eCPM等级
                            nativeAdView = adView;
                             
                            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, 
                                    ViewGroup.LayoutParams.MATCH_PARENT
                            );
                            adView.setLayoutParams(params);
                             
                            container.removeAllViews();
                            container.addView(adView);
                             
                            adView.render();
                             
                            nativeRetryCount = 0;
                        }
                        if (nativeAdListener != null) {
                            nativeAdListener.onNativeAdLoaded();
                        }
                        // 使用nativeAdView代替nativeAd进行竞价结果上报
                        if (nativeAdView != null) {
                            reportBiddingResult(nativeAdView);
                        }
                        // 处理并发送eCPM数据
                        handleAdEcpm(NATIVE_PLACEMENT_ID);
                    }
                    
                    @Override
                    public void onRenderSuccess(NativeExpressADView adView) {
                        Log.d(TAG, "原生广告渲染成功");
                        if (nativeAdListener != null) {
                            nativeAdListener.onNativeAdRenderSuccess();
                        }
                    }
                    
                    @Override
                    public void onRenderFail(NativeExpressADView adView) {
                        Log.e(TAG, "原生广告渲染失败");
                        if (nativeAdListener != null) {
                            nativeAdListener.onNativeAdRenderFail();
                        }
                    }
                    
                    @Override
                    public void onADExposure(NativeExpressADView nativeExpressADView) {
                        Log.d(TAG, "原生广告曝光回调");
                        if (nativeAdListener != null) {
                            nativeAdListener.onNativeAdExposure();
                        }
                    }
                    
                    @Override
                    public void onADClicked(NativeExpressADView nativeExpressADView) {
                        Log.d(TAG, "原生广告点击回调");
                        if (nativeAdListener != null) {
                            nativeAdListener.onNativeAdClicked();
                        }
                    }
                    
                    @Override
                    public void onADClosed(NativeExpressADView nativeExpressADView) {
                        Log.d(TAG, "原生广告关闭回调");
                        if (container != null && nativeExpressADView.getParent() == container) {
                            container.removeView(nativeExpressADView);
                        }
                        nativeExpressADView.destroy();
                    }
                    
                    @Override
                    public void onADLeftApplication(NativeExpressADView nativeExpressADView) {
                        Log.d(TAG, "用户离开应用回调");
                    }
                    
                    @Override
                    public void onNoAD(AdError adError) {
                        Log.e(TAG, "原生广告加载失败: code:[ " + adError.getErrorCode() + " ]desc:[ " + adError.getErrorMsg() + " ]");
                        if (nativeAdListener != null) {
                            nativeAdListener.onNativeAdFailedToShow(adError.getErrorMsg());
                        }
                        reportBiddingNoAd(nativeAd);
                        try {
                            BiddingC2SUtils.reportAdError(nativeAd, adError);
                        } catch (Exception e) {
                            Log.e(TAG, "调用BiddingC2SUtils.reportAdError异常: " + e.getMessage());
                        }
                        
                        String errorCode = String.valueOf(adError.getErrorCode());
                        if ("4001".equals(errorCode)) {
                            Log.e(TAG, "优量汇4001错误：广告请求参数错误，请检查配置");
                        } else {
                            scheduleNativeRetry(activity, container);
                        }
                    }
                };
                
                nativeAd.loadAD(1);
                lastNativeLoadTime = currentTime;
            }
        } else {
            long delayTime = MIN_NATIVE_REFRESH_INTERVAL - elapsedTime;
            Log.d(TAG, "未达到最小加载间隔，延迟 " + delayTime + "ms 后加载");
            nativeHandler.postDelayed(() -> {
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                    loadNativeAdWithFrequencyControl(activity, container);
                }
            }, delayTime);
        }
    }
    
    /**
     * 销毁横幅广告资源
     */
    public void destroyBannerAd() {
        if (bannerHandler != null) {
            bannerHandler.removeCallbacksAndMessages(null);
        }
        if (bannerView != null) {
            bannerView.destroy();
            bannerView = null;
        }
        bannerRetryCount = 0;
        lastBannerLoadTime = 0;
    }

    /**
     * 初始化插屏全屏广告
     */
    public void initInterstitialAd(Activity activity) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "SDK尚未初始化，尝试重新初始化");
                initSDK(activity.getApplicationContext());
            }

            if (interstitialAd == null) {
                UnifiedInterstitialADListener adListener = new UnifiedInterstitialADListener() {
                    @Override
                    public void onADReceive() {
                        Log.d(TAG, "插屏全屏广告加载成功");
                        lastInterstitialLoadTime = System.currentTimeMillis();
                        interstitialRetryCount = 0;
                        isInterstitialLoading = false;
                        if (interstitialAdListener != null) {
                            interstitialAdListener.onInterstitialAdLoaded();
                        }
                        reportBiddingResult(interstitialAd);
                        // 处理并发送eCPM数据
                        handleAdEcpm(INTERSTITIAL_PLACEMENT_ID);
                        
                        // 设置媒体监听器
                        if (mediaListener == null) {
                            setupMediaListener();
                        }
                        interstitialAd.setMediaListener(mediaListener);
                    }

                    @Override
                    public void onNoAD(AdError adError) {
                        Log.e(TAG, "插屏全屏广告加载失败: code:[ " + adError.getErrorCode() + " ]desc:[ " + adError.getErrorMsg() + " ]");
                Log.e(TAG, "插屏广告位ID: " + INTERSTITIAL_PLACEMENT_ID);
                        isInterstitialLoading = false;
                        if (interstitialAdListener != null) {
                            interstitialAdListener.onInterstitialAdFailedToShow(adError.getErrorMsg());
                        }
                        reportBiddingNoAd(interstitialAd);
                        try {
                            BiddingC2SUtils.reportAdError(interstitialAd, adError);
                        } catch (Exception e) {
                            Log.e(TAG, "调用BiddingC2SUtils.reportAdError异常: " + e.getMessage());
                        }

                        // 处理重试逻辑
                        handleInterstitialRetry(activity, adError);
                    }

                    @Override
                    public void onVideoCached() {
                        Log.d(TAG, "插屏全屏广告视频素材缓存完成");
                    }

                    @Override
                    public void onRenderSuccess() {
                        Log.d(TAG, "插屏全屏广告渲染成功");
                    }

                    @Override
                    public void onRenderFail() {
                        Log.e(TAG, "插屏全屏广告渲染失败");
                        isInterstitialLoading = false;
                    }

                    @Override
                    public void onADOpened() {
                        Log.d(TAG, "插屏全屏广告展开");
                        if (interstitialAdListener != null) {
                            interstitialAdListener.onInterstitialAdShow();
                        }
                    }

                    @Override
                    public void onADExposure() {
                        Log.d(TAG, "插屏全屏广告曝光");
                        // 曝光后加载下一条广告
                        loadInterstitialAdWithFrequencyControl(activity);
                        if (interstitialAdListener != null) {
                            interstitialAdListener.onInterstitialAdExposure();
                        }
                    }

                    @Override
                    public void onADClicked() {
                        Log.d(TAG, "插屏全屏广告点击");
                        if (interstitialAdListener != null) {
                            interstitialAdListener.onInterstitialAdClicked();
                        }
                    }

                    @Override
                    public void onADClosed() {
                        Log.d(TAG, "插屏全屏广告关闭");
                        if (interstitialAdListener != null) {
                            interstitialAdListener.onInterstitialAdClosed();
                        }
                    }

                    @Override
                    public void onADLeftApplication() {
                        Log.d(TAG, "用户点击插屏全屏广告离开应用");
                    }
                };

                // 创建插屏全屏广告实例
                interstitialAd = new UnifiedInterstitialAD(activity, INTERSTITIAL_PLACEMENT_ID, adListener);

                // 配置服务端验证
                ServerSideVerificationOptions options = new ServerSideVerificationOptions.Builder()
                        .setCustomData("APP's custom data")
                        .setUserId(getUserId())
                        .build();
                interstitialAd.setServerSideVerificationOptions(options);

                // 设置视频参数
                VideoOption videoOption = new VideoOption.Builder()
                        .setAutoPlayMuted(true)
                        .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.ALWAYS)
                        .setDetailPageMuted(false)
                        .build();
                interstitialAd.setVideoOption(videoOption);

                // 设置加载参数
                Map<String, String> info = new HashMap<>();
                info.put("ad_type", "full_screen_interstitial");
                info.put("app_scene", "normal");
                info.put("interstitial_type", "full_screen");
                info.put("is_full_screen", "1");
                LoadAdParams loadAdParams = new LoadAdParams();
                loadAdParams.setDevExtra(info);
                interstitialAd.setLoadAdParams(loadAdParams);

                // 设置负反馈监听
                interstitialAd.setNegativeFeedbackListener(() -> Log.d(TAG, "用户投诉插屏广告成功"));
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化插屏全屏广告异常: " + e.getMessage());
        }
    }

    /**
     * 设置媒体监听器
     */
    private void setupMediaListener() {
        mediaListener = new UnifiedInterstitialMediaListener() {
            @Override
            public void onVideoInit() {
                Log.d(TAG, "插屏全屏广告视频播放View初始化完成");
            }

            @Override
            public void onVideoLoading() {
                Log.d(TAG, "插屏全屏广告视频素材下载中");
            }

            @Override
            public void onVideoReady(long videoDuration) {
                Log.d(TAG, "插屏全屏广告视频准备就绪，视频时长: " + videoDuration + "ms");
            }

            @Override
            public void onVideoStart() {
                Log.d(TAG, "插屏全屏广告视频开始播放");
            }

            @Override
            public void onVideoPause() {
                Log.d(TAG, "插屏全屏广告视频暂停播放");
            }

            @Override
            public void onVideoComplete() {
                Log.d(TAG, "插屏全屏广告视频播放完成");
            }

            @Override
            public void onVideoError(AdError error) {
                Log.e(TAG, "插屏全屏广告视频播放错误: " + error.getErrorMsg());
            }

            @Override
            public void onVideoPageOpen() {
                Log.d(TAG, "插屏全屏广告视频详情页打开");
            }

            @Override
            public void onVideoPageClose() {
                Log.d(TAG, "插屏全屏广告视频详情页关闭");
            }
        };
    }

    /**
     * 处理插屏广告加载失败的重试逻辑
     */
    private void handleInterstitialRetry(Activity activity, AdError adError) {
        if (interstitialRetryRunnable != null) {
            interstitialHandler.removeCallbacks(interstitialRetryRunnable);
        }

        String errorCode = String.valueOf(adError.getErrorCode());
        long delayTime = getDelayTimeByErrorCode(errorCode, interstitialRetryCount);

        if (delayTime == -1) {
            Log.e(TAG, "根据优量汇规则，此错误不应重试: " + errorCode);
            return;
        }

        Log.d(TAG, "根据错误码调整延迟时间: " + delayTime + "ms");
        interstitialHandler.postDelayed(() -> {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                scheduleInterstitialRetry(activity);
            }
        }, delayTime);
    }

    /**
     * 显示插屏全屏广告
     */
    public void showInterstitialAd(Activity activity) {
        try {
            // 检查激励广告是否正在播放，如果是，则不显示插屏广告
            if (isRewardAdPlaying) {
                Log.d(TAG, "激励广告正在播放，不显示插屏广告");
                return;
            }

            // 确保广告实例已初始化
            if (interstitialAd == null) {
                initInterstitialAd(activity);
            }

            // 检查广告是否准备好并显示
            if (interstitialAd != null && interstitialAd.isValid()) {
                interstitialAd.showFullScreenAD(activity);
                Log.d(TAG, "展示插屏全屏广告");
            } else {
                Log.d(TAG, "插屏全屏广告未就绪，开始加载");
                loadInterstitialAdWithFrequencyControl(activity);
            }
        } catch (Exception e) {
            Log.e(TAG, "插屏全屏广告加载显示异常: " + e.getMessage());
        }
    }

    /**
     * 带频率控制的插屏全屏广告加载方法
     */
    public void loadInterstitialAdWithFrequencyControl(Activity activity) {
        if (isInterstitialLoading) {
            Log.d(TAG, "插屏全屏广告正在加载中，避免重复请求");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastInterstitialLoadTime;

        if (elapsedTime >= MIN_INTERSTITIAL_REFRESH_INTERVAL || lastInterstitialLoadTime == 0) {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && interstitialAd != null) {
                isInterstitialLoading = true;
                Log.d(TAG, "准备加载插屏全屏广告，广告位ID: " + INTERSTITIAL_PLACEMENT_ID);
                interstitialAd.loadFullScreenAD();
                Log.d(TAG, "已调用loadFullScreenAD方法");
            } else if (interstitialAd == null) {
                Log.e(TAG, "插屏全屏广告实例未初始化，无法加载广告");
                initInterstitialAd(activity);
            }
        } else {
            long delayTime = MIN_INTERSTITIAL_REFRESH_INTERVAL - elapsedTime;
            Log.d(TAG, "未达到最小加载间隔，延迟 " + delayTime + "ms 后加载");
            interstitialHandler.postDelayed(() -> {
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && interstitialAd != null) {
                    isInterstitialLoading = true;
                    interstitialAd.loadFullScreenAD();
                    Log.d(TAG, "延迟后开始加载插屏全屏广告");
                }
            }, delayTime);
        }
    }

    /**
     * 安排插屏全屏广告重试加载
     */
    private void scheduleInterstitialRetry(Activity activity) {
        interstitialRetryCount++;
        long retryDelay = Math.min(10000 * (long) Math.pow(2, interstitialRetryCount - 1), 120000);

        Log.d(TAG, "安排插屏全屏广告重试，第" + interstitialRetryCount + "次，延迟 " + retryDelay + "ms");

        interstitialRetryRunnable = () -> {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && interstitialAd != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastInterstitialLoadTime >= MIN_INTERSTITIAL_REFRESH_INTERVAL * 1.5) {
                    loadInterstitialAdWithFrequencyControl(activity);
                } else {
                    long safeDelay = (long)(MIN_INTERSTITIAL_REFRESH_INTERVAL * 1.5) - (currentTime - lastInterstitialLoadTime);
                    Log.d(TAG, "为避免频率过高，额外延迟 " + safeDelay + "ms");
                    interstitialHandler.postDelayed(() -> {
                        loadInterstitialAdWithFrequencyControl(activity);
                    }, safeDelay);
                }
            }
        };

        interstitialHandler.postDelayed(interstitialRetryRunnable, retryDelay);
    }

    /**
     * 获取插屏广告有效性
     */
    public boolean isInterstitialAdValid() {
        return interstitialAd != null && interstitialAd.isValid();
    }

    /**
     * 获取当前插屏广告的eCPM等级
     */
    public String getInterstitialAdECPMLevel() {
        if (interstitialAd != null && interstitialAd.isValid()) {
            return interstitialAd.getECPMLevel();
        }
        return "";
    }
    
    /**
     * 获取当前激励视频广告的eCPM等级
     */
    public String getRewardAdECPMLevel() {
        if (rewardVideoAd != null && rewardVideoAd.isValid()) {
            return rewardVideoAd.getECPMLevel();
        }
        return "";
    }
    
    /**
     * 获取当前横幅广告的eCPM等级
     */
    public String getBannerAdECPMLevel() {
        if (bannerView != null && bannerView.isValid()) {
            return bannerView.getECPMLevel();
        }
        return "";
    }
    
    /**
     * 获取当前原生广告的eCPM等级
     */
    public String getNativeAdECPMLevel() {
        if (nativeAdView != null) {
            try {
                // 尝试直接调用NativeExpressADView的getECPMLevel方法
                return nativeAdView.getECPMLevel();
            } catch (Exception e) {
                // 如果直接调用失败，尝试反射方式获取
                try {
                    return nativeAdView.getClass().getMethod("getECPMLevel").invoke(nativeAdView).toString();
                } catch (Exception ex) {
                    Log.e(TAG, "获取原生广告eCPM等级异常: " + ex.getMessage());
                }
            }
        }
        return "";
    }
    
    /**
     * 获取当前开屏广告的eCPM等级
     */
    public String getSplashAdECPMLevel() {
        if (splashAd != null && splashAd.isValid()) {
            return splashAd.getECPMLevel();
        }
        return "";
    }

    /**
     * 清除插屏广告资源
     */
    public void destroyInterstitialAd() {
        if (interstitialRetryRunnable != null) {
            interstitialHandler.removeCallbacks(interstitialRetryRunnable);
        }
        if (interstitialAd != null) {
            interstitialAd.close();
            interstitialAd = null;
        }
        isInterstitialLoading = false;
        interstitialRetryCount = 0;
        lastInterstitialLoadTime = 0;
    }

    /**
     * 设置激励视频广告监听器
     */
    public void setRewardAdListener(RewardAdListener listener) {
        this.rewardAdListener = listener;
    }

    /**
     * 设置竞价结果上报类型
     */
    public void setReportBiddingWinLoss(int reportType) {
        BiddingC2SUtils.setReportBiddingWinLoss(reportType);
    }

    /**
     * 上报广告竞价结果到优量汇SDK
     */
    private void reportBiddingResult(Object ad) {
        try {
            if (ad == null) {
                Log.w(TAG, "reportBiddingResult: Ad object is null");
                return;
            }
            
            Log.d(TAG, "上报竞价结果，广告类型: " + ad.getClass().getSimpleName());
            BiddingC2SUtils.reportBiddingWinLoss(ad);
        } catch (Exception e) {
            Log.e(TAG, "上报竞价结果异常: " + e.getMessage(), e);
        }
    }

    /**
     * 上报广告无填充结果到优量汇SDK
     */
    private void reportBiddingNoAd(Object ad) {
        try {
            if (ad == null) {
                Log.w(TAG, "reportBiddingNoAd: Ad object is null");
                return;
            }
            
            Log.d(TAG, "上报无广告结果，广告类型: " + ad.getClass().getSimpleName());
            try {
                BiddingC2SUtils.reportBiddingNoAd(ad);
            } catch (Exception e) {
                Log.e(TAG, "调用BiddingC2SUtils.reportBiddingNoAd异常: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "上报无广告结果异常: " + e.getMessage(), e);
        }
    }

    /**
     * 设置横幅广告监听器
     */
    public void setBannerAdListener(BannerAdListener listener) {
        this.bannerAdListener = listener;
    }

    /**
     * 设置插屏广告监听器
     */
    public void setInterstitialAdListener(InterstitialAdListener listener) {
        this.interstitialAdListener = listener;
    }

    /**
     * 设置原生广告监听器
     */
    public void setNativeAdListener(NativeAdListener listener) {
        this.nativeAdListener = listener;
    }

    /**
     * 显示激励视频广告
     */
    public void showRewardAd(Activity activity, RewardAdListener listener) {
        this.rewardAdListener = listener;
        try {
            if (rewardVideoAd == null) {
                rewardVideoAd = new RewardVideoAD(activity, REWARD_PLACEMENT_ID, new RewardVideoADListener() {
                    @Override
                    public void onADLoad() {
                        Log.d(TAG, "激励视频广告加载成功");
                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdLoaded();
                        }

                        if (rewardVideoAd != null) {
                            ServerSideVerificationOptions options = new ServerSideVerificationOptions.Builder()
                                    .setCustomData("APP's custom data")
                                    .setUserId(getUserId())
                                    .build();
                            rewardVideoAd.setServerSideVerificationOptions(options);

                            reportBiddingResult(rewardVideoAd);
                            // 处理并发送eCPM数据
                            handleAdEcpm(REWARD_PLACEMENT_ID);
                        }
                    }

                    @Override
                    public void onError(AdError adError) {
                        Log.e(TAG, "激励视频广告加载失败: code:[ " + adError.getErrorCode() + " ]desc:[ " + adError.getErrorMsg() + " ]");

                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdFailedToShow();
                        }

                        reportBiddingNoAd(rewardVideoAd);
                        BiddingC2SUtils.reportAdError(rewardVideoAd, adError);
                    }

                    @Override
                    public void onVideoCached() {
                        Log.d(TAG, "激励视频缓存成功");
                    }

                    @Override
                    public void onADShow() {
                        Log.d(TAG, "激励视频广告显示");
                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdStarted(); // 调用新增的回调方法
                        }
                        // 不要在这里立即预加载新广告，避免覆盖当前广告的监听器
                        // 预加载应该在广告完全关闭后进行
                    }

                    @Override
                    public void onADExpose() {
                        Log.d(TAG, "激励视频广告曝光");
                    }

                    @Override
                    public void onReward(Map<String, Object> map) {
                        Log.d(TAG, "激励视频广告奖励发放1111");
                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdRewarded();
                        }
                    }

                    @Override
                    public void onADClick() {
                        Log.d(TAG, "激励视频广告点击");
                    }

                    @Override
                    public void onVideoComplete() {
                        Log.d(TAG, "激励视频广告播放完成");
                    }

                    @Override
                    public void onADClose() {
                        Log.d(TAG, "激励视频广告关闭");
                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdClosed();
                        }
                        // 广告完全关闭后再预加载下一个广告
                        if (rewardVideoAd != null) {
                            rewardVideoAd.loadAD();
                        }
                    }
                });
            }

            if (rewardVideoAd != null && rewardVideoAd.isValid()) {
                rewardVideoAd.showAD();
            } else if (rewardVideoAd != null) {
                rewardVideoAd.loadAD();
            } else {
                Log.e(TAG, "激励视频广告实例未初始化");
                if (rewardAdListener != null) {
                    rewardAdListener.onRewardAdFailedToShow();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "激励视频广告加载异常: " + e.getMessage());
            if (rewardAdListener != null) {
                rewardAdListener.onRewardAdFailedToShow();
            }
        }
    }

    /**
     * 显示激励视频广告 - 兼容QuizActivity的调用方式
     * 注意：此方法仅用于兼容旧代码，新代码应使用带有完整监听器的showRewardAd方法
     * 此方法不直接增加体力，而是通过回调让调用方处理奖励逻辑，避免重复增加体力
     */
    public void showRewardAd(Activity activity, int requestCode) {
        // 创建一个默认的监听器，但不直接处理体力奖励
        // 体力奖励应由QuizActivity中的自定义监听器处理
        RewardAdListener tempListener = new RewardAdListener() {
            @Override
            public void onRewardAdLoaded() {
                Log.d(TAG, "激励视频广告加载成功");
            }

            @Override
            public void onRewardAdFailedToShow() {
                Log.d(TAG, "激励视频广告显示失败");
            }

            @Override
            public void onRewardAdRewarded() {
                Log.d(TAG, "激励视频广告奖励发放111");
                // 不在这里处理体力奖励，避免与QuizActivity中的逻辑重复
                // 体力奖励应由调用方的监听器处理
            }

            @Override
            public void onRewardAdClosed() {
                Log.d(TAG, "激励视频广告关闭");
                // 同步设置广告播放状态
                setRewardAdPlaying(false);
                
                // 注意：不能直接调用QuizActivity的loadUserStamina()方法，因为它是private的
                // QuizActivity已经在自己的监听器中处理了体力值更新和UI刷新
                // 这里只需要设置播放状态为false即可
            }

            @Override
            public void onRewardAdStarted() {
                Log.d(TAG, "激励视频广告开始播放");
                // 同步设置广告播放状态
                setRewardAdPlaying(true);
            }
        };
        
        showRewardAd(activity, tempListener);
    }

    /**
     * 预加载激励视频广告
     */
    public void preloadRewardVideoAd(Context context) {
        try {
            if (rewardVideoAd == null) {
                rewardVideoAd = new RewardVideoAD(context, REWARD_PLACEMENT_ID, new RewardVideoADListener() {
                    @Override
                    public void onADLoad() {
                        Log.d(TAG, "激励视频广告预加载成功");

                        if (rewardVideoAd != null) {
                            ServerSideVerificationOptions options = new ServerSideVerificationOptions.Builder()
                                    .setCustomData("APP's custom data")
                                    .setUserId(getUserId())
                                    .build();
                            rewardVideoAd.setServerSideVerificationOptions(options);
                             
                            reportBiddingResult(rewardVideoAd);
                            // 处理并发送eCPM数据
                            handleAdEcpm(REWARD_PLACEMENT_ID);
                        }
                    }

                    @Override
                    public void onError(AdError error) {
                        Log.e(TAG, "激励视频广告预加载失败: " + error.getErrorMsg());

                        BiddingC2SUtils.reportAdError(rewardVideoAd, error);
                        
                        String errorCode = String.valueOf(error.getErrorCode());
                        if ("4001".equals(errorCode)) {
                            Log.e(TAG, "优量汇4001错误：广告请求参数错误，请检查配置");
                        } else if ("4004".equals(errorCode)) {
                            Log.e(TAG, "优量汇4004错误：当前没有合适的广告填充");
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                if (rewardVideoAd != null) {
                                    rewardVideoAd.loadAD();
                                }
                            }, 30000);
                        }
                    }

                    @Override
                    public void onVideoCached() {
                        Log.d(TAG, "激励视频缓存成功");
                    }

                    @Override
                    public void onADShow() {
                        Log.d(TAG, "激励视频广告显示");
                        if (rewardVideoAd != null) {
                            rewardVideoAd.loadAD();
                        }
                    }

                    @Override
                    public void onADExpose() {
                        Log.d(TAG, "激励视频广告曝光");
                    }

                    @Override
                    public void onReward(Map<String, Object> map) {
                        Log.d(TAG, "激励视频广告奖励发放11");
                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdRewarded();
                        }
                    }

                    @Override
                    public void onADClick() {
                        Log.d(TAG, "激励视频广告点击");
                    }

                    @Override
                    public void onVideoComplete() {
                        Log.d(TAG, "激励视频广告播放完成");
                    }

                    @Override
                    public void onADClose() {
                        Log.d(TAG, "激励视频广告关闭");
                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdClosed();
                        }
                    }
                });
            }

            rewardVideoAd.loadAD();

        } catch (Exception e) {
            Log.e(TAG, "激励视频广告预加载异常: " + e.getMessage());
        }
    }

    /**
     * 显示原生广告
     */
    public void showNativeAd(Activity activity, ViewGroup container) {
        try {
            final int width = activity.getResources().getDisplayMetrics().widthPixels;
            
            int adjustedWidth = width;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                DisplayMetrics metrics = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                adjustedWidth = metrics.widthPixels;
            }
            
            final int height = (int) (adjustedWidth / 2.5f);

            NativeExpressAD.NativeExpressADListener adListener = new NativeExpressAD.NativeExpressADListener() {
                @Override
                public void onADLoaded(List<NativeExpressADView> list) {
                    Log.d(TAG, "原生广告加载成功，返回" + list.size() + "个广告");
                    if (list != null && list.size() > 0) {
                        NativeExpressADView adView = list.get(0);

                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 
                                ViewGroup.LayoutParams.MATCH_PARENT
                        );
                        adView.setLayoutParams(params);

                        container.removeAllViews();
                        container.addView(adView);

                        adView.render();
                    }
                    if (nativeAdListener != null) {
                        nativeAdListener.onNativeAdLoaded();
                    }
                    reportBiddingResult(nativeAd);
                }

                @Override
                public void onRenderSuccess(NativeExpressADView adView) {
                    Log.d(TAG, "原生广告渲染成功");
                    if (nativeAdListener != null) {
                        nativeAdListener.onNativeAdRenderSuccess();
                    }
                }

                @Override
                public void onRenderFail(NativeExpressADView adView) {
                    Log.e(TAG, "原生广告渲染失败");
                    if (nativeAdListener != null) {
                        nativeAdListener.onNativeAdRenderFail();
                    }
                }

                @Override
                public void onADExposure(NativeExpressADView nativeExpressADView) {
                    Log.d(TAG, "原生广告曝光回调");
                    if (nativeAdListener != null) {
                        nativeAdListener.onNativeAdExposure();
                    }
                }

                @Override
                public void onADClicked(NativeExpressADView nativeExpressADView) {
                    Log.d(TAG, "原生广告点击回调");
                    if (nativeAdListener != null) {
                        nativeAdListener.onNativeAdClicked();
                    }
                }

                @Override
                public void onADClosed(NativeExpressADView nativeExpressADView) {
                    Log.d(TAG, "原生广告关闭回调");
                    if (container != null && nativeExpressADView.getParent() == container) {
                        container.removeView(nativeExpressADView);
                    }
                    nativeExpressADView.destroy();
                }

                @Override
                public void onADLeftApplication(NativeExpressADView nativeExpressADView) {
                    Log.d(TAG, "用户离开应用回调");
                }

                @Override
                public void onNoAD(AdError adError) {
                    Log.e(TAG, "原生广告加载失败: code:[ " + adError.getErrorCode() + " ]desc:[ " + adError.getErrorMsg() + " ]");
                    if (nativeAdListener != null) {
                        nativeAdListener.onNativeAdFailedToShow(adError.getErrorMsg());
                    }
                    reportBiddingNoAd(nativeAd);
                    try {
                        BiddingC2SUtils.reportAdError(nativeAd, adError);
                    } catch (Exception e) {
                        Log.e(TAG, "调用BiddingC2SUtils.reportAdError异常: " + e.getMessage());
                    }
                    
                    String errorCode = String.valueOf(adError.getErrorCode());
                    if ("4001".equals(errorCode)) {
                        Log.e(TAG, "优量汇4001错误：广告请求参数错误，请检查配置");
                    } else {
                        scheduleNativeRetry(activity, container);
                    }
                }
            };

            nativeAd = new NativeExpressAD(activity, new ADSize(adjustedWidth, height), NATIVE_PLACEMENT_ID, adListener);
            nativeAd.loadAD(1);

        } catch (Exception e) {
            Log.e(TAG, "原生广告加载异常: " + e.getMessage());
        }
    }

    /**
     * 检查广告是否准备就绪
     */
    public boolean isAdReady(String placementId) {
        switch (placementId) {
            case SPLASH_PLACEMENT_ID:
                return splashAd != null && splashAd.isValid();
            case BANNER_PLACEMENT_ID:
                return bannerView != null;
            case INTERSTITIAL_PLACEMENT_ID:
                return interstitialAd != null && interstitialAd.isValid();
            case REWARD_PLACEMENT_ID:
                return rewardVideoAd != null && rewardVideoAd.isValid();
            case NATIVE_PLACEMENT_ID:
                return nativeAd != null;
            default:
                return false;
        }
    }

    /**
     * 激励视频广告监听器接口
     */
    public interface RewardAdListener {
        void onRewardAdLoaded();
        void onRewardAdFailedToShow();
        void onRewardAdRewarded();
        void onRewardAdClosed();
        void onRewardAdStarted(); // 新增方法：广告开始播放
    }

    /**
     * 横幅广告监听器接口
     */
    public interface BannerAdListener {
        void onBannerAdLoaded();
        void onBannerAdFailedToShow(String errorMsg);
        void onBannerAdExposure();
        void onBannerAdClicked();
        void onBannerAdClosed();
    }

    /**
     * 插屏广告监听器接口
     */
    public interface InterstitialAdListener {
        void onInterstitialAdLoaded();
        void onInterstitialAdFailedToShow(String errorMsg);
        void onInterstitialAdShow();
        void onInterstitialAdExposure();
        void onInterstitialAdClicked();
        void onInterstitialAdClosed();
    }

    /**
     * 原生广告监听器接口
     */
    public interface NativeAdListener {
        void onNativeAdLoaded();
        void onNativeAdFailedToShow(String errorMsg);
        void onNativeAdExposure();
        void onNativeAdClicked();
        void onNativeAdRenderSuccess();
        void onNativeAdRenderFail();
    }

    /**
     * 竞价结果上报类型常量定义
     */
    public static final int REPORT_BIDDING_DISABLE = -1;
    public static final int REPORT_BIDDING_WIN = 0;
    public static final int REPORT_BIDDING_LOSS_LOW_PRICE = 1;
    public static final int REPORT_BIDDING_LOSS_NO_AD = 2;
    public static final int REPORT_BIDDING_LOSS_NOT_COMPETITION = 3;
    public static final int REPORT_BIDDING_LOSS_OTHER = 4;

    // 添加成员变量用于存储原生广告视图
    private NativeExpressADView nativeAdView = null;

    /**
     * 发送广告eCPM数据到服务器
     */
    public void sendAdEcpmData(String adType, String positionId, String ecpmLevel) {
        try {
            // 从SharedPreference获取用户ID
            String userId = SharedPreferenceUtil.getString(MyApplication.getInstance(), "user_id", "");
            if (userId.isEmpty()) {
                Log.w(TAG, "用户ID为空，无法发送eCPM数据（用户未登录）");
                return;
            }
            
            // 创建参数Map
            Map<String, String> params = new HashMap<>();
              
            // 添加基本参数
            params.put("user_id", userId);
            params.put("open_id", userId);
            params.put("ad_type", adType);
            params.put("position_id", positionId);
            params.put("ecpm_level", ecpmLevel);
            params.put("ad_network_name", "gdt"); // 优量汇
            params.put("platform", "android");
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            
            // 记录日志，即使eCPM等级为空也记录广告展示情况
            Log.d(TAG, "准备发送广告eCPM数据: adType=" + adType + ", positionId=" + positionId + ", ecpmLevel=" + ecpmLevel + ", userId=" + userId);
              
            // 调用ApiManager发送数据
            ApiManager.getInstance().uploadAdEcpm(params);
            Log.d(TAG, "已发送广告eCPM数据: adType=" + adType + ", positionId=" + positionId);
        } catch (Exception e) {
            Log.e(TAG, "发送广告eCPM数据异常: " + e.getMessage());
        }
    }
    
    /**
     * 处理广告eCPM数据
     */
    public void handleAdEcpm(String placementId) {
        String adType = "unknown";
        String ecpmLevel = "";
        
        // 根据广告位ID确定广告类型和获取eCPM等级
        switch (placementId) {
            case SPLASH_PLACEMENT_ID:
                adType = "splash";
                ecpmLevel = getSplashAdECPMLevel();
                break;
            case BANNER_PLACEMENT_ID:
                adType = "banner";
                ecpmLevel = getBannerAdECPMLevel();
                break;
            case INTERSTITIAL_PLACEMENT_ID:
                adType = "interstitial";
                ecpmLevel = getInterstitialAdECPMLevel();
                break;
            case REWARD_PLACEMENT_ID:
                adType = "reward_video";
                ecpmLevel = getRewardAdECPMLevel();
                break;
            case NATIVE_PLACEMENT_ID:
                adType = "native";
                ecpmLevel = getNativeAdECPMLevel();
                break;
            default:
                Log.w(TAG, "未知的广告位ID: " + placementId);
                return;
        }
        
        // 发送eCPM数据
        sendAdEcpmData(adType, placementId, ecpmLevel);
    }
    
    /**
     * 销毁广告资源
     */
    public void destroy() {
        if (splashAd != null) {
            splashAd = null;
        }

        if (bannerHandler != null) {
            bannerHandler.removeCallbacksAndMessages(null);
            bannerHandler = null;
        }
        if (bannerView != null) {
            bannerView.destroy();
            bannerView = null;
        }

        if (interstitialHandler != null) {
            interstitialHandler.removeCallbacksAndMessages(null);
            interstitialHandler = null;
        }
        if (interstitialAd != null) {
            interstitialAd = null;
        }

        if (rewardVideoAd != null) {
            rewardVideoAd = null;
        }

        if (nativeAd != null) {
            nativeAd = null;
        }
        
        if (nativeHandler != null) {
            nativeHandler.removeCallbacksAndMessages(null);
            nativeHandler = null;
        }

        rewardAdListener = null;
        splashAdDismissCallback = null;
        bannerAdListener = null;
        interstitialAdListener = null;
        nativeAdListener = null;
        bannerRetryCount = 0;
        lastBannerLoadTime = 0;
        interstitialRetryCount = 0;
        lastInterstitialLoadTime = 0;
        nativeRetryCount = 0;
        lastNativeLoadTime = 0;
    }
}
