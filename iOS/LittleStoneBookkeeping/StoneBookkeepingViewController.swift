import UIKit

class StoneBookkeepingViewController: UIViewController {
    
    // MARK: - UI Components
    private let statsView = UIView()
    private let balanceLabel = UILabel()
    private let incomeLabel = UILabel()
    private let expenseLabel = UILabel()
    private let tableView = UITableView()
    private let addButton = UIButton(type: .system)
    private let profileButton = UIButton(type: .system)
    
    // MARK: - Data
    private var transactions: [Transaction] = []
    private let dataManager = DataManager.shared
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        loadData()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        loadData()
    }
    
    private func setupUI() {
        view.backgroundColor = .systemBackground
        setupBookkeepingUI()
    }
    

    
    private func setupBookkeepingUI() {
        // 清除现有视图
        view.subviews.forEach { $0.removeFromSuperview() }
        
        // 顶部统计区域
        let statsView = createStatsView()
        
        // 添加按钮
        setupAddButton()
        
        // 我的页面按钮
        setupProfileButton()
        
        // 表格视图
        tableView.delegate = self
        tableView.dataSource = self
        tableView.register(TransactionCell.self, forCellReuseIdentifier: "TransactionCell")
        tableView.separatorStyle = .singleLine
        tableView.tableFooterView = UIView()
        
        // 按钮容器
        let buttonStack = UIStackView(arrangedSubviews: [addButton, profileButton])
        buttonStack.axis = .horizontal
        buttonStack.spacing = 12
        buttonStack.distribution = .fillEqually
        
        // 布局
        let stackView = UIStackView(arrangedSubviews: [statsView, buttonStack, tableView])
        stackView.axis = .vertical
        stackView.spacing = 16
        stackView.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(stackView)
        
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 16),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            stackView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -16),
            
            addButton.heightAnchor.constraint(equalToConstant: 50),
            profileButton.heightAnchor.constraint(equalToConstant: 50)
        ])
    }
    
    private func createStatsView() -> UIView {
        let containerView = UIView()
        containerView.backgroundColor = .systemGray6
        containerView.layer.cornerRadius = 12
        
        // 余额
        let balanceTitle = createStatTitleLabel("总余额")
        balanceLabel.font = UIFont.boldSystemFont(ofSize: 24)
        balanceLabel.textColor = .systemBlue
        balanceLabel.text = "¥0.00"
        
        // 收入
        let incomeTitle = createStatTitleLabel("总收入")
        incomeLabel.font = UIFont.systemFont(ofSize: 16)
        incomeLabel.textColor = .systemGreen
        incomeLabel.text = "¥0.00"
        
        // 支出
        let expenseTitle = createStatTitleLabel("总支出")
        expenseLabel.font = UIFont.systemFont(ofSize: 16)
        expenseLabel.textColor = .systemRed
        expenseLabel.text = "¥0.00"
        
        let balanceStack = UIStackView(arrangedSubviews: [balanceTitle, balanceLabel])
        balanceStack.axis = .vertical
        balanceStack.spacing = 4
        balanceStack.alignment = .center
        
        let incomeExpenseStack = UIStackView(arrangedSubviews: [incomeTitle, incomeLabel, expenseTitle, expenseLabel])
        incomeExpenseStack.axis = .vertical
        incomeExpenseStack.spacing = 8
        incomeExpenseStack.alignment = .leading
        
        let mainStack = UIStackView(arrangedSubviews: [balanceStack, incomeExpenseStack])
        mainStack.axis = .horizontal
        mainStack.spacing = 20
        mainStack.distribution = .fillEqually
        mainStack.translatesAutoresizingMaskIntoConstraints = false
        
        containerView.addSubview(mainStack)
        
        NSLayoutConstraint.activate([
            mainStack.topAnchor.constraint(equalTo: containerView.topAnchor, constant: 16),
            mainStack.leadingAnchor.constraint(equalTo: containerView.leadingAnchor, constant: 16),
            mainStack.trailingAnchor.constraint(equalTo: containerView.trailingAnchor, constant: -16),
            mainStack.bottomAnchor.constraint(equalTo: containerView.bottomAnchor, constant: -16)
        ])
        
        return containerView
    }
    
    private func createStatTitleLabel(_ text: String) -> UILabel {
        let label = UILabel()
        label.text = text
        label.font = UIFont.systemFont(ofSize: 14)
        label.textColor = .systemGray
        return label
    }
    
    private func loadData() {
        transactions = dataManager.getAllTransactions()
        
        // 计算统计信息
        totalIncome = transactions.filter { $0.type == .income }.reduce(0) { $0 + $1.amount }
        totalExpense = transactions.filter { $0.type == .expense }.reduce(0) { $0 + $1.amount }
        balance = totalIncome - totalExpense
        
        // 更新UI
        updateStatsDisplay()
        tableView.reloadData()
    }
    
    private func updateStatsDisplay() {
        balanceLabel.text = String(format: "¥%.2f", balance)
        incomeLabel.text = String(format: "¥%.2f", totalIncome)
        expenseLabel.text = String(format: "¥%.2f", totalExpense)
    }
    
    @objc private func addTransaction() {
        let addVC = AddTransactionViewController()
        addVC.onTransactionAdded = { [weak self] in
            self?.loadData()
        }
        present(UINavigationController(rootViewController: addVC), animated: true)
    }
    
    // MARK: - 我的页面导航
    @objc private func showProfile() {
        let profileVC = ProfileViewController()
        profileVC.modalPresentationStyle = .fullScreen
        present(profileVC, animated: true)
    }
    
    private func showAlert(title: String, message: String) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }
}

// MARK: - ATInterstitialDelegate



extension StoneBookkeepingViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return transactions.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "TransactionCell", for: indexPath) as! TransactionCell
        let transaction = transactions[indexPath.row]
        cell.configure(with: transaction)
        return cell
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 60
    }
    
    func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCell.EditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            let transaction = transactions[indexPath.row]
            dataManager.deleteTransaction(transaction.id)
            transactions.remove(at: indexPath.row)
            tableView.deleteRows(at: [indexPath], with: .automatic)
            loadData() // 重新计算统计信息
        }
    }
}

// 自定义表格单元格
class TransactionCell: UITableViewCell {
    private let categoryIconLabel = UILabel()
    private let categoryNameLabel = UILabel()
    private let amountLabel = UILabel()
    private let dateLabel = UILabel()
    private let noteLabel = UILabel()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        // 类别图标
        categoryIconLabel.font = UIFont.systemFont(ofSize: 20)
        
        // 类别名称
        categoryNameLabel.font = UIFont.boldSystemFont(ofSize: 16)
        
        // 金额
        amountLabel.font = UIFont.boldSystemFont(ofSize: 16)
        amountLabel.textAlignment = .right
        
        // 日期
        dateLabel.font = UIFont.systemFont(ofSize: 12)
        dateLabel.textColor = .systemGray
        
        // 备注
        noteLabel.font = UIFont.systemFont(ofSize: 12)
        noteLabel.textColor = .systemGray
        
        let leftStack = UIStackView(arrangedSubviews: [categoryIconLabel, categoryNameLabel])
        leftStack.axis = .horizontal
        leftStack.spacing = 8
        leftStack.alignment = .center
        
        let rightStack = UIStackView(arrangedSubviews: [amountLabel, dateLabel])
        rightStack.axis = .vertical
        rightStack.spacing = 4
        rightStack.alignment = .trailing
        
        let mainStack = UIStackView(arrangedSubviews: [leftStack, rightStack])
        mainStack.axis = .horizontal
        mainStack.distribution = .fill
        mainStack.translatesAutoresizingMaskIntoConstraints = false
        
        contentView.addSubview(mainStack)
        contentView.addSubview(noteLabel)
        
        NSLayoutConstraint.activate([
            mainStack.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 8),
            mainStack.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 16),
            mainStack.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -16),
            
            noteLabel.topAnchor.constraint(equalTo: mainStack.bottomAnchor, constant: 4),
            noteLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 16),
            noteLabel.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -16),
            noteLabel.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -8)
        ])
    }
    
    func configure(with transaction: Transaction) {
        categoryIconLabel.text = transaction.category.icon
        categoryNameLabel.text = transaction.category.name
        
        let amountText = String(format: "%@¥%.2f", transaction.type == .income ? "+" : "-", transaction.amount)
        amountLabel.text = amountText
        amountLabel.textColor = transaction.type == .income ? .systemGreen : .systemRed
        
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "MM-dd HH:mm"
        dateLabel.text = dateFormatter.string(from: transaction.date)
        
        noteLabel.text = transaction.note.isEmpty ? "无备注" : transaction.note
    }
}