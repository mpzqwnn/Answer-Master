import UIKit
import StoreKit
import AnyThinkSDK

class ProfileViewController: UIViewController {
    
    // 用户信息
    private let userAvatarImageView = UIImageView()
    private let userNameLabel = UILabel()
    private let phoneLabel = UILabel()
    private let userIdLabel = UILabel()
    
    // 原生广告占位
    private let nativeAdView = UIView()
    
    // 功能按钮
    private let feedbackButton = UIButton(type: .system)
    private let rateButton = UIButton(type: .system)
    private let aboutButton = UIButton(type: .system)
    private let privacyButton = UIButton(type: .system)
    private let termsButton = UIButton(type: .system)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        loadUserInfo()
    }
    
    private func setupUI() {
        view.backgroundColor = .systemBackground
        title = "我的"
        
        // 用户信息区域
        setupUserInfoSection()
        
        // 功能按钮区域
        setupFunctionButtons()
        
        // 原生广告区域
        setupNativeAdSection()
        
        // 布局
        let stackView = UIStackView(arrangedSubviews: [
            createUserInfoCard(),
            createFunctionButtonsCard(),
            nativeAdView
        ])
        stackView.axis = .vertical
        stackView.spacing = 16
        stackView.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(stackView)
        
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 16),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            stackView.bottomAnchor.constraint(lessThanOrEqualTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -16)
        ])
    }
    
    private func setupUserInfoSection() {
        // 用户头像
        userAvatarImageView.image = UIImage(systemName: "person.circle.fill")
        userAvatarImageView.tintColor = .systemBlue
        userAvatarImageView.contentMode = .scaleAspectFill
        userAvatarImageView.layer.cornerRadius = 30
        userAvatarImageView.clipsToBounds = true
        
        // 用户名
        userNameLabel.font = UIFont.boldSystemFont(ofSize: 18)
        userNameLabel.text = "用户昵称"
        
        // 手机号
        phoneLabel.font = UIFont.systemFont(ofSize: 14)
        phoneLabel.textColor = .systemGray
        
        // 用户ID
        userIdLabel.font = UIFont.systemFont(ofSize: 12)
        userIdLabel.textColor = .systemGray2
    }
    
    private func createUserInfoCard() -> UIView {
        let cardView = UIView()
        cardView.backgroundColor = .systemGray6
        cardView.layer.cornerRadius = 12
        
        let infoStack = UIStackView(arrangedSubviews: [
            userAvatarImageView,
            createUserInfoStack()
        ])
        infoStack.axis = .horizontal
        infoStack.spacing = 12
        infoStack.alignment = .center
        infoStack.translatesAutoresizingMaskIntoConstraints = false
        
        cardView.addSubview(infoStack)
        
        NSLayoutConstraint.activate([
            userAvatarImageView.widthAnchor.constraint(equalToConstant: 60),
            userAvatarImageView.heightAnchor.constraint(equalToConstant: 60),
            
            infoStack.topAnchor.constraint(equalTo: cardView.topAnchor, constant: 16),
            infoStack.leadingAnchor.constraint(equalTo: cardView.leadingAnchor, constant: 16),
            infoStack.trailingAnchor.constraint(equalTo: cardView.trailingAnchor, constant: -16),
            infoStack.bottomAnchor.constraint(equalTo: cardView.bottomAnchor, constant: -16)
        ])
        
        return cardView
    }
    
    private func createUserInfoStack() -> UIStackView {
        let infoStack = UIStackView(arrangedSubviews: [userNameLabel, phoneLabel, userIdLabel])
        infoStack.axis = .vertical
        infoStack.spacing = 4
        return infoStack
    }
    
    private func setupFunctionButtons() {
        // 意见反馈
        feedbackButton.setTitle("意见反馈", for: .normal)
        feedbackButton.setImage(UIImage(systemName: "text.bubble"), for: .normal)
        feedbackButton.addTarget(self, action: #selector(showFeedback), for: .touchUpInside)
        
        // 给个好评
        rateButton.setTitle("给个好评", for: .normal)
        rateButton.setImage(UIImage(systemName: "star.fill"), for: .normal)
        rateButton.addTarget(self, action: #selector(rateApp), for: .touchUpInside)
        
        // 关于我们
        aboutButton.setTitle("关于我们", for: .normal)
        aboutButton.setImage(UIImage(systemName: "info.circle"), for: .normal)
        aboutButton.addTarget(self, action: #selector(showAbout), for: .touchUpInside)
        
        // 隐私协议
        privacyButton.setTitle("隐私协议", for: .normal)
        privacyButton.setImage(UIImage(systemName: "lock.shield"), for: .normal)
        privacyButton.addTarget(self, action: #selector(showPrivacy), for: .touchUpInside)
        
        // 用户协议
        termsButton.setTitle("用户协议", for: .normal)
        termsButton.setImage(UIImage(systemName: "doc.text"), for: .normal)
        termsButton.addTarget(self, action: #selector(showTerms), for: .touchUpInside)
        

    }
    
    private func createFunctionButtonsCard() -> UIView {
        let cardView = UIView()
        cardView.backgroundColor = .systemGray6
        cardView.layer.cornerRadius = 12
        
        let buttons = [
            feedbackButton, rateButton, aboutButton, 
            privacyButton, termsButton
        ]
        
        let buttonStack = UIStackView(arrangedSubviews: buttons)
        buttonStack.axis = .vertical
        buttonStack.spacing = 0
        buttonStack.translatesAutoresizingMaskIntoConstraints = false
        
        cardView.addSubview(buttonStack)
        
        // 配置按钮样式
        buttons.forEach { button in
            button.contentHorizontalAlignment = .left
            button.titleEdgeInsets = UIEdgeInsets(top: 0, left: 12, bottom: 0, right: 0)
            button.imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 0)
            button.heightAnchor.constraint(equalToConstant: 50).isActive = true
        }
        
        NSLayoutConstraint.activate([
            buttonStack.topAnchor.constraint(equalTo: cardView.topAnchor),
            buttonStack.leadingAnchor.constraint(equalTo: cardView.leadingAnchor),
            buttonStack.trailingAnchor.constraint(equalTo: cardView.trailingAnchor),
            buttonStack.bottomAnchor.constraint(equalTo: cardView.bottomAnchor)
        ])
        
        return cardView
    }
    
    private func setupNativeAdSection() {
        nativeAdView.backgroundColor = .systemGray5
        nativeAdView.layer.cornerRadius = 8
        nativeAdView.heightAnchor.constraint(equalToConstant: 100).isActive = true
        
        let adLabel = UILabel()
        adLabel.text = "广告"
        adLabel.textAlignment = .center
        adLabel.textColor = .systemGray
        adLabel.translatesAutoresizingMaskIntoConstraints = false
        
        nativeAdView.addSubview(adLabel)
        
        NSLayoutConstraint.activate([
            adLabel.centerXAnchor.constraint(equalTo: nativeAdView.centerXAnchor),
            adLabel.centerYAnchor.constraint(equalTo: nativeAdView.centerYAnchor)
        ])
    }
    
    private func loadUserInfo() {
        let defaults = UserDefaults.standard
        let phone = defaults.string(forKey: "user_phone") ?? "未登录"
        let isLoggedIn = defaults.bool(forKey: "is_logged_in")
        
        if isLoggedIn {
            userNameLabel.text = "用户_" + (phone.suffix(4))
            phoneLabel.text = phone
            userIdLabel.text = "ID: " + String(abs(phone.hash))
        } else {
            userNameLabel.text = "游客用户"
            phoneLabel.text = "未登录"
            userIdLabel.text = "ID: 000000"
        }
    }
    
    private func loadNativeAd() {
        // 原生广告占位视图
        let adLabel = UILabel()
        adLabel.text = "广告区域"
        adLabel.textAlignment = .center
        adLabel.textColor = .systemGray
        adLabel.translatesAutoresizingMaskIntoConstraints = false
        
        nativeAdView.subviews.forEach { $0.removeFromSuperview() }
        nativeAdView.addSubview(adLabel)
        
        NSLayoutConstraint.activate([
            adLabel.centerXAnchor.constraint(equalTo: nativeAdView.centerXAnchor),
            adLabel.centerYAnchor.constraint(equalTo: nativeAdView.centerYAnchor)
        ])
    }
    
    // MARK: - 按钮事件
    
    @objc private func showFeedback() {
        let feedbackVC = FeedbackViewController()
        feedbackVC.interfaceSwitchHandler = { [weak self] in
            // 当从反馈页面返回时，不需要执行界面切换
            // 只需确保页面正常关闭即可
        }
        let navController = UINavigationController(rootViewController: feedbackVC)
        present(navController, animated: true)
    }
    
    @objc private func rateApp() {
        // 请求App Store评分
        if #available(iOS 14.0, *) {
            if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene {
                SKStoreReviewController.requestReview(in: scene)
            }
        } else {
            SKStoreReviewController.requestReview()
        }
        
        showAlert(message: "感谢您的评价！")
    }
    
    @objc private func showAbout() {
        let alert = UIAlertController(
            title: "关于我们",
            message: "小石头记账 - 简单易用的个人财务管理工具\n版本 1.0.0",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }
    
    @objc private func showPrivacy() {
        let alert = UIAlertController(
            title: "隐私协议",
            message: "我们非常重视您的隐私安全，详细协议请查看应用内完整版本。",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }
    
    @objc private func showTerms() {
        let alert = UIAlertController(
            title: "用户协议",
            message: "请仔细阅读用户协议，使用本应用即表示您同意相关条款。",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }
    

    
    private func showAlert(message: String) {
        let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }
}

// MARK: - ATNativeAdDelegate

extension ProfileViewController: ATNativeAdDelegate {
    func didFinishLoadingNativeAD(withPlacementID placementID: String) {
        print("原生广告加载成功: \(placementID)")
        loadNativeAd()
    }
    
    func didFailToLoadNativeAD(withPlacementID placementID: String, error: Error) {
        print("原生广告加载失败: \(error.localizedDescription)")
    }
}