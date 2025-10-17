#import "ProfileViewController.h"
#import "FeedbackViewController.h"

@interface ProfileViewController ()

// 用户信息
@property (nonatomic, strong) UIImageView *userAvatarImageView;
@property (nonatomic, strong) UILabel *userNameLabel;
@property (nonatomic, strong) UILabel *phoneLabel;
@property (nonatomic, strong) UILabel *userIdLabel;

// 原生广告占位
@property (nonatomic, strong) UIView *nativeAdView;

// 功能按钮
@property (nonatomic, strong) UIButton *feedbackButton;
@property (nonatomic, strong) UIButton *rateButton;
@property (nonatomic, strong) UIButton *aboutButton;
@property (nonatomic, strong) UIButton *privacyButton;
@property (nonatomic, strong) UIButton *termsButton;

@end

@implementation ProfileViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupUI];
    [self loadUserInfo];
}

- (void)setupUI {
    self.view.backgroundColor = [UIColor systemBackgroundColor];
    self.title = @"我的";
    
    // 初始化UI组件
    [self setupUserInfoSection];
    [self setupFunctionButtons];
    [self setupNativeAdSection];
    
    // 布局
    [self setupLayout];
}

- (void)setupUserInfoSection {
    self.userAvatarImageView = [[UIImageView alloc] init];
    self.userNameLabel = [[UILabel alloc] init];
    self.phoneLabel = [[UILabel alloc] init];
    self.userIdLabel = [[UILabel alloc] init];
    
    // 用户头像
    self.userAvatarImageView.image = [UIImage systemImageNamed:@"person.circle.fill"];
    self.userAvatarImageView.tintColor = [UIColor systemBlueColor];
    self.userAvatarImageView.contentMode = UIViewContentModeScaleAspectFill;
    self.userAvatarImageView.layer.cornerRadius = 30;
    self.userAvatarImageView.clipsToBounds = YES;
    
    // 用户名
    self.userNameLabel.font = [UIFont boldSystemFontOfSize:18];
    self.userNameLabel.text = @"用户昵称";
    
    // 手机号
    self.phoneLabel.font = [UIFont systemFontOfSize:14];
    self.phoneLabel.textColor = [UIColor systemGrayColor];
    
    // 用户ID
    self.userIdLabel.font = [UIFont systemFontOfSize:12];
    self.userIdLabel.textColor = [UIColor systemGray2Color];
}

- (void)setupFunctionButtons {
    self.feedbackButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.rateButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.aboutButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.privacyButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.termsButton = [UIButton buttonWithType:UIButtonTypeSystem];
    
    // 意见反馈
    [self.feedbackButton setTitle:@"意见反馈" forState:UIControlStateNormal];
    [self.feedbackButton setImage:[UIImage systemImageNamed:@"text.bubble"] forState:UIControlStateNormal];
    [self.feedbackButton addTarget:self action:@selector(showFeedback) forControlEvents:UIControlEventTouchUpInside];
    
    // 给个好评
    [self.rateButton setTitle:@"给个好评" forState:UIControlStateNormal];
    [self.rateButton setImage:[UIImage systemImageNamed:@"star.fill"] forState:UIControlStateNormal];
    [self.rateButton addTarget:self action:@selector(rateApp) forControlEvents:UIControlEventTouchUpInside];
    
    // 关于我们
    [self.aboutButton setTitle:@"关于我们" forState:UIControlStateNormal];
    [self.aboutButton setImage:[UIImage systemImageNamed:@"info.circle"] forState:UIControlStateNormal];
    [self.aboutButton addTarget:self action:@selector(showAbout) forControlEvents:UIControlEventTouchUpInside];
    
    // 隐私协议
    [self.privacyButton setTitle:@"隐私协议" forState:UIControlStateNormal];
    [self.privacyButton setImage:[UIImage systemImageNamed:@"lock.shield"] forState:UIControlStateNormal];
    [self.privacyButton addTarget:self action:@selector(showPrivacy) forControlEvents:UIControlEventTouchUpInside];
    
    // 用户协议
    [self.termsButton setTitle:@"用户协议" forState:UIControlStateNormal];
    [self.termsButton setImage:[UIImage systemImageNamed:@"doc.text"] forState:UIControlStateNormal];
    [self.termsButton addTarget:self action:@selector(showTerms) forControlEvents:UIControlEventTouchUpInside];
}

- (void)setupNativeAdSection {
    self.nativeAdView = [[UIView alloc] init];
    self.nativeAdView.backgroundColor = [UIColor systemGray5Color];
    self.nativeAdView.layer.cornerRadius = 8;
    
    UILabel *adLabel = [[UILabel alloc] init];
    adLabel.text = @"广告";
    adLabel.textAlignment = NSTextAlignmentCenter;
    adLabel.textColor = [UIColor systemGrayColor];
    adLabel.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.nativeAdView addSubview:adLabel];
    
    [NSLayoutConstraint activateConstraints:@[
        [adLabel.centerXAnchor constraintEqualToAnchor:self.nativeAdView.centerXAnchor],
        [adLabel.centerYAnchor constraintEqualToAnchor:self.nativeAdView.centerYAnchor]
    ]];
    
    // 设置高度约束
    NSLayoutConstraint *heightConstraint = [self.nativeAdView.heightAnchor constraintEqualToConstant:100];
    heightConstraint.active = YES;
}

- (void)setupLayout {
    UIView *userInfoCard = [self createUserInfoCard];
    UIView *functionButtonsCard = [self createFunctionButtonsCard];
    
    UIStackView *stackView = [[UIStackView alloc] initWithArrangedSubviews:@[
        userInfoCard,
        functionButtonsCard,
        self.nativeAdView
    ]];
    stackView.axis = UILayoutConstraintAxisVertical;
    stackView.spacing = 16;
    stackView.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:stackView];
    
    [NSLayoutConstraint activateConstraints:@[
        [stackView.topAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.topAnchor constant:16],
        [stackView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:16],
        [stackView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-16],
        [stackView.bottomAnchor constraintLessThanOrEqualToAnchor:self.view.safeAreaLayoutGuide.bottomAnchor constant:-16]
    ]];
}

- (UIView *)createUserInfoCard {
    UIView *cardView = [[UIView alloc] init];
    cardView.backgroundColor = [UIColor systemGray6Color];
    cardView.layer.cornerRadius = 12;
    
    UIStackView *infoStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.userAvatarImageView,
        [self createUserInfoStack]
    ]];
    infoStack.axis = UILayoutConstraintAxisHorizontal;
    infoStack.spacing = 12;
    infoStack.alignment = UIStackViewAlignmentCenter;
    infoStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    [cardView addSubview:infoStack];
    
    [NSLayoutConstraint activateConstraints:@[
        [self.userAvatarImageView.widthAnchor constraintEqualToConstant:60],
        [self.userAvatarImageView.heightAnchor constraintEqualToConstant:60],
        
        [infoStack.topAnchor constraintEqualToAnchor:cardView.topAnchor constant:16],
        [infoStack.leadingAnchor constraintEqualToAnchor:cardView.leadingAnchor constant:16],
        [infoStack.trailingAnchor constraintEqualToAnchor:cardView.trailingAnchor constant:-16],
        [infoStack.bottomAnchor constraintEqualToAnchor:cardView.bottomAnchor constant:-16]
    ]];
    
    return cardView;
}

- (UIStackView *)createUserInfoStack {
    UIStackView *infoStack = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.userNameLabel,
        self.phoneLabel,
        self.userIdLabel
    ]];
    infoStack.axis = UILayoutConstraintAxisVertical;
    infoStack.spacing = 4;
    return infoStack;
}

- (UIView *)createFunctionButtonsCard {
    UIView *cardView = [[UIView alloc] init];
    cardView.backgroundColor = [UIColor systemGray6Color];
    cardView.layer.cornerRadius = 12;
    
    NSArray *buttons = @[
        self.feedbackButton,
        self.rateButton,
        self.aboutButton,
        self.privacyButton,
        self.termsButton
    ];
    
    UIStackView *buttonStack = [[UIStackView alloc] initWithArrangedSubviews:buttons];
    buttonStack.axis = UILayoutConstraintAxisVertical;
    buttonStack.spacing = 0;
    buttonStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    [cardView addSubview:buttonStack];
    
    // 配置按钮样式
    for (UIButton *button in buttons) {
        button.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        button.titleEdgeInsets = UIEdgeInsetsMake(0, 12, 0, 0);
        button.imageEdgeInsets = UIEdgeInsetsMake(0, 0, 0, 0);
        NSLayoutConstraint *heightConstraint = [button.heightAnchor constraintEqualToConstant:50];
        heightConstraint.active = YES;
    }
    
    [NSLayoutConstraint activateConstraints:@[
        [buttonStack.topAnchor constraintEqualToAnchor:cardView.topAnchor],
        [buttonStack.leadingAnchor constraintEqualToAnchor:cardView.leadingAnchor],
        [buttonStack.trailingAnchor constraintEqualToAnchor:cardView.trailingAnchor],
        [buttonStack.bottomAnchor constraintEqualToAnchor:cardView.bottomAnchor]
    ]];
    
    return cardView;
}

- (void)loadUserInfo {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSString *phone = [defaults stringForKey:@"user_phone"] ?: @"未登录";
    BOOL isLoggedIn = [defaults boolForKey:@"is_logged_in"];
    
    if (isLoggedIn) {
        self.userNameLabel.text = [@"用户_" stringByAppendingString:[phone substringFromIndex:MAX(0, (int)phone.length - 4)]];
        self.phoneLabel.text = phone;
        self.userIdLabel.text = [@"ID: " stringByAppendingString:[NSString stringWithFormat:@"%lu", labs(phone.hash)]];
    } else {
        self.userNameLabel.text = @"游客用户";
        self.phoneLabel.text = @"未登录";
        self.userIdLabel.text = @"ID: 000000";
    }
}

- (void)loadNativeAd {
    // 原生广告占位视图
    UILabel *adLabel = [[UILabel alloc] init];
    adLabel.text = @"广告区域";
    adLabel.textAlignment = NSTextAlignmentCenter;
    adLabel.textColor = [UIColor systemGrayColor];
    adLabel.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 移除所有子视图
    for (UIView *subview in self.nativeAdView.subviews) {
        [subview removeFromSuperview];
    }
    
    [self.nativeAdView addSubview:adLabel];
    
    [NSLayoutConstraint activateConstraints:@[
        [adLabel.centerXAnchor constraintEqualToAnchor:self.nativeAdView.centerXAnchor],
        [adLabel.centerYAnchor constraintEqualToAnchor:self.nativeAdView.centerYAnchor]
    ]];
}

#pragma mark - 按钮事件

- (void)showFeedback {
    FeedbackViewController *feedbackVC = [[FeedbackViewController alloc] init];
    __weak typeof(self) weakSelf = self;
    feedbackVC.interfaceSwitchHandler = ^{
        // 当从反馈页面返回时，不需要执行界面切换
        // 只需确保页面正常关闭即可
    };
    UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:feedbackVC];
    [self presentViewController:navController animated:YES completion:nil];
}

- (void)rateApp {
    // 请求App Store评分
    if (@available(iOS 14.0, *)) {
        UIWindowScene *scene = (UIWindowScene *)[[UIApplication sharedApplication].connectedScenes anyObject];
        if (scene) {
            [SKStoreReviewController requestReviewInScene:scene];
        }
    } else {
        [SKStoreReviewController requestReview];
    }
    
    [self showAlert:@"感谢您的评价！"];
}

- (void)showAbout {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"关于我们"
                                                                   message:@"小石头记账 - 简单易用的个人财务管理工具\n版本 1.0.0"
                                                            preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)showPrivacy {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"隐私协议"
                                                                   message:@"我们非常重视您的隐私安全，详细协议请查看应用内完整版本。"
                                                            preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)showTerms {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"用户协议"
                                                                   message:@"请仔细阅读用户协议，使用本应用即表示您同意相关条款。"
                                                            preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)showAlert:(NSString *)message {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:nil
                                                                   message:message
                                                            preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}

#pragma mark - ATNativeAdDelegate

- (void)didFinishLoadingNativeADWithPlacementID:(NSString *)placementID {
    NSLog(@"原生广告加载成功: %@", placementID);
    [self loadNativeAd];
}

- (void)didFailToLoadNativeADWithPlacementID:(NSString *)placementID error:(NSError *)error {
    NSLog(@"原生广告加载失败: %@", error.localizedDescription);
}

@end