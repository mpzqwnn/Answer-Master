#import "TransactionDetailViewController.h"

@interface TransactionDetailViewController ()

@property (nonatomic, strong) UIView *headerView;
@property (nonatomic, strong) UILabel *amountLabel;
@property (nonatomic, strong) UIButton *editButton;
@property (nonatomic, strong) UITableView *detailTableView;
@property (nonatomic, strong) NSArray *detailItems;

@end

@implementation TransactionDetailViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = @"账单详情";
    self.view.backgroundColor = [UIColor systemBackgroundColor];
    
    // 设置返回按钮
    UIBarButtonItem *backButton = [[UIBarButtonItem alloc] initWithTitle:@"返回" style:UIBarButtonItemStylePlain target:self action:@selector(backAction)];
    self.navigationItem.leftBarButtonItem = backButton;
    
    [self setupData];
    [self setupUI];
}

- (void)setupData {
    if (self.transaction) {
        // 使用传入的交易数据
        NSString *transactionType = (self.transaction.type == TransactionTypeExpense) ? @"支出" : @"收入";
        NSString *typeIcon = (self.transaction.type == TransactionTypeExpense) ? @"arrow.down.circle" : @"arrow.up.circle";
        
        // 格式化日期和时间
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        dateFormatter.dateFormat = @"yyyy-MM-dd";
        NSString *dateString = [dateFormatter stringFromDate:self.transaction.date];
        
        dateFormatter.dateFormat = @"HH:mm";
        NSString *timeString = [dateFormatter stringFromDate:self.transaction.date];
        
        // 设置类别图标
        NSString *categoryIcon = [self getCategoryIconForName:self.transaction.category.name];
        
        self.detailItems = @[
            @{@"title": @"类别", @"value": self.transaction.category.name, @"icon": categoryIcon},
            @{@"title": @"类型", @"value": transactionType, @"icon": typeIcon},
            @{@"title": @"日期", @"value": dateString, @"icon": @"calendar"},
            @{@"title": @"时间", @"value": timeString, @"icon": @"clock"},
            @{@"title": @"备注", @"value": (self.transaction.note.length > 0) ? self.transaction.note : @"无", @"icon": @"note.text"}
        ];
        
        // 设置金额
        self.amountLabel.text = [NSString stringWithFormat:@"¥%.2f", self.transaction.amount];
        self.amountLabel.textColor = (self.transaction.type == TransactionTypeExpense) ? [UIColor systemRedColor] : [UIColor systemGreenColor];
    } else {
        // 模拟交易数据作为备用
        self.detailItems = @[
            @{@"title": @"类别", @"value": @"购物", @"icon": @"bag"},
            @{@"title": @"类型", @"value": @"支出", @"icon": @"arrow.down.circle"},
            @{@"title": @"日期", @"value": @"2023-10-11", @"icon": @"calendar"},
            @{@"title": @"时间", @"value": @"15:30", @"icon": @"clock"},
            @{@"title": @"备注", @"value": @"生日礼物", @"icon": @"note.text"}
        ];
    }
}

- (NSString *)getCategoryIconForName:(NSString *)categoryName {
    // 映射类别名称到系统图标
    NSDictionary *iconMap = @{
        @"购物": @"bag",
        @"餐饮": @"fork.knife",
        @"交通": @"car",
        @"娱乐": @"gamecontroller",
        @"医疗": @"stethoscope",
        @"教育": @"book",
        @"住房": @"house",
        @"工资": @"dollarsign.circle",
        @"奖金": @"trophy",
        @"投资": @"chart.line",
        @"其他": @"ellipsis.circle"
    };
    
    NSString *icon = iconMap[categoryName];
    return icon ? icon : @"ellipsis.circle";
}

- (void)setupUI {
    // 头部视图
    self.headerView = [[UIView alloc] init];
    self.headerView.backgroundColor = [UIColor whiteColor];
    self.headerView.layer.shadowColor = [UIColor blackColor].CGColor;
    self.headerView.layer.shadowOffset = CGSizeMake(0, 2);
    self.headerView.layer.shadowOpacity = 0.1;
    self.headerView.layer.shadowRadius = 4;
    self.headerView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 金额标签
    self.amountLabel = [[UILabel alloc] init];
    self.amountLabel.text = @"¥222.00";
    self.amountLabel.font = [UIFont systemFontOfSize:32 weight:UIFontWeightMedium];
    self.amountLabel.textColor = [UIColor systemRedColor];
    self.amountLabel.textAlignment = NSTextAlignmentCenter;
    self.amountLabel.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 编辑按钮
    self.editButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.editButton setTitle:@"编辑" forState:UIControlStateNormal];
    [self.editButton setTitleColor:[UIColor systemBlueColor] forState:UIControlStateNormal];
    self.editButton.titleLabel.font = [UIFont systemFontOfSize:16];
    [self.editButton addTarget:self action:@selector(editAction) forControlEvents:UIControlEventTouchUpInside];
    self.editButton.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 表格视图
    self.detailTableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStyleGrouped];
    self.detailTableView.dataSource = self;
    self.detailTableView.delegate = self;
    self.detailTableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.detailTableView.backgroundColor = [UIColor systemGray5Color];
    [self.detailTableView registerClass:[UITableViewCell class] forCellReuseIdentifier:@"DetailCell"];
    self.detailTableView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 添加子视图
    [self.view addSubview:self.headerView];
    [self.headerView addSubview:self.amountLabel];
    [self.headerView addSubview:self.editButton];
    [self.view addSubview:self.detailTableView];
    
    // 布局
    [self setupLayout];
    
    // 添加底部操作按钮
    [self addBottomButton];
}

- (void)setupLayout {
    [NSLayoutConstraint activateConstraints:@[
        // 头部视图
        [self.headerView.topAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.topAnchor],
        [self.headerView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor],
        [self.headerView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor],
        [self.headerView.heightAnchor constraintEqualToConstant:120],
        
        // 金额标签
        [self.amountLabel.centerXAnchor constraintEqualToAnchor:self.headerView.centerXAnchor],
        [self.amountLabel.centerYAnchor constraintEqualToAnchor:self.headerView.centerYAnchor],
        
        // 编辑按钮
        [self.editButton.trailingAnchor constraintEqualToAnchor:self.headerView.trailingAnchor constant:-20],
        [self.editButton.topAnchor constraintEqualToAnchor:self.headerView.topAnchor constant:20],
        
        // 表格视图
        [self.detailTableView.topAnchor constraintEqualToAnchor:self.headerView.bottomAnchor],
        [self.detailTableView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor],
        [self.detailTableView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor],
        [self.detailTableView.bottomAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.bottomAnchor]
    ]];
}

- (void)addBottomButton {
    // 创建底部视图
    UIView *bottomView = [[UIView alloc] init];
    bottomView.backgroundColor = [UIColor whiteColor];
    bottomView.layer.shadowColor = [UIColor blackColor].CGColor;
    bottomView.layer.shadowOffset = CGSizeMake(0, -2);
    bottomView.layer.shadowOpacity = 0.1;
    bottomView.layer.shadowRadius = 4;
    bottomView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 删除按钮
    UIButton *deleteButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [deleteButton setTitle:@"删除记录" forState:UIControlStateNormal];
    [deleteButton setTitleColor:[UIColor systemRedColor] forState:UIControlStateNormal];
    deleteButton.titleLabel.font = [UIFont systemFontOfSize:18];
    deleteButton.backgroundColor = [UIColor systemGray5Color];
    deleteButton.layer.cornerRadius = 8;
    [deleteButton addTarget:self action:@selector(deleteAction) forControlEvents:UIControlEventTouchUpInside];
    deleteButton.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 添加子视图
    [self.view addSubview:bottomView];
    [bottomView addSubview:deleteButton];
    
    // 布局
    [NSLayoutConstraint activateConstraints:@[
        // 底部视图
        [bottomView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor],
        [bottomView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor],
        [bottomView.bottomAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.bottomAnchor],
        [bottomView.heightAnchor constraintEqualToConstant:80],
        
        // 删除按钮
        [deleteButton.leadingAnchor constraintEqualToAnchor:bottomView.leadingAnchor constant:20],
        [deleteButton.trailingAnchor constraintEqualToAnchor:bottomView.trailingAnchor constant:-20],
        [deleteButton.topAnchor constraintEqualToAnchor:bottomView.topAnchor constant:16],
        [deleteButton.bottomAnchor constraintEqualToAnchor:bottomView.bottomAnchor constant:-16],
        [deleteButton.heightAnchor constraintEqualToConstant:48]
    ]];
    
    // 调整表格视图底部约束
    [self.detailTableView.bottomAnchor constraintEqualToAnchor:bottomView.topAnchor].active = YES;
}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.detailItems.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"DetailCell" forIndexPath:indexPath];
    
    // 配置单元格样式
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    // 获取数据
    NSDictionary *item = self.detailItems[indexPath.row];
    
    // 设置图标
    UIImage *icon = [UIImage systemImageNamed:item[@"icon"]];
    UIImageView *iconView = [[UIImageView alloc] initWithImage:icon];
    iconView.tintColor = [UIColor systemGrayColor];
    iconView.frame = CGRectMake(0, 0, 24, 24);
    cell.accessoryView = iconView;
    
    // 设置标题和值
    cell.textLabel.text = item[@"title"];
    cell.textLabel.font = [UIFont systemFontOfSize:16];
    cell.textLabel.textColor = [UIColor systemGrayColor];
    
    cell.detailTextLabel.text = item[@"value"];
    cell.detailTextLabel.font = [UIFont systemFontOfSize:16 weight:UIFontWeightMedium];
    cell.detailTextLabel.textColor = [UIColor labelColor];
    
    // 添加分割线
    UIView *separatorView = [[UIView alloc] initWithFrame:CGRectMake(16, 43, tableView.bounds.size.width - 32, 1)];
    separatorView.backgroundColor = [UIColor systemGray2Color];
    [cell addSubview:separatorView];
    
    return cell;
}

#pragma mark - UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 44;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 20;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    return [[UIView alloc] init];
}

#pragma mark - 按钮事件

- (void)backAction {
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)editAction {
    // 跳转到编辑页面
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"编辑功能" 
                                                                   message:@"编辑功能开发中" 
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil];
    [alert addAction:okAction];
    
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)deleteAction {
    // 显示删除确认弹窗
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"删除确认" 
                                                                   message:@"确定要删除这条记录吗？" 
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction *deleteAction = [UIAlertAction actionWithTitle:@"删除" style:UIAlertActionStyleDestructive handler:^(UIAlertAction * _Nonnull action) {
        // 删除记录
        [self.navigationController popViewControllerAnimated:YES];
    }];
    
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil];
    
    [alert addAction:deleteAction];
    [alert addAction:cancelAction];
    
    [self presentViewController:alert animated:YES completion:nil];
}

@end