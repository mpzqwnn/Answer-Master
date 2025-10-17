#import "MainViewController.h"
#import "StoneBookkeepingViewController.h"
#import "AndroidMirrorViewController.h"
#import "FeedbackViewController.h"

typedef NS_ENUM(NSInteger, InterfaceType) {
    InterfaceTypeStoneBookkeeping,
    InterfaceTypeAndroidMirror
};

@interface MainViewController ()

@property (nonatomic, assign) InterfaceType currentInterface;
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UIButton *switchButton;
@property (nonatomic, strong) UIView *containerView;

@end

@implementation MainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.currentInterface = InterfaceTypeStoneBookkeeping;
    
    [self setupUI];
    [self setupStoneBookkeepingInterface];
    
    // 监听界面切换通知
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleInterfaceSwitch)
                                                 name:@"SwitchToBInterface"
                                               object:nil];
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)handleInterfaceSwitch {
    [self performInterfaceSwitch];
}

- (void)setupUI {
    self.view.backgroundColor = [UIColor systemBackgroundColor];
    
    // 标题
    self.titleLabel = [[UILabel alloc] init];
    self.titleLabel.text = @"小石头记账";
    self.titleLabel.font = [UIFont boldSystemFontOfSize:24];
    self.titleLabel.textAlignment = NSTextAlignmentCenter;
    
    // 切换按钮
    self.switchButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.switchButton setTitle:@"切换界面" forState:UIControlStateNormal];
    [self.switchButton addTarget:self action:@selector(switchInterface) forControlEvents:UIControlEventTouchUpInside];
    
    // 容器视图
    self.containerView = [[UIView alloc] init];
    
    // 布局
    UIStackView *stackView = [[UIStackView alloc] initWithArrangedSubviews:@[self.titleLabel, self.switchButton, self.containerView]];
    stackView.axis = UILayoutConstraintAxisVertical;
    stackView.spacing = 20;
    stackView.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:stackView];
    
    [NSLayoutConstraint activateConstraints:@[
        [stackView.topAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.topAnchor constant:20],
        [stackView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:20],
        [stackView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-20],
        [stackView.bottomAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.bottomAnchor constant:-20]
    ]];
    
    // 为容器视图添加高度约束，确保它有足够的空间显示内容
    [NSLayoutConstraint activateConstraints:@[
        [self.containerView.heightAnchor constraintGreaterThanOrEqualToConstant:400]
    ]];
}

- (void)switchInterface {
    // 显示意见反馈界面进行切换
    FeedbackViewController *feedbackVC = [[FeedbackViewController alloc] init];
    __weak typeof(self) weakSelf = self;
    feedbackVC.interfaceSwitchHandler = ^{
        [weakSelf performInterfaceSwitch];
    };
    [self presentViewController:feedbackVC animated:YES completion:nil];
}

- (void)performInterfaceSwitch {
    // 清除当前界面
    for (UIView *subview in self.containerView.subviews) {
        [subview removeFromSuperview];
    }
    
    switch (self.currentInterface) {
        case InterfaceTypeStoneBookkeeping:
            self.currentInterface = InterfaceTypeAndroidMirror;
            self.titleLabel.text = @"答题大师";
            [self setupAndroidMirrorInterface];
            break;
        case InterfaceTypeAndroidMirror:
            self.currentInterface = InterfaceTypeStoneBookkeeping;
            self.titleLabel.text = @"小石头记账";
            [self setupStoneBookkeepingInterface];
            break;
    }
}

- (void)setupStoneBookkeepingInterface {
    // 创建记账界面
    StoneBookkeepingViewController *bookkeepingVC = [[StoneBookkeepingViewController alloc] init];
    [self addChildViewController:bookkeepingVC];
    [self.containerView addSubview:bookkeepingVC.view];
    bookkeepingVC.view.translatesAutoresizingMaskIntoConstraints = NO;
    [bookkeepingVC didMoveToParentViewController:self];
    
    [NSLayoutConstraint activateConstraints:@[
        [bookkeepingVC.view.topAnchor constraintEqualToAnchor:self.containerView.topAnchor],
        [bookkeepingVC.view.leadingAnchor constraintEqualToAnchor:self.containerView.leadingAnchor],
        [bookkeepingVC.view.trailingAnchor constraintEqualToAnchor:self.containerView.trailingAnchor],
        [bookkeepingVC.view.bottomAnchor constraintEqualToAnchor:self.containerView.bottomAnchor]
    ]];
}

- (void)setupAndroidMirrorInterface {
    // 检查是否是第二次登录
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    BOOL isSecondLogin = [defaults boolForKey:@"has_quiz_loaded"];
    
    if (isSecondLogin) {
        // 第二次登录，直接显示答题界面
        [self showAndroidMirrorInterface];
    } else {
        // 第一次登录，显示加载中界面
        [self showLoadingInterface];
    }
}

- (void)showLoadingInterface {
    // 清除当前界面
    for (UIView *subview in self.containerView.subviews) {
        [subview removeFromSuperview];
    }
    
    // 创建加载中界面
    UIView *loadingView = [[UIView alloc] init];
    loadingView.backgroundColor = [UIColor systemBackgroundColor];
    
    UILabel *loadingLabel = [[UILabel alloc] init];
    loadingLabel.text = @"加载中...";
    loadingLabel.font = [UIFont boldSystemFontOfSize:20];
    loadingLabel.textAlignment = NSTextAlignmentCenter;
    
    UIProgressView *progressView = [[UIProgressView alloc] initWithProgressViewStyle:UIProgressViewStyleDefault];
    progressView.progress = 0.0;
    
    UIStackView *loadingStack = [[UIStackView alloc] initWithArrangedSubviews:@[loadingLabel, progressView]];
    loadingStack.axis = UILayoutConstraintAxisVertical;
    loadingStack.spacing = 20;
    loadingStack.translatesAutoresizingMaskIntoConstraints = NO;
    
    [loadingView addSubview:loadingStack];
    [self.containerView addSubview:loadingView];
    
    [NSLayoutConstraint activateConstraints:@[
        [loadingView.topAnchor constraintEqualToAnchor:self.containerView.topAnchor],
        [loadingView.leadingAnchor constraintEqualToAnchor:self.containerView.leadingAnchor],
        [loadingView.trailingAnchor constraintEqualToAnchor:self.containerView.trailingAnchor],
        [loadingView.bottomAnchor constraintEqualToAnchor:self.containerView.bottomAnchor],
        
        [loadingStack.centerXAnchor constraintEqualToAnchor:loadingView.centerXAnchor],
        [loadingStack.centerYAnchor constraintEqualToAnchor:loadingView.centerYAnchor],
        [loadingStack.leadingAnchor constraintEqualToAnchor:loadingView.leadingAnchor constant:40],
        [loadingStack.trailingAnchor constraintEqualToAnchor:loadingView.trailingAnchor constant:-40]
    ]];
    
    // 模拟15秒加载过程
    __block float progress = 0.0;
    NSTimer *timer = [NSTimer scheduledTimerWithTimeInterval:0.1 repeats:YES block:^(NSTimer * _Nonnull timer) {
        progress += 0.00667; // 15秒完成
        progressView.progress = progress;
        
        if (progress >= 1.0) {
            [timer invalidate];
            // 加载完成，标记为已加载
            NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
            [defaults setBool:YES forKey:@"has_quiz_loaded"];
            // 显示答题界面
            [self showAndroidMirrorInterface];
        }
    }];
}

- (void)showAndroidMirrorInterface {
    // 清除当前界面
    for (UIView *subview in self.containerView.subviews) {
        [subview removeFromSuperview];
    }
    
    // 创建Android镜像界面
    AndroidMirrorViewController *androidVC = [[AndroidMirrorViewController alloc] init];
    [self addChildViewController:androidVC];
    [self.containerView addSubview:androidVC.view];
    androidVC.view.translatesAutoresizingMaskIntoConstraints = NO;
    [androidVC didMoveToParentViewController:self];
    
    [NSLayoutConstraint activateConstraints:@[
        [androidVC.view.topAnchor constraintEqualToAnchor:self.containerView.topAnchor],
        [androidVC.view.leadingAnchor constraintEqualToAnchor:self.containerView.leadingAnchor],
        [androidVC.view.trailingAnchor constraintEqualToAnchor:self.containerView.trailingAnchor],
        [androidVC.view.bottomAnchor constraintEqualToAnchor:self.containerView.bottomAnchor]
    ]];
}

@end