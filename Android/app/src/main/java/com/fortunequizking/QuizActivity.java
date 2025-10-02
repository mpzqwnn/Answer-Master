package com.fortunequizking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fortunequizking.api.ApiCallback;
import com.fortunequizking.api.ApiManager;
import com.fortunequizking.model.AnswerStats;
import com.fortunequizking.model.Question;
import com.fortunequizking.model.QuizHistoryRecord;
import com.fortunequizking.model.StaminaUpdateResult;
import com.fortunequizking.model.UserInfo;
import com.fortunequizking.util.SharedPreferenceUtil;
import com.fortunequizking.util.TakuAdManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";

    // 用于跟踪设置弹窗状态的变量
    private AlertDialog settingsDialog = null;
    private static final int REWARD_AD_REQUEST_CODE = 1001;
    private static final int SETTINGS_REQUEST_CODE = 1002;
    private static final int REFRESH_INTERVAL = 10000; // 10秒广告刷新间隔

    private ApiManager apiManager;
    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private TextView questionText;
    private Button option1Button, option2Button, option3Button, option4Button;
    private TextView livesText, staminaText, userNameText;
    private Button watchAdButton, levelButton, lives_button; // 添加lives_button声明
    private TextView musicNote;
    private MediaPlayer mediaPlayer;
    private boolean isMusicPlaying = false;
    private Timer adTimer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int correctAnswers = 0;
    private int totalAnswers = 0;
    private boolean isAdInitialized = false; // 广告是否已经初始化

    private TextView userIdText; // 添加用户ID文本引用
    private TextView userMobileText; // 添加用户手机号文本引用
    private ToggleButton musicButton; // 修改为ToggleButton类型
    private int currentScore = 0;
    private int currentLevel = 1;
    private List<Question> questions = new ArrayList<>();
    private CountDownTimer interstitialAdTimer;
    private boolean isTimerRunning = false;
    private long questionStartTime;
    // 在类的成员变量区域添加以下变量
    private static final int AD_COOLDOWN_TIME_NORMAL = 60000; // 正常用户1分钟倒计时
    private static final int AD_COOLDOWN_TIME_RISK = 180000; // 触发风控用户3分钟倒计时
    private static final int AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_1 = 180000; // 层级处理第一层3分钟倒计时
    private static final int AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_2 = 300000; // 层级处理第二层5分钟倒计时
    private int currentAdCooldownLevel = 0; // 当前广告冷却层级
    private CountDownTimer adCooldownTimer; // 广告冷却计时器
    private boolean isAdCooldownActive = false; // 广告冷却状态
    private long lastAdRewardTime = 0; // 上次获得奖励的时间
    // 添加体力相关变量
    private int currentStamina = 0; // 用户当前体力值
    // 广告刷新计时器
    private Handler bannerAdRefreshHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerAdRefreshRunnable;
    private Handler nativeAdRefreshHandler = new Handler(Looper.getMainLooper());
    private Runnable nativeAdRefreshRunnable;
    private TextView statsText; // 添加答题统计文本引用
    private boolean isRewardAdPlaying = false; // 跟踪激励广告是否正在播放
    private boolean isRiskCheck = false; // 风控检查状态标志

    private RelativeLayout loadingLayout; // 加载中布局
    private LinearLayout mainContentLayout; // 主内容布局
    private LinearLayout questionAreaLayout; // 题目区域布局
    private LinearLayout topUserInfoLayout; // 顶部用户信息布局
    private boolean riskControlTriggered = false; // 风控触发状态标志
    private boolean isInvitationDialogShown = false; // 邀约弹窗是否已显示过

    // 计时器暂停状态
    private boolean isGlobalTimerPaused = false; // 全局计时器暂停状态
    private long interstitialTimerRemaining = 0; // 插屏广告计时器剩余时间
    private long cooldownTimerRemaining = 0; // 广告冷却计时器剩余时间

    // 插屏广告相关变量
    private boolean isInterstitialAdLoaded = false; // 标记插屏广告是否已加载完成
    private boolean isLoadingInterstitialAd = false; // 标记是否正在加载插屏广告
    private long lastInterstitialAdShownTime = 0; // 上次显示插屏广告的时间戳
    private static final long MIN_INTERSTITIAL_AD_INTERVAL = 10000; // 最小广告显示间隔（毫秒）
    private CountDownTimer loadingTimer; // 加载中计时器，新用户登录后显示15秒
    private boolean isFirstLoading = true; // 标志变量：是否是第一次加载

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // 初始化API管理器
        apiManager = ApiManager.getInstance();

        // 初始化加载布局
        topUserInfoLayout = findViewById(R.id.top_user_info_layout);
        loadingLayout = findViewById(R.id.loading_layout);
        mainContentLayout = findViewById(R.id.main_content_layout);
        questionAreaLayout = findViewById(R.id.question_area_layout);

        // 确保主内容布局初始可见（包含广告）
        if (mainContentLayout != null) {
            mainContentLayout.setVisibility(View.VISIBLE);
        }
        // 题目区域初始隐藏
        if (questionAreaLayout != null) {
            questionAreaLayout.setVisibility(View.GONE);
        }
        if (topUserInfoLayout != null) {
            topUserInfoLayout.setVisibility(View.GONE);
        }
        // 加载过程中显示加载布局
        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.VISIBLE);
        }

        // 初始化UI组件
        questionText = findViewById(R.id.question_text);
        option1Button = findViewById(R.id.option1_button);
        option2Button = findViewById(R.id.option2_button);
        option3Button = findViewById(R.id.option3_button);
        option4Button = findViewById(R.id.option4_button);
        livesText = findViewById(R.id.lives_text);
        watchAdButton = findViewById(R.id.watch_ad_button);
        levelButton = findViewById(R.id.level_button);
        userNameText = findViewById(R.id.user_name_text);

        // 初始化答题统计文本引用
        // 直接使用livesText作为统计文本视图，因为布局中没有专用的stats_text视图
        statsText = livesText;

        // 修复体力显示控件的初始化
        try {
            // 使用正确的按钮ID显示体力
            lives_button = findViewById(R.id.lives_button);
            staminaText = null; // 不再使用TextView显示体力
        } catch (Exception e) {
            Log.e(TAG, "未找到合适的视图显示体力");
        }

        try {
            userIdText = findViewById(R.id.user_name_text); // 使用已存在的user_name_text代替
        } catch (Exception e) {
            Log.e(TAG, "未找到user_id_text视图");
            userIdText = new TextView(this);
        }

        try {
            musicButton = findViewById(R.id.music_button);
        } catch (Exception e) {
            Log.e(TAG, "未找到music_button视图");
            musicButton = new ToggleButton(this);
        }

        try {
            musicNote = findViewById(R.id.music_note);
        } catch (Exception e) {
            Log.e(TAG, "未找到music_note视图");
        }

        // 确保音符视图初始可见且设置了正确的初始文本
        if (musicNote != null) {
            musicNote.setVisibility(View.VISIBLE);
            musicNote.setText("♫"); // 初始状态为停止
            musicNote.bringToFront(); // 确保音符显示在最上层
            Log.d(TAG, "音乐音符视图初始化完成");
        }

        // 添加音乐按钮点击事件
        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "音乐按钮被点击");
                toggleMusic();
            }
        });

        // 加载用户信息
        loadUserInfo();

        // 从本地加载其他数据，但不包括体力值
        loadQuizDataWithoutStamina();

        // 处理激励广告
        watchAdButton.setOnClickListener(v -> showSettingsPopupWithCountdown(v));

        // 添加这行代码，在初始化时就加载用户答题统计
        loadUserAnswerStats();

        // 直接设置按钮文本和状态
        if (watchAdButton != null) {
            watchAdButton.setEnabled(false);
        }

        // 从服务器获取用户体力值
        loadUserStamina();

        // 设置选项点击事件
        option1Button.setOnClickListener(v -> checkAnswer("A"));
        option2Button.setOnClickListener(v -> checkAnswer("B"));
        option3Button.setOnClickListener(v -> checkAnswer("C"));
        option4Button.setOnClickListener(v -> checkAnswer("D"));

        // 初始化广告相关操作 - 在启动计时器之前就初始化广告
        initAdListeners();
        initAdsAfterContentLoaded();

        // 启动15秒加载计时器，新用户登录后加载15秒
        startLoadingTimer();

        // 从API获取题目
        loadQuestionsFromApi();
    }

    /**
     * 恢复音乐播放状态
     */
    private void resumeMusicState() {
        // 从SharedPreference恢复音乐状态
        boolean savedMusicState = SharedPreferenceUtil.getBoolean(this, "music_state", false);
        isMusicPlaying = savedMusicState;

        if (isMusicPlaying) {
            startMusic();
            if (musicNote != null) {
                musicNote.setText("🎵"); // 播放中状态
            }
        } else {
            if (musicNote != null) {
                musicNote.setText("♫"); // 停止状态
            }
        }
    }

    /**
     * 暂停音乐播放并保存状态
     */
    private void pauseMusicPlayback() {
        // 保存当前音乐状态
        SharedPreferenceUtil.putBoolean(this, "music_state", isMusicPlaying);

        // 如果正在播放，暂停音乐
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    /**
     * 给予体力奖励
     *
     * @param amount 奖励的体力值
     */
    private void giveStaminaReward(int amount) {
        // 获取当前体力值
        int currentStamina = SharedPreferenceUtil.getInt(this, "stamina", 5);
        // 增加体力值
        int newStamina = currentStamina + amount;
        // 保存新的体力值
        SharedPreferenceUtil.putInt(this, "stamina", newStamina);
        // 更新UI
        updateStaminaDisplay();
        // 显示奖励提示
        Toast.makeText(this, "获得" + amount + "点体力！", Toast.LENGTH_SHORT).show();
        // 重新加载服务器数据以同步
        loadUserStamina();
    }

    /**
     * 初始化广告监听器，用于监控广告加载状态
     * 确保广告监听器只被设置一次，避免重复设置
     */
    private void initAdListeners() {
        // Taku横幅广告监听器
        TakuAdManager.getInstance().setBannerAdListener(new TakuAdManager.BannerAdListener() {
            @Override
            public void onBannerAdLoaded() {
                Log.d(TAG, "Taku横幅广告加载成功");
            }

            @Override
            public void onBannerAdExposure() {
                Log.d(TAG, "Taku横幅广告曝光");
                // 启动10秒横幅广告刷新计时器
                startBannerAdRefreshTimer();

                // 调用风控检查接口
                // performRiskCheck("横幅广告", false);
            }

            @Override
            public void onBannerAdClicked() {
                Log.d(TAG, "Taku横幅广告点击");
            }

            @Override
            public void onBannerAdClosed() {
                Log.d(TAG, "Taku横幅广告关闭，1秒后重新加载");
                // 取消已有的横幅广告刷新计时器
                cancelBannerAdRefreshTimer();
                // 延迟1秒后重新加载横幅广告
                handler.postDelayed(() -> {
                    ViewGroup bannerContainer = findViewById(R.id.banner_ad_container);
                    if (bannerContainer != null) {
                        TakuAdManager.getInstance().showBannerAd(QuizActivity.this, bannerContainer);
                    }
                }, 1000);
            }

            @Override
            public void onBannerAdFailedToShow(String errorMsg) {
                Log.e(TAG, "Taku横幅广告显示失败: " + errorMsg);
            }
        });

        // Taku原生广告监听器
        TakuAdManager.getInstance().setNativeAdListener(new TakuAdManager.NativeAdListener() {
            @Override
            public void onNativeAdLoaded() {
                Log.d(TAG, "Taku原生广告加载成功");
            }

            @Override
            public void onNativeAdFailedToShow(String errorMsg) {
                Log.e(TAG, "Taku原生广告显示失败: " + errorMsg);
            }

            @Override
            public void onNativeAdExposure() {
                Log.d(TAG, "Taku原生广告曝光");
                // 启动10秒原生广告刷新计时器
                startNativeAdRefreshTimer();

                // 调用统一的风控检查接口
                // performRiskCheck("原生广告", false);
            }

            @Override
            public void onNativeAdClicked() {
                Log.d(TAG, "Taku原生广告点击");
            }

            @Override
            public void onNativeAdRenderSuccess() {
                Log.d(TAG, "Taku原生广告渲染成功");
            }

            @Override
            public void onNativeAdRenderFail() {
                Log.e(TAG, "Taku原生广告渲染失败");
            }
        });

        // Taku插屏广告监听器
        TakuAdManager.getInstance().setInterstitialAdListener(new TakuAdManager.InterstitialAdListener() {
            @Override
            public void onInterstitialAdLoaded() {
                Log.d(TAG, "Taku插屏广告加载成功");
                isInterstitialAdLoaded = true;
                isLoadingInterstitialAd = false;
            }

            @Override
            public void onInterstitialAdFailedToShow(String errorMsg) {
                Log.e(TAG, "Taku插屏广告显示失败: " + errorMsg);
                // 直接重新启动计时器
                startInterstitialAdTimer();
            }

            @Override
            public void onInterstitialAdShow() {
                Log.d(TAG, "Taku插屏广告实际显示");
                // 暂停所有计时器
                pauseAllTimers();
                // 广告显示时明确禁用获取体力按钮
                if (watchAdButton != null) {
                    watchAdButton.setEnabled(false);
                    watchAdButton.setText("获取");
                }
                // 广告显示时不重新启动计时器，等待广告关闭后处理
            }

            @Override
            public void onInterstitialAdExposure() {
                Log.d(TAG, "Taku插屏广告曝光");

                // 调用统一风控检查方法
                // performRiskCheck("插屏广告", false);
            }

            @Override
            public void onInterstitialAdClicked() {
                Log.d(TAG, "Taku插屏广告点击");
            }

            @Override
            public void onInterstitialAdClosed() {
                Log.d(TAG, "Taku插屏广告关闭");
                // 恢复所有计时器
                resumeAllTimers();
                // 广告关闭后，更新上次广告显示时间，确保不会立即再次显示
                lastInterstitialAdShownTime = System.currentTimeMillis();
                TakuAdManager.getInstance().preloadInterstitialAd(QuizActivity.this);

                // 重新加载原生广告，解决插屏广告关闭后原生广告消失的问题
                ViewGroup nativeContainer = findViewById(R.id.native_ad_container);
                if (nativeContainer != null) {
                    Log.d(TAG, "插屏广告关闭，重新加载原生广告");
                    TakuAdManager.getInstance().showNativeAd(QuizActivity.this, nativeContainer);
                }

                // 启动10秒计时器，10秒后检查是否可以显示下一个广告
                startInterstitialAdTimer();
            }
        });
    }

    /**
     * 广告管理器初始化方法，确保TakuAdManager只被初始化一次
     * 集中管理所有广告相关的初始化操作
     */
    private void initAdManager() {
        Log.d(TAG, "开始初始化广告管理器");

        try {
            // 初始化Taku广告管理器（确保只初始化一次）
            TakuAdManager.getInstance().init(getApplicationContext());
            Log.d(TAG, "TakuAdManager初始化完成");

            // 初始化广告监听器
            initAdListeners();
            Log.d(TAG, "广告监听器初始化完成");

            // 预加载Taku激励广告
            Log.d(TAG, "预加载Taku激励视频广告");
            TakuAdManager.getInstance().preloadRewardVideoAd(this);

            // 预加载Taku插屏广告
            Log.d(TAG, "预加载Taku插屏广告");
            TakuAdManager.getInstance().preloadInterstitialAd(QuizActivity.this);

            // 初始化广告，确保在所有组件初始化完成后调用
            initAdsAfterContentLoaded();
            Log.d(TAG, "广告内容加载完成");
        } catch (Exception e) {
            Log.e(TAG, "广告管理器初始化异常: " + e.getMessage());
        }
    }

    /**
     * 暂停所有计时器
     */
    private void pauseAllTimers() {
        Log.d(TAG, "暂停所有计时器");
        isGlobalTimerPaused = true;
        // 特别处理体力倒计时的暂停，确保它不会丢失剩余时间
        if (isAdCooldownActive) {
            Log.d(TAG, "暂停体力倒计时");
            isTimerPaused = true;
        }
    }

    /**
     * 恢复所有计时器
     */
    private void resumeAllTimers() {
        Log.d(TAG, "恢复所有计时器");
        isGlobalTimerPaused = false;
        // 特别处理体力倒计时的恢复
        if (isAdCooldownActive && isTimerPaused) {
            Log.d(TAG, "恢复体力倒计时，继续之前的计时");
            // 如果计时器存在，直接恢复计时器状态
            if (adCooldownTimer != null) {
                Log.d(TAG, "计时器存在，直接恢复计时状态");
                isTimerPaused = false;
                // 重新启动计时器以继续之前的计时
                startAdCooldownTimer();
            } else {
                // 计时器不存在，重新启动计时器
                Log.d(TAG, "计时器不存在，重新启动计时器");
                startAdCooldownTimer();
            }
        }
    }

    /**
     * 页面内容完全加载后初始化并显示广告
     * 确保广告容器已经准备就绪再加载广告，避免横屏时广告提前加载的问题
     * 实现原生广告和横幅广告一起并行加载的功能
     */
    private void initAdsAfterContentLoaded() {
        Log.d(TAG, "页面内容已加载完成，准备初始化广告");

        // 立即初始化广告，因为容器通常已经准备好
        // 减少条件检查，只关注容器是否可用
        ViewGroup bannerContainer = findViewById(R.id.banner_ad_container);
        ViewGroup nativeContainer = findViewById(R.id.native_ad_container);

        // 只检查容器是否存在且主内容可见
        boolean isContentVisible = mainContentLayout != null && mainContentLayout.getVisibility() == View.VISIBLE;

        if (bannerContainer != null && nativeContainer != null && isContentVisible) {

            Log.d(TAG, "广告容器已准备就绪，主内容可见，开始加载广告");

            // 启动15秒定时插屏广告
            startInterstitialAdTimer();

            handler.postDelayed(() -> {
                // 原生广告和横幅广告一起并行加载
                TakuAdManager.getInstance().showNativeAd(QuizActivity.this, nativeContainer);
                TakuAdManager.getInstance().showBannerAd(QuizActivity.this, bannerContainer);
            }, 1000);

            // 广告初始化完成
            isAdInitialized = true;
        } else {
            Log.d(TAG, "广告容器或内容不可见，延迟重试");

            // 如果条件不满足，延迟一段时间后重试
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 再次尝试初始化广告
                    initAdsAfterContentLoaded();
                }
            }, 1000);
        }
    }

    // 创建一个新方法，不加载体力值的版本
    private void loadQuizDataWithoutStamina() {
        currentScore = SharedPreferenceUtil.getInt(this, "current_score", 0);
        currentLevel = SharedPreferenceUtil.getInt(this, "current_level", 1);
        currentQuestionIndex = SharedPreferenceUtil.getInt(this, "current_question_index", 0);
        // 不要在这里加载体力值，留给loadUserStamina()方法处理
        updateScoreAndLevel();
    }

    // 修改原有的loadQuizData方法，添加注释说明
    private void loadQuizData() {
        currentScore = SharedPreferenceUtil.getInt(this, "current_score", 0);
        currentLevel = SharedPreferenceUtil.getInt(this, "current_level", 1);
        currentQuestionIndex = SharedPreferenceUtil.getInt(this, "current_question_index", 0);
        // 注意：体力值不再从这里加载，而是通过loadUserStamina()方法从服务器获取
        // currentStamina = SharedPreferenceUtil.getInt(this, "current_stamina", 3);
        updateScoreAndLevel();
    }

    private void saveQuizData() {
        SharedPreferenceUtil.putInt(this, "current_score", currentScore);
        SharedPreferenceUtil.putInt(this, "current_level", currentLevel);
        // 删除下面这行
        // SharedPreferenceUtil.putInt(this, "current_lives", currentLives);
        SharedPreferenceUtil.putInt(this, "current_question_index", currentQuestionIndex);
        // 注释掉保存体力值的代码，不再使用本地存储
        // SharedPreferenceUtil.putInt(this, "current_stamina", currentStamina);
    }

    // 修改updateScoreAndLevel()方法，移除体力显示更新
    private void updateScoreAndLevel() {
        // 根据得分计算等级
        int newLevel = (currentScore / 100) + 1;
        if (newLevel != currentLevel) {
            currentLevel = newLevel;
            Toast.makeText(this, "恭喜升级到 " + currentLevel + " 级！", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadQuestionsFromApi() {
        // 确保主内容布局始终可见（包含广告）
        if (mainContentLayout != null) {
            mainContentLayout.setVisibility(View.VISIBLE);
        }

        // 确保加载中布局显示，题目区域暂时隐藏
        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.VISIBLE);
        }
        if (questionAreaLayout != null) {
            questionAreaLayout.setVisibility(View.GONE);
        }
        // 确保顶部用户信息布局隐藏
        if (topUserInfoLayout != null) {
            topUserInfoLayout.setVisibility(View.GONE);
        }

        // 从API获取题目
        apiManager.getQuestions(0, "", 20, 1, new ApiCallback<List<Question>>() {
            @Override
            public void onSuccess(List<Question> questionList) {
                if (questionList != null && !questionList.isEmpty()) {
                    questions.clear();
                    questions.addAll(questionList);

                    // 加载第一个问题（但不立即显示）
                    if (currentQuestionIndex < questions.size()) {
                        loadQuestion(currentQuestionIndex);
                    } else {
                        currentQuestionIndex = 0;
                        loadQuestion(0);
                    }

                } else {
                    // 如果API没有题目，使用本地题目
                    initLocalQuestions();
                    loadQuestion(currentQuestionIndex);
                }

                // 确保主内容布局可见（包含广告）
                if (mainContentLayout != null) {
                    mainContentLayout.setVisibility(View.VISIBLE);
                }
                // 不要在这里隐藏加载中布局，让startLoadingTimer处理
                // 不要在这里显示题目区域，让startLoadingTimer处理

                // 页面内容完全加载后，初始化广告（已经在onCreate中通过startLoadingTimer调用）
                // initAdsAfterContentLoaded();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "加载题目失败: " + error);
                // 检查是否是答题数量限制导致的失败
                if (error != null && error.contains("您今天已答10题")) {
                    runOnUiThread(() -> {
                        Toast.makeText(QuizActivity.this, error, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(QuizActivity.this,
                                com.fortunequizking.activity.LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    // 其他错误情况下，使用本地题目
                    initLocalQuestions();
                    loadQuestion(currentQuestionIndex);
                    Toast.makeText(QuizActivity.this, "网络异常，使用本地题目", Toast.LENGTH_SHORT).show();

                    // 确保主内容布局可见（包含广告）
                    if (mainContentLayout != null) {
                        mainContentLayout.setVisibility(View.VISIBLE);
                    }
                    // 不要在这里隐藏加载中布局，让startLoadingTimer处理
                    // 不要在这里显示题目区域，让startLoadingTimer处理
                }
            }
        });
    }

    private void initLocalQuestions() {
        // 示例问题 - 作为备用
        questions.clear();
        questions.add(new Question(1, "以下哪个是正确的Java关键字？", "class", "function", "1"));
        questions.add(new Question(1, "Android应用的主入口是什么？", "onCreate()", "main()", "1"));
        questions.add(new Question(2, "以下哪个是Android的UI线程？", "主线程", "后台线程", "1"));
        questions.add(new Question(2, "SharedPreferences用于存储什么？", "键值对数据", "大型数据库", "1"));
        questions.add(new Question(3, "RecyclerView的作用是什么？", "显示大量数据列表", "处理网络请求", "1"));
    }

    private void loadQuestion(int index) {
        if (index >= questions.size()) {
            // 题目答完了，从服务器重新获取新题目
            loadQuestionsFromApi();
            return;
        }

        Question question = questions.get(index);
        currentQuestionIndex = index;

        // 记录开始答题时间
        questionStartTime = System.currentTimeMillis();

        // 显示题目
        questionText.setText(question.getQuestionText());

        // 处理选项
        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            // 新版本：使用Map存储的选项
            Map<String, String> options = question.getOptions();
            option1Button.setText("A: " + (options.containsKey("A") ? options.get("A") : ""));
            option2Button.setText("B: " + (options.containsKey("B") ? options.get("B") : ""));
            option3Button.setText("C: " + (options.containsKey("C") ? options.get("C") : ""));
            option4Button.setText("D: " + (options.containsKey("D") ? options.get("D") : ""));

            // 显示所有选项按钮
            option1Button.setVisibility(View.VISIBLE);
            option2Button.setVisibility(View.VISIBLE);
            option3Button.setVisibility(View.VISIBLE);
            option4Button.setVisibility(View.VISIBLE);
        } else {
            // 旧版本：只有两个选项
            option1Button.setText("A: " + question.getOption1());
            option2Button.setText("B: " + question.getOption2());
            option3Button.setVisibility(View.GONE);
            option4Button.setVisibility(View.GONE);
        }

        // 更新UI
        updateScoreAndLevel();

        // 不要在这里隐藏加载中布局，让startLoadingTimer处理
        // if (loadingLayout != null) {
        // loadingLayout.setVisibility(View.GONE);
        // }

        // 启用选项按钮，允许用户答题
        option1Button.setEnabled(true);
        option2Button.setEnabled(true);
        option3Button.setEnabled(true);
        option4Button.setEnabled(true);
    }

    // 显示设置弹窗
    /**
     * 显示设置弹窗
     * 默认不显示倒计时和获取体力按钮
     */
    public void showSettingsPopup(View view) {
        showSettingsPopup(false);
    }

    /**
     * 显示带倒计时的设置弹窗
     * 用于activity_quiz.xml中需要显示倒计时和获取体力按钮的场景
     */
    public void showSettingsPopupWithCountdown(View view) {
        showSettingsPopup(true);
    }

    /**
     * 显示设置弹窗
     * 
     * @param fromQuizXml 是否从activity_quiz.xml打开的弹窗
     */
    public void showSettingsPopup(boolean fromQuizXml) {
        try {
            // 创建弹窗构建器
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // 从布局文件加载弹窗内容
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.activity_setting, null);
            builder.setView(dialogView);

            // 获取弹窗中的控件
            TextView nicknameText = dialogView.findViewById(R.id.nickname_text);
            TextView roleIdText = dialogView.findViewById(R.id.role_id_text);
            TextView registerTimeText = dialogView.findViewById(R.id.register_time_text);
            TextView loginTimeText = dialogView.findViewById(R.id.login_time_text);
            LinearLayout countdownLayout = dialogView.findViewById(R.id.countdown_layout);
            // Button logoutButton = dialogView.findViewById(R.id.logout_button);
            // TextView userAgreementText =
            // dialogView.findViewById(R.id.user_agreement_text);
            // TextView privacyAgreementText =
            // dialogView.findViewById(R.id.privacy_agreement_text);

            // 获取当前用户的真实信息
            loadUserDataForPopup(nicknameText, roleIdText, registerTimeText, loginTimeText);

            // 加载并显示答题统计
            loadUserAnswerStatsForPopup(dialogView);

            // 检查是否已有弹窗在显示，如果有则先关闭
            if (settingsDialog != null && settingsDialog.isShowing()) {
                settingsDialog.dismiss();
            }

            // 设置弹窗
            final AlertDialog dialog = builder.create();

            // 将弹窗赋值给成员变量
            settingsDialog = dialog;

            // 添加弹窗关闭监听器，当弹窗关闭时重置settingsDialog
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    settingsDialog = null;
                }
            });

            // 设置弹窗居中显示
            if (dialog.getWindow() != null) {
                dialog.getWindow().setGravity(Gravity.CENTER);

                // 设置弹窗宽度
                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                // 设置弹窗背景为透明
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setAttributes(params);
            }

            // 只有从activity_quiz.xml打开的弹窗才处理倒计时和获取体力按钮
            if (fromQuizXml) {
                // 显示倒计时和获取体力按钮区域
                if (countdownLayout != null) {
                    countdownLayout.setVisibility(View.VISIBLE);
                }

                final TextView countdownText = dialogView.findViewById(R.id.countdown_text);
                final Button getLivesButton = dialogView.findViewById(R.id.get_lives_button);

                // 初始时隐藏按钮，显示倒计时
                if (countdownText != null) {
                    countdownText.setVisibility(View.VISIBLE);
                }
                if (getLivesButton != null) {
                    getLivesButton.setVisibility(View.GONE);
                }

                // 实现8秒倒计时
                final int[] countdownSeconds = { 8 };
                final CountDownTimer countdownTimer = new CountDownTimer(8000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (countdownText != null) {
                            countdownSeconds[0] = (int) (millisUntilFinished / 1000);
                            countdownText.setText(countdownSeconds[0] + "秒后可获取体力");
                        }
                    }

                    @Override
                    public void onFinish() {
                        if (countdownText != null && getLivesButton != null) {
                            countdownText.setVisibility(View.GONE);
                            getLivesButton.setVisibility(View.VISIBLE);
                        }
                    }
                };
                countdownTimer.start();

                // 为获取体力按钮添加点击事件
                if (getLivesButton != null) {
                    getLivesButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 倒计时结束后，点击按钮再显示Taku激励广告
                            showRewardAd();
                            // 关闭设置弹窗
                            dialog.dismiss();
                        }
                    });
                }
            } else {
                // 如果不是从activity_quiz.xml打开的弹窗，隐藏倒计时和按钮
                TextView countdownText = dialogView.findViewById(R.id.countdown_text);
                Button getLivesButton = dialogView.findViewById(R.id.get_lives_button);
                if (countdownText != null) {
                    countdownText.setVisibility(View.GONE);
                }
                if (getLivesButton != null) {
                    getLivesButton.setVisibility(View.GONE);
                }
            }

            /*
             * 注销功能已注释掉
             * // 为注销按钮添加点击事件
             * if (logoutButton != null) {
             * logoutButton.setOnClickListener(new View.OnClickListener() {
             * 
             * @Override
             * public void onClick(View v) {
             * // 显示确认弹窗
             * new AlertDialog.Builder(QuizActivity.this)
             * .setTitle("确认注销")
             * .setMessage("确定要注销账号吗？")
             * .setPositiveButton("确定", new DialogInterface.OnClickListener() {
             * 
             * @Override
             * public void onClick(DialogInterface dialog, int which) {
             * // 执行注销操作
             * performLogout();
             * }
             * })
             * .setNegativeButton("取消", null)
             * .show();
             * }
             * });
             * }
             */

            // // 为用户协议添加点击事件
            // if (userAgreementText != null) {
            // userAgreementText.setOnClickListener(new View.OnClickListener() {
            // @Override
            // public void onClick(View v) {
            // startActivity(new Intent(QuizActivity.this, AgreementActivity.class)
            // .putExtra(AgreementActivity.EXTRA_AGREEMENT_TYPE,
            // AgreementActivity.TYPE_USER_AGREEMENT));
            // }
            // });
            // }
            //
            // // 为隐私政策添加点击事件
            // if (privacyAgreementText != null) {
            // privacyAgreementText.setOnClickListener(new View.OnClickListener() {
            // @Override
            // public void onClick(View v) {
            // startActivity(new Intent(QuizActivity.this, AgreementActivity.class)
            // .putExtra(AgreementActivity.EXTRA_AGREEMENT_TYPE,
            // AgreementActivity.TYPE_PRIVACY_AGREEMENT));
            // }
            // });
            // }

            // 显示弹窗
            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "显示设置弹窗失败: " + e.getMessage());
            e.printStackTrace();
            // 如果弹窗显示失败，回退到原来的方式
            Intent intent = new Intent(QuizActivity.this, SettingActivity.class);
            startActivity(intent);
        }
    }

    // 为弹窗加载用户数据
    private void loadUserDataForPopup(TextView nicknameText, TextView roleIdText, TextView registerTimeText,
            TextView loginTimeText) {
        try {
            // 从SharedPreference获取当前用户的真实信息
            String userId = SharedPreferenceUtil.getString(this, "user_id", "2581800015");
            String nickname = SharedPreferenceUtil.getString(this, "nickname", "头发长出来了吗");
            String registerTime = SharedPreferenceUtil.getString(this, "register_time", "2025/08/18 00:04:49");

            // 显示当前时间作为登录时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date());

            // 设置文本内容
            if (nicknameText != null) {
                nicknameText.setText(nickname);
            }
            if (roleIdText != null) {
                roleIdText.setText(userId);
            }
            if (registerTimeText != null) {
                registerTimeText.setText(registerTime);
            }
            if (loginTimeText != null) {
                loginTimeText.setText(currentTime);
            }
        } catch (Exception e) {
            Log.e(TAG, "加载用户数据失败: " + e.getMessage());
        }
    }

    // 为弹窗加载用户答题统计
    private void loadUserAnswerStatsForPopup(final View dialogView) {
        if (apiManager != null) {
            apiManager.getUserAnswerStats(new ApiCallback<AnswerStats>() {
                @Override
                public void onSuccess(AnswerStats stats) {
                    updateAnswerStatsForPopup(dialogView, stats.getTodayCount(), stats.getTotalCount(),
                            stats.getTodayCorrectCount());
                    // 加载答题历史记录
                    loadUserAnswerHistoryForPopup(dialogView);
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "加载答题统计失败: " + error);
                    // 加载失败时，使用本地缓存或默认值
                    int todayCount = SharedPreferenceUtil.getInt(QuizActivity.this, "today_answer_count", 0);
                    int totalCount = SharedPreferenceUtil.getInt(QuizActivity.this, "total_answer_count", 0);
                    int todayCorrectCount = SharedPreferenceUtil.getInt(QuizActivity.this, "today_correct_count", 0);
                    updateAnswerStatsForPopup(dialogView, todayCount, totalCount, todayCorrectCount);
                }
            });
        }
    }

    // 更新弹窗中的答题统计显示
    private void updateAnswerStatsForPopup(View dialogView, int todayCount, int totalCount, int todayCorrectCount) {
        try {
            // 直接通过ID查找答题统计相关的TextView
            TextView todayCorrectText = dialogView.findViewById(R.id.today_correct_text);
            TextView todayTotalText = dialogView.findViewById(R.id.today_total_text);

            if (todayCorrectText != null) {
                todayCorrectText.setText("今日答对: " + todayCorrectCount + "题");
            }
            if (todayTotalText != null) {
                todayTotalText.setText("今日答题: " + todayCount + "题");
            }
        } catch (Exception e) {
            Log.e(TAG, "更新弹窗答题统计失败: " + e.getMessage());
        }
    }

    // 为弹窗加载用户答题历史记录
    private void loadUserAnswerHistoryForPopup(final View dialogView) {
        if (apiManager != null) {
            // 使用正确的API方法，获取最近10条答题历史
            apiManager.getAnswerHistory(1, 10, new ApiCallback<List<QuizHistoryRecord>>() {
                @Override
                public void onSuccess(List<QuizHistoryRecord> quizHistoryList) {
                    // 将QuizHistoryRecord列表转换为Map列表，以适配updateAnswerHistoryForPopup方法
                    List<Map<String, Object>> historyList = new ArrayList<>();
                    if (quizHistoryList != null && !quizHistoryList.isEmpty()) {
                        for (QuizHistoryRecord record : quizHistoryList) {
                            Map<String, Object> historyMap = new HashMap<>();
                            historyMap.put("index", record.getIndex());
                            // 使用服务器返回的时间信息
                            historyMap.put("time", record.getTime());
                            // 添加正确/错误状态
                            historyMap.put("is_correct", record.getIsCorrect());
                            historyList.add(historyMap);
                        }
                    }
                    updateAnswerHistoryForPopup(dialogView, historyList);
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "加载答题历史失败: " + error);
                    // 创建一个空的历史记录列表传给更新方法，避免UI显示问题
                    updateAnswerHistoryForPopup(dialogView, new ArrayList<Map<String, Object>>());
                }
            });
        }
    }

    // 更新弹窗中的答题历史记录显示
    /**
     * 执行账号注销操作
     */
    /*
     * 注销功能已注释掉
     * private void performLogout() {
     * // 调用ApiManager中的注销方法
     * // 由于ApiManager中的logoutAndRedirectToLogin是私有方法，我们需要直接实现注销逻辑
     * // 清除本地保存的用户登录信息
     * SharedPreferenceUtil.putString(QuizActivity.this, "user_id", "");
     * SharedPreferenceUtil.putString(QuizActivity.this, "token", "");
     * SharedPreferenceUtil.putString(QuizActivity.this, "nickname", "");
     * SharedPreferenceUtil.putString(QuizActivity.this, "register_time", "");
     * SharedPreferenceUtil.putBoolean(QuizActivity.this, "is_login", false);
     * 
     * // 显示提示并跳转到登录页面
     * Toast.makeText(QuizActivity.this, "账号注销成功", Toast.LENGTH_SHORT).show();
     * Intent intent = new Intent(QuizActivity.this,
     * com.fortunequizking.activity.LoginActivity.class);
     * intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
     * Intent.FLAG_ACTIVITY_CLEAR_TASK);
     * startActivity(intent);
     * finish();
     * }
     */

    private void updateAnswerHistoryForPopup(View dialogView, List<Map<String, Object>> historyList) {
        try {
            // 查找历史记录容器（使用正确的ID）
            LinearLayout historyContainer = dialogView.findViewById(R.id.history_list_container);
            if (historyContainer != null) {
                // 清空现有内容
                historyContainer.removeAllViews();

                // 添加历史记录
                if (historyList != null && !historyList.isEmpty()) {
                    for (Map<String, Object> history : historyList) {
                        // 创建水平方向的LinearLayout来容纳文本和状态图标
                        LinearLayout historyItemLayout = new LinearLayout(QuizActivity.this);
                        historyItemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        historyItemLayout.setOrientation(LinearLayout.HORIZONTAL);
                        historyItemLayout.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                        historyItemLayout.setPadding(10, 5, 10, 5);

                        // 创建显示索引和时间的TextView
                        TextView historyItem = new TextView(QuizActivity.this);
                        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        historyItem.setLayoutParams(textParams);

                        String index = String.valueOf(history.getOrDefault("index", ""));
                        String time = String.valueOf(history.getOrDefault("time", ""));
                        historyItem.setText(index + ": " + time);
                        historyItem.setTextColor(getResources().getColor(android.R.color.white));
                        historyItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        historyItem.setGravity(Gravity.RIGHT);

                        // 创建显示对错状态的TextView
                        TextView statusIcon = new TextView(QuizActivity.this);
                        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                                20, 20); // 设置图标大小
                        iconParams.setMargins(8, 0, 0, 0); // 设置左边距
                        statusIcon.setLayoutParams(iconParams);
                        statusIcon.setGravity(Gravity.CENTER);
                        statusIcon.setTextSize(12);
                        statusIcon.setTextColor(getResources().getColor(android.R.color.white));

                        // 根据is_correct字段设置不同的背景和文本
                        int isCorrect = 0;
                        if (history.containsKey("is_correct")) {
                            Object isCorrectObj = history.get("is_correct");
                            if (isCorrectObj instanceof Integer) {
                                isCorrect = (Integer) isCorrectObj;
                            } else if (isCorrectObj instanceof Boolean) {
                                isCorrect = ((Boolean) isCorrectObj) ? 1 : 0;
                            }
                        }

                        if (isCorrect == 1) {
                            // 正确答案
                            statusIcon.setBackgroundResource(R.drawable.correct_answer_bg);
                            statusIcon.setText("√");
                        } else {
                            // 错误答案
                            statusIcon.setBackgroundResource(R.drawable.wrong_answer_bg);
                            statusIcon.setText("×");
                        }

                        // 将TextView添加到LinearLayout
                        historyItemLayout.addView(historyItem);
                        historyItemLayout.addView(statusIcon);

                        // 将LinearLayout添加到历史记录容器
                        historyContainer.addView(historyItemLayout);
                    }
                } else {
                    TextView emptyText = new TextView(QuizActivity.this);
                    emptyText.setText("暂无答题记录");
                    emptyText.setTextColor(getResources().getColor(android.R.color.white));
                    emptyText.setTextSize(14);
                    emptyText.setPadding(10, 20, 10, 20);
                    emptyText.setGravity(Gravity.CENTER);
                    historyContainer.addView(emptyText);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "更新弹窗答题历史失败: " + e.getMessage());
        }
    }

    // 添加缺少的applySavedTheme方法
    private void applySavedTheme() {
        // 实现一个简单的主题应用逻辑
        Log.d(TAG, "应用保存的主题");
        // 这里可以添加实际的主题应用代码
    }

    // 注意：体力、广告和统计相关方法在文件末尾定义，避免重复定义

    // 检查答案的方法
    private void checkAnswer(String selectedOption) {
        if (currentQuestionIndex >= questions.size()) {
            return;
        }

        // 首先检查体力是否足够
        if (currentStamina <= 0) {
            Toast.makeText(this, "体力不足，请观看广告获取体力！", Toast.LENGTH_SHORT).show();
            return;
        }

        Question currentQuestion = questions.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getCorrectAnswer();

        // 计算答题用时
        long timeSpent = (System.currentTimeMillis() - questionStartTime) / 1000;

        // 检查答案是否正确
        boolean isCorrect = selectedOption.equalsIgnoreCase(correctAnswer);

        // 存储原始答案，用于提交到服务器
        String answerToSubmit = selectedOption;
        // 用于显示给用户的答案（正确或错误）
        String answerToDisplay = selectedOption;
        // 用于显示给用户的"正确答案"，初始为真实的正确答案
        String displayedCorrectAnswer = correctAnswer;

        // 风控逻辑：如果触发了风控且用户选择了正确答案，则自动更换为错误答案
        // if (riskControlTriggered && isCorrect) {
        // riskControlTriggered = true;
        // Log.d(TAG, "触发风控，用户选择了正确答案，自动更换为错误答案");

        // // 找到一个错误的选项
        // String wrongAnswer = null;
        // Map<String, String> options = currentQuestion.getOptions();
        // if (options != null && !options.isEmpty()) {
        // // 新版本：遍历选项找到一个错误的
        // for (String key : options.keySet()) {
        // if (!key.equalsIgnoreCase(correctAnswer)) {
        // wrongAnswer = key;
        // break;
        // }
        // }
        // } else {
        // // 旧版本：只有两个选项
        // wrongAnswer = "1".equals(correctAnswer) ? "2" : "1";
        // }

        // if (wrongAnswer != null) {
        // // 更新要提交的答案为错误答案
        // answerToSubmit = wrongAnswer;
        // // 更新显示的答案为错误答案
        // answerToDisplay = wrongAnswer;
        // // 更新显示的"正确答案"为另一个错误选项（如果有多个错误选项）
        // if (options != null && options.size() > 2) {
        // // 尝试找第二个不同的错误选项
        // for (String key : options.keySet()) {
        // if (!key.equalsIgnoreCase(correctAnswer) &&
        // !key.equalsIgnoreCase(wrongAnswer)) {
        // displayedCorrectAnswer = key;
        // break;
        // }
        // }
        // }

        // isCorrect = false;

        // // 不要重置风控标志，以保持登录时的冷却时间
        // // riskControlTriggered = false;
        // }
        // }

        // 立即扣除1点体力（答完题就触发减体力）
        currentStamina--;
        updateStaminaDisplay();

        // 立即提交答案到服务器（答完题就上传答案）
        submitAnswerToServer(currentQuestion.getId(), answerToSubmit, (int) timeSpent);

        // 保存数据但不再保存体力值
        saveQuizData();

        if (isCorrect) {
            // 答案正确
            currentScore += 10;
            // 使用新的对话框替代Toast
            showAnswerResultDialog(true, "回答正确！");

            // 更新今日答对题数的本地缓存
            int todayCorrectCount = SharedPreferenceUtil.getInt(QuizActivity.this, "today_correct_count", 0);
            todayCorrectCount++;
            SharedPreferenceUtil.putInt(QuizActivity.this, "today_correct_count", todayCorrectCount);
        } else {
            // 答案错误
            // 使用新的对话框替代Toast，显示假的正确答案
            showAnswerResultDialog(false, "回答错误！\n正确答案：" + displayedCorrectAnswer);
        }

        // 立即显示加载中布局，并隐藏题目区域和顶部信息布局
        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.VISIBLE);
        }
        if (questionAreaLayout != null) {
            questionAreaLayout.setVisibility(View.GONE);
        }
        if (topUserInfoLayout != null) {
            topUserInfoLayout.setVisibility(View.GONE);
        }

        // 立即禁用选项按钮，防止用户在加载过程中点击
        option1Button.setEnabled(false);
        option2Button.setEnabled(false);
        option3Button.setEnabled(false);
        option4Button.setEnabled(false);

        // 增加一个短暂的延迟，让加载状态能够明显显示
        handler.postDelayed(() -> {
            // 增加题目索引并加载下一题
            currentQuestionIndex++;
            // 调用startLoadingTimer来处理加载布局的隐藏
            startLoadingTimer();
            loadQuestion(currentQuestionIndex);
        }, 800); // 保持延迟时间，确保加载状态清晰可见
    }

    /**
     * 检查用户当前状态
     */
    private void checkUserStatus() {
        apiManager.getCurrentUserInfo(new ApiCallback<UserInfo>() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                if (userInfo != null && userInfo.getStatus() != null && !userInfo.getStatus().equals("normal")) {
                    // 用户被封禁，显示提示并返回登录页面
                    runOnUiThread(() -> {
                        String reason = userInfo.getBanReason() != null ? userInfo.getBanReason() : "未知原因";
                        String expireDate = userInfo.getBanExpireDate() != null ? userInfo.getBanExpireDate() : "永久";
                        String message = reason + "，解封时间：" + expireDate;
                        Toast.makeText(QuizActivity.this, message, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(QuizActivity.this,
                                com.fortunequizking.activity.LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "检查用户状态失败: " + error);
            }
        });
    }

    private void submitAnswerToServer(int questionId, String selectedOption, int timeSpent) {
        // 在提交答案前先检查用户状态
        apiManager.getCurrentUserInfo(new ApiCallback<UserInfo>() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                if (userInfo != null && userInfo.getStatus() != null && !userInfo.getStatus().equals("normal")) {
                    // 用户被封禁，显示提示并返回登录页面
                    runOnUiThread(() -> {
                        String reason = userInfo.getBanReason() != null ? userInfo.getBanReason() : "未知原因";
                        String expireDate = userInfo.getBanExpireDate() != null ? userInfo.getBanExpireDate() : "永久";
                        String message = reason + "，解封时间：" + expireDate;
                        Toast.makeText(QuizActivity.this, message, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(QuizActivity.this,
                                com.fortunequizking.activity.LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
                    return;
                }

                // 执行风控检查
                // performRiskCheck("提交答案", true);

                // 风控检查通过后，再次检查用户状态
                apiManager.getCurrentUserInfo(new ApiCallback<UserInfo>() {
                    @Override
                    public void onSuccess(UserInfo updatedUserInfo) {
                        if (updatedUserInfo != null && updatedUserInfo.getStatus() != null
                                && !updatedUserInfo.getStatus().equals("normal")) {
                            // 用户被封禁，显示提示并返回登录页面
                            runOnUiThread(() -> {
                                String reason = updatedUserInfo.getBanReason() != null ? updatedUserInfo.getBanReason()
                                        : "未知原因";
                                String expireDate = updatedUserInfo.getBanExpireDate() != null
                                        ? updatedUserInfo.getBanExpireDate()
                                        : "永久";
                                String message = reason + "，解封时间：" + expireDate;
                                Toast.makeText(QuizActivity.this, message, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(QuizActivity.this,
                                        com.fortunequizking.activity.LoginActivity.class);
                                startActivity(intent);
                                finish();
                            });
                            return;
                        }

                        // 用户状态正常，继续提交答案
                        apiManager.submitAnswer(questionId, selectedOption, timeSpent, new ApiCallback<Object>() {
                            @Override
                            public void onSuccess(Object result) {
                                Log.d(TAG, "答案提交成功");
                                // 答案提交成功后，刷新答题统计
                                loadUserAnswerStats();
                                // 刷新用户体力值，因为服务器可能扣除了体力
                                loadUserStamina();
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e(TAG, "答案提交失败: " + error);
                                // 获取当前渠道
                                String currentChannel = apiManager.getChannel();
                                // 检查是否是答题数量限制导致的失败，且渠道不是赏帮赚
                                if (error != null && error.contains("您今天已答10题") && !"赏帮赚".equals(currentChannel)) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(QuizActivity.this, error, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(QuizActivity.this,
                                                com.fortunequizking.activity.LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                } else {
                                    // 其他错误情况下，仍然刷新答题统计
                                    loadUserAnswerStats();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "获取用户状态失败: " + error);
                        // 重新检查用户状态后再提交答案
                        apiManager.getCurrentUserInfo(new ApiCallback<UserInfo>() {
                            @Override
                            public void onSuccess(UserInfo fallbackUserInfo) {
                                if (fallbackUserInfo != null && fallbackUserInfo.getStatus() != null
                                        && !fallbackUserInfo.getStatus().equals("normal")) {
                                    // 用户被封禁，显示提示并返回登录页面
                                    runOnUiThread(() -> {
                                        String reason = fallbackUserInfo.getBanReason() != null
                                                ? fallbackUserInfo.getBanReason()
                                                : "未知原因";
                                        String expireDate = fallbackUserInfo.getBanExpireDate() != null
                                                ? fallbackUserInfo.getBanExpireDate()
                                                : "永久";
                                        String message = reason + "，解封时间：" + expireDate;
                                        Toast.makeText(QuizActivity.this, message, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(QuizActivity.this,
                                                com.fortunequizking.activity.LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                    return;
                                }

                                // 用户状态正常，继续提交答案
                                apiManager.submitAnswer(questionId, selectedOption, timeSpent,
                                        new ApiCallback<Object>() {
                                            @Override
                                            public void onSuccess(Object result) {
                                                Log.d(TAG, "答案提交成功");
                                                // 答案提交成功后，刷新答题统计
                                                loadUserAnswerStats();
                                                // 刷新用户体力值，因为服务器可能扣除了体力
                                                loadUserStamina();
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                Log.e(TAG, "答案提交失败: " + error);
                                                // 获取当前渠道
                                                String currentChannel = apiManager.getChannel();
                                                // 检查是否是答题数量限制导致的失败，且渠道不是赏帮赚
                                                if (error != null && error.contains("您今天已答10题")
                                                        && !"赏帮赚".equals(currentChannel)) {
                                                    runOnUiThread(() -> {
                                                        Toast.makeText(QuizActivity.this, error, Toast.LENGTH_SHORT)
                                                                .show();
                                                        Intent intent = new Intent(QuizActivity.this,
                                                                com.fortunequizking.activity.LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    });
                                                } else {
                                                    // 其他错误情况下，仍然刷新答题统计
                                                    loadUserAnswerStats();
                                                }
                                            }
                                        });
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e(TAG, "获取用户状态失败: " + error);
                                // 状态检查失败，直接尝试提交答案
                                apiManager.submitAnswer(questionId, selectedOption, timeSpent,
                                        new ApiCallback<Object>() {
                                            @Override
                                            public void onSuccess(Object result) {
                                                Log.d(TAG, "答案提交成功");
                                                // 答案提交成功后，刷新答题统计
                                                loadUserAnswerStats();
                                                // 刷新用户体力值，因为服务器可能扣除了体力
                                                loadUserStamina();
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                Log.e(TAG, "答案提交失败: " + error);
                                                // 获取当前渠道
                                                String currentChannel = apiManager.getChannel();
                                                // 检查是否是答题数量限制导致的失败，且渠道不是赏帮赚
                                                if (error != null && error.contains("您今天已答10题")
                                                        && !"赏帮赚".equals(currentChannel)) {
                                                    runOnUiThread(() -> {
                                                        Toast.makeText(QuizActivity.this, error, Toast.LENGTH_SHORT)
                                                                .show();
                                                        Intent intent = new Intent(QuizActivity.this,
                                                                com.fortunequizking.activity.LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    });
                                                } else {
                                                    // 其他错误情况下，仍然刷新答题统计
                                                    loadUserAnswerStats();
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "风控检查失败: " + error);
                // 风控检查失败，仍然尝试提交答案
                apiManager.submitAnswer(questionId, selectedOption, timeSpent, new ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object result) {
                        Log.d(TAG, "答案提交成功");
                        // 答案提交成功后，刷新答题统计
                        loadUserAnswerStats();
                        // 刷新用户体力值，因为服务器可能扣除了体力
                        loadUserStamina();
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "答案提交失败: " + error);
                        // 检查是否是答题数量限制导致的失败
                        if (error != null && error.contains("您今天已答10题")) {
                            runOnUiThread(() -> {
                                Toast.makeText(QuizActivity.this, error, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(QuizActivity.this,
                                        com.fortunequizking.activity.LoginActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            // 其他错误情况下，仍然刷新答题统计
                            loadUserAnswerStats();
                        }
                    }
                });
            }
        });
    }

    /**
     * 显示答题结果对话框
     *
     * @param isCorrect 是否回答正确
     * @param message   提示信息
     */
    private void showAnswerResultDialog(boolean isCorrect, String message) {
        // 恢复使用自定义对话框主题
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);

        // 创建对话框内容视图
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_answer_result, null);

        // 设置对话框内容
        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        ImageView iconImageView = dialogView.findViewById(R.id.dialog_icon);
        Button okButton = dialogView.findViewById(R.id.dialog_ok_button);

        // 根据是否正确设置不同的标题和图标
        if (isCorrect) {
            titleTextView.setText("恭喜");
            titleTextView.setTextColor(getResources().getColor(R.color.correct_green));
            iconImageView.setImageResource(R.drawable.ic_check_circle);
        } else {
            titleTextView.setText("很遗憾");
            titleTextView.setTextColor(getResources().getColor(R.color.wrong_red));
            iconImageView.setImageResource(R.drawable.ic_cancel_circle);
        }

        messageTextView.setText(message);

        // 设置对话框
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // 设置对话框居中显示
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.CENTER);

            // 设置对话框宽度
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
            dialog.getWindow().setAttributes(params);
        }

        // 设置按钮点击事件
        okButton.setOnClickListener(v -> dialog.dismiss());

        // 显示对话框
        dialog.show();
    }

    private void gameOver() {
        // 游戏结束也使用对话框
        showGameOverDialog(currentScore);
    }

    /**
     * 显示游戏结束对话框
     *
     * @param score 最终得分
     */
    private void showGameOverDialog(int score) {
        // 恢复使用自定义对话框主题
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_over, null);

        TextView scoreTextView = dialogView.findViewById(R.id.game_over_score);
        Button restartButton = dialogView.findViewById(R.id.game_over_restart);

        scoreTextView.setText(String.valueOf(score));

        AlertDialog dialog = builder.setView(dialogView).create();

        // 设置对话框居中显示并设置宽度
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.CENTER);

            // 设置对话框宽度
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            dialog.getWindow().setAttributes(params);
        }

        restartButton.setOnClickListener(v -> {
            dialog.dismiss();
            resetGame();
        });

        // 显示对话框
        dialog.show();
    }

    /**
     * 重置游戏状态
     */
    private void resetGame() {
        // 重置游戏状态
        currentScore = 0;
        currentLevel = 1;
        // 移除了重置体力的代码，体力只能通过观看广告获取
        currentQuestionIndex = 0;

        // 重置计时器状态
        cooldownTimerRemaining = 0;
        cooldownTimeElapsed = 0;
        isTimerPaused = false;

        // 重置层级状态
        currentAdCooldownLevel = 0;

        // 保存数据
        saveQuizData();

        // 重新开始游戏
        loadQuestionsFromApi();
    }

    // 添加变量来保存倒计时状态
    private long cooldownTimeRemaining = 0; // 剩余冷却时间
    private long cooldownTimeElapsed = 0; // 已冷却时间
    private boolean isTimerPaused = false; // 倒计时是否暂停

    /**
     * 获取本地保存的激励广告数量
     */
    private int getRewardAdCount() {
        return SharedPreferenceUtil.getInt(this, "ad_count_reward", 0);
    }

    // 添加广告冷却倒计时方法
    private void startAdCooldownTimer() {
        // 检查watchAdButton是否已初始化
        if (watchAdButton == null) {
            Log.e(TAG, "watchAdButton未初始化，无法启动冷却计时器");
            return;
        }

        // 取消已有的计时器
        if (adCooldownTimer != null) {
            adCooldownTimer.cancel();
        }

        isAdCooldownActive = true;
        isTimerPaused = false;
        watchAdButton.setEnabled(false);

        // 计算冷却时间：根据层级处理逻辑
        long cooldownTime;
        if (riskControlTriggered) {
            // 使用后端返回的层级信息
            if (currentAdCooldownLevel == 1) {
                // 第一层：3分钟
                cooldownTime = Math.max(0, AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_1 - cooldownTimeElapsed);
                Log.d(TAG, "层级处理第一层，使用3分钟减去已冷却时间：" + cooldownTimeElapsed + "ms，剩余：" + cooldownTime + "ms");
            } else if (currentAdCooldownLevel == 2) {
                // 第二层：5分钟
                cooldownTime = Math.max(0, AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_2 - cooldownTimeElapsed);
                Log.d(TAG, "层级处理第二层，使用5分钟减去已冷却时间：" + cooldownTimeElapsed + "ms，剩余：" + cooldownTime + "ms");
            } else {
                // 默认层级：使用第一层
                cooldownTime = Math.max(0, AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_1 - cooldownTimeElapsed);
                Log.d(TAG, "默认层级处理，使用3分钟减去已冷却时间：" + cooldownTimeElapsed + "ms，剩余：" + cooldownTime + "ms");
            }
        } else {
            // 检查激励广告数量，如果达到6条，将冷却时间改为1.5分钟
            int rewardAdCount = getRewardAdCount();
            if (rewardAdCount >= 6) {
                cooldownTime = 90 * 1000; // 1.5分钟
                Log.d(TAG, "激励广告数量已达到" + rewardAdCount + "条，将冷却时间调整为1.5分钟");
            } else {
                cooldownTime = AD_COOLDOWN_TIME_NORMAL; // 默认1分钟
            }
            // 如果有剩余时间，使用剩余时间继续计时
            if (cooldownTimerRemaining > 0) {
                cooldownTime = cooldownTimerRemaining;
            }
        }

        adCooldownTimer = new CountDownTimer(cooldownTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isTimerPaused || isGlobalTimerPaused) {
                    // 如果暂停，保存剩余时间和已冷却时间并取消计时器
                    cooldownTimerRemaining = millisUntilFinished;
                    // 计算已冷却时间：总时间减去剩余时间
                    if (riskControlTriggered) {
                        if (currentAdCooldownLevel == 1) {
                            cooldownTimeElapsed = AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_1 - millisUntilFinished;
                        } else if (currentAdCooldownLevel == 2) {
                            cooldownTimeElapsed = AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_2 - millisUntilFinished;
                        } else {
                            cooldownTimeElapsed = AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_1 - millisUntilFinished;
                        }
                    } else {
                        cooldownTimeElapsed = AD_COOLDOWN_TIME_NORMAL - millisUntilFinished;
                    }
                    cancel();
                    return;
                }

                // 更新按钮文本，显示剩余冷却时间
                if (watchAdButton != null) {
                    long secondsRemaining = millisUntilFinished / 1000;
                    watchAdButton.setText("获取(" + secondsRemaining + "s)");
                    watchAdButton.setEnabled(false);
                }

                // 更新已冷却时间
                if (riskControlTriggered) {
                    if (currentAdCooldownLevel == 1) {
                        cooldownTimeElapsed = AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_1 - millisUntilFinished;
                    } else if (currentAdCooldownLevel == 2) {
                        cooldownTimeElapsed = AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_2 - millisUntilFinished;
                    } else {
                        cooldownTimeElapsed = AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_1 - millisUntilFinished;
                    }
                } else {
                    cooldownTimeElapsed = AD_COOLDOWN_TIME_NORMAL - millisUntilFinished;
                }
            }

            @Override
            public void onFinish() {
                // 如果全局计时器被暂停，保存状态
                if (isGlobalTimerPaused) {
                    cooldownTimerRemaining = 0;
                    cooldownTimeElapsed = 0;
                    return;
                }

                // 倒计时结束，恢复按钮状态
                isAdCooldownActive = false;
                isTimerPaused = false;
                cooldownTimerRemaining = 0;
                cooldownTimeElapsed = 0;
                if (watchAdButton != null) {
                    watchAdButton.setEnabled(true);
                    watchAdButton.setText("获取 +");
                }
            }
        };

        adCooldownTimer.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 恢复音乐播放状态
        resumeMusicState();
        // 应用保存的主题
        applySavedTheme();
        // 更新体力显示
        updateStaminaDisplay();
        // 恢复所有计时器
        resumeAllTimers();
        // 广告显示逻辑已在onCreate中处理
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停音乐播放
        pauseMusicPlayback();
        // 暂停所有计时器
        pauseAllTimers();
        // 广告暂停逻辑已简化
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 屏幕方向变化时，需要重新初始化广告，但确保页面内容加载完成后再触发
        Log.d(TAG, "屏幕方向变化，准备重新初始化广告");

        // 先清除所有广告视图
        ViewGroup bannerContainer = findViewById(R.id.banner_ad_container);
        ViewGroup nativeContainer = findViewById(R.id.native_ad_container);

        if (bannerContainer != null) {
            bannerContainer.removeAllViews();
        }

        if (nativeContainer != null) {
            nativeContainer.removeAllViews();
        }

        // 重置广告初始化状态
        isAdInitialized = false;

        // 延迟更长时间，确保题目完全加载完成后再初始化广告
        // 横屏时需要更长的延迟，让页面布局和内容都能完全重新加载
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 检查题目是否已加载
                if (questions != null && !questions.isEmpty()) {
                    // 题目已加载，直接初始化广告
                    initAdsAfterContentLoaded();
                } else {
                    // 题目尚未加载，再延迟一段时间后重试
                    Log.d(TAG, "题目尚未加载完成，继续等待后再初始化广告");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            initAdsAfterContentLoaded();
                        }
                    }, 800);
                }
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源
        if (adTimer != null) {
            adTimer.cancel();
            adTimer = null;
        }
        // 释放Taku广告资源
        if (TakuAdManager.getInstance() != null) {
            TakuAdManager.getInstance().destroy();
            // 重置广告加载状态
            TakuAdManager.getInstance().resetInterstitialLoadingState();
        }
        // 释放音乐资源
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        // 清理广告刷新计时器
        cancelBannerAdRefreshTimer();
        cancelNativeAdRefreshTimer();
        // 移除所有引用，帮助垃圾回收
        apiManager = null;
        questionText = null;
        option1Button = null;
        option2Button = null;
        livesText = null;
        watchAdButton = null;
        levelButton = null;
        userNameText = null;
        staminaText = null;
        musicButton = null;
        musicNote = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 处理设置页面返回结果
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            // 刷新用户信息和设置
            loadUserInfo();
            applySavedTheme();
            // 刷新体力显示
            updateStaminaDisplay();
            // 重新加载用户答题统计
            loadUserAnswerStats();
        }
    }

    /**
     * 加载用户信息并显示到界面上
     */
    private void loadUserInfo() {
        // 从SharedPreference获取用户信息
        String userId = SharedPreferenceUtil.getString(this, "user_id", "");
        String userName = SharedPreferenceUtil.getString(this, "nickname", "Guest");
        String avatarUrl = SharedPreferenceUtil.getString(this, "avatar_url", "");
        String userMobile = SharedPreferenceUtil.getString(this, "mobile", ""); // 获取用户手机号

        // 恢复音乐状态
        isMusicPlaying = SharedPreferenceUtil.getBoolean(this, "music_state", false);
        if (isMusicPlaying) {
            startMusic();
        }

        // 定期检查用户状态
        checkUserStatus();

        // 显示用户信息到对应的TextView
        if (userIdText != null) {
            userIdText.setText("ID: " + userId);
        }

        if (userNameText != null) {
            userNameText.setText(userName);
        }

        // 显示用户手机号
        TextView userMobileText = findViewById(R.id.user_mobile_text);
        if (userMobileText != null && !userMobile.isEmpty()) {
            userMobileText.setText(userMobile);
        }

        // 尝试加载头像
        ImageView avatarImage = findViewById(R.id.avatar_image);
        if (avatarImage != null) {
            if (!avatarUrl.isEmpty()) {
                try {
                    // 使用Glide加载网络头像
                    Glide.with(this)
                            .load(avatarUrl)
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.user_avatar) // 默认头像
                            .error(R.drawable.user_avatar) // 加载失败时显示默认头像
                            .into(avatarImage);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to load avatar: " + e.getMessage());
                    // 加载失败时设置默认头像
                    avatarImage.setImageResource(R.drawable.user_avatar);
                }
            } else {
                // 没有头像URL时显示默认头像
                avatarImage.setImageResource(R.drawable.user_avatar);
            }
        }
    }

    /**
     * 切换音乐播放状态
     */
    private void toggleMusic() {
        Log.d(TAG, "切换音乐播放状态，当前状态：" + isMusicPlaying);
        if (isMusicPlaying) {
            // 停止音乐
            stopMusic();
        } else {
            // 播放音乐
            startMusic();
        }

        // 播放音符点击动画
        if (musicNote != null) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.music_note_click_animation);
            musicNote.startAnimation(animation);

            // 添加动画监听器，确保动画结束后音符仍然可见
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // 动画开始时不需要特别处理
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // 动画结束后，确保音符仍然可见
                    runOnUiThread(() -> {
                        musicNote.setVisibility(View.VISIBLE);
                        musicNote.setAlpha(1.0f); // 确保完全不透明
                    });
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // 动画重复时不需要特别处理
                }
            });
        }
    }

    /**
     * 播放背景音乐
     */
    private void startMusic() {
        // 实际项目中，这里应该调用音乐管理类的播放方法
        // 这里只是简单实现
        isMusicPlaying = true;
        // 更新按钮状态以改变背景颜色
        if (musicButton != null) {
            musicButton.setChecked(true);
        }
        // 更新悬浮符号文本
        if (musicNote != null) {
            runOnUiThread(() -> {
                musicNote.setText("♪"); // 改变图标表示正在播放
                musicNote.setVisibility(View.VISIBLE);
                // 简化刷新逻辑
                musicNote.invalidate();
                Log.d(TAG, "音乐开始播放，音符更新为：♪，视图状态：" + musicNote.getVisibility());
            });
        }
    }

    /**
     * 停止背景音乐
     */
    private void stopMusic() {
        // 实际项目中，这里应该调用音乐管理类的停止方法
        // 这里只是简单实现
        isMusicPlaying = false;
        // 更新按钮状态以改变背景颜色
        if (musicButton != null) {
            musicButton.setChecked(false);
        }
        // 更新悬浮符号文本
        if (musicNote != null) {
            runOnUiThread(() -> {
                musicNote.setText("♫"); // 改变图标表示停止播放
                musicNote.setVisibility(View.VISIBLE);
                // 简化刷新逻辑
                musicNote.invalidate();
                Log.d(TAG, "音乐停止播放，音符更新为：♫，视图状态：" + musicNote.getVisibility());
            });
        }
    }

    /**
     * 加载用户体力值
     */
    private void loadUserStamina() {
        Log.d(TAG, "开始从服务器加载用户体力值");
        ApiManager.getInstance().getUserStamina(new ApiCallback<Integer>() {
            @Override
            public void onSuccess(Integer stamina) {
                Log.d(TAG, "成功获取服务器体力值: " + stamina);
                currentStamina = stamina;
                // 移除本地保存，确保每次都从服务器获取最新值
                // SharedPreferenceUtil.putInt(QuizActivity.this, "current_stamina",
                // currentStamina);
                updateStaminaDisplay();
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "加载体力失败: " + errorMessage);
                // 服务器请求失败时，使用一个临时默认值而不是读取本地缓存
                currentStamina = 3; // 临时默认值，实际应考虑更好的错误处理
                updateStaminaDisplay();
            }
        });
    }

    /**
     * 更新体力显示
     */
    private void updateStaminaDisplay() {
        if (lives_button != null) {
            lives_button.setText("体力: " + currentStamina);
        }

        // 同时更新获取体力按钮的状态，确保冷却状态正确显示
        if (watchAdButton != null) {
            watchAdButton.setEnabled(false);
            if (!isAdCooldownActive) {
                // 显示剩余冷却时间
                // long cooldownTime = riskControlTriggered ? AD_COOLDOWN_TIME_RISK :
                // AD_COOLDOWN_TIME_NORMAL;
                // long secondsRemaining = cooldownTimeRemaining > 0 ? cooldownTimeRemaining /
                // 1000 : cooldownTime / 1000;
                // watchAdButton.setText("获取(" + secondsRemaining + "s)");
                // } else {
                watchAdButton.setEnabled(true);
                watchAdButton.setText("获取 +");
            }
        }
    }

    // 简化showRewardAd方法，只使用Taku广告并添加重试逻辑
    private void showRewardAd() {
        Log.d(TAG, "尝试显示Taku激励广告");

        // 检查是否处于冷却时间
        if (isAdCooldownActive) {
            Toast.makeText(this, "请等待冷却时间结束", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "广告处于冷却时间，无法显示");
            return;
        }

        // 移除了本地调试模式直接增加体力的代码
        // 体力现在只能通过观看广告获取

        // 显示Taku激励广告
        TakuAdManager.getInstance().showRewardAd(this, new TakuAdManager.RewardAdListener() {

            @Override
            public void onRewardAdLoaded() {
                Log.d(TAG, "Taku激励视频广告加载成功");
            }

            @Override
            public void onRewardAdStarted() {
                Log.d(TAG, "Taku激励视频广告开始播放");
                isRewardAdPlaying = true; // 设置激励广告播放状态为true
                // 暂停所有计时器
                pauseAllTimers();

                // 调用统一风控检查方法
                // performRiskCheck("激励广告", false);
            }

            @Override
            public void onRewardAdFailedToShow() {
                Log.d(TAG, "Taku激励视频广告显示失败，将尝试重试");
                isRewardAdPlaying = false;
                // 广告显示失败，由TakuAdManager内部处理重试
            }

            @Override
            public void onRewardAdRewarded() {
                Log.d(TAG, "Taku激励视频广告奖励发放");
                try {
                    // 直接调用ApiManager更新用户体力值
                    ApiManager.getInstance().updateUserStamina(1, new ApiCallback<StaminaUpdateResult>() {
                        @Override
                        public void onSuccess(StaminaUpdateResult result) {
                            // 将String类型的change转换为int类型
                            int changeValue = 0;
                            try {
                                changeValue = Integer.parseInt(result.getChange());
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "解析体力变化值失败: " + result.getChange(), e);
                            }
                            currentStamina = result.getStamina() + changeValue;
                            Log.d(TAG, "体力值更新成功: " + currentStamina + ", 变化值: " + changeValue);
                            updateStaminaDisplay();
                            // 显示奖励提示
                            Toast.makeText(QuizActivity.this, "获得1点体力！当前体力: " + currentStamina, Toast.LENGTH_SHORT)
                                    .show();

                            // 在奖励发放成功后启动18秒冷却计时器
                            startAdCooldownTimer();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.e(TAG, "更新体力失败: " + errorMessage);
                            Toast.makeText(QuizActivity.this, "更新体力失败，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "处理体力奖励异常: " + e.getMessage());
                }

                // 预加载下一个Taku激励广告
                TakuAdManager.getInstance().preloadRewardVideoAd(QuizActivity.this);
            }

            @Override
            public void onRewardAdClosed() {
                Log.d(TAG, "Taku激励视频广告关闭");
                isRewardAdPlaying = false; // 设置激励广告播放状态为false
                // 恢复所有计时器
                resumeAllTimers();
                // 恢复广告冷却计时器 - 如果触发风控且计时器已在运行，不需要重新启动
                if (isAdCooldownActive && !(riskControlTriggered && adCooldownTimer != null)) {
                    startAdCooldownTimer();
                }
                // 广告关闭后，更新上次广告显示时间，确保不会立即再次显示
                lastInterstitialAdShownTime = System.currentTimeMillis();
                TakuAdManager.getInstance().preloadInterstitialAd(QuizActivity.this);

                // 启动10秒计时器，10秒后检查是否可以显示下一个广告
                startInterstitialAdTimer();
                loadUserStamina();
            }
        });
    }

    /**
     * 启动加载计时器，根据是否首次加载决定行为
     * 第一次加载：显示15秒
     * 后续加载：检查题目是否已加载完成，如果已完成则立即关闭加载中界面
     */
    private void startLoadingTimer() {
        if (loadingTimer != null) {
            loadingTimer.cancel();
            loadingTimer = null;
        }

        // 加载过程中暂停所有计时器
        pauseAllTimers();

        if (isFirstLoading) {
            // 第一次加载：设置15秒计时器
            loadingTimer = new CountDownTimer(15000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // 倒计时进行中，不需要特别处理
                }

                @Override
                public void onFinish() {
                    // 隐藏加载布局
                    if (loadingLayout != null) {
                        loadingLayout.setVisibility(View.GONE);
                    }

                    // 显示题目区域
                    if (questionAreaLayout != null) {
                        questionAreaLayout.setVisibility(View.VISIBLE);
                    }
                    // 显示顶部用户信息布局
                    if (topUserInfoLayout != null) {
                        topUserInfoLayout.setVisibility(View.VISIBLE);
                    }

                    startAdCooldownTimer();
                    // 通过接口获取用户风控状态，而不是硬编码设置
                    // performRiskCheck("初始化", true);

                    // 启动插屏广告计时器（在体力冷却之后，避免被暂停）
                    startInterstitialAdTimer();

                    // 标记为非首次加载
                    isFirstLoading = false;

                    // 加载完成后恢复全局计时器
                    isGlobalTimerPaused = false;
                    isTimerPaused = false;
                }
            };
            loadingTimer.start();
        } else {
            // 非第一次加载：设置800毫秒计时器
            loadingTimer = new CountDownTimer(800, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // 不需要处理
                }

                @Override
                public void onFinish() {
                    // 隐藏加载布局
                    if (loadingLayout != null) {
                        loadingLayout.setVisibility(View.GONE);
                    }
                    // 显示题目区域
                    if (questionAreaLayout != null) {
                        questionAreaLayout.setVisibility(View.VISIBLE);
                    }
                    // 显示顶部用户信息布局
                    if (topUserInfoLayout != null) {
                        topUserInfoLayout.setVisibility(View.VISIBLE);
                    }

                    // 加载完成后恢复全局计时器
                    isGlobalTimerPaused = false;
                    isTimerPaused = false;
                }
            };
            loadingTimer.start();
        }
    }

    /**
     * 启动插屏广告计时器 - 10秒检查一次广告是否可以显示
     */
    private void startInterstitialAdTimer() {
        // 取消已有的计时器（如果存在）
        if (interstitialAdTimer != null) {
            interstitialAdTimer.cancel();
        }

        // 检查Activity生命周期状态，避免在不适合的时机加载/显示广告
        if (isFinishing() || isDestroyed()) {
            Log.d(TAG, "Activity已处于结束/销毁状态，不启动广告计时器");
            return;
        }

        // 如果广告未加载，先启动加载
        if (!isLoadingInterstitialAd && !isInterstitialAdLoaded && !isFinishing() && !isDestroyed()) {
            preloadNextInterstitialAd();
        }

        // 创建计时器，10秒后直接显示广告（如果已加载）
        long timerInterval = REFRESH_INTERVAL;
        Log.d(TAG, "启动插屏广告计时器，" + (timerInterval / 1000) + "秒后直接显示广告（如果已加载）");
        interstitialAdTimer = new CountDownTimer(timerInterval, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 插屏广告计时器不受全局暂停影响，继续计时
                // 不检查isGlobalTimerPaused，确保广告能正常显示
            }

            @Override
            public void onFinish() {
                // 插屏广告计时器不受全局暂停影响，继续处理
                // 不检查isGlobalTimerPaused，确保广告能正常显示

                // 10秒计时结束，直接显示广告（如果已加载）
                if (isInterstitialAdLoaded) {
                    Log.d(TAG, "10秒计时结束，广告已加载，直接显示广告");
                    // 更新上次广告显示时间
                    lastInterstitialAdShownTime = System.currentTimeMillis();
                    TakuAdManager.getInstance().showInterstitialAd(QuizActivity.this);
                } else {
                    // 广告未加载完成，继续检查（每1秒检查一次）
                    Log.d(TAG, "10秒计时结束，广告未加载完成，继续检查");
                    startInterstitialAdCheckTimer();
                }
            }
        };

        interstitialAdTimer.start();
    }

    /**
     * 启动插屏广告检查计时器 - 广告未加载完成时使用，每1秒检查一次
     */
    private void startInterstitialAdCheckTimer() {
        // 插屏广告检查计时器不受全局暂停影响，始终启动
        // 不检查isGlobalTimerPaused，确保广告能正常显示

        // 取消已有的计时器（如果存在）
        if (interstitialAdTimer != null) {
            interstitialAdTimer.cancel();
        }

        // 创建1秒检查计时器
        interstitialAdTimer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 插屏广告检查计时器不受全局暂停影响，继续计时
                // 不检查isGlobalTimerPaused，确保广告能正常显示
            }

            @Override
            public void onFinish() {
                // 插屏广告检查计时器不受全局暂停影响，继续处理
                // 不检查isGlobalTimerPaused，确保广告能正常显示

                // 检查广告是否已加载完成
                if (isInterstitialAdLoaded) {
                    // 广告已加载，立即显示
                    Log.d(TAG, "广告加载完成，立即显示广告");
                    if (shouldShowInterstitialAd()) {
                        // 更新上次广告显示时间
                        lastInterstitialAdShownTime = System.currentTimeMillis();
                        TakuAdManager.getInstance().showInterstitialAd(QuizActivity.this);
                    }
                } else {
                    // 广告未加载完成，继续检查
                    Log.d(TAG, "广告未加载完成，继续检查");
                    startInterstitialAdCheckTimer();
                }
            }
        };

        interstitialAdTimer.start();
    }

    /**
     * 预加载下一个插屏广告
     */
    private void preloadNextInterstitialAd() {
        // 如果广告未在加载且未加载完成，则开始预加载
        if (!isLoadingInterstitialAd && !isInterstitialAdLoaded) {
            isLoadingInterstitialAd = true;
            Log.d(TAG, "开始预加载下一个插屏广告");
            // 使用正确的预加载方法，而不是立即显示广告
            TakuAdManager.getInstance().preloadInterstitialAd(QuizActivity.this);
        } else if (isLoadingInterstitialAd) {
            Log.d(TAG, "广告正在加载中，无需重复预加载");
        } else if (isInterstitialAdLoaded) {
            Log.d(TAG, "广告已加载完成，无需预加载");
        }
    }

    /**
     * 检查是否应该显示插屏广告
     *
     * @return 是否应该显示广告
     */
    private boolean shouldShowInterstitialAd() {
        // 如果激励广告正在播放，则不显示插屏广告
        if (isRewardAdPlaying) {
            Log.d(TAG, "激励广告正在播放，不显示插屏广告");
            return false;
        }

        // 检查是否满足最小广告显示间隔要求
        long currentTime = System.currentTimeMillis();
        long timeSinceLastAd = currentTime - lastInterstitialAdShownTime;
        if (lastInterstitialAdShownTime > 0 && timeSinceLastAd < MIN_INTERSTITIAL_AD_INTERVAL) {
            Log.d(TAG, "广告显示间隔不足" + (MIN_INTERSTITIAL_AD_INTERVAL / 1000) + "秒，还需等待: "
                    + ((MIN_INTERSTITIAL_AD_INTERVAL - timeSinceLastAd) / 1000) + "秒");
            return false;
        }

        // 其他情况下显示插屏广告
        return true;
    }

    /**
     * 加载用户答题统计
     */
    private void loadUserAnswerStats() {
        apiManager.getUserAnswerStats(new ApiCallback<AnswerStats>() { // 修改回调类型
            @Override
            public void onSuccess(AnswerStats stats) {
                updateAnswerStats(stats.getTodayCount(), stats.getTotalCount(), stats.getTodayCorrectCount());
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "加载答题统计失败: " + error);
                // 检查是否是答题数量限制导致的失败
                if (error != null && error.contains("您今天已答10题")) {
                    runOnUiThread(() -> {
                        Toast.makeText(QuizActivity.this, error, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(QuizActivity.this,
                                com.fortunequizking.activity.LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    // 其他错误情况下，使用本地缓存或默认值
                    int todayCount = SharedPreferenceUtil.getInt(QuizActivity.this, "today_answer_count", 0);
                    int totalCount = SharedPreferenceUtil.getInt(QuizActivity.this, "total_answer_count", 0);
                    int todayCorrectCount = SharedPreferenceUtil.getInt(QuizActivity.this, "today_correct_count", 0);
                    updateAnswerStats(todayCount, totalCount, todayCorrectCount);
                }
            }
        });
    }

    /**
     * 更新答题统计显示
     */
    private void updateAnswerStats(int todayCount, int totalCount, int todayCorrectCount) {
        Log.d(TAG, "更新答题统计: 今日答题=" + todayCount + ", 今日答对=" + todayCorrectCount + ", 累计答题=" + totalCount);

        // 使用专用的statsText控件显示完整的答题统计
        if (statsText != null) {
            Log.d(TAG, "使用statsText显示完整答题统计");
            statsText.setText("今日答题: " + todayCount + "题 今日答对: " + todayCorrectCount + "题 累计答题: " + totalCount + "题");
        }

        // 使用livesText控件只显示今日答题数和历史答题数
        if (livesText != null) {
            Log.d(TAG, "使用livesText显示简化的答题统计");
            livesText.setText("今日答题: " + todayCount + "题 历史答题: " + totalCount + "题");
        } else {
            Log.e(TAG, "无法找到livesText视图");
        }

        // 保存到本地缓存
        Log.d(TAG, "保存答题统计到本地缓存");
        SharedPreferenceUtil.putInt(QuizActivity.this, "today_answer_count", todayCount);
        SharedPreferenceUtil.putInt(QuizActivity.this, "total_answer_count", totalCount);

        // 强制UI刷新
        runOnUiThread(() -> {
            if (statsText != null) {
                statsText.invalidate();
                statsText.requestLayout();
            }
            if (livesText != null) {
                livesText.invalidate();
                livesText.requestLayout();
            }
        });
    }

    /**
     * 启动横幅广告刷新计时器
     */
    private void startBannerAdRefreshTimer() {
        // 先取消已有的计时器
        cancelBannerAdRefreshTimer();

        bannerAdRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                // 检查Activity状态
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                Log.d(TAG, "横幅广告10秒刷新时间到，重新加载广告");
                ViewGroup bannerContainer = findViewById(R.id.banner_ad_container);
                if (bannerContainer != null) {
                    TakuAdManager.getInstance().showBannerAd(QuizActivity.this, bannerContainer);
                }
            }
        };

        // 10秒后刷新广告
        bannerAdRefreshHandler.postDelayed(bannerAdRefreshRunnable, REFRESH_INTERVAL);
    }

    /**
     * 取消横幅广告刷新计时器
     */
    private void cancelBannerAdRefreshTimer() {
        if (bannerAdRefreshRunnable != null) {
            bannerAdRefreshHandler.removeCallbacks(bannerAdRefreshRunnable);
            bannerAdRefreshRunnable = null;
        }
    }

    /**
     * 统一的风险检查方法
     *
     * @param context       上下文信息，用于日志记录
     * @param handleFailure 是否处理失败情况（初始化时需要处理失败，广告曝光时不需要）
     */
    private void performRiskCheck(String context, boolean handleFailure) {
        if (isRiskCheck) {
            return;
        }
        isRiskCheck = true;
        String userId = SharedPreferenceUtil.getString(QuizActivity.this, "user_id", "");
        if (!userId.isEmpty()) {
            // 如果是初始化场景，先立即启动体力冷却计时器，避免等待风控检查
            if (context.equals("初始化")) {
                isAdCooldownActive = true;
                startAdCooldownTimer();
            }

            // 只有在非初始化场景时才暂停计时器，避免暂停刚刚启动的体力冷却计时器
            if (!context.equals("初始化")) {
                pauseAllTimers();
            }

            apiManager.checkRisk(userId, new ApiCallback<Object>() {
                @Override
                public void onSuccess(Object result) {
                    boolean isRiskTriggered = false;
                    String riskType = "";
                    if (result != null) {
                        if (result instanceof Map) {
                            Map<String, Object> data = (Map<String, Object>) result;
                            if (data.containsKey("risk_triggered")) {
                                Object riskTriggeredObj = data.get("risk_triggered");
                                if (riskTriggeredObj instanceof Number) {
                                    isRiskTriggered = ((Number) riskTriggeredObj).intValue() == 1;
                                } else if (riskTriggeredObj instanceof Boolean) {
                                    isRiskTriggered = (Boolean) riskTriggeredObj;
                                } else if (riskTriggeredObj instanceof String) {
                                    String riskTriggeredStr = (String) riskTriggeredObj;
                                    isRiskTriggered = "1".equals(riskTriggeredStr)
                                            || "true".equalsIgnoreCase(riskTriggeredStr);
                                }
                            }

                            // 获取风控类型
                            if (data.containsKey("risk_type")) {
                                Object riskTypeObj = data.get("risk_type");
                                if (riskTypeObj instanceof String) {
                                    riskType = (String) riskTypeObj;
                                }
                            }
                        } else if (result instanceof String) {
                            String riskResult = (String) result;
                            isRiskTriggered = "risk_triggered_hard_question".equals(riskResult);
                        }
                    }

                    // 设置风控状态
                    riskControlTriggered = isRiskTriggered;

                    // 保存之前的风控等级，用于检测变化
                    int previousAdCooldownLevel = currentAdCooldownLevel;

                    // 从后端获取层级信息
                    if (result instanceof Map) {
                        Map<String, Object> data = (Map<String, Object>) result;
                        if (data.containsKey("risk_level")) {
                            Object riskLevelObj = data.get("risk_level");
                            if (riskLevelObj instanceof Number) {
                                currentAdCooldownLevel = ((Number) riskLevelObj).intValue();
                            } else if (riskLevelObj instanceof String) {
                                try {
                                    currentAdCooldownLevel = Integer.parseInt((String) riskLevelObj);
                                } catch (NumberFormatException e) {
                                    currentAdCooldownLevel = 1; // 解析失败时使用第一层
                                }
                            }
                        } else if (isRiskTriggered) {
                            // 如果没有层级信息但触发了风控，设置为第一层
                            currentAdCooldownLevel = 1;
                        }
                    } else if (isRiskTriggered) {
                        // 非Map类型结果但触发了风控，设置为第一层
                        currentAdCooldownLevel = 1;
                    }

                    // 处理邀约风控类型
                    if ("invitation".equals(riskType) && !isInvitationDialogShown) {
                        // 获取渠道名称
                        String channelName = apiManager.getChannel();
                        // 显示邀约风控弹窗
                        showInvitationDialog(userId, channelName);
                        // 标记弹窗已显示
                        isInvitationDialogShown = true;
                    }

                    if (context.equals("初始化")) {
                        // 如果风控检查结果与当前状态不同，更新状态
                        if (isAdCooldownActive != isRiskTriggered) {
                            isAdCooldownActive = isRiskTriggered;
                            // 如果风控触发状态变化，重新启动计时器
                            if (adCooldownTimer != null) {
                                adCooldownTimer.cancel();
                                adCooldownTimer = null;
                            }
                            startAdCooldownTimer();
                        }
                    } else {
                        resumeAllTimers();
                    }
                    Log.d(TAG, context + "风控检查结果: " + isRiskTriggered + ", 当前层级: " + currentAdCooldownLevel + ", 风控类型: "
                            + riskType);
                    isRiskCheck = false;
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, context + "风控检查失败: " + error);
                    if (handleFailure) {
                        // 失败时默认触发风控
                        riskControlTriggered = true;
                        isAdCooldownActive = true;
                        isRiskCheck = false;
                        startAdCooldownTimer();
                    }
                }
            });
        } else if (handleFailure) {
            // 用户ID为空时默认触发风控
            riskControlTriggered = true;
            isAdCooldownActive = true;
            isRiskCheck = false;
            startAdCooldownTimer();
        }
    }

    /**
     * 显示邀约风控弹窗
     */
    private void showInvitationDialog(String userId, String channelName) {
        try {
            // 获取弹窗视图
            RelativeLayout invitationDialog = findViewById(R.id.invitation_dialog);
            TextView invitationContent = findViewById(R.id.invitation_content);
            Button invitationConfirmButton = findViewById(R.id.invitation_confirm_button);

            if (invitationDialog != null && invitationContent != null && invitationConfirmButton != null) {
                String content = "用户" + userId + "，恭喜您获得续做任务的机会，激励不变，请截此图后，到（" + channelName + "）领取续做任务。";
                invitationContent.setText(content);

                // 设置关闭按钮点击事件
                invitationConfirmButton.setOnClickListener(v -> {
                    invitationDialog.setVisibility(View.GONE);
                });

                // 显示弹窗
                invitationDialog.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "显示邀约风控弹窗失败: " + e.getMessage());
        }
    }

    /**
     * 启动原生广告刷新计时器
     */
    private void startNativeAdRefreshTimer() {
        // 先取消已有的计时器
        cancelNativeAdRefreshTimer();

        nativeAdRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                // 检查Activity状态
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                Log.d(TAG, "原生广告10秒刷新时间到，重新加载广告");
                ViewGroup nativeContainer = findViewById(R.id.native_ad_container);
                if (nativeContainer != null) {
                    TakuAdManager.getInstance().showNativeAd(QuizActivity.this, nativeContainer);
                }
            }
        };

        // 10秒后刷新广告
        nativeAdRefreshHandler.postDelayed(nativeAdRefreshRunnable, REFRESH_INTERVAL);
    }

    /**
     * 取消原生广告刷新计时器
     */
    private void cancelNativeAdRefreshTimer() {
        if (nativeAdRefreshRunnable != null) {
            nativeAdRefreshHandler.removeCallbacks(nativeAdRefreshRunnable);
            nativeAdRefreshRunnable = null;
        }
    }
}
