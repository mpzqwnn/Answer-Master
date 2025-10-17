#import "ProfileViewController.h"

@interface ProfileViewController ()

@property (nonatomic, strong) UIView *headerView;
@property (nonatomic, strong) UIImageView *avatarImageView;
@property (nonatomic, strong) UILabel *userNameLabel;
@property (nonatomic, strong) UILabel *userIdLabel;
@property (nonatomic, strong) UITableView *settingsTableView;
@property (nonatomic, strong) NSArray *settingsSections;

@end

@implementation ProfileViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = @"我的";
    self.view.backgroundColor = [UIColor systemBackgroundColor];
    
    [self setupData];
    [self setupUI];
}

- (void)setupData {
    // 设置菜单项
    self.settingsSections = @[
        @{
            @"title": @"我的数据",
            @"items": @[
                @{@"title": @"账单详情", @"icon": @"chart.bar", @"showArrow": @(YES)},
                @{@"title": @"数据统计", @"icon": @"chart.pie", @"showArrow": @(YES)}
            ]
        },
        @{
            @"title": @"设置",
            @"items": @[
                @{@"title": @"关于我们", @"icon": @"info.circle", @"showArrow": @(YES)},
                @{@"title": @"隐私协议", @"icon": @"shield", @"showArrow": @(YES)},
                @{@"title": @"用户协议", @"icon": @"doc.text", @"showArrow": @(YES)}
            ]
        }
    ];
}

- (void)setupUI {
    [self setupHeaderView];
    [self setupTableView];
    [self setupLayout];
}

- (void)setupHeaderView {
    self.headerView = [[UIView alloc] init];
    self.headerView.backgroundColor = [UIColor systemBlueColor];
    self.headerView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 头像
    self.avatarImageView = [[UIImageView alloc] init];
    self.avatarImageView.image = [UIImage systemImageNamed:@"person.circle"];
    self.avatarImageView.contentMode = UIViewContentModeScaleAspectFit;
    self.avatarImageView.tintColor = [UIColor whiteColor];
    self.avatarImageView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 用户名
    self.userNameLabel = [[UILabel alloc] init];
    self.userNameLabel.text = @"150****0212";
    self.userNameLabel.textColor = [UIColor whiteColor];
    self.userNameLabel.font = [UIFont systemFontOfSize:20 weight:UIFontWeightMedium];
    self.userNameLabel.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 用户ID
    self.userIdLabel = [[UILabel alloc] init];
    self.userIdLabel.text = @"ID: 0233";
    self.userIdLabel.textColor = [UIColor colorWithRed:0.9 green:0.9 blue:1.0 alpha:1.0];
    self.userIdLabel.font = [UIFont systemFontOfSize:14];
    self.userIdLabel.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 添加子视图
    [self.headerView addSubview:self.avatarImageView];
    [self.headerView addSubview:self.userNameLabel];
    [self.headerView addSubview:self.userIdLabel];
    
    // 布局
    [NSLayoutConstraint activateConstraints:@[
        [self.avatarImageView.topAnchor constraintEqualToAnchor:self.headerView.topAnchor constant:32],
        [self.avatarImageView.centerXAnchor constraintEqualToAnchor:self.headerView.centerXAnchor],
        [self.avatarImageView.widthAnchor constraintEqualToConstant:80],
        [self.avatarImageView.heightAnchor constraintEqualToConstant:80],
        
        [self.userNameLabel.topAnchor constraintEqualToAnchor:self.avatarImageView.bottomAnchor constant:16],
        [self.userNameLabel.centerXAnchor constraintEqualToAnchor:self.headerView.centerXAnchor],
        
        [self.userIdLabel.topAnchor constraintEqualToAnchor:self.userNameLabel.bottomAnchor constant:8],
        [self.userIdLabel.centerXAnchor constraintEqualToAnchor:self.headerView.centerXAnchor],
        [self.userIdLabel.bottomAnchor constraintEqualToAnchor:self.headerView.bottomAnchor constant:-32]
    ]];
}

- (void)setupTableView {
    self.settingsTableView = [[UITableView alloc] init];
    self.settingsTableView.delegate = self;
    self.settingsTableView.dataSource = self;
    self.settingsTableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.settingsTableView.showsVerticalScrollIndicator = NO;
    self.settingsTableView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 注册单元格
    [self.settingsTableView registerClass:[UITableViewCell class] forCellReuseIdentifier:@"SettingCell"];
    
    [self.view addSubview:self.settingsTableView];
}

- (void)setupLayout {
    [self.view addSubview:self.headerView];
    
    [NSLayoutConstraint activateConstraints:@[
        [self.headerView.topAnchor constraintEqualToAnchor:self.view.topAnchor],
        [self.headerView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor],
        [self.headerView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor],
        [self.headerView.heightAnchor constraintEqualToConstant:250],
        
        [self.settingsTableView.topAnchor constraintEqualToAnchor:self.headerView.bottomAnchor constant:-20],
        [self.settingsTableView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:16],
        [self.settingsTableView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-16],
        [self.settingsTableView.bottomAnchor constraintEqualToAnchor:self.view.bottomAnchor]
    ]];
}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return self.settingsSections.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSDictionary *sectionDict = self.settingsSections[section];
    return [sectionDict[@"items"] count];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    NSDictionary *sectionDict = self.settingsSections[section];
    return sectionDict[@"title"];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"SettingCell" forIndexPath:indexPath];
    
    // 清除单元格默认内容
    cell.textLabel.text = nil;
    cell.imageView.image = nil;
    cell.accessoryType = UITableViewCellAccessoryNone;
    
    // 配置单元格
    NSDictionary *sectionDict = self.settingsSections[indexPath.section];
    NSArray *items = sectionDict[@"items"];
    NSDictionary *item = items[indexPath.row];
    
    // 图标
    NSString *iconName = item[@"icon"];
    cell.imageView.image = [UIImage systemImageNamed:iconName];
    cell.imageView.tintColor = [UIColor systemBlueColor];
    
    // 标题
    cell.textLabel.text = item[@"title"];
    cell.textLabel.font = [UIFont systemFontOfSize:16];
    
    // 箭头
    if ([item[@"showArrow"] boolValue]) {
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    }
    
    // 分割线
    cell.separatorInset = UIEdgeInsetsMake(0, 56, 0, 0);
    
    return cell;
}

#pragma mark - UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 50;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 32;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    // 处理菜单项点击
    NSDictionary *sectionDict = self.settingsSections[indexPath.section];
    NSArray *items = sectionDict[@"items"];
    NSDictionary *item = items[indexPath.row];
    
    NSString *title = item[@"title"];
    [self handleSettingItemTap:title];
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    UIView *headerView = [[UIView alloc] init];
    headerView.backgroundColor = [UIColor clearColor];
    
    UILabel *titleLabel = [[UILabel alloc] init];
    titleLabel.text = [self tableView:tableView titleForHeaderInSection:section];
    titleLabel.font = [UIFont systemFontOfSize:14 weight:UIFontWeightMedium];
    titleLabel.textColor = [UIColor systemGrayColor];
    titleLabel.translatesAutoresizingMaskIntoConstraints = NO;
    
    [headerView addSubview:titleLabel];
    
    [NSLayoutConstraint activateConstraints:@[
        [titleLabel.leadingAnchor constraintEqualToAnchor:headerView.leadingAnchor constant:12],
        [titleLabel.centerYAnchor constraintEqualToAnchor:headerView.centerYAnchor]
    ]];
    
    return headerView;
}

- (void)handleSettingItemTap:(NSString *)title {
    // 根据不同的菜单项标题执行不同的操作
    if ([title isEqualToString:@"账单详情"] || [title isEqualToString:@"数据统计"]) {
        // 跳转到相应页面或显示提示
        [self showAlertWithTitle:title message:@"功能开发中"];
    } else if ([title isEqualToString:@"关于我们"]) {
        [self showAboutUsAlert];
    } else if ([title isEqualToString:@"隐私协议"] || [title isEqualToString:@"用户协议"]) {
        // 跳转到协议页面或显示提示
        [self showAlertWithTitle:title message:[NSString stringWithFormat:@"%@内容展示", title]];
    }
}

- (void)showAboutUsAlert {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"关于我们" 
                                                                   message:@"小石记账 v1.0.0\n简单、便捷的记账工具\n让您轻松管理个人财务" 
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil];
    [alert addAction:okAction];
    
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)showAlertWithTitle:(NSString *)title message:(NSString *)message {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title 
                                                                   message:message 
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil];
    [alert addAction:okAction];
    
    [self presentViewController:alert animated:YES completion:nil];
}

@end