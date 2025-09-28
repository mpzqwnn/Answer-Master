package com.fortunequizking.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;

import com.anythink.nativead.api.ATNativeEventListener;
import com.anythink.nativead.api.ATNative;
import com.anythink.banner.api.ATBannerView;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATAdStatusInfo;
import com.anythink.core.api.AdError;
import com.anythink.interstitial.api.ATInterstitial;
import com.anythink.interstitial.api.ATInterstitialListener;
import com.anythink.rewardvideo.api.ATRewardVideoAd;
import com.anythink.rewardvideo.api.ATRewardVideoListener;
import com.anythink.nativead.api.ATNativeNetworkListener;
import com.anythink.banner.api.ATBannerExListener;
import com.anythink.core.api.ATShowConfig;
import com.anythink.core.api.ATAdRevenueListener;
import com.anythink.core.api.ATNetworkConfirmInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import android.view.Gravity;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fortunequizking.api.ApiManager;
import com.fortunequizking.util.SharedPreferenceUtil;
import com.fortunequizking.MyApplication;

public class TakuAdManager {
    private static final String TAG = "TakuAdManager";

    // 广告位ID常量定义
    private static final String SPLASH_PLACEMENT_ID = "YOUR_SPLASH_PLACEMENT_ID";
    private static final String BANNER_PLACEMENT_ID = "b68bab76437dbf";
    private static final String INTERSTITIAL_PLACEMENT_ID = "b68bab861be9c9";
    private static final String REWARD_PLACEMENT_ID = "b68bab75709b8f";
    private static final String NATIVE_PLACEMENT_ID = "b68bab735815a1";

    // 广告刷新间隔常量（毫秒）
    private static final long MIN_BANNER_REFRESH_INTERVAL = 10000; // 30秒
    private static final long MIN_INTERSTITIAL_REFRESH_INTERVAL = 10000; // 10秒
    private static final long MIN_NATIVE_REFRESH_INTERVAL = 10000; // 45秒

    // 重试延迟基数（毫秒）
    private static final long RETRY_BASE_DELAY = 10000; // 10秒
    private static final long MAX_RETRY_DELAY = 120000; // 120秒
    
    // 横幅广告尺寸常量（dp）
    private static final int BANNER_WIDTH_DP = -1; // -1表示MATCH_PARENT
    
    // 横幅广告场景ID和展示自定义扩展参数
    private static final String BANNER_SCENARIO = "banner_ad_show_1";
    private static final String BANNER_SHOW_CUSTOM_EXT = "banner_ad_show_custom_ext";

    // 单例模式
    private static TakuAdManager instance;

    // 广告实例
    private ATBannerView bannerView;
    private ATInterstitial interstitialAd;
    private ATRewardVideoAd rewardVideoAd;
    private ATNative nativeAd;
    
    // 存储最后一个广告容器的引用，解决广告加载成功后无法显示的问题
    private ViewGroup lastBannerContainer;
    
    // 获取横幅广告视图实例的方法
    public ATBannerView getBannerView() {
        return bannerView;
    }

    // 监听器
    private BannerAdListener bannerAdListener;
    private InterstitialAdListener interstitialAdListener;
    private RewardAdListener rewardAdListener;
    private NativeAdListener nativeAdListener;
    
    // 广告收益监听器实现类
    private class AdRevenueListenerImpl implements ATAdRevenueListener {
        @Override
        public void onAdRevenuePaid(ATAdInfo atAdInfo) {
            // 可以在这里处理广告收益相关逻辑
            Log.d(TAG, "广告收益数据: " + atAdInfo.toString());
        }
    }
    


    // 状态变量
    private boolean isInitialized = false;
    private String userId;
    private boolean isDebugMode = false;

    // 广告加载状态
    private boolean isBannerLoading = false;
    private boolean isInterstitialLoading = false;
    private boolean isNativeLoading = false;
    private boolean isRewardAdPlaying = false;

    // 重试计数
    private int interstitialRetryCount = 0;
    private int nativeRetryCount = 0;

    // 上次加载时间
    private long lastBannerLoadTime = 0;
    private long lastInterstitialLoadTime = 0;
    private long lastNativeLoadTime = 0;

    // 重试任务
    private Runnable interstitialRetryRunnable;
    private Runnable nativeRetryRunnable;

    // Handler
    private Handler interstitialHandler;
    private Handler nativeHandler;

    // APP_ID和APP_KEY从AndroidManifest中获取
    // private static final String APP_ID = "a68bab61c2bd06";
    private static final String APP_ID = "a68afadf78e295";
    // private static final String APP_KEY = "a0e037782e35324e11f95d5f1133b396e";
    private static final String APP_KEY = "a3d0f2d8f2e8d08709d1bb74e20edb3e7";

    // 私有构造函数
    private TakuAdManager() {
        // 初始化Handler
        interstitialHandler = new Handler(Looper.getMainLooper());
        nativeHandler = new Handler(Looper.getMainLooper());
    }

    // 获取单例实例
    public static synchronized TakuAdManager getInstance() {
        if (instance == null) {
            instance = new TakuAdManager();
        }
        return instance;
    }

    /**
     * 初始化广告SDK
     */
    public void init(Context context) {
        try {
            if (isInitialized) {
                Log.d(TAG, "Taku SDK已初始化");
                return;
            }

            // 修改初始化方式，使用正确的API
            com.anythink.core.api.ATSDK.init(context, APP_ID, APP_KEY);
            isInitialized = true;

            Log.d(TAG, "Taku SDK初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "Taku SDK初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 设置用户ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
        try {
            // 当前SDK版本没有setUserData方法
            Log.d(TAG, "设置用户ID: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "设置用户ID异常: " + e.getMessage(), e);
        }
    }

    /**
     * 设置调试模式
     */
    public void setDebugMode(boolean isDebugMode) {
        this.isDebugMode = isDebugMode;
        try {
            // 当前SDK版本可能没有setDebugLog方法
            Log.d(TAG, "设置调试模式: " + isDebugMode);
        } catch (Exception e) {
            Log.e(TAG, "设置调试模式异常: " + e.getMessage(), e);
        }
    }

    /**
     * 当前SDK版本暂不支持开屏广告功能
     */
    public void showSplashAd(Activity activity) {
        Log.w(TAG, "当前SDK版本暂不支持开屏广告功能");
    }

    // ------------------------------ 横幅广告相关方法 ------------------------------
    
    // 横幅广告配置常量
    private static final long BANNER_MIN_LOAD_INTERVAL = 60000; // 最小加载间隔：60秒
    private static final int BANNER_HEIGHT_DP = 60; // 横幅高度：60dp
    private static final int MAX_RETRY_COUNT = 3; // 最大重试次数
    private static final long RETRY_INTERVAL_MS = 8000; // 重试间隔：8秒
    
    // 广告容器引用
    private ViewGroup bannerContainer;
    private Activity bannerActivity;
    
    // 横幅广告状态
    private boolean isBannerAdShowing = false;
    private int bannerRetryCount = 0;
    
    /**
     * 显示横幅广告
     * 全新实现，简化流程，提高稳定性
     * 
     * @param activity 活动上下文
     * @param container 广告容器
     */
    public void showBannerAd(Activity activity, ViewGroup container) {
        Log.d(TAG, "请求显示横幅广告");
        
        // 1. 基础参数验证
        if (!validateAdContext(activity, container)) {
            return;
        }
        
        // 2. 保存上下文引用
        this.bannerActivity = activity;
        this.bannerContainer = container;
        
        // 3. 初始化广告视图
        initBannerAd();
        
        // 4. 尝试立即加载和显示广告
        loadAndShowBannerAd();
    }
    
    /**
     * 隐藏横幅广告
     * 安全地移除广告视图并清理资源
     */
    public void hideBannerAd() {
        Log.d(TAG, "隐藏横幅广告");
        
        // 标记状态
        isBannerAdShowing = false;
        
        // 移除广告视图
        removeBannerView();
        
        // 清理引用
        bannerContainer = null;
    }
    
    /**
     * 初始化横幅广告视图
     * 全新实现，确保正确配置广告参数
     */
    private void initBannerAd() {
        Log.d(TAG, "初始化横幅广告");
        
        // 检查是否已初始化
        if (bannerView != null) {
            return;
        }
        
        try {
            // 创建广告视图实例
            bannerView = new ATBannerView(bannerActivity);
            bannerView.setPlacementId(BANNER_PLACEMENT_ID);
            
            // 设置广告尺寸
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = dpToPixel(BANNER_HEIGHT_DP);
            
            // 配置广告参数
            configureBannerAd(width, height);
            
            // 设置监听器
            setupBannerAdCallbacks();
            
            Log.d(TAG, "横幅广告初始化成功，尺寸: " + width + "x" + height);
            
        } catch (Exception e) {
            Log.e(TAG, "横幅广告初始化失败: " + e.getMessage(), e);
            bannerView = null;
            notifyBannerError("初始化失败", e.getMessage());
        }
    }
    
    /**
     * 配置横幅广告参数
     */
    private void configureBannerAd(int width, int height) {
        if (bannerView == null || bannerActivity == null) {
            return;
        }
        
        // 设置本地额外参数
        Map<String, Object> localParams = new HashMap<>();
        localParams.put(com.anythink.core.api.ATAdConst.KEY.AD_WIDTH, width);
        localParams.put(com.anythink.core.api.ATAdConst.KEY.AD_HEIGHT, height);
        bannerView.setLocalExtra(localParams);
        
        // 设置布局参数
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);
        bannerView.setLayoutParams(layoutParams);
        
        // 设置广告场景
        ATShowConfig showConfig = new ATShowConfig.Builder()
                .scenarioId("main_banner")
                .showCustomExt("{\"source\":\"main_screen\"}")
                .build();
        bannerView.setShowConfig(showConfig);
        
        // 设置收益监听器
        bannerView.setAdRevenueListener(new AdRevenueListenerImpl());
    }
    
    /**
     * 设置横幅广告回调监听器
     * 全新实现，简化回调处理逻辑
     */
    private void setupBannerAdCallbacks() {
        if (bannerView == null) {
            return;
        }
        
        bannerView.setBannerAdListener(new ATBannerExListener() {
            @Override
            public void onBannerLoaded() {
                Log.d(TAG, "横幅广告加载成功");
                isBannerLoading = false;
                bannerRetryCount = 0;
                lastBannerLoadTime = System.currentTimeMillis();
                
                // 广告加载成功后，如果需要显示则立即添加到容器
                if (isBannerAdShowing && bannerContainer != null) {
                    addBannerToContainer();
                }
                
                // 通知外部监听器
                if (bannerAdListener != null) {
                    bannerAdListener.onBannerAdLoaded();
                }
            }
            
            @Override
            public void onBannerFailed(AdError adError) {
                String errorInfo = adError != null ? adError.getFullErrorInfo() : "未知错误";
                Log.e(TAG, "横幅广告加载失败: " + errorInfo);
                isBannerLoading = false;
                
                // 通知错误
                notifyBannerError("加载失败", errorInfo);
                
                // 处理重试逻辑
                handleBannerRetry();
            }
            
            @Override
            public void onBannerClicked(ATAdInfo adInfo) {
                Log.d(TAG, "横幅广告点击");
                if (bannerAdListener != null) {
                    bannerAdListener.onBannerAdClicked();
                }
            }
            
            @Override
            public void onBannerShow(ATAdInfo adInfo) {
                Log.d(TAG, "横幅广告展示");
                if (bannerAdListener != null) {
                    bannerAdListener.onBannerAdExposure();
                }
                
                // 处理eCPM数据
                handleAdEcpm(BANNER_PLACEMENT_ID, adInfo);
            }
            
            @Override
            public void onBannerClose(ATAdInfo adInfo) {
                Log.d(TAG, "横幅广告关闭");
                
                // 移除广告视图
                removeBannerView();
                
                // 通知外部监听器
                if (bannerAdListener != null) {
                    bannerAdListener.onBannerAdClosed();
                }
            }
            
            // 以下是SDK要求实现的其他方法，保持空实现以避免不必要的操作
            @Override
            public void onDeeplinkCallback(boolean isRefresh, ATAdInfo adInfo, boolean isSuccess) {}
            
            @Override
            public void onDownloadConfirm(Context context, ATAdInfo adInfo, ATNetworkConfirmInfo networkConfirmInfo) {}
            
            @Override
            public void onBannerAutoRefreshed(ATAdInfo adInfo) {}
            
            @Override
            public void onBannerAutoRefreshFail(AdError adError) {}
        });
    }
    
    /**
     * 加载并显示横幅广告
     * 整合加载和显示逻辑
     */
    private void loadAndShowBannerAd() {
        if (bannerView == null || bannerActivity == null) {
            Log.w(TAG, "广告视图或活动上下文为空，无法加载广告");
            return;
        }
        
        // 标记为显示中
        isBannerAdShowing = true;
        
        // 检查广告是否已就绪
        if (isBannerAdReady()) {
            // 广告已就绪，直接显示
            addBannerToContainer();
        } else {
            // 广告未就绪，触发加载
            triggerBannerLoad();
        }
    }
    
    /**
     * 检查横幅广告是否就绪
     */
    private boolean isBannerAdReady() {
        try {
            if (bannerView == null) {
                return false;
            }
            
            ATAdStatusInfo statusInfo = bannerView.checkAdStatus();
            return statusInfo != null && statusInfo.isReady();
        } catch (Exception e) {
            Log.w(TAG, "检查广告状态异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 触发横幅广告加载
     * 包含频率控制逻辑
     */
    private void triggerBannerLoad() {
        // 避免重复加载
        if (isBannerLoading) {
            Log.d(TAG, "横幅广告正在加载中，避免重复请求");
            return;
        }
        
        // 频率控制检查
        if (!checkLoadFrequency()) {
            return;
        }
        
        // 执行加载
        try {
            isBannerLoading = true;
            bannerView.loadAd();
            Log.d(TAG, "已触发横幅广告加载请求");
        } catch (Exception e) {
            Log.e(TAG, "加载广告异常: " + e.getMessage(), e);
            isBannerLoading = false;
            notifyBannerError("加载异常", e.getMessage());
            handleBannerRetry();
        }
    }
    
    /**
     * 检查加载频率
     */
    private boolean checkLoadFrequency() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastBannerLoadTime;
        
        if (elapsedTime < BANNER_MIN_LOAD_INTERVAL && lastBannerLoadTime > 0) {
            long remainingTime = BANNER_MIN_LOAD_INTERVAL - elapsedTime;
            Log.d(TAG, "未达到最小加载间隔，还需等待" + (remainingTime / 1000) + "秒");
            
            // 安排延迟加载
            scheduleDelayedLoad(remainingTime);
            return false;
        }
        
        return true;
    }
    
    /**
     * 安排延迟加载
     */
    private void scheduleDelayedLoad(long delayMs) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isBannerAdShowing) {
                    triggerBannerLoad();
                }
            }
        }, delayMs);
    }
    
    /**
     * 处理横幅广告重试逻辑
     */
    private void handleBannerRetry() {
        if (bannerRetryCount < MAX_RETRY_COUNT) {
            bannerRetryCount++;
            long retryDelay = RETRY_INTERVAL_MS * bannerRetryCount;
            Log.d(TAG, "安排横幅广告重试，第" + bannerRetryCount + "次，延迟" + retryDelay + "ms");
            
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isBannerAdShowing) {
                        triggerBannerLoad();
                    }
                }
            }, retryDelay);
        } else {
            Log.w(TAG, "已达到最大重试次数，停止重试");
            bannerRetryCount = 0;
        }
    }
    
    /**
     * 将横幅广告添加到容器
     */
    private void addBannerToContainer() {
        if (bannerView == null || bannerContainer == null || bannerActivity == null) {
            Log.w(TAG, "无法添加广告到容器：缺少必要的引用");
            return;
        }
        
        try {
            // 确保在UI线程操作
            if (Looper.myLooper() != Looper.getMainLooper()) {
                bannerActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addBannerToContainer();
                    }
                });
                return;
            }
            
            // 先移除已有的广告视图
            removeBannerView();
            
            // 添加广告视图到容器
            bannerContainer.addView(bannerView);
            bannerView.setVisibility(View.VISIBLE);
            
            Log.d(TAG, "横幅广告已成功添加到容器");
            
        } catch (Exception e) {
            Log.e(TAG, "添加广告到容器异常: " + e.getMessage(), e);
            notifyBannerError("显示失败", e.getMessage());
        }
    }
    
    /**
     * 移除横幅广告视图
     */
    private void removeBannerView() {
        if (bannerView == null) {
            return;
        }
        
        try {
            // 确保在UI线程操作
            if (Looper.myLooper() != Looper.getMainLooper() && bannerActivity != null) {
                bannerActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeBannerView();
                    }
                });
                return;
            }
            
            // 从父容器中移除
            if (bannerView.getParent() instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) bannerView.getParent();
                parent.removeView(bannerView);
                Log.d(TAG, "横幅广告已从父容器移除");
            }
            
        } catch (Exception e) {
            Log.w(TAG, "移除广告视图异常: " + e.getMessage());
        }
    }
    
    /**
     * 验证广告上下文是否有效
     */
    private boolean validateAdContext(Activity activity, ViewGroup container) {
        // 检查Activity状态
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            Log.w(TAG, "Activity状态无效，不显示广告");
            notifyBannerError("上下文无效", "Activity状态不正确");
            return false;
        }
        
        // 检查容器状态
        if (container == null || !container.isAttachedToWindow()) {
            Log.w(TAG, "广告容器无效或未附加到窗口，不显示广告");
            notifyBannerError("容器无效", "广告容器不可用");
            return false;
        }
        
        return true;
    }
    
    /**
     * 通知横幅广告错误
     */
    private void notifyBannerError(String errorType, String errorMessage) {
        if (bannerAdListener != null) {
            try {
                bannerAdListener.onBannerAdFailedToShow(errorType + ": " + errorMessage);
            } catch (Exception e) {
                Log.e(TAG, "通知广告错误异常: " + e.getMessage());
            }
        }
    }
    
    /**
     * dp转像素
     */
    private int dpToPixel(int dpValue) {
        if (bannerActivity == null) {
            return dpValue;
        }
        
        final float scale = bannerActivity.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 预加载插屏广告
     */
    public void preloadInterstitialAd(Activity activity) {
        if (interstitialAd == null) {
            initInterstitialAd(activity);
        }

        try {
            ATAdStatusInfo statusInfo = interstitialAd.checkAdStatus();
            if (!statusInfo.isReady()) {
                Log.d(TAG, "插屏广告未就绪，开始加载");
                loadInterstitialAdWithFrequencyControl(activity);
            }

        } catch (Exception e) {
            Log.e(TAG, "插屏广告加载异常: " + e.getMessage(), e);
            if (interstitialAdListener != null) {
                interstitialAdListener.onInterstitialAdFailedToShow(e.getMessage());
            }
            // 安排重试
            scheduleInterstitialRetry(activity);
        }
    }

    /**
     * 显示插屏广告
     */
    public void showInterstitialAd(Activity activity) {
        if (interstitialAd == null) {
            initInterstitialAd(activity);
        }

        try {
            ATAdStatusInfo statusInfo = interstitialAd.checkAdStatus();
            if (statusInfo.isReady()) {
                interstitialAd.show(activity);
                Log.d(TAG, "展示插屏广告");
            } else {
                Log.d(TAG, "插屏广告未就绪，开始加载");
                loadInterstitialAdWithFrequencyControl(activity);
                if (interstitialAdListener != null) {
                    interstitialAdListener.onInterstitialAdFailedToShow("广告未就绪");
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "插屏广告显示异常: " + e.getMessage(), e);
            if (interstitialAdListener != null) {
                interstitialAdListener.onInterstitialAdFailedToShow(e.getMessage());
            }
            // 安排重试
            scheduleInterstitialRetry(activity);
        }
    }

    /**
     * 初始化插屏广告
     */
    private void initInterstitialAd(Activity activity) {
        try {
            interstitialAd = new ATInterstitial(activity, INTERSTITIAL_PLACEMENT_ID);
            interstitialAd.setAdListener(new com.anythink.interstitial.api.ATInterstitialListener() {
                // 移除所有@Override注解
                // 修改方法名为正确的onInterstitialAdLoaded
                public void onInterstitialAdLoaded() {
                    Log.d(TAG, "插屏广告加载成功");
                    isInterstitialLoading = false;
                    lastInterstitialLoadTime = System.currentTimeMillis();
                    interstitialRetryCount = 0;

                    if (interstitialAdListener != null) {
                        interstitialAdListener.onInterstitialAdLoaded();
                    }
                }

                public void onInterstitialAdLoadFail(AdError adError) {
                    Log.e(TAG, "插屏广告加载失败: " + adError.getFullErrorInfo());
                    isInterstitialLoading = false;

                    if (interstitialAdListener != null) {
                        interstitialAdListener.onInterstitialAdFailedToShow(adError.getFullErrorInfo());
                    }

                    // 处理重试逻辑
                    if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                        scheduleInterstitialRetry(activity);
                    }
                }

                public void onInterstitialAdShow(ATAdInfo atAdInfo) {
                    Log.d(TAG, "插屏广告展示");
                    if (interstitialAdListener != null) {
                        interstitialAdListener.onInterstitialAdShow();
                    }
                    // 处理eCPM数据
                    handleAdEcpm(INTERSTITIAL_PLACEMENT_ID, atAdInfo);
                }

                // 检查方法名是否正确
                public void onInterstitialAdClicked(ATAdInfo atAdInfo) {
                    Log.d(TAG, "插屏广告点击");
                    if (interstitialAdListener != null) {
                        interstitialAdListener.onInterstitialAdClicked();
                    }
                }

                public void onInterstitialAdExposure(ATAdInfo atAdInfo) {
                    Log.d(TAG, "插屏广告曝光");
                    if (interstitialAdListener != null) {
                        interstitialAdListener.onInterstitialAdExposure();
                    }
                }

                public void onInterstitialAdClose(ATAdInfo atAdInfo) {
                    Log.d(TAG, "插屏广告关闭");
                    if (interstitialAdListener != null) {
                        interstitialAdListener.onInterstitialAdClosed();
                    }
                }

                public void onInterstitialAdVideoStart(ATAdInfo atAdInfo) {
                    Log.d(TAG, "插屏广告视频开始播放");
                }

                public void onInterstitialAdVideoEnd(ATAdInfo atAdInfo) {
                    Log.d(TAG, "插屏广告视频播放结束");
                }

                // 调整方法参数顺序
                public void onInterstitialAdVideoError(AdError adError) {
                    Log.e(TAG, "插屏广告视频播放错误: " + adError.getFullErrorInfo());
                }
            });

            // 初始化时不自动加载，等待showInterstitialAd调用时再加载
            // loadInterstitialAdWithFrequencyControl(activity);

        } catch (Exception e) {
            Log.e(TAG, "插屏广告初始化异常: " + e.getMessage(), e);
        }
    }

    /**
     * 带频率控制的插屏广告加载方法
     */
    public void loadInterstitialAdWithFrequencyControl(Activity activity) {
        if (isInterstitialLoading) {
            Log.d(TAG, "插屏广告正在加载中，避免重复请求");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastInterstitialLoadTime;

        if (elapsedTime >= MIN_INTERSTITIAL_REFRESH_INTERVAL || lastInterstitialLoadTime == 0) {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && interstitialAd != null) {
                isInterstitialLoading = true;
                interstitialAd.load();
                Log.d(TAG, "开始加载插屏广告");
            }
        } else {
            long delayTime = MIN_INTERSTITIAL_REFRESH_INTERVAL - elapsedTime;
            Log.d(TAG, "未达到最小加载间隔，延迟 " + delayTime + "ms 后加载");
            interstitialHandler.postDelayed(() -> {
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && interstitialAd != null) {
                    isInterstitialLoading = true;
                    interstitialAd.load();
                    Log.d(TAG, "延迟后开始加载插屏广告");
                }
            }, delayTime);
        }
    }

    /**
     * 安排插屏广告重试加载
     */
    private void scheduleInterstitialRetry(Activity activity) {
        interstitialRetryCount++;
        long retryDelay = Math.min(RETRY_BASE_DELAY * (long) Math.pow(2, interstitialRetryCount - 1), MAX_RETRY_DELAY);

        Log.d(TAG, "安排插屏广告重试，第" + interstitialRetryCount + "次，延迟 " + retryDelay + "ms");

        interstitialRetryRunnable = () -> {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && interstitialAd != null) {
                loadInterstitialAdWithFrequencyControl(activity);
            }
        };

        interstitialHandler.postDelayed(interstitialRetryRunnable, retryDelay);
    }

    /**
     * 预加载激励视频广告
     */
    public void preloadRewardVideoAd(Activity activity) {
        try {
            if (rewardVideoAd == null) {
                rewardVideoAd = new ATRewardVideoAd(activity, REWARD_PLACEMENT_ID);
                rewardVideoAd.setAdListener(new com.anythink.rewardvideo.api.ATRewardVideoListener() {
                    // 移除所有@Override注解
                    public void onRewardedVideoAdLoaded() {
                        Log.d(TAG, "激励视频广告加载成功");
                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdLoaded();
                        }
                    }

                    // 添加缺失的onRewardedVideoAdFailed方法
                    public void onRewardedVideoAdFailed(AdError adError) {
                        Log.e(TAG, "激励视频广告失败: " + adError.getFullErrorInfo());
                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdFailedToShow();
                        }
                    }

                    public void onRewardedVideoAdLoadFail(AdError adError) {
                        Log.e(TAG, "激励视频广告加载失败: " + adError.getFullErrorInfo());
                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdFailedToShow();
                        }
                    }

                    public void onRewardedVideoAdPlayStart(ATAdInfo atAdInfo) {
                        Log.d(TAG, "激励视频广告展示");
                        isRewardAdPlaying = true;
                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdStarted();
                        }
                        // 处理eCPM数据
                        handleAdEcpm(REWARD_PLACEMENT_ID, atAdInfo);
                    }

                    public void onRewardedVideoAdPlayEnd(ATAdInfo atAdInfo) {
                        Log.d(TAG, "激励视频广告播放结束");
                    }

                    public void onRewardedVideoAdClosed(ATAdInfo atAdInfo) {
                        Log.d(TAG, "激励视频广告关闭");
                        isRewardAdPlaying = false;
                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdClosed();
                        }
                    }

                    // 调整方法参数顺序
                    public void onRewardedVideoAdPlayFailed(AdError adError, ATAdInfo atAdInfo) {
                        Log.e(TAG, "激励视频广告播放失败: " + adError.getFullErrorInfo());
                    }

                    // 检查onReward方法是否存在于当前SDK版本中
                    public void onReward(ATAdInfo atAdInfo) {
                        Log.d(TAG, "激励视频广告奖励发放");
                        if (rewardAdListener != null) {
                            rewardAdListener.onRewardAdRewarded();
                        }
                    }

                    // 确保实现onRewardedVideoAdPlayClicked方法
                    public void onRewardedVideoAdPlayClicked(ATAdInfo atAdInfo) {
                        Log.d(TAG, "激励视频广告点击");
                    }
                });
            }

            // 使用反射方式尝试加载激励视频广告
            try {
                // 尝试通过反射调用可能存在的加载方法
                java.lang.reflect.Method loadMethod = rewardVideoAd.getClass().getMethod("load");
                loadMethod.invoke(rewardVideoAd);
                Log.d(TAG, "通过反射调用load方法开始加载激励视频广告");
            } catch (Exception e1) {
                try {
                    java.lang.reflect.Method loadAdMethod = rewardVideoAd.getClass().getMethod("loadAd");
                    loadAdMethod.invoke(rewardVideoAd);
                    Log.d(TAG, "通过反射调用loadAd方法开始加载激励视频广告");
                } catch (Exception e2) {
                    Log.e(TAG, "无法找到激励视频广告合适的加载方法: " + e2.getMessage());
                    if (rewardAdListener != null) {
                        rewardAdListener.onRewardAdFailedToShow();
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "激励视频广告预加载异常: " + e.getMessage(), e);
        }
    }

    /**
     * 显示激励视频广告
     */
    public void showRewardAd(Activity activity, RewardAdListener listener) {
        this.rewardAdListener = listener;
        try {
            if (rewardVideoAd == null) {
                preloadRewardVideoAd(activity);
                Log.d(TAG, "激励视频广告实例未初始化，正在初始化并预加载");
                if (listener != null) {
                    listener.onRewardAdFailedToShow();
                }
                return;
            }

            ATAdStatusInfo statusInfo = rewardVideoAd.checkAdStatus();
            if (statusInfo.isReady()) {
                rewardVideoAd.show(activity);
                Log.d(TAG, "展示激励视频广告");
            } else {
                Log.d(TAG, "激励视频广告未就绪，开始加载");
                preloadRewardVideoAd(activity);
                if (listener != null) {
                    listener.onRewardAdFailedToShow();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "激励视频广告加载异常: " + e.getMessage(), e);
            if (listener != null) {
                listener.onRewardAdFailedToShow();
            }
        }
    }

    /**
     * 显示原生广告
     */
    public void showNativeAd(Activity activity, ViewGroup container) {
        try {
            if (nativeAd == null) {
                // 使用正确的监听器类型
                nativeAd = new ATNative(activity, NATIVE_PLACEMENT_ID,
                        new com.anythink.nativead.api.ATNativeNetworkListener() {
                            @Override
                            public void onNativeAdLoadFail(AdError adError) {
                                Log.e(TAG, "原生广告加载失败: " + adError.getFullErrorInfo());
                                isNativeLoading = false;

                                if (nativeAdListener != null) {
                                    nativeAdListener.onNativeAdFailedToShow(adError.getFullErrorInfo());
                                }

                                // 处理重试逻辑
                                if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                                    scheduleNativeRetry(activity, container);
                                }
                            }

                            @Override
                            public void onNativeAdLoaded() {
                                Log.d(TAG, "原生广告加载成功");
                                isNativeLoading = false;
                                lastNativeLoadTime = System.currentTimeMillis();
                                nativeRetryCount = 0;

                                // 广告加载成功后，确保将广告视图添加到容器中
                                if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && container != null) {
                                    try {
                                        renderNativeAdToContainer(activity, container);
                                    } catch (Exception e) {
                                        Log.e(TAG, "添加原生广告视图到容器异常: " + e.getMessage(), e);
                                    }
                                }

                                if (nativeAdListener != null) {
                                    nativeAdListener.onNativeAdLoaded();
                                }
                            }
                        });
            }

            // 确保广告容器不为空
            if (container != null) {
                // 先清空容器，避免旧广告残留
                container.removeAllViews();
                
                // 检查广告是否就绪，如果就绪直接显示
                if (isAdReady(NATIVE_PLACEMENT_ID)) {
                    try {
                        renderNativeAdToContainer(activity, container);
                    } catch (Exception e) {
                        Log.e(TAG, "直接显示原生广告异常: " + e.getMessage(), e);
                    }
                } else {
                    // 广告未就绪，进行加载
                    loadNativeAdWithFrequencyControl(activity);
                }
            } else {
                Log.e(TAG, "原生广告容器为空，无法显示广告");
            }

        } catch (Exception e) {
            Log.e(TAG, "原生广告显示异常: " + e.getMessage(), e);
            if (nativeAdListener != null) {
                nativeAdListener.onNativeAdFailedToShow(e.getMessage());
            }
        }
    }

    /**
     * 将原生广告渲染到容器中
     */
    private void renderNativeAdToContainer(Activity activity, ViewGroup container) {
        try {
            // 清空容器
            container.removeAllViews();
            
            // 获取NativeAd对象
            com.anythink.nativead.api.NativeAd nativeAdObj = null;
            try {
                // 根据原生广告文档，直接使用getNativeAd()方法
                nativeAdObj = nativeAd.getNativeAd();
                Log.d(TAG, "成功获取NativeAd对象: " + (nativeAdObj != null ? nativeAdObj.getClass().getSimpleName() : "null"));
            } catch (Exception e) {
                Log.e(TAG, "获取NativeAd对象失败: " + e.getMessage());
            }
            
            if (nativeAdObj != null) {
                // 创建ATNativeAdView
                com.anythink.nativead.api.ATNativeAdView nativeAdView = new com.anythink.nativead.api.ATNativeAdView(activity);
                Log.d(TAG, "成功创建ATNativeAdView");
                
                // 改进宽高计算逻辑，确保广告占满容器
                int width = 0;
                if (container.getWidth() > 0) {
                    width = container.getWidth();
                } else {
                    // 如果容器宽度未测量，使用屏幕宽度并考虑边距
                    width = activity.getResources().getDisplayMetrics().widthPixels;
                    // 减去可能的边距
                    int margin = (int) (activity.getResources().getDisplayMetrics().density * 16); // 16dp边距
                    width = Math.max(0, width - margin * 2);
                }
                int height = (int) (width / 2.0f); // 设置合适的高度比例
                Log.d(TAG, "设置广告宽高: 宽=" + width + ", 高=" + height);
                
                // 设置布局参数，使用MATCH_PARENT确保占满容器
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                nativeAdView.setLayoutParams(params);
                Log.d(TAG, "已设置ATNativeAdView布局参数");
                
                // 为内部类引用创建final临时变量
                final com.anythink.nativead.api.NativeAd finalNativeAdObj = nativeAdObj;
                final com.anythink.nativead.api.ATNativeAdView finalNativeAdView = nativeAdView;
                
                // 判断是否为模板渲染类型
                boolean isExpress = false;
                try {
                    isExpress = nativeAdObj.isNativeExpress();
                    Log.d(TAG, "广告类型: " + (isExpress ? "模板渲染" : "自渲染"));
                } catch (Exception e) {
                    Log.w(TAG, "获取广告渲染类型失败，默认为自渲染: " + e.getMessage());
                }
                
                if (isExpress) {
                    try {
                        // 模板渲染，selfRenderView传null
                        nativeAdObj.renderAdContainer(nativeAdView, null);
                        Log.d(TAG, "已调用模板渲染renderAdContainer");
                        
                        // 配置广告点击事件等
                        com.anythink.nativead.api.ATNativePrepareInfo prepareInfo = new com.anythink.nativead.api.ATNativePrepareInfo();
                        nativeAdObj.prepare(nativeAdView, prepareInfo);
                        Log.d(TAG, "已调用模板渲染prepare");
                        
                        // 添加到容器
                        container.addView(nativeAdView);
                        Log.d(TAG, "模板渲染广告视图已添加到容器");
                    } catch (Exception e) {
                        Log.e(TAG, "模板渲染广告失败: " + e.getMessage(), e);
                        addDefaultAdView(activity, container, "广告加载中...");
                    }
                } else {
                    try {
                        // 自渲染，创建自定义视图
                        View selfRenderView = createSelfRenderView(activity);
                        if (selfRenderView != null) {
                            Log.d(TAG, "成功创建自渲染视图");
                            
                            // 设置自渲染视图的宽高
                            selfRenderView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
                            
                            // 渲染广告容器
                            nativeAdObj.renderAdContainer(nativeAdView, selfRenderView);
                            Log.d(TAG, "已调用自渲染renderAdContainer");
                            
                            // 配置广告点击事件等
                            com.anythink.nativead.api.ATNativePrepareInfo prepareInfo = new com.anythink.nativead.api.ATNativePrepareInfo();
                            nativeAdObj.prepare(nativeAdView, prepareInfo);
                            Log.d(TAG, "已调用自渲染prepare");
                            
                            // 添加到容器
                            container.addView(nativeAdView);
                            Log.d(TAG, "自渲染广告视图已添加到容器");
                            
                            // 绑定广告素材
                            bindAdMaterialToView(nativeAdObj, selfRenderView);
                        } else {
                            Log.e(TAG, "创建自渲染视图失败");
                            addDefaultAdView(activity, container, "广告内容准备中...");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "自渲染广告失败: " + e.getMessage(), e);
                        addDefaultAdView(activity, container, "广告加载失败");
                    }
                }
                
                // 设置广告事件监听
                nativeAdObj.setNativeEventListener(new com.anythink.nativead.api.ATNativeEventListener() {
                    @Override
                    public void onAdImpressed(com.anythink.nativead.api.ATNativeAdView view, ATAdInfo atAdInfo) {
                        Log.d(TAG, "原生广告曝光回调");
                        if (nativeAdListener != null) {
                            nativeAdListener.onNativeAdExposure();
                        }
                        // 处理并上传原生广告的eCPM数据
                        handleAdEcpm(NATIVE_PLACEMENT_ID, atAdInfo);
                    }

                    @Override
                    public void onAdClicked(com.anythink.nativead.api.ATNativeAdView view, ATAdInfo atAdInfo) {
                        Log.d(TAG, "原生广告点击回调");
                        if (nativeAdListener != null) {
                            nativeAdListener.onNativeAdClicked();
                        }
                    }

                    @Override
                    public void onAdVideoStart(com.anythink.nativead.api.ATNativeAdView view) {
                        Log.d(TAG, "原生广告视频开始播放");
                    }

                    @Override
                    public void onAdVideoEnd(com.anythink.nativead.api.ATNativeAdView view) {
                        Log.d(TAG, "原生广告视频播放结束");
                    }

                    @Override
                    public void onAdVideoProgress(com.anythink.nativead.api.ATNativeAdView view, int progress) {
                        Log.d(TAG, "原生广告视频播放进度: " + progress);
                    }
                });
                
                // 设置广告关闭事件回调
                nativeAdObj.setDislikeCallbackListener(new com.anythink.nativead.api.ATNativeDislikeListener() {
                    @Override
                    public void onAdCloseButtonClick(com.anythink.nativead.api.ATNativeAdView view, ATAdInfo atAdInfo) {
                        Log.d(TAG, "原生广告关闭按钮点击");
                        if (view != null && view.getParent() instanceof ViewGroup) {
                            ((ViewGroup) view.getParent()).removeView(view);
                        }
                        // 清理广告资源
                        finalNativeAdObj.clear(view);
                    }
                });
                
                Log.d(TAG, "原生广告视图已添加到容器");
                
                if (nativeAdListener != null) {
                    nativeAdListener.onNativeAdRenderSuccess();
                }
            } else {
                Log.e(TAG, "获取NativeAd对象失败");
                if (nativeAdListener != null) {
                    nativeAdListener.onNativeAdRenderFail();
                }
                
                // 添加默认提示信息
                TextView errorText = new TextView(activity);
                errorText.setText("广告内容准备中...");
                errorText.setTextSize(14);
                errorText.setTextColor(Color.GRAY);
                errorText.setPadding(0, 20, 0, 20);
                errorText.setGravity(Gravity.CENTER);
                container.addView(errorText);
            }
        } catch (Exception e) {
            Log.e(TAG, "渲染原生广告异常: " + e.getMessage(), e);
            if (nativeAdListener != null) {
                nativeAdListener.onNativeAdRenderFail();
            }
        }
    }

    /**
     * 创建自渲染视图
     */
    private View createSelfRenderView(Context context) {
        try {
            LinearLayout adContainer = new LinearLayout(context);
            adContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            adContainer.setOrientation(LinearLayout.VERTICAL);
            adContainer.setPadding(10, 10, 10, 10);
            adContainer.setBackgroundColor(Color.parseColor("#F5F5F5"));
            
            // 添加"广告"标签
            TextView adLabel = new TextView(context);
            adLabel.setText("广告");
            adLabel.setTextSize(10);
            adLabel.setTextColor(Color.WHITE);
            adLabel.setBackgroundColor(Color.parseColor("#FF6B6B"));
            adLabel.setPadding(4, 1, 4, 1);
            adLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            adContainer.addView(adLabel);
            
            // 创建广告标题
            TextView titleView = new TextView(context);
            titleView.setId(View.generateViewId());
            titleView.setTextSize(16);
            titleView.setTextColor(Color.BLACK);
            titleView.setPadding(0, 8, 0, 4);
            adContainer.addView(titleView);
            
            // 创建广告描述
            TextView descView = new TextView(context);
            descView.setId(View.generateViewId());
            descView.setTextSize(14);
            descView.setTextColor(Color.GRAY);
            descView.setPadding(0, 0, 0, 8);
            adContainer.addView(descView);
            
            // 创建"查看详情"按钮
            TextView actionBtn = new TextView(context);
            actionBtn.setId(View.generateViewId());
            actionBtn.setText("查看详情");
            actionBtn.setTextSize(14);
            actionBtn.setTextColor(Color.WHITE);
            actionBtn.setBackgroundColor(Color.parseColor("#4CAF50"));
            actionBtn.setPadding(16, 8, 16, 8);
            actionBtn.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.gravity = Gravity.END;
            actionBtn.setLayoutParams(btnParams);
            adContainer.addView(actionBtn);
            
            return adContainer;
        } catch (Exception e) {
            Log.e(TAG, "创建自渲染视图失败: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 添加默认的广告提示视图
     */
    private void addDefaultAdView(Activity activity, ViewGroup container, String text) {
        try {
            // 清空容器
            container.removeAllViews();
            
            LinearLayout defaultView = new LinearLayout(activity);
            defaultView.setOrientation(LinearLayout.VERTICAL);
            defaultView.setGravity(Gravity.CENTER);
            defaultView.setBackgroundColor(Color.parseColor("#F5F5F5"));
            
            // 设置宽高
            int width = container.getWidth() > 0 ? container.getWidth() : activity.getResources().getDisplayMetrics().widthPixels;
            int height = (int) (width / 2.0f);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
            defaultView.setLayoutParams(params);
            
            // 添加广告标签
            TextView adLabel = new TextView(activity);
            adLabel.setText("广告");
            adLabel.setTextSize(10);
            adLabel.setTextColor(Color.WHITE);
            adLabel.setBackgroundColor(Color.parseColor("#FF6B6B"));
            adLabel.setPadding(4, 1, 4, 1);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.gravity = Gravity.CENTER_HORIZONTAL;
            adLabel.setLayoutParams(labelParams);
            
            // 添加提示文本
            TextView hintText = new TextView(activity);
            hintText.setText(text);
            hintText.setTextSize(14);
            hintText.setTextColor(Color.GRAY);
            hintText.setPadding(0, 20, 0, 20);
            hintText.setGravity(Gravity.CENTER);
            
            defaultView.addView(adLabel);
            defaultView.addView(hintText);
            
            container.addView(defaultView);
            Log.d(TAG, "已添加默认广告提示视图: " + text);
        } catch (Exception e) {
            Log.e(TAG, "添加默认广告提示视图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将广告素材绑定到视图
     */
    private void bindAdMaterialToView(com.anythink.nativead.api.NativeAd nativeAd, View selfRenderView) {
        try {
            if (nativeAd == null || !(selfRenderView instanceof ViewGroup)) {
                return;
            }
            
            ViewGroup container = (ViewGroup) selfRenderView;
            TextView titleView = null;
            TextView descView = null;
            TextView actionBtn = null;
            
            // 查找视图
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                if (child instanceof TextView) {
                    if (titleView == null && ((TextView) child).getTextSize() == 16 * container.getResources().getDisplayMetrics().scaledDensity) {
                        titleView = (TextView) child;
                    } else if (descView == null && ((TextView) child).getTextSize() == 14 * container.getResources().getDisplayMetrics().scaledDensity &&
                            ((TextView) child).getCurrentTextColor() == Color.GRAY) {
                        descView = (TextView) child;
                    } else if (actionBtn == null && "查看详情".equals(((TextView) child).getText().toString())) {
                        actionBtn = (TextView) child;
                    }
                }
            }
            
            // 填充广告素材
            if (titleView != null) {
                try {
                    // 使用反射方式获取标题
                    String title = null;
                    try {
                        Method getTitleMethod = nativeAd.getClass().getMethod("getTitle");
                        title = (String) getTitleMethod.invoke(nativeAd);
                    } catch (Exception e) {
                        // 尝试备选方法
                        try {
                            Method getAdTitleMethod = nativeAd.getClass().getMethod("getAdTitle");
                            title = (String) getAdTitleMethod.invoke(nativeAd);
                        } catch (Exception ex) {
                            Log.e(TAG, "获取广告标题失败: " + ex.getMessage());
                        }
                    }
                    
                    if (title != null && !title.isEmpty()) {
                        titleView.setText(title);
                    } else {
                        titleView.setText("精彩内容推荐");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "获取广告标题失败: " + e.getMessage());
                    titleView.setText("精彩内容推荐");
                }
            }
            
            if (descView != null) {
                try {
                    // 使用反射方式获取描述
                    String description = null;
                    try {
                        Method getDescMethod = nativeAd.getClass().getMethod("getDescription");
                        description = (String) getDescMethod.invoke(nativeAd);
                    } catch (Exception e) {
                        // 尝试备选方法
                        try {
                            Method getAdDescMethod = nativeAd.getClass().getMethod("getAdDescription");
                            description = (String) getAdDescMethod.invoke(nativeAd);
                        } catch (Exception ex) {
                            Log.e(TAG, "获取广告描述失败: " + ex.getMessage());
                        }
                    }
                    
                    if (description != null && !description.isEmpty()) {
                        descView.setText(description);
                    } else {
                        descView.setText("点击查看更多详情");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "获取广告描述失败: " + e.getMessage());
                    descView.setText("点击查看更多详情");
                }
            }
            
            // 为按钮添加点击事件
            if (actionBtn != null) {
                actionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            // 使用反射方式调用handleClick方法，避免参数不匹配问题
                            try {
                                Method handleClickMethod = nativeAd.getClass().getMethod("handleClick", View.class);
                                handleClickMethod.invoke(nativeAd, v);
                            } catch (Exception e) {
                                // 尝试两个参数的版本
                                try {
                                    // 获取ATNativeAdView
                                    Object nativeAdView = null;
                                    try {
                                        if (v.getContext() != null) {
                                            nativeAdView = Class.forName("com.anythink.nativead.api.ATNativeAdView").getConstructor(Context.class).newInstance(v.getContext());
                                        }
                                    } catch (Exception ex) {
                                        Log.e(TAG, "创建ATNativeAdView失败: " + ex.getMessage());
                                    }
                                    
                                    if (nativeAdView != null) {
                                        Method handleClickMethod = nativeAd.getClass().getMethod("handleClick", nativeAdView.getClass(), View.class);
                                        handleClickMethod.invoke(nativeAd, nativeAdView, v);
                                    }
                                } catch (Exception ex) {
                                    Log.e(TAG, "广告点击处理异常: " + ex.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "广告点击处理异常: " + e.getMessage());
                        }
                    }
                });
            }
            
            Log.d(TAG, "广告素材绑定完成");
        } catch (Exception e) {
            Log.e(TAG, "绑定广告素材失败: " + e.getMessage(), e);
        }
    }

    /**
     * 原生广告渲染器（为了保持兼容性保留）
     */
    private class ATNativeAdRenderer {
        private Activity activity;
        
        public ATNativeAdRenderer(Activity activity) {
            this.activity = activity;
        }
        
        public View createAdView(Context context, ViewGroup parent) {
            return createSelfRenderView(context);
        }
        
        public void renderAdView(View adView, ATNative nativeAd) {
            try {
                // 清空容器
                if (adView instanceof ViewGroup) {
                    ((ViewGroup)adView).removeAllViews();
                }
                
                // 添加"广告"标签
                TextView adLabel = new TextView(adView.getContext());
                adLabel.setText("广告");
                adLabel.setTextSize(10);
                adLabel.setTextColor(Color.WHITE);
                adLabel.setBackgroundColor(Color.parseColor("#FF6B6B"));
                adLabel.setPadding(4, 1, 4, 1);
                adLabel.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, 
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                if (adView instanceof ViewGroup) {
                    ((ViewGroup)adView).addView(adLabel);
                }
                
                // 获取NativeAd对象
                com.anythink.nativead.api.NativeAd nativeAdObj = null;
                try {
                    nativeAdObj = nativeAd.getNativeAd();
                } catch (Exception e) {
                    Log.e(TAG, "获取NativeAd对象失败: " + e.getMessage());
                }
                
                // 创建广告标题
                TextView titleView = new TextView(adView.getContext());
                titleView.setId(View.generateViewId());
                titleView.setTextSize(16);
                titleView.setTextColor(Color.BLACK);
                titleView.setPadding(0, 8, 0, 4);
                if (adView instanceof ViewGroup) {
                    ((ViewGroup)adView).addView(titleView);
                }
                
                // 创建广告描述
                TextView descView = new TextView(adView.getContext());
                descView.setId(View.generateViewId());
                descView.setTextSize(14);
                descView.setTextColor(Color.GRAY);
                descView.setPadding(0, 0, 0, 8);
                if (adView instanceof ViewGroup) {
                    ((ViewGroup)adView).addView(descView);
                }
                
                // 创建"查看详情"按钮
                TextView actionBtn = new TextView(adView.getContext());
                actionBtn.setId(View.generateViewId());
                actionBtn.setText("查看详情");
                actionBtn.setTextSize(14);
                actionBtn.setTextColor(Color.WHITE);
                actionBtn.setBackgroundColor(Color.parseColor("#4CAF50"));
                actionBtn.setPadding(16, 8, 16, 8);
                actionBtn.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, 
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                btnParams.gravity = Gravity.END;
                actionBtn.setLayoutParams(btnParams);
                
                // 创建final副本，以便在内部类中使用
                final com.anythink.nativead.api.NativeAd finalNativeAdObj = nativeAdObj;
                final View finalAdView = adView;
                
                // 为按钮添加点击事件
                actionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (finalNativeAdObj != null) {
                                // 使用反射方式调用handleClick方法，避免参数不匹配问题
                                try {
                                    Method handleClickMethod = finalNativeAdObj.getClass().getMethod("handleClick", View.class);
                                    handleClickMethod.invoke(finalNativeAdObj, v);
                                } catch (Exception e) {
                                    // 尝试两个参数的版本
                                    try {
                                        // 获取ATNativeAdView
                                        Object nativeAdView = null;
                                        try {
                                            nativeAdView = Class.forName("com.anythink.nativead.api.ATNativeAdView").getConstructor(Context.class).newInstance(activity);
                                        } catch (Exception ex) {
                                            Log.e(TAG, "创建ATNativeAdView失败: " + ex.getMessage());
                                        }
                                        
                                        if (nativeAdView != null) {
                                            Method handleClickMethod = finalNativeAdObj.getClass().getMethod("handleClick", nativeAdView.getClass(), View.class);
                                            handleClickMethod.invoke(finalNativeAdObj, nativeAdView, v);
                                        }
                                    } catch (Exception ex) {
                                        Log.e(TAG, "广告点击处理异常: " + ex.getMessage());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "广告点击处理异常: " + e.getMessage());
                        }
                    }
                });
                
                if (adView instanceof ViewGroup) {
                    ((ViewGroup)adView).addView(actionBtn);
                }
                
                // 尝试填充广告素材
                if (nativeAdObj != null) {
                    try {
                        // 使用反射方式获取标题
                        String title = null;
                        try {
                            Method getTitleMethod = nativeAdObj.getClass().getMethod("getTitle");
                            title = (String) getTitleMethod.invoke(nativeAdObj);
                        } catch (Exception e) {
                            // 尝试备选方法
                            try {
                                Method getAdTitleMethod = nativeAdObj.getClass().getMethod("getAdTitle");
                                title = (String) getAdTitleMethod.invoke(nativeAdObj);
                            } catch (Exception ex) {
                                Log.e(TAG, "获取广告标题失败: " + ex.getMessage());
                            }
                        }
                        
                        if (title != null && !title.isEmpty()) {
                            titleView.setText(title);
                        } else {
                            titleView.setText("精彩内容推荐");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "获取广告标题失败: " + e.getMessage());
                        titleView.setText("精彩内容推荐");
                    }
                    
                    try {
                        // 使用反射方式获取描述
                        String description = null;
                        try {
                            Method getDescMethod = nativeAdObj.getClass().getMethod("getDescription");
                            description = (String) getDescMethod.invoke(nativeAdObj);
                        } catch (Exception e) {
                            // 尝试备选方法
                            try {
                                Method getAdDescMethod = nativeAdObj.getClass().getMethod("getAdDescription");
                                description = (String) getAdDescMethod.invoke(nativeAdObj);
                            } catch (Exception ex) {
                                Log.e(TAG, "获取广告描述失败: " + ex.getMessage());
                            }
                        }
                        
                        if (description != null && !description.isEmpty()) {
                            descView.setText(description);
                        } else {
                            descView.setText("点击查看更多详情");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "获取广告描述失败: " + e.getMessage());
                        descView.setText("点击查看更多详情");
                    }
                } else {
                    titleView.setText("精彩内容推荐");
                    descView.setText("点击查看更多详情");
                }
            } catch (Exception e) {
                Log.e(TAG, "原生广告渲染异常: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 带频率控制的原生广告加载方法
     */
    public void loadNativeAdWithFrequencyControl(Activity activity) {
        if (isNativeLoading) {
            Log.d(TAG, "原生广告正在加载中，避免重复请求");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastNativeLoadTime;

        if (elapsedTime >= MIN_NATIVE_REFRESH_INTERVAL || lastNativeLoadTime == 0) {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && nativeAd != null) {
                isNativeLoading = true;
                lastNativeLoadTime = currentTime;
                try {
                    // 设置本地参数
                    Map<String, Object> localMap = new HashMap<>();
                    // 获取屏幕宽度，并考虑可能的边距
                    int width = activity.getResources().getDisplayMetrics().widthPixels;
                    int margin = (int) (activity.getResources().getDisplayMetrics().density * 16); // 16dp边距
                    width = Math.max(0, width - margin * 2);
                    int height = (int) (width / 2.0f); // 设置合适的高度比例
                    localMap.put(com.anythink.core.api.ATAdConst.KEY.AD_WIDTH, width);
                    localMap.put(com.anythink.core.api.ATAdConst.KEY.AD_HEIGHT, height);
                    nativeAd.setLocalExtra(localMap);
                    
                    // 调用makeAdRequest方法加载原生广告
                    nativeAd.makeAdRequest();
                    Log.d(TAG, "开始加载原生广告");
                } catch (Exception e) {
                    Log.e(TAG, "原生广告加载异常: " + e.getMessage(), e);
                    isNativeLoading = false;
                    if (nativeAdListener != null) {
                        nativeAdListener.onNativeAdFailedToShow(e.getMessage());
                    }
                    // 安排重试
                    scheduleNativeRetry(activity, null);
                }
            }
        } else {
            long delayTime = MIN_NATIVE_REFRESH_INTERVAL - elapsedTime;
            Log.d(TAG, "未达到最小加载间隔，延迟 " + delayTime + "ms 后加载");
            nativeHandler.postDelayed(() -> {
                if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && nativeAd != null) {
                    isNativeLoading = true;
                    lastNativeLoadTime = System.currentTimeMillis();
                    try {
                        // 设置本地参数
                        Map<String, Object> localMap = new HashMap<>();
                        // 获取屏幕宽度，并考虑可能的边距
                        int width = activity.getResources().getDisplayMetrics().widthPixels;
                        int margin = (int) (activity.getResources().getDisplayMetrics().density * 16); // 16dp边距
                        width = Math.max(0, width - margin * 2);
                        int height = (int) (width / 2.0f); // 设置合适的高度比例
                        localMap.put(com.anythink.core.api.ATAdConst.KEY.AD_WIDTH, width);
                        localMap.put(com.anythink.core.api.ATAdConst.KEY.AD_HEIGHT, height);
                        nativeAd.setLocalExtra(localMap);
                        
                        // 调用makeAdRequest方法加载原生广告
                        nativeAd.makeAdRequest();
                        Log.d(TAG, "延迟后开始加载原生广告");
                    } catch (Exception e) {
                        Log.e(TAG, "延迟后原生广告加载异常: " + e.getMessage(), e);
                        isNativeLoading = false;
                        if (nativeAdListener != null) {
                            nativeAdListener.onNativeAdFailedToShow(e.getMessage());
                        }
                        // 安排重试
                        scheduleNativeRetry(activity, null);
                    }
                }
            }, delayTime);
        }
    }

    /**
     * 安排原生广告重试加载
     */
    private void scheduleNativeRetry(Activity activity, ViewGroup container) {
        nativeRetryCount++;
        long retryDelay = Math.min(RETRY_BASE_DELAY * (long) Math.pow(2, nativeRetryCount - 1), MAX_RETRY_DELAY);

        Log.d(TAG, "安排原生广告重试，第" + nativeRetryCount + "次，延迟 " + retryDelay + "ms");

        nativeRetryRunnable = () -> {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed() && nativeAd != null) {
                loadNativeAdWithFrequencyControl(activity);
            }
        };

        nativeHandler.postDelayed(nativeRetryRunnable, retryDelay);
    }

    /**
     * 检查广告是否就绪
     */
    public boolean isAdReady(String placementId) {
        try {
            if (placementId.equals(BANNER_PLACEMENT_ID) && bannerView != null) {
                // 使用checkAdStatus方法检查横幅广告状态
                ATAdStatusInfo statusInfo = bannerView.checkAdStatus();
                return statusInfo.isReady();
            } else if (placementId.equals(INTERSTITIAL_PLACEMENT_ID) && interstitialAd != null) {
                ATAdStatusInfo statusInfo = interstitialAd.checkAdStatus();
                return statusInfo.isReady();
            } else if (placementId.equals(REWARD_PLACEMENT_ID) && rewardVideoAd != null) {
                ATAdStatusInfo statusInfo = rewardVideoAd.checkAdStatus();
                return statusInfo.isReady();
            } else if (placementId.equals(NATIVE_PLACEMENT_ID) && nativeAd != null) {
                ATAdStatusInfo statusInfo = nativeAd.checkAdStatus();
                return statusInfo.isReady();
            }
        } catch (Exception e) {
            Log.e(TAG, "检查广告状态异常: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * 设置横幅广告监听器
     */
    public void setBannerAdListener(BannerAdListener bannerAdListener) {
        this.bannerAdListener = bannerAdListener;
    }

    /**
     * 设置插屏广告监听器
     */
    public void setInterstitialAdListener(InterstitialAdListener interstitialAdListener) {
        this.interstitialAdListener = interstitialAdListener;
    }

    /**
     * 设置激励视频广告监听器
     */
    public void setRewardAdListener(RewardAdListener rewardAdListener) {
        this.rewardAdListener = rewardAdListener;
    }

    /**
     * 设置原生广告监听器
     */
    public void setNativeAdListener(NativeAdListener nativeAdListener) {
        this.nativeAdListener = nativeAdListener;
    }

    /**
     * 销毁所有广告资源
     */
    public void destroy() {
        try {
            if (interstitialHandler != null) {
                if (interstitialRetryRunnable != null) {
                    interstitialHandler.removeCallbacks(interstitialRetryRunnable);
                }
                interstitialHandler = null;
            }

            if (nativeHandler != null) {
                if (nativeRetryRunnable != null) {
                    nativeHandler.removeCallbacks(nativeRetryRunnable);
                }
                nativeHandler = null;
            }

            // 释放广告资源
            if (bannerView != null) {
                try {
                    // 移除视图的父容器引用
                    if (bannerView.getParent() instanceof ViewGroup) {
                        ((ViewGroup) bannerView.getParent()).removeView(bannerView);
                    }
                    // 调用destroy方法销毁横幅广告资源
                    bannerView.destroy();
                } catch (Exception e) {
                    Log.w(TAG, "移除横幅广告视图异常: " + e.getMessage());
                }
                bannerView = null;
            }

            // 对于插屏广告，设置为null即可释放
            interstitialAd = null;

            // 对于激励视频广告，设置为null即可释放
            rewardVideoAd = null;

            // 对于原生广告，设置为null即可释放
            nativeAd = null;

            // 清空监听器
            bannerAdListener = null;
            interstitialAdListener = null;
            rewardAdListener = null;
            nativeAdListener = null;

            Log.d(TAG, "Taku广告资源已释放");
        } catch (Exception e) {
            Log.e(TAG, "Taku广告资源释放异常: " + e.getMessage(), e);
        }
    }

    /**
     * 重置插屏广告加载状态
     * 用于解决广告加载卡住的问题
     */
    public void resetInterstitialLoadingState() {
        synchronized (this) {
            isInterstitialLoading = false;
            interstitialRetryCount = 0;
            lastInterstitialLoadTime = 0;
            Log.d(TAG, "插屏广告加载状态已重置");
        }
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
     * 激励视频广告监听器接口
     */
    public interface RewardAdListener {
        void onRewardAdLoaded();

        void onRewardAdFailedToShow();

        void onRewardAdRewarded();

        void onRewardAdClosed();

        void onRewardAdStarted();
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
     * 处理广告eCPM数据
     */
    private void handleAdEcpm(String placementId, ATAdInfo atAdInfo) {
        try {
            String adType = getAdTypeByPlacementId(placementId);
            String ecpmValue = "0";
            String networkName = "";
            
            // 直接从ATAdInfo对象获取eCPM值和network_name
            if (atAdInfo != null) {
                double ecpm = atAdInfo.getEcpm();
                ecpmValue = String.valueOf(ecpm);
                
                // 尝试获取network_name
                try {
                    // 由于ATAdInfo类没有直接提供getNetworkName方法，我们使用反射获取
                    Method getNetworkNameMethod = atAdInfo.getClass().getMethod("getNetworkName");
                    Object networkNameObj = getNetworkNameMethod.invoke(atAdInfo);
                    if (networkNameObj != null) {
                        networkName = networkNameObj.toString();
                    }
                } catch (Exception e) {
                    // 如果反射获取失败，尝试获取原始数据中的network_name
                    try {
                        Method getJsonObjectMethod = atAdInfo.getClass().getMethod("getJsonObject");
                        Object jsonObj = getJsonObjectMethod.invoke(atAdInfo);
                        if (jsonObj != null && jsonObj instanceof Map) {
                            Map<String, Object> jsonMap = (Map<String, Object>) jsonObj;
                            if (jsonMap.containsKey("network_name")) {
                                Object networkNameObj = jsonMap.get("network_name");
                                if (networkNameObj != null) {
                                    networkName = networkNameObj.toString();
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, "无法获取network_name: " + ex.getMessage());
                    }
                }
                
                Log.d(TAG, "从ATAdInfo获取eCPM成功: " + ecpmValue + ", currency: " + atAdInfo.getCurrency() + ", precision: " + atAdInfo.getEcpmPrecision() + ", network_name: " + networkName);
            } else {
                // 如果ATAdInfo为空，回退到原来的反射方式获取
                Log.w(TAG, "ATAdInfo为空，回退到反射方式获取eCPM");
                if (adType.equals("splash")) {
                    ecpmValue = getSplashAdECPMLevel(placementId);
                } else if (adType.equals("banner")) {
                    ecpmValue = getBannerAdECPMLevel(placementId);
                } else if (adType.equals("interstitial")) {
                    ecpmValue = getInterstitialAdECPMLevel(placementId);
                } else if (adType.equals("reward")) {
                    ecpmValue = getRewardAdECPMLevel(placementId);
                } else if (adType.equals("native")) {
                    ecpmValue = getNativeAdECPMLevel(placementId);
                }
            }
            
            // 发送eCPM数据
            sendAdEcpmData(adType, placementId, ecpmValue, networkName);
        } catch (Exception e) {
            Log.e(TAG, "处理广告eCPM数据异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 兼容旧的调用方式
     */
    private void handleAdEcpm(String placementId) {
        handleAdEcpm(placementId, null);
    }

    /**
     * 根据广告位ID获取广告类型
     */
    private String getAdTypeByPlacementId(String placementId) {
        if (placementId.equals(SPLASH_PLACEMENT_ID)) {
            return "splash";
        } else if (placementId.equals(BANNER_PLACEMENT_ID)) {
            return "banner";
        } else if (placementId.equals(INTERSTITIAL_PLACEMENT_ID)) {
            return "interstitial";
        } else if (placementId.equals(REWARD_PLACEMENT_ID)) {
            return "reward";
        } else if (placementId.equals(NATIVE_PLACEMENT_ID)) {
            return "native";
        }
        return "unknown";
    }

    /**
     * 获取开屏广告eCPM等级
     */
    private String getSplashAdECPMLevel(String placementId) {
        try {
            // 当前SDK版本暂不支持开屏广告功能
            return "0";
        } catch (Exception e) {
            Log.e(TAG, "获取开屏广告eCPM等级异常: " + e.getMessage());
            return "0";
        }
    }

    /**
     * 获取横幅广告eCPM等级（保持兼容性）
     */
    private String getBannerAdECPMLevel(String placementId) {
        try {
            if (bannerView != null) {
                ATAdStatusInfo statusInfo = bannerView.checkAdStatus();
                if (statusInfo != null) {
                    // 简化的eCPM获取逻辑
                    try {
                        // 优先尝试直接获取
                        Method getEcpmMethod = statusInfo.getClass().getMethod("getEcpm");
                        Object ecpm = getEcpmMethod.invoke(statusInfo);
                        if (ecpm != null) {
                            return String.valueOf(ecpm);
                        }
                    } catch (NoSuchMethodException e) {
                        // 备选方案：使用反射获取loadInfoList
                        try {
                            Method method = statusInfo.getClass().getMethod("getLoadInfoList");
                            Object result = method.invoke(statusInfo);
                            if (result instanceof List && !((List) result).isEmpty()) {
                                Object adInfoObj = ((List) result).get(0);
                                if (adInfoObj instanceof Map) {
                                    Map<String, Object> adInfo = (Map<String, Object>) adInfoObj;
                                    if (adInfo.containsKey("ecpm")) {
                                        Object ecpmObj = adInfo.get("ecpm");
                                        if (ecpmObj != null) {
                                            return String.valueOf(ecpmObj);
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Log.w(TAG, "获取eCPM数据失败: " + ex.getMessage());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "获取横幅广告eCPM异常: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取横幅广告eCPM等级异常: " + e.getMessage());
        }
        return "0";
    }

    /**
     * 获取插屏广告eCPM等级
     */
    private String getInterstitialAdECPMLevel(String placementId) {
        try {
            if (interstitialAd != null) {
                ATAdStatusInfo statusInfo = interstitialAd.checkAdStatus();
                // 使用反射尝试获取eCPM数据，避免直接调用不存在的方法
                if (statusInfo != null) {
                    try {
                        Method method = statusInfo.getClass().getMethod("getLoadInfoList");
                        Object result = method.invoke(statusInfo);
                        if (result instanceof List && !((List) result).isEmpty()) {
                            Object adInfoObj = ((List) result).get(0);
                            if (adInfoObj instanceof Map) {
                                Map<String, Object> adInfo = (Map<String, Object>) adInfoObj;
                                if (adInfo.containsKey("ecpm")) {
                                    Object ecpmObj = adInfo.get("ecpm");
                                    if (ecpmObj != null) {
                                        return String.valueOf(ecpmObj);
                                    }
                                }
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        // 方法不存在，正常返回默认值
                        Log.d(TAG, "ATAdStatusInfo类不存在getLoadInfoList()方法");
                    } catch (Exception e) {
                        Log.e(TAG, "反射获取插屏广告eCPM等级异常: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取插屏广告eCPM等级异常: " + e.getMessage());
        }
        return "0";
    }

    /**
     * 获取激励视频广告eCPM等级
     */
    private String getRewardAdECPMLevel(String placementId) {
        try {
            if (rewardVideoAd != null) {
                ATAdStatusInfo statusInfo = rewardVideoAd.checkAdStatus();
                // 使用反射尝试获取eCPM数据，避免直接调用不存在的方法
                if (statusInfo != null) {
                    try {
                        Method method = statusInfo.getClass().getMethod("getLoadInfoList");
                        Object result = method.invoke(statusInfo);
                        if (result instanceof List && !((List) result).isEmpty()) {
                            Object adInfoObj = ((List) result).get(0);
                            if (adInfoObj instanceof Map) {
                                Map<String, Object> adInfo = (Map<String, Object>) adInfoObj;
                                if (adInfo.containsKey("ecpm")) {
                                    Object ecpmObj = adInfo.get("ecpm");
                                    if (ecpmObj != null) {
                                        return String.valueOf(ecpmObj);
                                    }
                                }
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        // 方法不存在，正常返回默认值
                        Log.d(TAG, "ATAdStatusInfo类不存在getLoadInfoList()方法");
                    } catch (Exception e) {
                        Log.e(TAG, "反射获取激励视频广告eCPM等级异常: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取激励视频广告eCPM等级异常: " + e.getMessage());
        }
        return "0";
    }

    /**
     * 获取原生广告eCPM等级
     */
    private String getNativeAdECPMLevel(String placementId) {
        try {
            if (nativeAd != null) {
                ATAdStatusInfo statusInfo = nativeAd.checkAdStatus();
                // 使用反射尝试获取eCPM数据，避免直接调用不存在的方法
                if (statusInfo != null) {
                    try {
                        Method method = statusInfo.getClass().getMethod("getLoadInfoList");
                        Object result = method.invoke(statusInfo);
                        if (result instanceof List && !((List) result).isEmpty()) {
                            Object adInfoObj = ((List) result).get(0);
                            if (adInfoObj instanceof Map) {
                                Map<String, Object> adInfo = (Map<String, Object>) adInfoObj;
                                if (adInfo.containsKey("ecpm")) {
                                    Object ecpmObj = adInfo.get("ecpm");
                                    if (ecpmObj != null) {
                                        return String.valueOf(ecpmObj);
                                    }
                                }
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        // 方法不存在，正常返回默认值
                        Log.d(TAG, "ATAdStatusInfo类不存在getLoadInfoList()方法");
                    } catch (Exception e) {
                        Log.e(TAG, "反射获取原生广告eCPM等级异常: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取原生广告eCPM等级异常: " + e.getMessage());
        }
        return "0";
    }

    /**
     * 发送广告eCPM数据
     */
    private void sendAdEcpmData(String adType, String positionId, String ecpmValue, String networkName) {
        try {
            // 获取用户ID
            String userId = SharedPreferenceUtil.getString(MyApplication.getInstance(), "user_id", "");
            if (userId.isEmpty()) {
                Log.w(TAG, "用户ID为空，无法发送eCPM数据");
                return;
            }

            // 构建请求参数，使用正确的ecpm字段
            Map<String, String> params = new HashMap<>();
            params.put("user_id", userId);
            params.put("ad_type", adType);
            params.put("position_id", positionId);
            params.put("ecpm_level", ecpmValue); 
            params.put("ecpm", ecpmValue);  // 修改为正确的字段名
            
            // 如果network_name不为空，则添加到参数中
            if (!networkName.isEmpty()) {
                params.put("ad_network_name", networkName);
            }

            // 发送eCPM数据
            ApiManager.getInstance().uploadAdEcpm(params);
            Log.d(TAG, "发送广告eCPM数据成功: adType=" + adType + ", positionId=" + positionId + ", ecpm=" + ecpmValue + ", network_name=" + params.get("ad_network_name"));
        } catch (Exception e) {
            Log.e(TAG, "发送广告eCPM数据异常: " + e.getMessage(), e);
        }
    }
}