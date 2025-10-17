#import "StoneBookkeepingViewController.h"
#import "DataManager.h"
#import "AddTransactionViewController.h"
#import "ProfileViewController.h"
#import "AIAssistantViewController.h"
#import "TransactionDetailViewController.h"
#import "Transaction.h"
#import "Category.h"

// MARK: - TransactionCell

@interface TransactionCell : UITableViewCell

@property (nonatomic, strong) UIImageView *categoryIconView;
@property (nonatomic, strong) UILabel *categoryNameLabel;
@property (nonatomic, strong) UILabel *amountLabel;
@property (nonatomic, strong) UILabel *dateLabel;
@property (nonatomic, strong) UIImageView *expandIconView;

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
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    
    // 类别图标
    self.categoryIconView = [[UIImageView alloc] init];
    self.categoryIconView.contentMode = UIViewContentModeCenter;
    self.categoryIconView.backgroundColor = [UIColor colorWithRed:0.92 green:0.92 blue:0.96 alpha:1.0];
    self.categoryIconView.layer.cornerRadius = 20;
    
    // 类别名称
    self.categoryNameLabel = [[UILabel alloc] init];
    self.categoryNameLabel.font = [UIFont systemFontOfSize:16];
    
    // 日期
    self.dateLabel = [[UILabel alloc] init];
    self.dateLabel.font = [UIFont systemFontOfSize:14];
    self.dateLabel.textColor = [UIColor systemGrayColor];
    
    // 金额
    self.amountLabel = [[UILabel alloc] init];
    self.amountLabel.font = [UIFont boldSystemFontOfSize:16];
    self.amountLabel.textAlignment = NSTextAlignmentRight;
    
    // 展开图标
    self.expandIconView = [[UIImageView alloc] initWithImage:[UIImage systemImageNamed:@"chevron.right"]];
    self.expandIconView.tintColor = [UIColor systemGrayColor];
    
    // 左侧垂直布局
    UIStackView *leftStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.categoryNameLabel,
        self.dateLabel
    ]];
    leftStack.axis = UILayoutConstraintAxisVertical;
    leftStack.spacing = 4;
    leftStack.alignment = UIStackViewAlignmentLeading;
    
    // 右侧水平布局
    UIStackView *rightStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.amountLabel,
        self.expandIconView
    ]];
    rightStack.axis = UILayoutConstraintAxisHorizontal;
    rightStack.spacing = 8;
    rightStack.alignment = UIStackViewAlignmentCenter;
    
    // 主水平布局
    UIStackView *mainStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.categoryIconView,
        leftStack,
        rightStack
    ]];
    mainStack.axis = UILayoutConstraintAxisHorizontal;
    mainStack.spacing = 12;
    mainStack.alignment = UIStackViewAlignmentCenter;
    mainStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.contentView addSubview:mainStack];
    
    [NSLayoutConstraint activateConstraints:@[
        [mainStack.topAnchor constraintEqualToAnchor:self.contentView.topAnchor constant:12],
        [mainStack.leadingAnchor constraintEqualToAnchor:self.contentView.leadingAnchor constant:16],
        [mainStack.trailingAnchor constraintEqualToAnchor:self.contentView.trailingAnchor constant:-16],
        [mainStack.bottomAnchor constraintEqualToAnchor:self.contentView.bottomAnchor constant:-12],
        
        [self.categoryIconView.widthAnchor constraintEqualToConstant:40],
        [self.categoryIconView.heightAnchor constraintEqualToConstant:40],
        [self.expandIconView.widthAnchor constraintEqualToConstant:20],
        [self.expandIconView.heightAnchor constraintEqualToConstant:20]
    ]];
}

- (void)configureWithTransaction:(Transaction *)transaction {
    // 使用系统图标代替文本
    UIImage *categoryImage = [self getCategoryImageForName:transaction.category.name];
    self.categoryIconView.image = categoryImage;
    self.categoryIconView.tintColor = [UIColor systemBlueColor];
    
    self.categoryNameLabel.text = transaction.category.name;
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd"];
    self.dateLabel.text = [dateFormatter stringFromDate:transaction.date];
    
    self.amountLabel.text = [NSString stringWithFormat:@"-%@%.2f", @"¥", transaction.amount];
    self.amountLabel.textColor = [UIColor systemRedColor];
}

- (UIImage *)getCategoryImageForName:(NSString *)name {
    // 根据类别名称返回对应的系统图标
    if ([name isEqualToString:@"购物"]) {
        return [UIImage systemImageNamed:@"bag"];
    } else if ([name isEqualToString:@"餐饮"]) {
        return [UIImage systemImageNamed:@"fork.knife"];
    } else if ([name isEqualToString:@"交通"]) {
        return [UIImage systemImageNamed:@"car"];
    } else if ([name isEqualToString:@"娱乐"]) {
        return [UIImage systemImageNamed:@"gamecontroller"];
    } else if ([name isEqualToString:@"医疗"]) {
        return [UIImage systemImageNamed:@"cross"];
    } else if ([name isEqualToString:@"教育"]) {
        return [UIImage systemImageNamed:@"book"];
    } else if ([name isEqualToString:@"工资"]) {
        return [UIImage systemImageNamed:@"banknote"];
    } else if ([name isEqualToString:@"奖金"]) {
        return [UIImage systemImageNamed:@"gift"];
    } else if ([name isEqualToString:@"投资"]) {
        return [UIImage systemImageNamed:@"chart.pie"];
    } else {
        return [UIImage systemImageNamed:@"ellipsis"];
    }
}

@end

// MARK: - StoneBookkeepingViewController

@interface StoneBookkeepingViewController () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) UIView *headerView;
@property (nonatomic, strong) UILabel *dateLabel;
@property (nonatomic, strong) UIButton *dateButton;
@property (nonatomic, strong) UIStackView *statsStackView;
@property (nonatomic, strong) UILabel *expenseLabel;
@property (nonatomic, strong) UILabel *incomeLabel;
@property (nonatomic, strong) UITableView *tableView;
@property (nonatomic, strong) UIButton *addButton;
@property (nonatomic, strong) UIView *bottomNavBar;
@property (nonatomic, strong) UIButton *homeButton;
@property (nonatomic, strong) UIButton *profileButton;
@property (nonatomic, strong) UIButton *aiAssistantButton;

@property (nonatomic, strong) NSArray<Transaction *> *transactions;
@property (nonatomic, strong) DataManager *dataManager;

@property (nonatomic, assign) double todayExpense;
@property (nonatomic, assign) double todayIncome;

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
    self.view.backgroundColor = [UIColor colorWithRed:0.96 green:0.96 blue:0.98 alpha:1.0];
    
    // 设置导航栏样式
    [self.navigationController setNavigationBarHidden:YES animated:NO];
    
    // 头部视图
    [self setupHeaderView];
    
    // 表格视图
    [self setupTableView];
    
    // 底部导航栏
    [self setupBottomNavBar];
    
    // 浮动添加按钮
    [self setupAddButton];
    
    // AI助手按钮
    [self setupAIAssistantButton];
}

- (void)setupHeaderView {
    self.headerView = [[UIView alloc] init];
    self.headerView.backgroundColor = [UIColor whiteColor];
    self.headerView.layer.shadowColor = [UIColor blackColor].CGColor;
    self.headerView.layer.shadowOffset = CGSizeMake(0, 2);
    self.headerView.layer.shadowOpacity = 0.1;
    self.headerView.layer.shadowRadius = 4;
    self.headerView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 日期选择器
    UIView *dateSection = [[UIView alloc] init];
    self.dateLabel = [[UILabel alloc] init];
    self.dateLabel.text = @"今日";
    self.dateLabel.font = [UIFont boldSystemFontOfSize:18];
    
    self.dateButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.dateButton setImage:[UIImage systemImageNamed:@"calendar"] forState:UIControlStateNormal];
    self.dateButton.tintColor = [UIColor systemGrayColor];
    [self.dateButton addTarget:self action:@selector(showDatePicker) forControlEvents:UIControlEventTouchUpInside];
    
    UIStackView *dateStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.dateLabel,
        self.dateButton
    ]];
    dateStack.axis = UILayoutConstraintAxisHorizontal;
    dateStack.spacing = 8;
    dateStack.alignment = UIStackViewAlignmentCenter;
    dateStack.distribution = UIStackViewDistributionFill;
    dateStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 统计区域
    UILabel *expenseTitleLabel = [[UILabel alloc] init];
    expenseTitleLabel.text = @"支出";
    expenseTitleLabel.font = [UIFont systemFontOfSize:14];
    expenseTitleLabel.textColor = [UIColor systemGrayColor];
    
    self.expenseLabel = [[UILabel alloc] init];
    self.expenseLabel.text = @"¥0.00";
    self.expenseLabel.font = [UIFont systemFontOfSize:16];
    self.expenseLabel.textColor = [UIColor systemRedColor];
    
    UILabel *incomeTitleLabel = [[UILabel alloc] init];
    incomeTitleLabel.text = @"收入";
    incomeTitleLabel.font = [UIFont systemFontOfSize:14];
    incomeTitleLabel.textColor = [UIColor systemGrayColor];
    
    self.incomeLabel = [[UILabel alloc] init];
    self.incomeLabel.text = @"¥0.00";
    self.incomeLabel.font = [UIFont systemFontOfSize:16];
    self.incomeLabel.textColor = [UIColor systemGreenColor];
    
    UIStackView *expenseStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        expenseTitleLabel,
        self.expenseLabel
    ]];
    expenseStack.axis = UILayoutConstraintAxisVertical;
    expenseStack.spacing = 4;
    expenseStack.alignment = UIStackViewAlignmentCenter;
    
    UIStackView *incomeStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        incomeTitleLabel,
        self.incomeLabel
    ]];
    incomeStack.axis = UILayoutConstraintAxisVertical;
    incomeStack.spacing = 4;
    incomeStack.alignment = UIStackViewAlignmentCenter;
    
    self.statsStackView = [[UIStackView alloc] initWithArrangedSubviews:@[
        expenseStack,
        incomeStack
    ]];
    self.statsStackView.axis = UILayoutConstraintAxisHorizontal;
    self.statsStackView.distribution = UIStackViewDistributionFillEqually;
    self.statsStackView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 主垂直布局
    UIStackView *headerStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        dateStack,
        self.statsStackView
    ]];
    headerStack.axis = UILayoutConstraintAxisVertical;
    headerStack.spacing = 16;
    headerStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.headerView addSubview:headerStack];
    [self.view addSubview:self.headerView];
    
    [NSLayoutConstraint activateConstraints:@[
        [self.headerView.topAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.topAnchor],
        [self.headerView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor],
        [self.headerView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor],
        
        [headerStack.topAnchor constraintEqualToAnchor:self.headerView.topAnchor constant:16],
        [headerStack.leadingAnchor constraintEqualToAnchor:self.headerView.leadingAnchor constant:16],
        [headerStack.trailingAnchor constraintEqualToAnchor:self.headerView.trailingAnchor constant:-16],
        [headerStack.bottomAnchor constraintEqualToAnchor:self.headerView.bottomAnchor constant:-16],
        
        [self.dateButton.widthAnchor constraintEqualToConstant:24],
        [self.dateButton.heightAnchor constraintEqualToConstant:24]
    ]];
}

- (void)setupTableView {
    self.tableView = [[UITableView alloc] init];
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    [self.tableView registerClass:[TransactionCell class] forCellReuseIdentifier:@"TransactionCell"];
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.tableView.backgroundColor = [UIColor clearColor];
    self.tableView.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:self.tableView];
    
    [NSLayoutConstraint activateConstraints:@[
        [self.tableView.topAnchor constraintEqualToAnchor:self.headerView.bottomAnchor],
        [self.tableView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor],
        [self.tableView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor],
        [self.tableView.bottomAnchor constraintEqualToAnchor:self.view.bottomAnchor]
    ]];
}

- (void)setupBottomNavBar {
    self.bottomNavBar = [[UIView alloc] init];
    self.bottomNavBar.backgroundColor = [UIColor whiteColor];
    self.bottomNavBar.layer.shadowColor = [UIColor blackColor].CGColor;
    self.bottomNavBar.layer.shadowOffset = CGSizeMake(0, -2);
    self.bottomNavBar.layer.shadowOpacity = 0.1;
    self.bottomNavBar.layer.shadowRadius = 4;
    self.bottomNavBar.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 首页按钮
    self.homeButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.homeButton setImage:[UIImage systemImageNamed:@"house.fill"] forState:UIControlStateNormal];
    [self.homeButton setTitle:@"首页" forState:UIControlStateNormal];
    self.homeButton.imageView.contentMode = UIViewContentModeScaleAspectFit;
    self.homeButton.titleLabel.font = [UIFont systemFontOfSize:12];
    self.homeButton.tintColor = [UIColor systemBlueColor];
    [self.homeButton addTarget:self action:@selector(showHome) forControlEvents:UIControlEventTouchUpInside];
    
    // AI助手按钮
    self.aiAssistantButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.aiAssistantButton setImage:[UIImage systemImageNamed:@"brain.fill"] forState:UIControlStateNormal];
    [self.aiAssistantButton setTitle:@"AI助手" forState:UIControlStateNormal];
    self.aiAssistantButton.imageView.contentMode = UIViewContentModeScaleAspectFit;
    self.aiAssistantButton.titleLabel.font = [UIFont systemFontOfSize:12];
    self.aiAssistantButton.tintColor = [UIColor systemGrayColor];
    [self.aiAssistantButton addTarget:self action:@selector(showAIAssistant) forControlEvents:UIControlEventTouchUpInside];
    
    // 我的按钮
    self.profileButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.profileButton setImage:[UIImage systemImageNamed:@"person"] forState:UIControlStateNormal];
    [self.profileButton setTitle:@"我的" forState:UIControlStateNormal];
    self.profileButton.imageView.contentMode = UIViewContentModeScaleAspectFit;
    self.profileButton.titleLabel.font = [UIFont systemFontOfSize:12];
    self.profileButton.tintColor = [UIColor systemGrayColor];
    [self.profileButton addTarget:self action:@selector(showProfile) forControlEvents:UIControlEventTouchUpInside];
    
    // 设置按钮样式（图片在上，文字在下）
    [self setupButtonLayout:self.homeButton];
    [self setupButtonLayout:self.aiAssistantButton];
    [self setupButtonLayout:self.profileButton];
    
    UIStackView *navStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.homeButton,
        self.aiAssistantButton,
        self.profileButton
    ]];
    navStack.axis = UILayoutConstraintAxisHorizontal;
    navStack.distribution = UIStackViewDistributionFillEqually;
    navStack.alignment = UIStackViewAlignmentCenter;
    navStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.bottomNavBar addSubview:navStack];
    [self.view addSubview:self.bottomNavBar];
    
    [NSLayoutConstraint activateConstraints:@[
        [self.bottomNavBar.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor],
        [self.bottomNavBar.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor],
        [self.bottomNavBar.bottomAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.bottomAnchor],
        [self.bottomNavBar.heightAnchor constraintEqualToConstant:80],
        
        [navStack.topAnchor constraintEqualToAnchor:self.bottomNavBar.topAnchor],
        [navStack.leadingAnchor constraintEqualToAnchor:self.bottomNavBar.leadingAnchor],
        [navStack.trailingAnchor constraintEqualToAnchor:self.bottomNavBar.trailingAnchor],
        [navStack.bottomAnchor constraintEqualToAnchor:self.bottomNavBar.bottomAnchor]
    ]];
    
    // 调整表格视图的底部约束，避免被导航栏遮挡
    [NSLayoutConstraint deactivateConstraints:[self.tableView.constraints filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(NSLayoutConstraint *constraint, NSDictionary *bindings) {
        return constraint.firstAttribute == NSLayoutAttributeBottom && [constraint.firstItem isEqual:self.tableView];
    }]]];
    
    [NSLayoutConstraint activateConstraints:@[
        [self.tableView.bottomAnchor constraintEqualToAnchor:self.bottomNavBar.topAnchor]
    ]];
}

- (void)setupButtonLayout:(UIButton *)button {
    button.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
    button.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    
    // 设置图片和文字的间距
    button.imageEdgeInsets = UIEdgeInsetsMake(0, 0, 15, 0);
    button.titleEdgeInsets = UIEdgeInsetsMake(30, -button.imageView.bounds.size.width, 0, 0);
}

- (void)setupAddButton {
    self.addButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.addButton setTitle:@"+" forState:UIControlStateNormal];
    self.addButton.titleLabel.font = [UIFont systemFontOfSize:28];
    self.addButton.backgroundColor = [UIColor systemBlueColor];
    [self.addButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    self.addButton.layer.cornerRadius = 25;
    self.addButton.layer.shadowColor = [UIColor systemBlueColor].CGColor;
    self.addButton.layer.shadowOffset = CGSizeMake(0, 4);
    self.addButton.layer.shadowOpacity = 0.3;
    self.addButton.layer.shadowRadius = 6;
    [self.addButton addTarget:self action:@selector(addTransaction) forControlEvents:UIControlEventTouchUpInside];
    self.addButton.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:self.addButton];
    
    [NSLayoutConstraint activateConstraints:@[
        [self.addButton.centerXAnchor constraintEqualToAnchor:self.view.centerXAnchor],
        [self.addButton.bottomAnchor constraintEqualToAnchor:self.bottomNavBar.topAnchor constant:-15],
        [self.addButton.widthAnchor constraintEqualToConstant:50],
        [self.addButton.heightAnchor constraintEqualToConstant:50]
    ]];
}

// 移除浮动AI助手按钮，因为已经整合到底部导航栏
- (void)setupAIAssistantButton {
    // 不再需要浮动的AI助手按钮
}

- (void)loadData {
    self.transactions = [self.dataManager getAllTransactions];
    
    // 计算今日的收支
    [self calculateTodayStats];
    
    // 更新UI
    [self updateStatsDisplay];
    [self.tableView reloadData];
}

- (void)calculateTodayStats {
    NSDate *today = [NSDate date];
    NSDateComponents *components = [[NSCalendar currentCalendar] components:NSCalendarUnitYear | NSCalendarUnitMonth | NSCalendarUnitDay fromDate:today];
    components.hour = 0;
    components.minute = 0;
    components.second = 0;
    NSDate *startOfDay = [[NSCalendar currentCalendar] dateFromComponents:components];
    
    components.hour = 23;
    components.minute = 59;
    components.second = 59;
    NSDate *endOfDay = [[NSCalendar currentCalendar] dateFromComponents:components];
    
    self.todayExpense = 0;
    self.todayIncome = 0;
    
    for (Transaction *transaction in self.transactions) {
        if ([transaction.date compare:startOfDay] != NSOrderedAscending && [transaction.date compare:endOfDay] != NSOrderedDescending) {
            if (transaction.type == TransactionTypeIncome) {
                self.todayIncome += transaction.amount;
            } else {
                self.todayExpense += transaction.amount;
            }
        }
    }
}

- (void)updateStatsDisplay {
    self.expenseLabel.text = [NSString stringWithFormat:@"¥%.2f", self.todayExpense];
    self.incomeLabel.text = [NSString stringWithFormat:@"¥%.2f", self.todayIncome];
}

- (void)showDatePicker {
    // 这里可以实现日期选择功能
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"选择日期" message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    [alert addAction:[UIAlertAction actionWithTitle:@"今日" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.dateLabel.text = @"今日";
        [self loadData];
    }]];
    [alert addAction:[UIAlertAction actionWithTitle:@"本月" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.dateLabel.text = @"本月";
        // 这里可以实现按月筛选的逻辑
        [self loadData];
    }]];
    [alert addAction:[UIAlertAction actionWithTitle:@"自定义" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        // 可以显示日期选择器
        [self loadData];
    }]];
    [alert addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil]];
    
    [self presentViewController:alert animated:YES completion:nil];
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

- (void)showHome {
    // 已经在首页，无需操作
}

- (void)showProfile {
    ProfileViewController *profileVC = [[ProfileViewController alloc] init];
    profileVC.modalPresentationStyle = UIModalPresentationFullScreen;
    [self presentViewController:profileVC animated:YES completion:nil];
}

- (void)showAIAssistant {
    // 跳转到AI助手页面
    AIAssistantViewController *aiAssistantVC = [[AIAssistantViewController alloc] init];
    [self.navigationController pushViewController:aiAssistantVC animated:YES];
}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.transactions.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    TransactionCell *cell = [tableView dequeueReusableCellWithIdentifier:@"TransactionCell" forIndexPath:indexPath];
    Transaction *transaction = self.transactions[indexPath.row];
    [cell configureWithTransaction:transaction];
    
    // 添加分割线
    if (indexPath.row < self.transactions.count - 1) {
        UIView *separatorLine = [[UIView alloc] initWithFrame:CGRectMake(72, 59, tableView.frame.size.width - 72, 1)];
        separatorLine.backgroundColor = [UIColor colorWithRed:0.90 green:0.90 blue:0.92 alpha:1.0];
        [cell.contentView addSubview:separatorLine];
    }
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 60;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    UIView *headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, tableView.frame.size.width, 40)];
    headerView.backgroundColor = [UIColor colorWithRed:0.96 green:0.96 blue:0.98 alpha:1.0];
    
    UILabel *headerLabel = [[UILabel alloc] initWithFrame:CGRectMake(16, 12, 100, 16)];
    headerLabel.text = @"支出明细";
    headerLabel.font = [UIFont systemFontOfSize:14];
    headerLabel.textColor = [UIColor systemGrayColor];
    [headerView addSubview:headerLabel];
    
    return headerView;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 40;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    // 跳转到交易详情页
    TransactionDetailViewController *detailVC = [[TransactionDetailViewController alloc] init];
    detailVC.transaction = self.transactions[indexPath.row];
    [self.navigationController pushViewController:detailVC animated:YES];
}

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

- (NSString *)formatDate:(NSDate *)date {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    return [formatter stringFromDate:date];
}

@end