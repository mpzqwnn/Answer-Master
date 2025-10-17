#import "AndroidMirrorViewController.h"

// 广告相关常量 - 与Android项目保持一致
static const NSInteger AD_COOLDOWN_TIME_NORMAL = 60000; // 正常用户1分钟倒计时
static const NSInteger AD_COOLDOWN_TIME_RISK = 180000; // 触发风控用户3分钟倒计时
static const NSInteger AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_1 = 180000; // 层级处理第一层3分钟
static const NSInteger AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_2 = 300000; // 层级处理第二层5分钟
static const NSInteger REFRESH_INTERVAL = 8000; // 广告刷新间隔8秒
static const NSInteger MIN_INTERSTITIAL_AD_INTERVAL = 10000; // 最小插屏广告间隔10秒
static const NSInteger MAX_AD_CHECK_TIME = 30000; // 最大广告检查时间30秒

@interface AndroidMirrorViewController ()

// 应用ID和AppKey（根据Taku iOS Demo配置）
@property (nonatomic, strong) NSString *TAKU_APP_ID;
@property (nonatomic, strong) NSString *TAKU_APP_KEY;

// 广告位ID - 根据苹果SDK配置
@property (nonatomic, strong) NSString *REWARDED_VIDEO_PLACEMENT_ID;
@property (nonatomic, strong) NSString *INTERSTITIAL_PLACEMENT_ID;
@property (nonatomic, strong) NSString *BANNER_PLACEMENT_ID;

// 模拟Android项目的UI组件
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UILabel *questionLabel;
@property (nonatomic, strong) NSArray<UIButton *> *optionButtons;
@property (nonatomic, strong) UILabel *livesLabel;
@property (nonatomic, strong) UILabel *staminaLabel;
@property (nonatomic, strong) UILabel *userNameLabel;
@property (nonatomic, strong) UIButton *watchAdButton;
@property (nonatomic, strong) UIButton *levelButton;
@property (nonatomic, strong) UIButton *settingsButton;
@property (nonatomic, strong) UIButton *feedbackButton;
@property (nonatomic, strong) UILabel *statsText;
@property (nonatomic, strong) UIView *bannerAdView; // 横幅广告容器

// 模拟数据
@property (nonatomic, assign) NSInteger currentQuestionIndex;
@property (nonatomic, assign) NSInteger lives;
@property (nonatomic, assign) NSInteger stamina;
@property (nonatomic, strong) NSString *userName;
@property (nonatomic, assign) NSInteger currentLevel;
@property (nonatomic, assign) NSInteger correctAnswers;
@property (nonatomic, assign) NSInteger totalAnswers;
@property (nonatomic, assign) NSInteger currentScore;

// 广告和风控相关变量
@property (nonatomic, assign) NSInteger currentAdCooldownLevel; // 当前广告冷却层级
@property (nonatomic, assign) NSInteger staminaCooldownTime; // 体力冷却时间
@property (nonatomic, assign) BOOL isAdCooldownActive; // 广告冷却状态
@property (nonatomic, assign) NSTimeInterval lastAdRewardTime; // 上次获得奖励的时间
@property (nonatomic, assign) BOOL isRewardAdPlaying; // 激励广告是否正在播放
@property (nonatomic, assign) BOOL isRiskCheck; // 风控检查状态
@property (nonatomic, assign) BOOL riskControlTriggered; // 风控触发状态
@property (nonatomic, assign) BOOL isInterstitialAdLoaded; // 插屏广告是否已加载
@property (nonatomic, assign) BOOL isLoadingInterstitialAd; // 是否正在加载插屏广告
@property (nonatomic, assign) BOOL isInterstitialAdShowing; // 插屏广告是否正在显示
@property (nonatomic, assign) NSTimeInterval lastInterstitialAdShownTime; // 上次显示插屏广告的时间
@property (nonatomic, assign) NSTimeInterval adCheckStartTime; // 广告检查开始时间

// 计时器
@property (nonatomic, strong) NSTimer *adCooldownTimer;
@property (nonatomic, strong) NSTimer *bannerAdRefreshTimer;
@property (nonatomic, strong) NSTimer *nativeAdRefreshTimer;
@property (nonatomic, strong) NSTimer *adCheckTimer;

// 模拟题目数据
@property (nonatomic, strong) NSArray<NSDictionary *> *questions;

@end

@implementation AndroidMirrorViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // 初始化常量
    self.TAKU_APP_ID = @"a67f4ab312d2be";
    self.TAKU_APP_KEY = @"7eae0567827cfe2b22874061763f30c9";
    self.REWARDED_VIDEO_PLACEMENT_ID = @"b67f4ab43d2fe1";
    self.INTERSTITIAL_PLACEMENT_ID = @"b67f4ab43d2fe2";
    self.BANNER_PLACEMENT_ID = @"b67f4ab43d2fe3";
    
    // 初始化题目数据
    self.questions = @[
        @{@"text": @"中国的首都是哪个城市？", @"options": @[@"上海", @"北京", @"广州", @"深圳"], @"correctIndex": @1},
        @{@"text": @"以下哪个不是编程语言？", @"options": @[@"Python", @"Java", @"HTML", @"Swift"], @"correctIndex": @2},
        @{@"text": @"太阳系中最大的行星是？", @"options": @[@"地球", @"火星", @"木星", @"土星"], @"correctIndex": @2},
        @{@"text": @"水的化学式是？", @"options": @[@"H2O", @"CO2", @"O2", @"N2"], @"correctIndex": @0},
        @{@"text": @"以下哪个不是水果？", @"options": @[@"苹果", @"香蕉", @"土豆", @"橙子"], @"correctIndex": @2}
    ];
    
    // 初始化UI组件
    [self initializeUIComponents];
    
    // 检查是否是第二次登录
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    BOOL isSecondLogin = [defaults boolForKey:@"has_quiz_loaded"];
    
    if (isSecondLogin) {
        // 第二次登录，显示简化界面
        [self setupSecondLoginUI];
    } else {
        // 第一次登录，显示完整答题界面
        [self setupUI];
        [self initializeAdSDK];
        [self loadQuestion];
        [self updateStats];
        [self startAdRefreshTimers];
        [self fetchAppConfig];
    }
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    // 恢复计时器
    [self resumeTimers];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    // 暂停计时器
    [self pauseTimers];
}

- (void)dealloc {
    // 清理计时器
    [self invalidateAllTimers];
}

#pragma mark - UI初始化

- (void)initializeUIComponents {
    // 初始化UI组件
    self.titleLabel = [[UILabel alloc] init];
    self.questionLabel = [[UILabel alloc] init];
    
    // 初始化选项按钮
    NSMutableArray *buttons = [NSMutableArray array];
    for (int i = 0; i < 4; i++) {
        UIButton *button = [UIButton buttonWithType:UIButtonTypeSystem];
        [buttons addObject:button];
    }
    self.optionButtons = [buttons copy];
    
    self.livesLabel = [[UILabel alloc] init];
    self.staminaLabel = [[UILabel alloc] init];
    self.userNameLabel = [[UILabel alloc] init];
    self.watchAdButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.levelButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.settingsButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.feedbackButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.statsText = [[UILabel alloc] init];
    self.bannerAdView = [[UIView alloc] init];
    
    // 初始化数据
    self.currentQuestionIndex = 0;
    self.lives = 3;
    self.stamina = 5;
    self.userName = @"用户123";
    self.currentLevel = 1;
    self.correctAnswers = 0;
    self.totalAnswers = 0;
    self.currentScore = 0;
    
    // 广告相关变量初始化
    self.currentAdCooldownLevel = 0;
    self.staminaCooldownTime = AD_COOLDOWN_TIME_NORMAL;
    self.isAdCooldownActive = NO;
    self.lastAdRewardTime = 0;
    self.isRewardAdPlaying = NO;
    self.isRiskCheck = NO;
    self.riskControlTriggered = NO;
    self.isInterstitialAdLoaded = NO;
    self.isLoadingInterstitialAd = NO;
    self.isInterstitialAdShowing = NO;
    self.lastInterstitialAdShownTime = 0;
    self.adCheckStartTime = 0;
}

- (void)setupUI {
    self.view.backgroundColor = [UIColor colorWithRed:0.95 green:0.95 blue:0.95 alpha:1.0];
    
    // 标题
    self.titleLabel.text = @"答题大师";
    self.titleLabel.font = [UIFont boldSystemFontOfSize:24];
    self.titleLabel.textAlignment = NSTextAlignmentCenter;
    self.titleLabel.textColor = [UIColor darkGrayColor];
    
    // 用户信息区域
    UIView *userInfoView = [self createUserInfoView];
    
    // 题目区域
    self.questionLabel.font = [UIFont systemFontOfSize:18];
    self.questionLabel.textAlignment = NSTextAlignmentCenter;
    self.questionLabel.numberOfLines = 0;
    self.questionLabel.textColor = [UIColor darkGrayColor];
    
    // 选项按钮
    for (int i = 0; i < self.optionButtons.count; i++) {
        UIButton *button = self.optionButtons[i];
        [button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        button.titleLabel.font = [UIFont systemFontOfSize:16];
        button.backgroundColor = [UIColor systemBlueColor];
        button.layer.cornerRadius = 8;
        button.tag = i;
        [button addTarget:self action:@selector(optionSelected:) forControlEvents:UIControlEventTouchUpInside];
    }
    
    // 底部按钮区域
    UIView *bottomButtonsView = [self createBottomButtonsView];
    
    // 横幅广告区域
    self.bannerAdView.backgroundColor = [UIColor lightGrayColor];
    self.bannerAdView.layer.cornerRadius = 8;
    self.bannerAdView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 布局
    UIStackView *stackView = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.titleLabel,
        userInfoView,
        self.questionLabel
    ]];
    
    stackView.axis = UILayoutConstraintAxisVertical;
    stackView.spacing = 20;
    stackView.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:stackView];
    [self.view addSubview:bottomButtonsView];
    [self.view addSubview:self.bannerAdView];
    
    // 选项按钮布局
    UIStackView *optionsStack = [[UIStackView alloc] initWithArrangedSubviews:self.optionButtons];
    optionsStack.axis = UILayoutConstraintAxisVertical;
    optionsStack.spacing = 10;
    optionsStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:optionsStack];
    
    [NSLayoutConstraint activateConstraints:@[
        [stackView.topAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.topAnchor constant:20],
        [stackView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:20],
        [stackView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-20],
        
        [optionsStack.topAnchor constraintEqualToAnchor:stackView.bottomAnchor constant:30],
        [optionsStack.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:20],
        [optionsStack.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-20],
        
        [self.bannerAdView.topAnchor constraintEqualToAnchor:optionsStack.bottomAnchor constant:20],
        [self.bannerAdView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:20],
        [self.bannerAdView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-20],
        [self.bannerAdView.heightAnchor constraintEqualToConstant:50],
        
        [bottomButtonsView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:20],
        [bottomButtonsView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-20],
        [bottomButtonsView.bottomAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.bottomAnchor constant:-20]
    ]];
    
    // 设置选项按钮高度
    for (UIButton *button in self.optionButtons) {
        [button.heightAnchor constraintEqualToConstant:50].active = YES;
    }
}

- (void)setupSecondLoginUI {
    // 清除现有视图
    for (UIView *subview in self.view.subviews) {
        [subview removeFromSuperview];
    }
    
    self.view.backgroundColor = [UIColor colorWithRed:0.95 green:0.95 blue:0.95 alpha:1.0];
    
    // 欢迎标题
    UILabel *welcomeLabel = [[UILabel alloc] init];
    welcomeLabel.text = @"欢迎回来！";
    welcomeLabel.font = [UIFont boldSystemFontOfSize:28];
    welcomeLabel.textAlignment = NSTextAlignmentCenter;
    welcomeLabel.textColor = [UIColor darkGrayColor];
    
    // 开始游戏按钮
    UIButton *startGameButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [startGameButton setTitle:@"开始游戏" forState:UIControlStateNormal];
    startGameButton.titleLabel.font = [UIFont boldSystemFontOfSize:20];
    startGameButton.backgroundColor = [UIColor systemGreenColor];
    [startGameButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    startGameButton.layer.cornerRadius = 12;
    [startGameButton addTarget:self action:@selector(startGame) forControlEvents:UIControlEventTouchUpInside];
    
    // 渠道选择标题
    UILabel *channelLabel = [[UILabel alloc] init];
    channelLabel.text = @"选择游戏渠道";
    channelLabel.font = [UIFont systemFontOfSize:16];
    channelLabel.textAlignment = NSTextAlignmentCenter;
    channelLabel.textColor = [UIColor darkGrayColor];
    
    // 渠道选择按钮
    NSArray<UIButton *> *channelButtons = @[
        [self createChannelButtonWithTitle:@"官方渠道" tag:0],
        [self createChannelButtonWithTitle:@"合作渠道" tag:1],
        [self createChannelButtonWithTitle:@"测试渠道" tag:2]
    ];
    
    UIStackView *channelStack = [[UIStackView alloc] initWithArrangedSubviews:channelButtons];
    channelStack.axis = UILayoutConstraintAxisVertical;
    channelStack.spacing = 12;
    channelStack.distribution = UIStackViewDistributionFillEqually;
    
    // 布局
    UIStackView *mainStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        welcomeLabel,
        startGameButton,
        channelLabel,
        channelStack
    ]];
    mainStack.axis = UILayoutConstraintAxisVertical;
    mainStack.spacing = 30;
    mainStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:mainStack];
    
    [NSLayoutConstraint activateConstraints:@[
        [mainStack.centerXAnchor constraintEqualToAnchor:self.view.centerXAnchor],
        [mainStack.centerYAnchor constraintEqualToAnchor:self.view.centerYAnchor],
        [mainStack.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:40],
        [mainStack.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-40],
        
        [startGameButton.heightAnchor constraintEqualToConstant:60],
        [channelButtons.firstObject.heightAnchor constraintEqualToConstant:50]
    ]];
}

- (UIButton *)createChannelButtonWithTitle:(NSString *)title tag:(NSInteger)tag {
    UIButton *button = [UIButton buttonWithType:UIButtonTypeSystem];
    [button setTitle:title forState:UIControlStateNormal];
    button.titleLabel.font = [UIFont systemFontOfSize:16];
    button.backgroundColor = [UIColor systemBlueColor];
    [button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    button.layer.cornerRadius = 8;
    button.tag = tag;
    [button addTarget:self action:@selector(channelSelected:) forControlEvents:UIControlEventTouchUpInside];
    return button;
}

- (void)startGame {
    // 切换到完整答题界面
    [self setupUI];
    [self initializeAdSDK];
    [self loadQuestion];
    [self updateStats];
    [self startAdRefreshTimers];
    [self fetchAppConfig];
}

- (void)channelSelected:(UIButton *)sender {
    NSArray<NSString *> *channelNames = @[@"官方渠道", @"合作渠道", @"测试渠道"];
    NSString *selectedChannel = channelNames[sender.tag];
    
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"渠道选择" message:[NSString stringWithFormat:@"您选择了：%@", selectedChannel] preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}

- (UIView *)createUserInfoView {
    UIView *containerView = [[UIView alloc] init];
    containerView.backgroundColor = [UIColor whiteColor];
    containerView.layer.cornerRadius = 8;
    
    // 用户名
    self.userNameLabel.text = self.userName;
    self.userNameLabel.font = [UIFont boldSystemFontOfSize:16];
    
    // 生命值
    self.livesLabel.font = [UIFont systemFontOfSize:14];
    self.livesLabel.textColor = [UIColor systemRedColor];
    
    // 体力值
    self.staminaLabel.font = [UIFont systemFontOfSize:14];
    self.staminaLabel.textColor = [UIColor systemBlueColor];
    
    // 等级
    UILabel *levelLabel = [[UILabel alloc] init];
    levelLabel.text = [NSString stringWithFormat:@"等级: %ld", (long)self.currentLevel];
    levelLabel.font = [UIFont systemFontOfSize:14];
    
    UIStackView *statsStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.livesLabel,
        self.staminaLabel,
        levelLabel
    ]];
    statsStack.axis = UILayoutConstraintAxisHorizontal;
    statsStack.spacing = 15;
    statsStack.distribution = UIStackViewDistributionFillEqually;
    
    UIStackView *mainStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.userNameLabel,
        statsStack
    ]];
    mainStack.axis = UILayoutConstraintAxisVertical;
    mainStack.spacing = 8;
    mainStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    [containerView addSubview:mainStack];
    
    [NSLayoutConstraint activateConstraints:@[
        [mainStack.topAnchor constraintEqualToAnchor:containerView.topAnchor constant:12],
        [mainStack.leadingAnchor constraintEqualToAnchor:containerView.leadingAnchor constant:12],
        [mainStack.trailingAnchor constraintEqualToAnchor:containerView.trailingAnchor constant:-12],
        [mainStack.bottomAnchor constraintEqualToAnchor:containerView.bottomAnchor constant:-12]
    ]];
    
    return containerView;
}

- (UIView *)createBottomButtonsView {
    UIView *containerView = [[UIView alloc] init];
    
    // 看广告按钮
    [self.watchAdButton setTitle:@"看广告恢复体力" forState:UIControlStateNormal];
    self.watchAdButton.titleLabel.font = [UIFont systemFontOfSize:14];
    [self.watchAdButton addTarget:self action:@selector(watchAd) forControlEvents:UIControlEventTouchUpInside];
    
    // 等级按钮
    [self.levelButton setTitle:@"等级详情" forState:UIControlStateNormal];
    self.levelButton.titleLabel.font = [UIFont systemFontOfSize:14];
    [self.levelButton addTarget:self action:@selector(showLevelDetails) forControlEvents:UIControlEventTouchUpInside];
    
    // 设置按钮
    [self.settingsButton setTitle:@"设置" forState:UIControlStateNormal];
    self.settingsButton.titleLabel.font = [UIFont systemFontOfSize:14];
    [self.settingsButton addTarget:self action:@selector(showSettings) forControlEvents:UIControlEventTouchUpInside];
    
    // 反馈按钮
    [self.feedbackButton setTitle:@"意见反馈" forState:UIControlStateNormal];
    self.feedbackButton.titleLabel.font = [UIFont systemFontOfSize:14];
    [self.feedbackButton addTarget:self action:@selector(showFeedback) forControlEvents:UIControlEventTouchUpInside];
    
    UIStackView *buttonsStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.watchAdButton,
        self.levelButton,
        self.settingsButton,
        self.feedbackButton
    ]];
    buttonsStack.axis = UILayoutConstraintAxisHorizontal;
    buttonsStack.spacing = 10;
    buttonsStack.distribution = UIStackViewDistributionFillEqually;
    buttonsStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    [containerView addSubview:buttonsStack];
    
    [NSLayoutConstraint activateConstraints:@[
        [buttonsStack.topAnchor constraintEqualToAnchor:containerView.topAnchor],
        [buttonsStack.leadingAnchor constraintEqualToAnchor:containerView.leadingAnchor],
        [buttonsStack.trailingAnchor constraintEqualToAnchor:containerView.trailingAnchor],
        [buttonsStack.bottomAnchor constraintEqualToAnchor:containerView.bottomAnchor],
        [buttonsStack.heightAnchor constraintEqualToConstant:40]
    ]];
    
    return containerView;
}

#pragma mark - 广告SDK初始化

- (void)initializeAdSDK {
    // 首先检查隐私合规状态
    [self checkPrivacyCompliance];
    
    @try {
        // 设置自定义数据
        NSDictionary *customData = @{
            @"app_id": self.TAKU_APP_ID,
            @"app_key": self.TAKU_APP_KEY
        };
        
        // 配置SDK参数
        [ATAPI setLogEnabled:YES]; // 开启日志
        [ATAPI integrationChecking]; // 开启集成检测
        
        // 初始化SDK
        [[ATAPI sharedInstance] startWithAppID:self.TAKU_APP_ID appKey:self.TAKU_APP_KEY error:nil];
        
        // 设置广告代理
        // 注意：iOS SDK使用统一的ATAdManager，代理设置在各广告类型展示时进行
        
        NSLog(@"广告SDK初始化成功");
        
        // 初始化成功后加载广告
        [self loadAds];
        
    } @catch (NSException *exception) {
        NSLog(@"广告SDK初始化失败: %@", exception);
    }
}

#pragma mark - 隐私合规配置

- (void)checkPrivacyCompliance {
    // 检查用户隐私授权状态
    if (@available(iOS 14, *)) {
        [ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
            dispatch_async(dispatch_get_main_queue(), ^{
                switch (status) {
                    case ATTrackingManagerAuthorizationStatusAuthorized:
                        NSLog(@"用户已授权广告追踪");
                        [self setPrivacyConsent:YES];
                        break;
                    case ATTrackingManagerAuthorizationStatusDenied:
                        NSLog(@"用户拒绝广告追踪");
                        [self setPrivacyConsent:NO];
                        break;
                    case ATTrackingManagerAuthorizationStatusNotDetermined:
                        NSLog(@"用户未决定广告追踪");
                        [self setPrivacyConsent:NO];
                        break;
                    case ATTrackingManagerAuthorizationStatusRestricted:
                        NSLog(@"广告追踪受限");
                        [self setPrivacyConsent:NO];
                        break;
                    default:
                        NSLog(@"未知的广告追踪状态");
                        [self setPrivacyConsent:NO];
                        break;
                }
            });
        }];
    } else {
        // iOS 14以下版本，默认授权
        [self setPrivacyConsent:YES];
    }
}

- (void)setPrivacyConsent:(BOOL)consent {
    // 设置用户隐私同意状态
    // 注意：iOS SDK中隐私设置通常在初始化时通过customData参数配置
    // 这里只需要记录状态，实际配置在初始化时完成
    
    if (consent) {
        NSLog(@"用户同意隐私政策，启用个性化广告");
    } else {
        NSLog(@"用户拒绝隐私政策，使用非个性化广告");
    }
}

// 加载所有广告
- (void)loadAds {
    [self loadBannerAd];
    [self loadInterstitialAd];
    [self loadRewardedVideoAd];
}

// 加载横幅广告
- (void)loadBannerAd {
    NSString *placementID = self.BANNER_PLACEMENT_ID;
    
    // 设置横幅广告尺寸
    CGSize bannerSize = CGSizeMake(320, 50);
    NSDictionary *extra = @{kATAdLoadingExtraBannerAdSizeKey: [NSValue valueWithCGSize:bannerSize]};
    
    // 加载横幅广告
    [[ATAdManager sharedManager] loadADWithPlacementID:placementID extra:extra delegate:self];
    NSLog(@"开始加载横幅广告: %@", placementID);
}

// 加载插屏广告
- (void)loadInterstitialAd {
    NSString *placementID = self.INTERSTITIAL_PLACEMENT_ID;
    
    // 加载插屏广告
    [[ATAdManager sharedManager] loadADWithPlacementID:placementID extra:nil delegate:self];
    NSLog(@"开始加载插屏广告: %@", placementID);
}

// 加载激励视频广告
- (void)loadRewardedVideoAd {
    NSString *placementID = self.REWARDED_VIDEO_PLACEMENT_ID;
    
    // 加载激励视频广告
    [[ATAdManager sharedManager] loadADWithPlacementID:placementID extra:nil delegate:self];
    NSLog(@"开始加载激励视频广告: %@", placementID);
}

#pragma mark - 答题逻辑

- (void)loadQuestion {
    if (self.currentQuestionIndex >= self.questions.count) {
        self.currentQuestionIndex = 0;
    }
    
    NSDictionary *question = self.questions[self.currentQuestionIndex];
    self.questionLabel.text = question[@"text"];
    
    NSArray *options = question[@"options"];
    for (int i = 0; i < self.optionButtons.count; i++) {
        if (i < options.count) {
            [self.optionButtons[i] setTitle:options[i] forState:UIControlStateNormal];
        }
    }
}

- (void)optionSelected:(UIButton *)sender {
    NSDictionary *question = self.questions[self.currentQuestionIndex];
    NSInteger correctIndex = [question[@"correctIndex"] integerValue];
    
    self.totalAnswers++;
    
    if (sender.tag == correctIndex) {
        // 回答正确
        self.correctAnswers++;
        self.currentScore += 10;
        self.stamina--;
        
        // 显示正确反馈
        [self showAnswerFeedback:YES];
    } else {
        // 回答错误
        self.lives--;
        
        // 显示错误反馈
        [self showAnswerFeedback:NO];
    }
    
    [self updateStats];
    
    // 检查游戏状态
    [self checkGameStatus];
    
    // 下一题
    self.currentQuestionIndex++;
    if (self.currentQuestionIndex >= self.questions.count) {
        self.currentQuestionIndex = 0;
    }
    
    // 延迟加载下一题
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self loadQuestion];
    });
}

- (void)showAnswerFeedback:(BOOL)isCorrect {
    NSString *message = isCorrect ? @"回答正确！" : @"回答错误！";
    UIColor *color = isCorrect ? [UIColor systemGreenColor] : [UIColor systemRedColor];
    
    UILabel *feedbackLabel = [[UILabel alloc] init];
    feedbackLabel.text = message;
    feedbackLabel.textColor = color;
    feedbackLabel.font = [UIFont boldSystemFontOfSize:18];
    feedbackLabel.textAlignment = NSTextAlignmentCenter;
    feedbackLabel.alpha = 0;
    
    [self.view addSubview:feedbackLabel];
    feedbackLabel.translatesAutoresizingMaskIntoConstraints = NO;
    
    [NSLayoutConstraint activateConstraints:@[
        [feedbackLabel.centerXAnchor constraintEqualToAnchor:self.view.centerXAnchor],
        [feedbackLabel.centerYAnchor constraintEqualToAnchor:self.view.centerYAnchor]
    ]];
    
    // 动画显示
    [UIView animateWithDuration:0.3 animations:^{
        feedbackLabel.alpha = 1;
    } completion:^(BOOL finished) {
        [UIView animateWithDuration:0.3 delay:0.5 options:0 animations:^{
            feedbackLabel.alpha = 0;
        } completion:^(BOOL finished) {
            [feedbackLabel removeFromSuperview];
        }];
    }];
}

- (void)updateStats {
    self.livesLabel.text = [NSString stringWithFormat:@"生命: %ld", (long)self.lives];
    self.staminaLabel.text = [NSString stringWithFormat:@"体力: %ld", (long)self.stamina];
    
    // 更新用户信息显示
    self.userNameLabel.text = self.userName;
}

- (void)checkGameStatus {
    if (self.lives <= 0) {
        // 游戏结束
        [self showGameOver];
    } else if (self.stamina <= 0) {
        // 体力不足
        [self showStaminaWarning];
    }
}

- (void)showGameOver {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"游戏结束" message:[NSString stringWithFormat:@"最终得分: %ld", (long)self.currentScore] preferredStyle:UIAlertControllerStyleAlert];
    
    [alert addAction:[UIAlertAction actionWithTitle:@"重新开始" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self restartGame];
    }]];
    
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)showStaminaWarning {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"体力不足" message:@"观看广告可以恢复体力" preferredStyle:UIAlertControllerStyleAlert];
    
    [alert addAction:[UIAlertAction actionWithTitle:@"观看广告" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self watchAd];
    }]];
    
    [alert addAction:[UIAlertAction actionWithTitle:@"稍后再说" style:UIAlertActionStyleCancel handler:nil]];
    
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)restartGame {
    self.lives = 3;
    self.stamina = 5;
    self.currentQuestionIndex = 0;
    self.correctAnswers = 0;
    self.totalAnswers = 0;
    self.currentScore = 0;
    
    [self loadQuestion];
    [self updateStats];
}

#pragma mark - 广告相关方法

- (void)watchAd {
    // 检查激励视频广告是否可用
    if ([[ATAdManager sharedManager] adReadyForPlacementID:self.REWARDED_VIDEO_PLACEMENT_ID]) {
        // 显示激励视频广告
        [[ATAdManager sharedManager] showRewardedVideoWithPlacementID:self.REWARDED_VIDEO_PLACEMENT_ID inViewController:self delegate:self];
        self.isRewardAdPlaying = YES;
    } else {
        // 广告未准备好
        UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"广告未准备好" message:@"请稍后再试" preferredStyle:UIAlertControllerStyleAlert];
        [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil]];
        [self presentViewController:alert animated:YES completion:nil];
    }
}

- (void)showLevelDetails {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"等级详情" message:[NSString stringWithFormat:@"当前等级: %ld\n正确率: %.1f%%", (long)self.currentLevel, (self.totalAnswers > 0 ? (float)self.correctAnswers / self.totalAnswers * 100 : 0)] preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)showSettings {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"设置" message:@"游戏设置功能" preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)showFeedback {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"意见反馈" message:@"反馈功能开发中" preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}

#pragma mark - 计时器管理

- (void)startAdRefreshTimers {
    // 横幅广告刷新计时器
    self.bannerAdRefreshTimer = [NSTimer scheduledTimerWithTimeInterval:REFRESH_INTERVAL / 1000.0 target:self selector:@selector(refreshBannerAd) userInfo:nil repeats:YES];
    
    // 原生广告刷新计时器
    self.nativeAdRefreshTimer = [NSTimer scheduledTimerWithTimeInterval:REFRESH_INTERVAL / 1000.0 target:self selector:@selector(refreshNativeAd) userInfo:nil repeats:YES];
}

- (void)refreshBannerAd {
    // 刷新横幅广告
    if (!self.isRewardAdPlaying && !self.isInterstitialAdShowing) {
        [self loadBannerAd];
    }
}

- (void)refreshNativeAd {
    // 刷新原生广告（如果需要）
    // 这里可以添加原生广告刷新逻辑
}

- (void)pauseTimers {
    [self.bannerAdRefreshTimer invalidate];
    [self.nativeAdRefreshTimer invalidate];
    self.bannerAdRefreshTimer = nil;
    self.nativeAdRefreshTimer = nil;
}

- (void)resumeTimers {
    [self startAdRefreshTimers];
}

- (void)invalidateAllTimers {
    [self.adCooldownTimer invalidate];
    [self.bannerAdRefreshTimer invalidate];
    [self.nativeAdRefreshTimer invalidate];
    [self.adCheckTimer invalidate];
    
    self.adCooldownTimer = nil;
    self.bannerAdRefreshTimer = nil;
    self.nativeAdRefreshTimer = nil;
    self.adCheckTimer = nil;
}

#pragma mark - 应用配置获取

- (void)fetchAppConfig {
    // 模拟获取应用配置
    // 在实际应用中，这里应该从服务器获取配置
    NSLog(@"获取应用配置");
}

#pragma mark - 广告代理方法

// 广告加载成功
- (void)didFinishLoadingADWithPlacementID:(NSString *)placementID {
    NSLog(@"广告加载成功: %@", placementID);
    
    if ([placementID isEqualToString:self.INTERSTITIAL_PLACEMENT_ID]) {
        self.isInterstitialAdLoaded = YES;
        self.isLoadingInterstitialAd = NO;
    }
}

// 广告加载失败
- (void)didFailToLoadADWithPlacementID:(NSString *)placementID error:(NSError *)error {
    NSLog(@"广告加载失败: %@, 错误: %@", placementID, error);
    
    if ([placementID isEqualToString:self.INTERSTITIAL_PLACEMENT_ID]) {
        self.isLoadingInterstitialAd = NO;
    }
}

// 激励视频广告播放完成
- (void)rewardedVideoDidRewardSuccessForPlacemenID:(NSString *)placementID extra:(NSDictionary *)extra {
    NSLog(@"激励视频广告奖励发放: %@", placementID);
    
    // 发放奖励
    self.stamina += 3; // 恢复3点体力
    self.lastAdRewardTime = [[NSDate date] timeIntervalSince1970];
    
    [self updateStats];
    
    // 显示奖励提示
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"奖励发放" message:@"体力已恢复3点！" preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}

// 激励视频广告播放完成
- (void)rewardedVideoDidEndPlayingForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
    NSLog(@"激励视频广告播放完成: %@", placementID);
    self.isRewardAdPlaying = NO;
}

// 激励视频广告播放失败
- (void)rewardedVideoDidFailToPlayForPlacementID:(NSString *)placementID error:(NSError *)error extra:(NSDictionary *)extra {
    NSLog(@"激励视频广告播放失败: %@, 错误: %@", placementID, error);
    self.isRewardAdPlaying = NO;
}

// 插屏广告展示成功
- (void)interstitialDidShowForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
    NSLog(@"插屏广告展示成功: %@", placementID);
    self.isInterstitialAdShowing = YES;
    self.lastInterstitialAdShownTime = [[NSDate date] timeIntervalSince1970];
}

// 插屏广告关闭
- (void)interstitialDidCloseForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
    NSLog(@"插屏广告关闭: %@", placementID);
    self.isInterstitialAdShowing = NO;
    self.isInterstitialAdLoaded = NO;
    
    // 重新加载插屏广告
    [self loadInterstitialAd];
}

// 横幅广告加载成功
- (void)bannerView:(ATBannerView *)bannerView didShowAdWithPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
    NSLog(@"横幅广告展示成功: %@", placementID);
    
    // 将横幅广告添加到容器中
    [self.bannerAdView.subviews makeObjectsPerformSelector:@selector(removeFromSuperview)];
    [self.bannerAdView addSubview:bannerView];
    
    // 设置横幅广告布局
    bannerView.translatesAutoresizingMaskIntoConstraints = NO;
    [NSLayoutConstraint activateConstraints:@[
        [bannerView.topAnchor constraintEqualToAnchor:self.bannerAdView.topAnchor],
        [bannerView.leadingAnchor constraintEqualToAnchor:self.bannerAdView.leadingAnchor],
        [bannerView.trailingAnchor constraintEqualToAnchor:self.bannerAdView.trailingAnchor],
        [bannerView.bottomAnchor constraintEqualToAnchor:self.bannerAdView.bottomAnchor]
    ]];
}

// 横幅广告加载失败
- (void)bannerView:(ATBannerView *)bannerView didFailToShowAdWithPlacementID:(NSString *)placementID error:(NSError *)error {
    NSLog(@"横幅广告展示失败: %@, 错误: %@", placementID, error);
}

@end