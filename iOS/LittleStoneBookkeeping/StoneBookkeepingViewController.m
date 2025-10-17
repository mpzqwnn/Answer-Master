#import "StoneBookkeepingViewController.h"
#import "DataManager.h"
#import "AddTransactionViewController.h"
#import "ProfileViewController.h"
#import "Transaction.h"
#import "Category.h"

// MARK: - TransactionCell

@interface TransactionCell : UITableViewCell

@property (nonatomic, strong) UILabel *categoryIconLabel;
@property (nonatomic, strong) UILabel *categoryNameLabel;
@property (nonatomic, strong) UILabel *amountLabel;
@property (nonatomic, strong) UILabel *dateLabel;
@property (nonatomic, strong) UILabel *noteLabel;

- (void)configureWithTransaction:(Transaction *)transaction;

@end

@implementation TransactionCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self setupUI];
    }
    return self;
}

- (void)setupUI {
    // 类别图标
    self.categoryIconLabel = [[UILabel alloc] init];
    self.categoryIconLabel.font = [UIFont systemFontOfSize:20];
    
    // 类别名称
    self.categoryNameLabel = [[UILabel alloc] init];
    self.categoryNameLabel.font = [UIFont boldSystemFontOfSize:16];
    
    // 金额
    self.amountLabel = [[UILabel alloc] init];
    self.amountLabel.font = [UIFont boldSystemFontOfSize:16];
    self.amountLabel.textAlignment = NSTextAlignmentRight;
    
    // 日期
    self.dateLabel = [[UILabel alloc] init];
    self.dateLabel.font = [UIFont systemFontOfSize:12];
    self.dateLabel.textColor = [UIColor systemGrayColor];
    
    // 备注
    self.noteLabel = [[UILabel alloc] init];
    self.noteLabel.font = [UIFont systemFontOfSize:12];
    self.noteLabel.textColor = [UIColor systemGrayColor];
    
    UIStackView *leftStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.categoryIconLabel,
        self.categoryNameLabel
    ]];
    leftStack.axis = UILayoutConstraintAxisHorizontal;
    leftStack.spacing = 8;
    leftStack.alignment = UIStackViewAlignmentCenter;
    
    UIStackView *rightStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.amountLabel,
        self.dateLabel
    ]];
    rightStack.axis = UILayoutConstraintAxisVertical;
    rightStack.spacing = 4;
    rightStack.alignment = UIStackViewAlignmentTrailing;
    
    UIStackView *mainStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        leftStack,
        rightStack
    ]];
    mainStack.axis = UILayoutConstraintAxisHorizontal;
    mainStack.distribution = UIStackViewDistributionFill;
    mainStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.contentView addSubview:mainStack];
    [self.contentView addSubview:self.noteLabel];
    
    [NSLayoutConstraint activateConstraints:@[
        [mainStack.topAnchor constraintEqualToAnchor:self.contentView.topAnchor constant:8],
        [mainStack.leadingAnchor constraintEqualToAnchor:self.contentView.leadingAnchor constant:16],
        [mainStack.trailingAnchor constraintEqualToAnchor:self.contentView.trailingAnchor constant:-16],
        
        [self.noteLabel.topAnchor constraintEqualToAnchor:mainStack.bottomAnchor constant:4],
        [self.noteLabel.leadingAnchor constraintEqualToAnchor:self.contentView.leadingAnchor constant:16],
        [self.noteLabel.trailingAnchor constraintEqualToAnchor:self.contentView.trailingAnchor constant:-16],
        [self.noteLabel.bottomAnchor constraintEqualToAnchor:self.contentView.bottomAnchor constant:-8]
    ]];
}

- (void)configureWithTransaction:(Transaction *)transaction {
    self.categoryIconLabel.text = transaction.category.icon;
    self.categoryNameLabel.text = transaction.category.name;
    
    NSString *amountText = [NSString stringWithFormat:@"%@¥%.2f", transaction.type == TransactionTypeIncome ? @"+" : @"-", transaction.amount];
    self.amountLabel.text = amountText;
    self.amountLabel.textColor = transaction.type == TransactionTypeIncome ? [UIColor systemGreenColor] : [UIColor systemRedColor];
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"MM-dd HH:mm"];
    self.dateLabel.text = [dateFormatter stringFromDate:transaction.date];
    
    self.noteLabel.text = transaction.note.length == 0 ? @"无备注" : transaction.note;
}

@end

// MARK: - StoneBookkeepingViewController

@interface StoneBookkeepingViewController () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) UIView *statsView;
@property (nonatomic, strong) UILabel *balanceLabel;
@property (nonatomic, strong) UILabel *incomeLabel;
@property (nonatomic, strong) UILabel *expenseLabel;
@property (nonatomic, strong) UITableView *tableView;
@property (nonatomic, strong) UIButton *addButton;
@property (nonatomic, strong) UIButton *profileButton;

@property (nonatomic, strong) NSArray<Transaction *> *transactions;
@property (nonatomic, strong) DataManager *dataManager;

@property (nonatomic, assign) double balance;
@property (nonatomic, assign) double totalIncome;
@property (nonatomic, assign) double totalExpense;

@end

@implementation StoneBookkeepingViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.dataManager = [DataManager shared];
    
    [self setupUI];
    [self loadData];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self loadData];
}

- (void)setupUI {
    self.view.backgroundColor = [UIColor systemBackgroundColor];
    [self setupBookkeepingUI];
}

- (void)setupBookkeepingUI {
    // 清除现有视图
    for (UIView *subview in self.view.subviews) {
        [subview removeFromSuperview];
    }
    
    // 顶部统计区域
    self.statsView = [self createStatsView];
    
    // 添加按钮
    [self setupAddButton];
    
    // 我的页面按钮
    [self setupProfileButton];
    
    // 表格视图
    self.tableView = [[UITableView alloc] init];
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    [self.tableView registerClass:[TransactionCell class] forCellReuseIdentifier:@"TransactionCell"];
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
    self.tableView.tableFooterView = [[UIView alloc] init];
    
    // 按钮容器
    UIStackView *buttonStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.addButton,
        self.profileButton
    ]];
    buttonStack.axis = UILayoutConstraintAxisHorizontal;
    buttonStack.spacing = 12;
    buttonStack.distribution = UIStackViewDistributionFillEqually;
    
    // 布局
    UIStackView *stackView = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.statsView,
        buttonStack,
        self.tableView
    ]];
    stackView.axis = UILayoutConstraintAxisVertical;
    stackView.spacing = 16;
    stackView.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:stackView];
    
    [NSLayoutConstraint activateConstraints:@[
        [stackView.topAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.topAnchor constant:16],
        [stackView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:16],
        [stackView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-16],
        [stackView.bottomAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.bottomAnchor constant:-16],
        
        [self.addButton.heightAnchor constraintEqualToConstant:50],
        [self.profileButton.heightAnchor constraintEqualToConstant:50]
    ]];
}

- (UIView *)createStatsView {
    UIView *containerView = [[UIView alloc] init];
    containerView.backgroundColor = [UIColor systemGray6Color];
    containerView.layer.cornerRadius = 12;
    
    // 余额
    UILabel *balanceTitle = [self createStatTitleLabel:@"总余额"];
    self.balanceLabel = [[UILabel alloc] init];
    self.balanceLabel.font = [UIFont boldSystemFontOfSize:24];
    self.balanceLabel.textColor = [UIColor systemBlueColor];
    self.balanceLabel.text = @"¥0.00";
    
    // 收入
    UILabel *incomeTitle = [self createStatTitleLabel:@"总收入"];
    self.incomeLabel = [[UILabel alloc] init];
    self.incomeLabel.font = [UIFont systemFontOfSize:16];
    self.incomeLabel.textColor = [UIColor systemGreenColor];
    self.incomeLabel.text = @"¥0.00";
    
    // 支出
    UILabel *expenseTitle = [self createStatTitleLabel:@"总支出"];
    self.expenseLabel = [[UILabel alloc] init];
    self.expenseLabel.font = [UIFont systemFontOfSize:16];
    self.expenseLabel.textColor = [UIColor systemRedColor];
    self.expenseLabel.text = @"¥0.00";
    
    UIStackView *balanceStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        balanceTitle,
        self.balanceLabel
    ]];
    balanceStack.axis = UILayoutConstraintAxisVertical;
    balanceStack.spacing = 4;
    balanceStack.alignment = UIStackViewAlignmentCenter;
    
    UIStackView *incomeExpenseStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        incomeTitle,
        self.incomeLabel,
        expenseTitle,
        self.expenseLabel
    ]];
    incomeExpenseStack.axis = UILayoutConstraintAxisVertical;
    incomeExpenseStack.spacing = 8;
    incomeExpenseStack.alignment = UIStackViewAlignmentLeading;
    
    UIStackView *mainStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        balanceStack,
        incomeExpenseStack
    ]];
    mainStack.axis = UILayoutConstraintAxisHorizontal;
    mainStack.spacing = 20;
    mainStack.distribution = UIStackViewDistributionFillEqually;
    mainStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    [containerView addSubview:mainStack];
    
    [NSLayoutConstraint activateConstraints:@[
        [mainStack.topAnchor constraintEqualToAnchor:containerView.topAnchor constant:16],
        [mainStack.leadingAnchor constraintEqualToAnchor:containerView.leadingAnchor constant:16],
        [mainStack.trailingAnchor constraintEqualToAnchor:containerView.trailingAnchor constant:-16],
        [mainStack.bottomAnchor constraintEqualToAnchor:containerView.bottomAnchor constant:-16]
    ]];
    
    return containerView;
}

- (UILabel *)createStatTitleLabel:(NSString *)text {
    UILabel *label = [[UILabel alloc] init];
    label.text = text;
    label.font = [UIFont systemFontOfSize:14];
    label.textColor = [UIColor systemGrayColor];
    return label;
}

- (void)setupAddButton {
    self.addButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.addButton setTitle:@"添加记录" forState:UIControlStateNormal];
    self.addButton.titleLabel.font = [UIFont boldSystemFontOfSize:18];
    self.addButton.backgroundColor = [UIColor systemBlueColor];
    [self.addButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    self.addButton.layer.cornerRadius = 8;
    [self.addButton addTarget:self action:@selector(addTransaction) forControlEvents:UIControlEventTouchUpInside];
}

- (void)setupProfileButton {
    self.profileButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.profileButton setTitle:@"我的" forState:UIControlStateNormal];
    self.profileButton.titleLabel.font = [UIFont boldSystemFontOfSize:18];
    self.profileButton.backgroundColor = [UIColor systemGrayColor];
    [self.profileButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    self.profileButton.layer.cornerRadius = 8;
    [self.profileButton addTarget:self action:@selector(showProfile) forControlEvents:UIControlEventTouchUpInside];
}

- (void)loadData {
    self.transactions = [self.dataManager getAllTransactions];
    
    // 计算统计信息
    self.totalIncome = 0;
    self.totalExpense = 0;
    
    for (Transaction *transaction in self.transactions) {
        if (transaction.type == TransactionTypeIncome) {
            self.totalIncome += transaction.amount;
        } else {
            self.totalExpense += transaction.amount;
        }
    }
    
    self.balance = self.totalIncome - self.totalExpense;
    
    // 更新UI
    [self updateStatsDisplay];
    [self.tableView reloadData];
}

- (void)updateStatsDisplay {
    self.balanceLabel.text = [NSString stringWithFormat:@"¥%.2f", self.balance];
    self.incomeLabel.text = [NSString stringWithFormat:@"¥%.2f", self.totalIncome];
    self.expenseLabel.text = [NSString stringWithFormat:@"¥%.2f", self.totalExpense];
}

- (void)addTransaction {
    AddTransactionViewController *addVC = [[AddTransactionViewController alloc] init];
    __weak typeof(self) weakSelf = self;
    addVC.onTransactionAdded = ^{
        [weakSelf loadData];
    };
    
    UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:addVC];
    [self presentViewController:navController animated:YES completion:nil];
}

- (void)showProfile {
    ProfileViewController *profileVC = [[ProfileViewController alloc] init];
    profileVC.modalPresentationStyle = UIModalPresentationFullScreen;
    [self presentViewController:profileVC animated:YES completion:nil];
}

- (void)showAlertWithTitle:(NSString *)title message:(NSString *)message {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.transactions.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    TransactionCell *cell = [tableView dequeueReusableCellWithIdentifier:@"TransactionCell" forIndexPath:indexPath];
    Transaction *transaction = self.transactions[indexPath.row];
    [cell configureWithTransaction:transaction];
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 60;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        Transaction *transaction = self.transactions[indexPath.row];
        [self.dataManager deleteTransaction:transaction];
        
        NSMutableArray *mutableTransactions = [self.transactions mutableCopy];
        [mutableTransactions removeObjectAtIndex:indexPath.row];
        self.transactions = [mutableTransactions copy];
        
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
        [self loadData]; // 重新计算统计信息
    }
}

@end