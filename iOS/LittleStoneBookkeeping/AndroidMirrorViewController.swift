import UIKit
import AnyThinkSDK
import AnyThinkRewardedVideo
import AnyThinkInterstitial
import AnyThinkBanner
import AppTrackingTransparency

class AndroidMirrorViewController: UIViewController {
    
    // 广告相关常量 - 与Android项目保持一致
    private static let AD_COOLDOWN_TIME_NORMAL = 60000 // 正常用户1分钟倒计时
    private static let AD_COOLDOWN_TIME_RISK = 180000 // 触发风控用户3分钟倒计时
    private static let AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_1 = 180000 // 层级处理第一层3分钟
    private static let AD_COOLDOWN_TIME_HIERARCHICAL_LEVEL_2 = 300000 // 层级处理第二层5分钟
    private static let REFRESH_INTERVAL = 8000 // 广告刷新间隔8秒
    private static let MIN_INTERSTITIAL_AD_INTERVAL = 10000 // 最小插屏广告间隔10秒
    private static let MAX_AD_CHECK_TIME = 30000 // 最大广告检查时间30秒
    
    // 应用ID和AppKey（根据Taku iOS Demo配置）
    private let TAKU_APP_ID = "a67f4ab312d2be"
    private let TAKU_APP_KEY = "7eae0567827cfe2b22874061763f30c9"
    
    // 广告位ID - 根据苹果SDK配置
    private let REWARDED_VIDEO_PLACEMENT_ID = "b67f4ab43d2fe1"
    private let INTERSTITIAL_PLACEMENT_ID = "b67f4ab43d2fe2"
    private let BANNER_PLACEMENT_ID = "b67f4ab43d2fe3"
    
    // 模拟Android项目的UI组件
    private let titleLabel = UILabel()
    private let questionLabel = UILabel()
    private let optionButtons = [UIButton(), UIButton(), UIButton(), UIButton()]
    private let livesLabel = UILabel()
    private let staminaLabel = UILabel()
    private let userNameLabel = UILabel()
    private let watchAdButton = UIButton(type: .system)
    private let levelButton = UIButton(type: .system)
    private let settingsButton = UIButton(type: .system)
    private let feedbackButton = UIButton(type: .system)
    private let statsText = UILabel()
    private let bannerAdView = UIView() // 横幅广告容器
    
    // 模拟数据
    private var currentQuestionIndex = 0
    private var lives = 3
    private var stamina = 5
    private var userName = "用户123"
    private var currentLevel = 1
    private var correctAnswers = 0
    private var totalAnswers = 0
    private var currentScore = 0
    
    // 广告和风控相关变量
    private var currentAdCooldownLevel = 0 // 当前广告冷却层级
    private var staminaCooldownTime = AD_COOLDOWN_TIME_NORMAL // 体力冷却时间
    private var isAdCooldownActive = false // 广告冷却状态
    private var lastAdRewardTime: TimeInterval = 0 // 上次获得奖励的时间
    private var isRewardAdPlaying = false // 激励广告是否正在播放
    private var isRiskCheck = false // 风控检查状态
    private var riskControlTriggered = false // 风控触发状态
    private var isInterstitialAdLoaded = false // 插屏广告是否已加载
    private var isLoadingInterstitialAd = false // 是否正在加载插屏广告
    private var isInterstitialAdShowing = false // 插屏广告是否正在显示
    private var lastInterstitialAdShownTime: TimeInterval = 0 // 上次显示插屏广告的时间
    private var adCheckStartTime: TimeInterval = 0 // 广告检查开始时间
    
    // 计时器
    private var adCooldownTimer: Timer?
    private var bannerAdRefreshTimer: Timer?
    private var nativeAdRefreshTimer: Timer?
    private var adCheckTimer: Timer?
    
    // 模拟题目数据
    private let questions = [
        Question(text: "中国的首都是哪个城市？", options: ["上海", "北京", "广州", "深圳"], correctIndex: 1),
        Question(text: "以下哪个不是编程语言？", options: ["Python", "Java", "HTML", "Swift"], correctIndex: 2),
        Question(text: "太阳系中最大的行星是？", options: ["地球", "火星", "木星", "土星"], correctIndex: 2),
        Question(text: "水的化学式是？", options: ["H2O", "CO2", "O2", "N2"], correctIndex: 0),
        Question(text: "以下哪个不是水果？", options: ["苹果", "香蕉", "土豆", "橙子"], correctIndex: 2)
    ]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // 检查是否是第二次登录
        let defaults = UserDefaults.standard
        let isSecondLogin = defaults.bool(forKey: "has_quiz_loaded")
        
        if isSecondLogin {
            // 第二次登录，显示简化界面
            setupSecondLoginUI()
        } else {
            // 第一次登录，显示完整答题界面
            setupUI()
            initializeAdSDK()
            loadQuestion()
            updateStats()
            startAdRefreshTimers()
            fetchAppConfig()
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        // 恢复计时器
        resumeTimers()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        // 暂停计时器
        pauseTimers()
    }
    
    deinit {
        // 清理计时器
        invalidateAllTimers()
    }
    
    private func setupUI() {
        view.backgroundColor = UIColor(red: 0.95, green: 0.95, blue: 0.95, alpha: 1.0)
        
        // 标题
        titleLabel.text = "答题大师"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 24)
        titleLabel.textAlignment = .center
        titleLabel.textColor = .darkGray
        
        // 用户信息区域
        let userInfoView = createUserInfoView()
        
        // 题目区域
        questionLabel.font = UIFont.systemFont(ofSize: 18)
        questionLabel.textAlignment = .center
        questionLabel.numberOfLines = 0
        questionLabel.textColor = .darkGray
        
        // 选项按钮
        for (index, button) in optionButtons.enumerated() {
            button.setTitleColor(.white, for: .normal)
            button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
            button.backgroundColor = UIColor.systemBlue
            button.layer.cornerRadius = 8
            button.tag = index
            button.addTarget(self, action: #selector(optionSelected(_:)), for: .touchUpInside)
        }
        
        // 底部按钮区域
        let bottomButtonsView = createBottomButtonsView()
        
        // 横幅广告区域
        bannerAdView.backgroundColor = .lightGray
        bannerAdView.layer.cornerRadius = 8
        bannerAdView.translatesAutoresizingMaskIntoConstraints = false
        
        // 布局
        let stackView = UIStackView(arrangedSubviews: [
            titleLabel,
            userInfoView,
            questionLabel
        ])
        
        stackView.axis = .vertical
        stackView.spacing = 20
        stackView.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(stackView)
        view.addSubview(bottomButtonsView)
        view.addSubview(bannerAdView)
        
        // 选项按钮布局
        let optionsStack = UIStackView(arrangedSubviews: optionButtons)
        optionsStack.axis = .vertical
        optionsStack.spacing = 10
        optionsStack.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(optionsStack)
        
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 20),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            
            optionsStack.topAnchor.constraint(equalTo: stackView.bottomAnchor, constant: 30),
            optionsStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            optionsStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            
            bannerAdView.topAnchor.constraint(equalTo: optionsStack.bottomAnchor, constant: 20),
            bannerAdView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            bannerAdView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            bannerAdView.heightAnchor.constraint(equalToConstant: 50),
            
            bottomButtonsView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            bottomButtonsView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            bottomButtonsView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -20)
        ])
        
        // 设置选项按钮高度
        for button in optionButtons {
            button.heightAnchor.constraint(equalToConstant: 50).isActive = true
        }
    }
    
    private func setupSecondLoginUI() {
        // 清除现有视图
        view.subviews.forEach { $0.removeFromSuperview() }
        
        view.backgroundColor = UIColor(red: 0.95, green: 0.95, blue: 0.95, alpha: 1.0)
        
        // 欢迎标题
        let welcomeLabel = UILabel()
        welcomeLabel.text = "欢迎回来！"
        welcomeLabel.font = UIFont.boldSystemFont(ofSize: 28)
        welcomeLabel.textAlignment = .center
        welcomeLabel.textColor = .darkGray
        
        // 开始游戏按钮
        let startGameButton = UIButton(type: .system)
        startGameButton.setTitle("开始游戏", for: .normal)
        startGameButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 20)
        startGameButton.backgroundColor = UIColor.systemGreen
        startGameButton.setTitleColor(.white, for: .normal)
        startGameButton.layer.cornerRadius = 12
        startGameButton.addTarget(self, action: #selector(startGame), for: .touchUpInside)
        
        // 渠道选择标题
        let channelLabel = UILabel()
        channelLabel.text = "选择游戏渠道"
        channelLabel.font = UIFont.systemFont(ofSize: 16)
        channelLabel.textAlignment = .center
        channelLabel.textColor = .darkGray
        
        // 渠道选择按钮
        let channelButtons = [
            createChannelButton(title: "官方渠道", tag: 0),
            createChannelButton(title: "合作渠道", tag: 1),
            createChannelButton(title: "测试渠道", tag: 2)
        ]
        
        let channelStack = UIStackView(arrangedSubviews: channelButtons)
        channelStack.axis = .vertical
        channelStack.spacing = 12
        channelStack.distribution = .fillEqually
        
        // 布局
        let mainStack = UIStackView(arrangedSubviews: [welcomeLabel, startGameButton, channelLabel, channelStack])
        mainStack.axis = .vertical
        mainStack.spacing = 30
        mainStack.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(mainStack)
        
        NSLayoutConstraint.activate([
            mainStack.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            mainStack.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            mainStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 40),
            mainStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -40),
            
            startGameButton.heightAnchor.constraint(equalToConstant: 60),
            channelButtons.first!.heightAnchor.constraint(equalToConstant: 50)
        ])
    }
    
    private func createChannelButton(title: String, tag: Int) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(title, for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        button.backgroundColor = UIColor.systemBlue
        button.setTitleColor(.white, for: .normal)
        button.layer.cornerRadius = 8
        button.tag = tag
        button.addTarget(self, action: #selector(channelSelected(_:)), for: .touchUpInside)
        return button
    }
    
    @objc private func startGame() {
        // 切换到完整答题界面
        setupUI()
        initializeAdSDK()
        loadQuestion()
        updateStats()
        startAdRefreshTimers()
        fetchAppConfig()
    }
    
    @objc private func channelSelected(_ sender: UIButton) {
        let channelNames = ["官方渠道", "合作渠道", "测试渠道"]
        let selectedChannel = channelNames[sender.tag]
        
        let alert = UIAlertController(title: "渠道选择", message: "您选择了：\(selectedChannel)", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }
    
    private func createUserInfoView() -> UIView {
        let containerView = UIView()
        containerView.backgroundColor = .white
        containerView.layer.cornerRadius = 8
        
        // 用户名
        userNameLabel.text = userName
        userNameLabel.font = UIFont.boldSystemFont(ofSize: 16)
        
        // 生命值
        livesLabel.font = UIFont.systemFont(ofSize: 14)
        livesLabel.textColor = .systemRed
        
        // 体力值
        staminaLabel.font = UIFont.systemFont(ofSize: 14)
        staminaLabel.textColor = .systemBlue
        
        // 等级
        let levelLabel = UILabel()
        levelLabel.text = "等级: \(currentLevel)"
        levelLabel.font = UIFont.systemFont(ofSize: 14)
        
        let statsStack = UIStackView(arrangedSubviews: [livesLabel, staminaLabel, levelLabel])
        statsStack.axis = .horizontal
        statsStack.spacing = 15
        statsStack.distribution = .fillEqually
        
        let mainStack = UIStackView(arrangedSubviews: [userNameLabel, statsStack])
        mainStack.axis = .vertical
        mainStack.spacing = 8
        mainStack.translatesAutoresizingMaskIntoConstraints = false
        
        containerView.addSubview(mainStack)
        
        NSLayoutConstraint.activate([
            mainStack.topAnchor.constraint(equalTo: containerView.topAnchor, constant: 12),
            mainStack.leadingAnchor.constraint(equalTo: containerView.leadingAnchor, constant: 12),
            mainStack.trailingAnchor.constraint(equalTo: containerView.trailingAnchor, constant: -12),
            mainStack.bottomAnchor.constraint(equalTo: containerView.bottomAnchor, constant: -12)
        ])
        
        return containerView
    }
    
    private func createBottomButtonsView() -> UIView {
        let containerView = UIView()
        
        // 看广告按钮
        watchAdButton.setTitle("看广告恢复体力", for: .normal)
        watchAdButton.titleLabel?.font = UIFont.systemFont(ofSize: 14)
        watchAdButton.addTarget(self, action: #selector(watchAd), for: .touchUpInside)
        
        // 等级按钮
        levelButton.setTitle("等级详情", for: .normal)
        levelButton.titleLabel?.font = UIFont.systemFont(ofSize: 14)
        levelButton.addTarget(self, action: #selector(showLevelDetails), for: .touchUpInside)
        
        // 设置按钮
        settingsButton.setTitle("设置", for: .normal)
        settingsButton.titleLabel?.font = UIFont.systemFont(ofSize: 14)
        settingsButton.addTarget(self, action: #selector(showSettings), for: .touchUpInside)
        
        // 反馈按钮
        feedbackButton.setTitle("意见反馈", for: .normal)
        feedbackButton.titleLabel?.font = UIFont.systemFont(ofSize: 14)
        feedbackButton.addTarget(self, action: #selector(showFeedback), for: .touchUpInside)
        
        let buttonsStack = UIStackView(arrangedSubviews: [watchAdButton, levelButton, settingsButton, feedbackButton])
        buttonsStack.axis = .horizontal
        buttonsStack.spacing = 10
        buttonsStack.distribution = .fillEqually
        buttonsStack.translatesAutoresizingMaskIntoConstraints = false
        
        containerView.addSubview(buttonsStack)
        
        NSLayoutConstraint.activate([
            buttonsStack.topAnchor.constraint(equalTo: containerView.topAnchor),
            buttonsStack.leadingAnchor.constraint(equalTo: containerView.leadingAnchor),
            buttonsStack.trailingAnchor.constraint(equalTo: containerView.trailingAnchor),
            buttonsStack.bottomAnchor.constraint(equalTo: containerView.bottomAnchor),
            buttonsStack.heightAnchor.constraint(equalToConstant: 40)
        ])
        
        return containerView
    }
    
    // MARK: - 广告SDK初始化
    private func initializeAdSDK() {
        // 首先检查隐私合规状态
        checkPrivacyCompliance()
        
        do {
            // 设置自定义数据
            let customData: [String: Any] = [
                "app_id": TAKU_APP_ID,
                "app_key": TAKU_APP_KEY
            ]
            
            // 配置SDK参数
            ATAPI.setLogEnabled(true) // 开启日志
            ATAPI.integrationChecking() // 开启集成检测
            
            // 初始化SDK
            try ATAPI.sharedInstance().start(withAppID: TAKU_APP_ID, appKey: TAKU_APP_KEY)
            
            // 设置广告代理
            // 注意：iOS SDK使用统一的ATAdManager，代理设置在各广告类型展示时进行
            
            print("广告SDK初始化成功")
            
            // 初始化成功后加载广告
            loadAds()
            
        } catch {
            print("广告SDK初始化失败: \(error)")
        }
    }
    
    // MARK: - 隐私合规配置
    private func checkPrivacyCompliance() {
        // 检查用户隐私授权状态
        if #available(iOS 14, *) {
            ATTrackingManager.requestTrackingAuthorization { status in
                DispatchQueue.main.async {
                    switch status {
                    case .authorized:
                        print("用户已授权广告追踪")
                        self.setPrivacyConsent(true)
                    case .denied:
                        print("用户拒绝广告追踪")
                        self.setPrivacyConsent(false)
                    case .notDetermined:
                        print("用户未决定广告追踪")
                        self.setPrivacyConsent(false)
                    case .restricted:
                        print("广告追踪受限")
                        self.setPrivacyConsent(false)
                    @unknown default:
                        print("未知的广告追踪状态")
                        self.setPrivacyConsent(false)
                    }
                }
            }
        } else {
            // iOS 14以下版本，默认授权
            setPrivacyConsent(true)
        }
    }
    
    private func setPrivacyConsent(_ consent: Bool) {
        // 设置用户隐私同意状态
        // 注意：iOS SDK中隐私设置通常在初始化时通过customData参数配置
        // 这里只需要记录状态，实际配置在初始化时完成
        
        if consent {
            print("用户同意隐私政策，启用个性化广告")
        } else {
            print("用户拒绝隐私政策，使用非个性化广告")
        }
    }
    
    // 加载所有广告
    private func loadAds() {
        loadBannerAd()
        loadInterstitialAd()
        loadRewardedVideoAd()
    }
    
    // 加载横幅广告
    private func loadBannerAd() {
        let placementID = BANNER_PLACEMENT_ID
        
        // 设置横幅广告尺寸
        let bannerSize = CGSize(width: 320, height: 50)
        let extra = [kATAdLoadingExtraBannerAdSizeKey: NSValue(cgSize: bannerSize)]
        
        // 加载横幅广告
        ATAdManager.shared().loadAD(withPlacementID: placementID, extra: extra, delegate: self)
        print("开始加载横幅广告: \(placementID)")
    }
    
    // 加载插屏广告
    private func loadInterstitialAd() {
        let placementID = INTERSTITIAL_PLACEMENT_ID
        
        // 加载插屏广告
        ATAdManager.shared().loadAD(withPlacementID: placementID, extra: nil, delegate: self)
        print("开始加载插屏广告: \(placementID)")
    }
    
    // 加载激励视频广告
    private func loadRewardedVideoAd() {
        let placementID = REWARDED_VIDEO_PLACEMENT_ID
        
        // 设置激励视频额外参数
        var extra: [String: Any] = [:]
        extra[kATAdLoadingExtraMediaExtraKey] = "media_val_AndroidMirror"
        extra[kATAdLoadingExtraUserIDKey] = "test_user_id"
        extra[kATAdLoadingExtraRewardNameKey] = "reward_Name"
        extra[kATAdLoadingExtraRewardAmountKey] = 3
        
        // 加载激励视频广告
        ATAdManager.shared().loadAD(withPlacementID: placementID, extra: extra, delegate: self)
        print("开始加载激励视频广告: \(placementID)")
    }
    
    // MARK: - 广告展示方法
    
    // 展示横幅广告
    private func showBannerAd() {
        let placementID = BANNER_PLACEMENT_ID
        
        // 检查横幅广告是否就绪
        if !ATAdManager.shared().bannerReady(forPlacementID: placementID) {
            print("横幅广告未就绪，重新加载")
            loadBannerAd()
            return
        }
        
        // 获取横幅广告视图
        if let bannerView = ATAdManager.shared().retrieveBannerView(forPlacementID: placementID) {
            // 添加到视图
            view.addSubview(bannerView)
            
            // 设置布局约束
            bannerView.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
                bannerView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
                bannerView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -10),
                bannerView.widthAnchor.constraint(equalToConstant: 320),
                bannerView.heightAnchor.constraint(equalToConstant: 50)
            ])
            
            print("横幅广告展示成功: \(placementID)")
        } else {
            print("横幅广告视图获取失败")
        }
    }
    
    // 展示插屏广告
    private func showInterstitialAd() {
        let placementID = INTERSTITIAL_PLACEMENT_ID
        
        // 场景统计
        ATAdManager.shared().entryInterstitialScenario(withPlacementID: placementID, scene: "")
        
        // 检查插屏广告是否就绪
        if !ATAdManager.shared().interstitialReady(forPlacementID: placementID) {
            print("插屏广告未就绪，重新加载")
            loadInterstitialAd()
            return
        }
        
        // 展示插屏广告
        ATAdManager.shared().showInterstitial(withPlacementID: placementID, in: self, delegate: self)
        print("插屏广告展示成功: \(placementID)")
    }
    
    // 展示激励视频广告
    private func showRewardedVideoAd() {
        let placementID = REWARDED_VIDEO_PLACEMENT_ID
        
        // 场景统计
        ATAdManager.shared().entryRewardedVideoScenario(withPlacementID: placementID, scene: "")
        
        // 检查激励视频广告是否就绪
        if !ATAdManager.shared().rewardedVideoReady(forPlacementID: placementID) {
            print("激励视频广告未就绪，重新加载")
            loadRewardedVideoAd()
            return
        }
        
        // 创建展示配置
        let config = ATShowConfig(scene: "", showCustomExt: "testShowCustomExt")
        
        // 展示激励视频广告
        ATAdManager.shared().showRewardedVideo(withPlacementID: placementID, config: config, in: self, delegate: self)
        print("激励视频广告展示成功: \(placementID)")
    }
    
    // MARK: - 广告检查方法
    
    // 检查广告是否准备好
    private func isRewardedVideoAdReady() -> Bool {
        let placementID = REWARDED_VIDEO_PLACEMENT_ID
        let isReady = ATAdManager.shared().rewardedVideoReady(forPlacementID: placementID)
        print("激励视频广告就绪状态: \(isReady ? "就绪" : "未就绪")")
        return isReady
    }
    
    private func isInterstitialAdReady() -> Bool {
        let placementID = INTERSTITIAL_PLACEMENT_ID
        let isReady = ATAdManager.shared().interstitialReady(forPlacementID: placementID)
        print("插屏广告就绪状态: \(isReady ? "就绪" : "未就绪")")
        return isReady
    }
    
    private func isBannerAdReady() -> Bool {
        let placementID = BANNER_PLACEMENT_ID
        let isReady = ATAdManager.shared().bannerReady(forPlacementID: placementID)
        print("横幅广告就绪状态: \(isReady ? "就绪" : "未就绪")")
        return isReady
    }
    
    // MARK: - 风控和广告刷新机制
    private func fetchAppConfig() {
        // 模拟从后端获取应用配置，包括体力冷却时间
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            // 模拟后端返回的配置数据
            self.staminaCooldownTime = self.isRiskCheck ? 
                AndroidMirrorViewController.AD_COOLDOWN_TIME_RISK : 
                AndroidMirrorViewController.AD_COOLDOWN_TIME_NORMAL
            
            print("应用配置获取完成，体力冷却时间: \(self.staminaCooldownTime)ms")
        }
    }
    
    private func startAdRefreshTimers() {
        // 横幅广告刷新计时器（8秒间隔）
        bannerAdRefreshTimer = Timer.scheduledTimer(withTimeInterval: TimeInterval(AndroidMirrorViewController.REFRESH_INTERVAL) / 1000.0, 
                                                   repeats: true) { [weak self] _ in
            self?.refreshBannerAd()
        }
        
        // 原生广告刷新计时器
        nativeAdRefreshTimer = Timer.scheduledTimer(withTimeInterval: TimeInterval(AndroidMirrorViewController.REFRESH_INTERVAL) / 1000.0, 
                                                    repeats: true) { [weak self] _ in
            self?.checkAdStatus()
        }
    }
    
    private func refreshBannerAd() {
        // 刷新横幅广告
        if let bannerAd = bannerAdView.subviews.first as? ATBannerView {
            bannerAd.loadAd()
        }
    }
    
    private func checkAdStatus() {
        // 检查广告状态，模拟Android项目的广告检查逻辑
        let currentTime = Date().timeIntervalSince1970 * 1000
        
        // 检查插屏广告是否可以显示
        if !isInterstitialAdShowing && 
           currentTime - lastInterstitialAdShownTime > AndroidMirrorViewController.MIN_INTERSTITIAL_AD_INTERVAL {
            showInterstitialAdIfAvailable()
        }
        
        // 检查风控状态
        checkRiskControl()
    }
    
    private func checkRiskControl() {
        // 模拟风控检查逻辑
        if correctAnswers > 10 && Double(correctAnswers) / Double(totalAnswers) > 0.8 {
            // 高正确率触发风控
            if !riskControlTriggered {
                riskControlTriggered = true
                isRiskCheck = true
                staminaCooldownTime = AndroidMirrorViewController.AD_COOLDOWN_TIME_RISK
                showAlert(message: "检测到异常行为，广告冷却时间已延长")
            }
        }
    }
    
    private func showInterstitialAdIfAvailable() {
        if isInterstitialAdReady() {
            showInterstitialAd()
            lastInterstitialAdShownTime = Date().timeIntervalSince1970 * 1000
            isInterstitialAdShowing = true
        }
    }
    
    // MARK: - 计时器管理
    private func pauseTimers() {
        bannerAdRefreshTimer?.invalidate()
        nativeAdRefreshTimer?.invalidate()
        adCooldownTimer?.invalidate()
    }
    
    private func resumeTimers() {
        startAdRefreshTimers()
        
        // 如果广告冷却正在进行，重新启动计时器
        if isAdCooldownActive {
            startAdCooldownTimer()
        }
    }
    
    private func invalidateAllTimers() {
        bannerAdRefreshTimer?.invalidate()
        nativeAdRefreshTimer?.invalidate()
        adCooldownTimer?.invalidate()
        adCheckTimer?.invalidate()
        
        bannerAdRefreshTimer = nil
        nativeAdRefreshTimer = nil
        adCooldownTimer = nil
        adCheckTimer = nil
    }
    
    private func startAdCooldownTimer() {
        adCooldownTimer?.invalidate()
        
        let cooldownSeconds = Double(staminaCooldownTime) / 1000.0
        
        adCooldownTimer = Timer.scheduledTimer(withTimeInterval: cooldownSeconds, 
                                             repeats: false) { [weak self] _ in
            self?.isAdCooldownActive = false
            self?.watchAdButton.isEnabled = true
            self?.watchAdButton.setTitle("看广告恢复体力", for: .normal)
            self?.showAlert(message: "广告冷却时间结束，可以观看广告了")
        }
        
        isAdCooldownActive = true
        watchAdButton.isEnabled = false
        watchAdButton.setTitle("广告冷却中...", for: .normal)
    }
    
    private func loadQuestion() {
        guard currentQuestionIndex < questions.count else {
            // 所有题目已回答完毕
            showCompletionAlert()
            return
        }
        
        let question = questions[currentQuestionIndex]
        questionLabel.text = question.text
        
        for (index, button) in optionButtons.enumerated() {
            button.setTitle(question.options[index], for: .normal)
            button.backgroundColor = UIColor.systemBlue // 重置按钮颜色
            button.isEnabled = true
        }
    }
    
    private func updateStats() {
        livesLabel.text = "生命: \(lives)"
        staminaLabel.text = "体力: \(stamina)"
        userNameLabel.text = "\(userName) (正确率: \(totalAnswers > 0 ? Int(Double(correctAnswers) / Double(totalAnswers) * 100) : 0)%)"
    }
    
    @objc private func optionSelected(_ sender: UIButton) {
        guard stamina > 0 else {
            showAlert(message: "体力不足，请恢复体力后继续答题")
            return
        }
        
        stamina -= 1
        totalAnswers += 1
        
        let question = questions[currentQuestionIndex]
        let isCorrect = sender.tag == question.correctIndex
        
        if isCorrect {
            correctAnswers += 1
            sender.backgroundColor = .systemGreen
            showAlert(message: "回答正确！")
        } else {
            lives -= 1
            sender.backgroundColor = .systemRed
            // 显示正确答案
            optionButtons[question.correctIndex].backgroundColor = .systemGreen
            showAlert(message: "回答错误！正确答案是: \(question.options[question.correctIndex])")
        }
        
        // 禁用所有按钮
        for button in optionButtons {
            button.isEnabled = false
        }
        
        // 延迟加载下一题
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) { [weak self] in
            self?.currentQuestionIndex += 1
            self?.loadQuestion()
            self?.updateStats()
            
            if self?.lives ?? 0 <= 0 {
                self?.showGameOverAlert()
            }
        }
    }
    
    @objc private func watchAd() {
        // 检查广告冷却状态
        if isAdCooldownActive {
            showAlert(message: "广告冷却中，请稍后再试")
            return
        }
        
        // 检查风控状态
        if isRiskCheck {
            showAlert(message: "检测到异常行为，暂时无法观看广告")
            return
        }
        
        // 检查激励广告是否可用
        if isRewardedVideoAdReady() {
            // 显示激励广告
            showRewardedVideoAd()
            isRewardAdPlaying = true
        } else {
            showAlert(message: "广告加载中，请稍后再试")
            // 重新加载广告
            loadRewardedVideoAd()
        }
    }
    
    @objc private func showLevelDetails() {
        let alert = UIAlertController(
            title: "等级详情",
            message: "当前等级: \(currentLevel)\n正确答题数: \(correctAnswers)\n总答题数: \(totalAnswers)",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }
    
    @objc private func showSettings() {
        let alert = UIAlertController(
            title: "设置",
            message: "音效设置\n通知设置\n隐私设置",
            preferredStyle: .actionSheet
        )
        alert.addAction(UIAlertAction(title: "音效开关", style: .default))
        alert.addAction(UIAlertAction(title: "通知设置", style: .default))
        alert.addAction(UIAlertAction(title: "隐私政策", style: .default))
        alert.addAction(UIAlertAction(title: "取消", style: .cancel))
        present(alert, animated: true)
    }
    
    @objc private func showFeedback() {
        let feedbackVC = FeedbackViewController()
        present(feedbackVC, animated: true)
    }
    
    private func showCompletionAlert() {
        let alert = UIAlertController(
            title: "答题完成",
            message: "恭喜您完成了所有题目！\n正确率: \(Int(Double(correctAnswers) / Double(totalAnswers) * 100))%",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "重新开始", style: .default) { [weak self] _ in
            self?.restartGame()
        })
        present(alert, animated: true)
    }
    
    private func showGameOverAlert() {
        let alert = UIAlertController(
            title: "游戏结束",
            message: "生命值耗尽！\n最终正确率: \(Int(Double(correctAnswers) / Double(totalAnswers) * 100))%",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "重新开始", style: .default) { [weak self] _ in
            self?.restartGame()
        })
        present(alert, animated: true)
    }
    
    private func restartGame() {
        currentQuestionIndex = 0
        lives = 3
        stamina = 5
        correctAnswers = 0
        totalAnswers = 0
        currentLevel = 1
        loadQuestion()
        updateStats()
    }
    
    private func showAlert(message: String) {
        let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }
}

// 题目数据模型
struct Question {
    let text: String
    let options: [String]
    let correctIndex: Int
}

// MARK: - 广告回调协议实现
extension AndroidMirrorViewController: ATRewardedVideoDelegate {
    func rewardedVideoDidStartPlaying(forPlacementID placementID: String, extra: [AnyHashable : Any]?) {
        print("激励广告开始播放: \(placementID)")
    }
    
    func rewardedVideoDidEndPlaying(forPlacementID placementID: String, extra: [AnyHashable : Any]?) {
        print("激励广告播放结束: \(placementID)")
    }
    
    func rewardedVideoDidRewardSuccess(forPlacemenID placementID: String, extra: [AnyHashable : Any]?) {
        print("激励广告奖励发放成功: \(placementID)")
        
        // 广告观看成功，恢复体力（与Android项目保持一致：体力+1）
        stamina += 1
        currentScore += 10
        lastAdRewardTime = Date().timeIntervalSince1970 * 1000
        
        // 启动广告冷却计时器
        startAdCooldownTimer()
        
        updateStats()
        showAlert(message: "获得1点体力！当前体力: \(stamina)")
        
        // 重新加载激励广告
        loadRewardedVideoAd()
    }
    
    func rewardedVideoDidClose(forPlacementID placementID: String, rewarded: Bool, extra: [AnyHashable : Any]?) {
        print("激励广告关闭: \(placementID), rewarded: \(rewarded)")
        isRewardAdPlaying = false
        
        // 重新加载激励广告
        loadRewardedVideoAd()
    }
    
    func rewardedVideoDidClick(forPlacementID placementID: String, extra: [AnyHashable : Any]?) {
        print("激励广告被点击: \(placementID)")
    }
    
    func rewardedVideoDidFailToPlay(forPlacementID placementID: String, error: Error, extra: [AnyHashable : Any]?) {
        print("激励广告播放失败: \(error.localizedDescription)")
        isRewardAdPlaying = false
        showAlert(message: "广告播放失败，请重试")
        
        // 重新加载激励广告
        loadRewardedVideoAd()
    }
    
    func rewardedVideoDidDeepLinkOrJump(forPlacementID placementID: String, extra: [AnyHashable : Any]?, result: Bool) {
        print("激励广告深度链接或跳转: \(placementID), result: \(result)")
    }
}

extension AndroidMirrorViewController: ATInterstitialDelegate {
    func interstitialDidShow(forPlacementID placementID: String, extra: [AnyHashable : Any]?) {
        print("插屏广告展示: \(placementID)")
        isInterstitialAdShowing = true
    }
    
    func interstitialDidClose(forPlacementID placementID: String, extra: [AnyHashable : Any]?) {
        print("插屏广告关闭: \(placementID)")
        isInterstitialAdShowing = false
        
        // 广告关闭后重新加载
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            self.loadInterstitialAd()
        }
    }
    
    func interstitialDidClick(forPlacementID placementID: String, extra: [AnyHashable : Any]?) {
        print("插屏广告被点击: \(placementID)")
    }
    
    func interstitialDidFail(toShowForPlacementID placementID: String, error: Error, extra: [AnyHashable : Any]?) {
        print("插屏广告展示失败: \(error.localizedDescription)")
        isInterstitialAdShowing = false
    }
    
    func interstitialDidStartPlayingVideo(forPlacementID placementID: String, extra: [AnyHashable : Any]?) {
        print("插屏广告开始播放视频: \(placementID)")
    }
    
    func interstitialDidEndPlayingVideo(forPlacementID placementID: String, extra: [AnyHashable : Any]?) {
        print("插屏广告视频播放结束: \(placementID)")
    }
}

extension AndroidMirrorViewController: ATBannerDelegate {
    func bannerView(_ bannerView: ATBannerView!, didShowAdWithPlacementID placementID: String!) {
        print("横幅广告展示成功")
    }
    
    func bannerView(_ bannerView: ATBannerView!, didFailToShowAdWithPlacementID placementID: String!, error: Error!) {
        print("横幅广告展示失败: \(error.localizedDescription)")
    }
    
    func bannerView(_ bannerView: ATBannerView!, didClickWithPlacementID placementID: String!) {
        print("横幅广告被点击")
    }
    
    func bannerView(_ bannerView: ATBannerView!, didAutoRefreshWithPlacement placementID: String!) {
        print("横幅广告自动刷新")
    }
    
    func bannerView(_ bannerView: ATBannerView!, failedToAutoRefreshWithPlacementID placementID: String!, error: Error!) {
        print("横幅广告自动刷新失败: \(error.localizedDescription)")
    }
}

// MARK: - ATAdLoadingDelegate
extension AndroidMirrorViewController: ATAdLoadingDelegate {
    
    func didFinishLoadingAD(withPlacementID placementID: String) {
        print("广告加载成功: \(placementID)")
        
        if placementID == REWARDED_VIDEO_PLACEMENT_ID {
            // 更新UI状态
            DispatchQueue.main.async {
                self.watchAdButton.isEnabled = true
                self.watchAdButton.setTitle("看广告恢复体力", for: .normal)
            }
        } else if placementID == BANNER_PLACEMENT_ID {
            // 显示横幅广告
            DispatchQueue.main.async {
                self.showBannerAd()
            }
        }
    }
    
    func didFailToLoadAD(withPlacementID placementID: String, error: Error) {
        print("广告加载失败: \(placementID), error: \(error.localizedDescription)")
        
        // 重试逻辑
        if placementID == REWARDED_VIDEO_PLACEMENT_ID {
            // 更新UI状态
            DispatchQueue.main.async {
                self.watchAdButton.isEnabled = false
                self.watchAdButton.setTitle("广告加载中...", for: .normal)
            }
            
            // 延迟重试
            DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                self.loadRewardedVideoAd()
            }
        }
    }
    
    func didRevenueForPlacementID(_ placementID: String, extra: [AnyHashable : Any]?) {
        print("广告收益回调: \(placementID)")
    }
        
    }
}