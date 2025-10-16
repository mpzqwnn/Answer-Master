import UIKit

class MainViewController: UIViewController {
    
    // 界面状态管理
    private var currentInterface: InterfaceType = .stoneBookkeeping
    
    // 界面类型枚举
    private enum InterfaceType {
        case stoneBookkeeping  // A面：小石头记账
        case androidMirror     // B面：Android镜像界面
    }
    
    // 记账相关属性
    private var transactions: [Transaction] = []
    private var categories: [Category] = [
        Category(id: 1, name: "餐饮", icon: "🍽️"),
        Category(id: 2, name: "交通", icon: "🚗"),
        Category(id: 3, name: "购物", icon: "🛍️"),
        Category(id: 4, name: "娱乐", icon: "🎮"),
        Category(id: 5, name: "医疗", icon: "🏥"),
        Category(id: 6, name: "教育", icon: "📚"),
        Category(id: 7, name: "收入", icon: "💰")
    ]
    
    // UI组件
    private let titleLabel = UILabel()
    private let switchButton = UIButton(type: .system)
    private let containerView = UIView()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupStoneBookkeepingInterface()
        
        // 监听界面切换通知
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleInterfaceSwitch),
            name: NSNotification.Name("SwitchToBInterface"),
            object: nil
        )
    }
    
    @objc private func handleInterfaceSwitch() {
        performInterfaceSwitch()
    }
    
    private func setupUI() {
        view.backgroundColor = .systemBackground
        
        // 标题
        titleLabel.text = "小石头记账"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 24)
        titleLabel.textAlignment = .center
        
        // 切换按钮
        switchButton.setTitle("切换界面", for: .normal)
        switchButton.addTarget(self, action: #selector(switchInterface), for: .touchUpInside)
        
        // 布局
        let stackView = UIStackView(arrangedSubviews: [titleLabel, switchButton, containerView])
        stackView.axis = .vertical
        stackView.spacing = 20
        stackView.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(stackView)
        
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 20),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            stackView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -20)
        ])
    }
    
    @objc private func switchInterface() {
        // 显示意见反馈界面进行切换
        let feedbackVC = FeedbackViewController()
        feedbackVC.interfaceSwitchHandler = { [weak self] in
            self?.performInterfaceSwitch()
        }
        present(feedbackVC, animated: true)
    }
    
    private func performInterfaceSwitch() {
        // 清除当前界面
        containerView.subviews.forEach { $0.removeFromSuperview() }
        
        switch currentInterface {
        case .stoneBookkeeping:
            currentInterface = .androidMirror
            titleLabel.text = "答题大师"
            setupAndroidMirrorInterface()
        case .androidMirror:
            currentInterface = .stoneBookkeeping
            titleLabel.text = "小石头记账"
            setupStoneBookkeepingInterface()
        }
    }
    
    private func setupStoneBookkeepingInterface() {
        // 创建记账界面
        let bookkeepingVC = StoneBookkeepingViewController()
        addChild(bookkeepingVC)
        containerView.addSubview(bookkeepingVC.view)
        bookkeepingVC.view.translatesAutoresizingMaskIntoConstraints = false
        bookkeepingVC.didMove(toParent: self)
        
        NSLayoutConstraint.activate([
            bookkeepingVC.view.topAnchor.constraint(equalTo: containerView.topAnchor),
            bookkeepingVC.view.leadingAnchor.constraint(equalTo: containerView.leadingAnchor),
            bookkeepingVC.view.trailingAnchor.constraint(equalTo: containerView.trailingAnchor),
            bookkeepingVC.view.bottomAnchor.constraint(equalTo: containerView.bottomAnchor)
        ])
    }
    
    private func setupAndroidMirrorInterface() {
        // 检查是否是第二次登录
        let defaults = UserDefaults.standard
        let isSecondLogin = defaults.bool(forKey: "has_quiz_loaded")
        
        if isSecondLogin {
            // 第二次登录，直接显示答题界面
            showAndroidMirrorInterface()
        } else {
            // 第一次登录，显示加载中界面
            showLoadingInterface()
        }
    }
    
    private func showLoadingInterface() {
        // 清除当前界面
        containerView.subviews.forEach { $0.removeFromSuperview() }
        
        // 创建加载中界面
        let loadingView = UIView()
        loadingView.backgroundColor = .systemBackground
        
        let loadingLabel = UILabel()
        loadingLabel.text = "加载中..."
        loadingLabel.font = UIFont.boldSystemFont(ofSize: 20)
        loadingLabel.textAlignment = .center
        
        let progressView = UIProgressView(progressViewStyle: .default)
        progressView.progress = 0.0
        
        let loadingStack = UIStackView(arrangedSubviews: [loadingLabel, progressView])
        loadingStack.axis = .vertical
        loadingStack.spacing = 20
        loadingStack.translatesAutoresizingMaskIntoConstraints = false
        
        loadingView.addSubview(loadingStack)
        containerView.addSubview(loadingView)
        
        NSLayoutConstraint.activate([
            loadingView.topAnchor.constraint(equalTo: containerView.topAnchor),
            loadingView.leadingAnchor.constraint(equalTo: containerView.leadingAnchor),
            loadingView.trailingAnchor.constraint(equalTo: containerView.trailingAnchor),
            loadingView.bottomAnchor.constraint(equalTo: containerView.bottomAnchor),
            
            loadingStack.centerXAnchor.constraint(equalTo: loadingView.centerXAnchor),
            loadingStack.centerYAnchor.constraint(equalTo: loadingView.centerYAnchor),
            loadingStack.leadingAnchor.constraint(equalTo: loadingView.leadingAnchor, constant: 40),
            loadingStack.trailingAnchor.constraint(equalTo: loadingView.trailingAnchor, constant: -40)
        ])
        
        // 模拟15秒加载过程
        var progress: Float = 0.0
        let timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { timer in
            progress += 0.00667 // 15秒完成
            progressView.progress = progress
            
            if progress >= 1.0 {
                timer.invalidate()
                // 加载完成，标记为已加载
                let defaults = UserDefaults.standard
                defaults.set(true, forKey: "has_quiz_loaded")
                // 显示答题界面
                self.showAndroidMirrorInterface()
            }
        }
    }
    
    private func showAndroidMirrorInterface() {
        // 清除当前界面
        containerView.subviews.forEach { $0.removeFromSuperview() }
        
        // 创建Android镜像界面
        let androidVC = AndroidMirrorViewController()
        addChild(androidVC)
        containerView.addSubview(androidVC.view)
        androidVC.view.translatesAutoresizingMaskIntoConstraints = false
        androidVC.didMove(toParent: self)
        
        NSLayoutConstraint.activate([
            androidVC.view.topAnchor.constraint(equalTo: containerView.topAnchor),
            androidVC.view.leadingAnchor.constraint(equalTo: containerView.leadingAnchor),
            androidVC.view.trailingAnchor.constraint(equalTo: containerView.trailingAnchor),
            androidVC.view.bottomAnchor.constraint(equalTo: containerView.bottomAnchor)
        ])
    }
}

// 数据模型
struct Transaction {
    let id: UUID
    let amount: Double
    let category: Category
    let date: Date
    let note: String
    let type: TransactionType
}

enum TransactionType {
    case income
    case expense
}

struct Category {
    let id: Int
    let name: String
    let icon: String
}