import UIKit

class AddTransactionViewController: UIViewController {
    
    var onTransactionAdded: (() -> Void)?
    
    private let dataManager = DataManager.shared
    
    // UI组件
    private let typeSegmentedControl = UISegmentedControl(items: ["支出", "收入"])
    private let amountTextField = UITextField()
    private let categoryPicker = UIPickerView()
    private let noteTextField = UITextField()
    private let saveButton = UIButton(type: .system)
    
    // 数据
    private let categories: [Category] = [
        Category(id: 1, name: "餐饮", icon: "🍽️"),
        Category(id: 2, name: "交通", icon: "🚗"),
        Category(id: 3, name: "购物", icon: "🛍️"),
        Category(id: 4, name: "娱乐", icon: "🎮"),
        Category(id: 5, name: "医疗", icon: "🏥"),
        Category(id: 6, name: "教育", icon: "📚"),
        Category(id: 7, name: "工资", icon: "💰"),
        Category(id: 8, name: "奖金", icon: "🏆"),
        Category(id: 9, name: "投资", icon: "📈"),
        Category(id: 10, name: "其他", icon: "📝")
    ]
    
    private var selectedCategory: Category?
    private var selectedType: TransactionType = .expense
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupNavigation()
        selectedCategory = categories.first
    }
    
    private func setupNavigation() {
        title = "添加记录"
        navigationItem.leftBarButtonItem = UIBarButtonItem(barButtonSystemItem: .cancel, target: self, action: #selector(cancel))
    }
    
    private func setupUI() {
        view.backgroundColor = .systemBackground
        
        // 类型选择
        typeSegmentedControl.selectedSegmentIndex = 0
        typeSegmentedControl.addTarget(self, action: #selector(typeChanged), for: .valueChanged)
        
        // 金额输入
        amountTextField.placeholder = "输入金额"
        amountTextField.borderStyle = .roundedRect
        amountTextField.keyboardType = .decimalPad
        amountTextField.font = UIFont.systemFont(ofSize: 18)
        
        // 类别选择器
        categoryPicker.delegate = self
        categoryPicker.dataSource = self
        
        // 备注输入
        noteTextField.placeholder = "备注（可选）"
        noteTextField.borderStyle = .roundedRect
        noteTextField.font = UIFont.systemFont(ofSize: 16)
        
        // 保存按钮
        saveButton.setTitle("保存", for: .normal)
        saveButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 18)
        saveButton.backgroundColor = .systemBlue
        saveButton.setTitleColor(.white, for: .normal)
        saveButton.layer.cornerRadius = 8
        saveButton.addTarget(self, action: #selector(saveTransaction), for: .touchUpInside)
        
        // 布局
        let stackView = UIStackView(arrangedSubviews: [
            createLabel("类型:"), typeSegmentedControl,
            createLabel("金额:"), amountTextField,
            createLabel("类别:"), categoryPicker,
            createLabel("备注:"), noteTextField,
            saveButton
        ])
        
        stackView.axis = .vertical
        stackView.spacing = 16
        stackView.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(stackView)
        
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 20),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            
            amountTextField.heightAnchor.constraint(equalToConstant: 44),
            noteTextField.heightAnchor.constraint(equalToConstant: 44),
            saveButton.heightAnchor.constraint(equalToConstant: 50),
            categoryPicker.heightAnchor.constraint(equalToConstant: 120)
        ])
        
        // 添加金额输入框的工具栏
        setupAmountTextFieldToolbar()
    }
    
    private func createLabel(_ text: String) -> UILabel {
        let label = UILabel()
        label.text = text
        label.font = UIFont.boldSystemFont(ofSize: 16)
        return label
    }
    
    private func setupAmountTextFieldToolbar() {
        let toolbar = UIToolbar()
        toolbar.sizeToFit()
        
        let doneButton = UIBarButtonItem(title: "完成", style: .done, target: self, action: #selector(doneButtonTapped))
        let flexibleSpace = UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: nil, action: nil)
        
        toolbar.items = [flexibleSpace, doneButton]
        amountTextField.inputAccessoryView = toolbar
    }
    
    @objc private func doneButtonTapped() {
        amountTextField.resignFirstResponder()
    }
    
    @objc private func typeChanged() {
        selectedType = typeSegmentedControl.selectedSegmentIndex == 0 ? .expense : .income
    }
    
    @objc private func cancel() {
        dismiss(animated: true)
    }
    
    @objc private func saveTransaction() {
        guard let amountText = amountTextField.text, !amountText.isEmpty,
              let amount = Double(amountText), amount > 0 else {
            showAlert(message: "请输入有效的金额")
            return
        }
        
        guard let category = selectedCategory else {
            showAlert(message: "请选择类别")
            return
        }
        
        let note = noteTextField.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        
        let transaction = Transaction(
            id: UUID(),
            amount: amount,
            category: category,
            date: Date(),
            note: note,
            type: selectedType
        )
        
        dataManager.saveTransaction(transaction)
        
        showAlert(message: "记录添加成功") { [weak self] in
            self?.onTransactionAdded?()
            self?.dismiss(animated: true)
        }
    }
    
    private func showAlert(message: String, completion: (() -> Void)? = nil) {
        let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "确定", style: .default) { _ in
            completion?()
        })
        present(alert, animated: true)
    }
}

extension AddTransactionViewController: UIPickerViewDelegate, UIPickerViewDataSource {
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return categories.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        let category = categories[row]
        return "\(category.icon) \(category.name)"
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        selectedCategory = categories[row]
    }
    
    func pickerView(_ pickerView: UIPickerView, rowHeightForComponent component: Int) -> CGFloat {
        return 30
    }
}