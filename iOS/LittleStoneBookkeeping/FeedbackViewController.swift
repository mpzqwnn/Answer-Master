import UIKit

class FeedbackViewController: UIViewController {
    
    var interfaceSwitchHandler: (() -> Void)?
    
    private let titleLabel = UILabel()
    private let feedbackTextView = UITextView()
    private let submitButton = UIButton(type: .system)
    private let placeholderLabel = UILabel()
    
    // 双击检测
    private var lastTapTime: TimeInterval = 0
    private let doubleTapInterval: TimeInterval = 0.5
    private var tapCount = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupGestures()
    }
    
    private func setupUI() {
        view.backgroundColor = .systemBackground
        
        // 标题
        titleLabel.text = "意见反馈"
        titleLabel.font = UIFont.boldSystemFont(ofSize: 20)
        titleLabel.textAlignment = .center
        
        // 反馈文本框
        feedbackTextView.layer.borderWidth = 1
        feedbackTextView.layer.borderColor = UIColor.systemGray4.cgColor
        feedbackTextView.layer.cornerRadius = 8
        feedbackTextView.font = UIFont.systemFont(ofSize: 16)
        feedbackTextView.delegate = self
        feedbackTextView.textContainerInset = UIEdgeInsets(top: 12, left: 8, bottom: 12, right: 8)
        
        // 占位符
        placeholderLabel.text = "请输入您的宝贵意见..."
        placeholderLabel.textColor = .systemGray3
        placeholderLabel.font = UIFont.systemFont(ofSize: 16)
        
        // 提交按钮
        submitButton.setTitle("提交", for: .normal)
        submitButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 18)
        submitButton.backgroundColor = .systemBlue
        submitButton.setTitleColor(.white, for: .normal)
        submitButton.layer.cornerRadius = 8
        
        // 布局
        let stackView = UIStackView(arrangedSubviews: [titleLabel, feedbackTextView, submitButton])
        stackView.axis = .vertical
        stackView.spacing = 20
        stackView.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(stackView)
        view.addSubview(placeholderLabel)
        
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 40),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            
            feedbackTextView.heightAnchor.constraint(equalToConstant: 120),
            submitButton.heightAnchor.constraint(equalToConstant: 50),
            
            placeholderLabel.topAnchor.constraint(equalTo: feedbackTextView.topAnchor, constant: 12),
            placeholderLabel.leadingAnchor.constraint(equalTo: feedbackTextView.leadingAnchor, constant: 12)
        ])
        
        // 添加关闭按钮
        let closeButton = UIBarButtonItem(barButtonSystemItem: .close, target: self, action: #selector(close))
        navigationItem.rightBarButtonItem = closeButton
    }
    
    private func setupGestures() {
        // 为提交按钮添加点击手势
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleSubmitTap))
        submitButton.addGestureRecognizer(tapGesture)
        submitButton.isUserInteractionEnabled = true
    }
    
    @objc private func handleSubmitTap(_ gesture: UITapGestureRecognizer) {
        let currentTime = Date().timeIntervalSince1970
        
        if currentTime - lastTapTime < doubleTapInterval {
            tapCount += 1
            if tapCount >= 2 {
                // 检测到双击
                handleDoubleTap()
                tapCount = 0
                return
            }
        } else {
            tapCount = 1
        }
        
        lastTapTime = currentTime
        
        // 延迟重置计数
        DispatchQueue.main.asyncAfter(deadline: .now() + doubleTapInterval) { [weak self] in
            if self?.tapCount == 1 {
                // 单次点击处理
                self?.handleSingleTap()
            }
            self?.tapCount = 0
        }
    }
    
    private func handleSingleTap() {
        // 单次点击：正常提交逻辑
        guard let feedbackText = feedbackTextView.text, !feedbackText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            showAlert(message: "请输入反馈内容")
            return
        }
        
        // 模拟提交成功
        showAlert(message: "反馈提交成功！")
        
        // 清空文本框
        feedbackTextView.text = ""
        placeholderLabel.isHidden = false
        
        // 关闭键盘
        feedbackTextView.resignFirstResponder()
    }
    
    private func handleDoubleTap() {
        // 双击：切换界面逻辑（仅在文本框为空时生效）
        guard feedbackTextView.text?.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ?? true else {
            // 文本框有内容，不执行切换
            return
        }
        
        // 直接执行界面切换
        performInterfaceSwitch()
    }
    
    private func performInterfaceSwitch() {
        // 执行界面切换
        dismiss(animated: true) { [weak self] in
            self?.interfaceSwitchHandler?()
        }
    }
    
    private func showAlert(message: String) {
        let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }
    
    @objc private func close() {
        dismiss(animated: true)
    }
}

extension FeedbackViewController: UITextViewDelegate {
    func textViewDidChange(_ textView: UITextView) {
        placeholderLabel.isHidden = !textView.text.isEmpty
    }
    
    func textViewDidBeginEditing(_ textView: UITextView) {
        placeholderLabel.isHidden = true
    }
    
    func textViewDidEndEditing(_ textView: UITextView) {
        placeholderLabel.isHidden = !textView.text.isEmpty
    }
}