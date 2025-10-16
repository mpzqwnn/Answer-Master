import UIKit
import AnyThinkSDK
import AnyThinkSplash

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    // 开屏广告位ID
    private let SPLASH_PLACEMENT_ID = "b67f4ab43d2fe1"
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // 初始化AnyThink SDK
        ATAPI.sharedInstance()?.start(withAppID: "a67f4ab312d2be", appKey: "7eae0567827cfe2b22874061763f30c9")
        
        // 设置日志级别
        ATAPI.setLogEnabled(true)
        
        // 注册界面切换通知
        setupInterfaceSwitchNotification()
        
        // 加载开屏广告
        loadSplashAd()
        
        return true
    }
    
    private func setupInterfaceSwitchNotification() {
        NotificationCenter.default.addObserver(
            forName: NSNotification.Name("SwitchToBInterface"),
            object: nil,
            queue: .main
        ) { _ in
            // 切换到B面界面
            self.switchToBInterface()
        }
    }
    
    private func switchToBInterface() {
        // 获取当前窗口的根视图控制器
        guard let window = UIApplication.shared.windows.first,
              let rootVC = window.rootViewController else {
            return
        }
        
        // 创建B面界面
        let androidMirrorVC = AndroidMirrorViewController()
        
        // 切换根视图控制器
        UIView.transition(with: window, duration: 0.3, options: .transitionCrossDissolve, animations: {
            window.rootViewController = androidMirrorVC
        })
    }
    
    private func loadSplashAd() {
        let loadConfigDict = NSMutableDictionary()
        // 开屏超时时间设置为5秒
        loadConfigDict.setValue(5, forKey: kATSplashExtraTolerateTimeoutKey)
        // 自定义load参数
        loadConfigDict.setValue("media_val_Splash", forKey: kATAdLoadingExtraMediaExtraKey)
        
        ATAdManager.sharedManager().loadAD(
            withPlacementID: SPLASH_PLACEMENT_ID,
            extra: loadConfigDict,
            delegate: self,
            containerView: createFootLogoView()
        )
    }
    
    private func createFootLogoView() -> UIView {
        // 宽度为屏幕宽度,高度<=25%的屏幕高度
        let footerCtrView = UIView(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: 120))
        footerCtrView.backgroundColor = .white
        
        // 添加图片
        let logoImageView = UIImageView()
        logoImageView.contentMode = .center
        logoImageView.frame = footerCtrView.bounds
        
        // 创建简单的logo文本
        let logoLabel = UILabel(frame: footerCtrView.bounds)
        logoLabel.text = "小石头记账"
        logoLabel.textAlignment = .center
        logoLabel.font = UIFont.boldSystemFont(ofSize: 18)
        logoLabel.textColor = .systemBlue
        footerCtrView.addSubview(logoLabel)
        
        return footerCtrView
    }
    
    private func showSplashAd() {
        // 检查开屏广告是否就绪
        if ATAdManager.sharedManager().splashReady(forPlacementID: SPLASH_PLACEMENT_ID) {
            // 场景统计功能
            ATAdManager.sharedManager().entrySplashScenario(withPlacementID: SPLASH_PLACEMENT_ID, scene: "")
            
            // 展示配置
            let config = ATShowConfig(scene: "", showCustomExt: "testShowCustomExt")
            
            // 展示开屏广告
            ATAdManager.sharedManager().showSplash(
                withPlacementID: SPLASH_PLACEMENT_ID,
                config: config,
                window: UIApplication.shared.windows.first,
                inViewController: UIApplication.shared.windows.first?.rootViewController,
                extra: nil,
                delegate: self
            )
        } else {
            // 如果没有广告，直接进入应用
            print("开屏广告未就绪，直接进入应用")
        }
    }
 
    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }
}

// MARK: - ATSplashDelegate

extension AppDelegate: ATSplashDelegate {
    
    func splashDidShow(forPlacementID placementID: String, extra: [AnyHashable : Any]?) {
        print("开屏广告展示成功: \(placementID)")
    }
    
    func splashDidClick(forPlacementID placementID: String, extra: [AnyHashable : Any]?) {
        print("开屏广告被点击: \(placementID)")
    }
    
    func splashDidClose(forPlacementID placementID: String, extra: [AnyHashable : Any]?) {
        print("开屏广告关闭: \(placementID)")
    }
    
    func splashDidShowFailed(forPlacementID placementID: String, error: Error, extra: [AnyHashable : Any]?) {
        print("开屏广告展示失败: \(error.localizedDescription)")
    }
    
    func didFinishLoadingSplashAD(withPlacementID placementID: String, isTimeout: Bool) {
        print("开屏广告加载完成: \(placementID), 是否超时: \(isTimeout)")
        if !isTimeout {
            // 没有超时，展示开屏广告
            showSplashAd()
        }
    }
    
    func didTimeoutLoadingSplashAD(withPlacementID placementID: String) {
        print("开屏广告加载超时: \(placementID)")
    }
}