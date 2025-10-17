#import "FeedbackViewController.h"

@interface FeedbackViewController () <UITextViewDelegate>

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UITextView *feedbackTextView;
@property (nonatomic, strong) UIButton *submitButton;
@property (nonatomic, strong) UILabel *placeholderLabel;

// 双击检测
@property (nonatomic, assign) NSTimeInterval lastTapTime;
@property (nonatomic, assign) NSTimeInterval doubleTapInterval;
@property (nonatomic, assign) NSInteger tapCount;

@end

@implementation FeedbackViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.lastTapTime = 0;
    self.doubleTapInterval = 0.5;
    self.tapCount = 0;
    
    [self setupUI];
    [self setupGestures];
}

- (void)setupUI {
    self.view.backgroundColor = [UIColor systemBackgroundColor];
    
    // 标题
    self.titleLabel = [[UILabel alloc] init];
    self.titleLabel.text = @"意见反馈";
    self.titleLabel.font = [UIFont boldSystemFontOfSize:20];
    self.titleLabel.textAlignment = NSTextAlignmentCenter;
    
    // 反馈文本框
    self.feedbackTextView = [[UITextView alloc] init];
    self.feedbackTextView.layer.borderWidth = 1;
    self.feedbackTextView.layer.borderColor = [UIColor systemGray4Color].CGColor;
    self.feedbackTextView.layer.cornerRadius = 8;
    self.feedbackTextView.font = [UIFont systemFontOfSize:16];
    self.feedbackTextView.delegate = self;
    self.feedbackTextView.textContainerInset = UIEdgeInsetsMake(12, 8, 12, 8);
    
    // 占位符
    self.placeholderLabel = [[UILabel alloc] init];
    self.placeholderLabel.text = @"请输入您的宝贵意见...";
    self.placeholderLabel.textColor = [UIColor systemGray3Color];
    self.placeholderLabel.font = [UIFont systemFontOfSize:16];
    
    // 提交按钮
    self.submitButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.submitButton setTitle:@"提交" forState:UIControlStateNormal];
    self.submitButton.titleLabel.font = [UIFont boldSystemFontOfSize:18];
    self.submitButton.backgroundColor = [UIColor systemBlueColor];
    [self.submitButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    self.submitButton.layer.cornerRadius = 8;
    
    // 布局
    UIStackView *stackView = [[UIStackView alloc] initWithArrangedSubviews:@[
        self.titleLabel,
        self.feedbackTextView,
        self.submitButton
    ]];
    
    stackView.axis = UILayoutConstraintAxisVertical;
    stackView.spacing = 20;
    stackView.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:stackView];
    [self.view addSubview:self.placeholderLabel];
    
    [NSLayoutConstraint activateConstraints:@[
        [stackView.topAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.topAnchor constant:40],
        [stackView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:20],
        [stackView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-20],
        
        [self.feedbackTextView.heightAnchor constraintEqualToConstant:120],
        [self.submitButton.heightAnchor constraintEqualToConstant:50],
        
        [self.placeholderLabel.topAnchor constraintEqualToAnchor:self.feedbackTextView.topAnchor constant:12],
        [self.placeholderLabel.leadingAnchor constraintEqualToAnchor:self.feedbackTextView.leadingAnchor constant:12]
    ]];
    
    // 添加关闭按钮
    UIBarButtonItem *closeButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemClose target:self action:@selector(close)];
    self.navigationItem.rightBarButtonItem = closeButton;
}

- (void)setupGestures {
    // 为提交按钮添加点击手势
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleSubmitTap:)];
    [self.submitButton addGestureRecognizer:tapGesture];
    self.submitButton.userInteractionEnabled = YES;
}

- (void)handleSubmitTap:(UITapGestureRecognizer *)gesture {
    NSTimeInterval currentTime = [[NSDate date] timeIntervalSince1970];
    
    if (currentTime - self.lastTapTime < self.doubleTapInterval) {
        self.tapCount++;
        if (self.tapCount >= 2) {
            // 检测到双击
            [self handleDoubleTap];
            self.tapCount = 0;
            return;
        }
    } else {
        self.tapCount = 1;
    }
    
    self.lastTapTime = currentTime;
    
    // 延迟重置计数
    __weak typeof(self) weakSelf = self;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(self.doubleTapInterval * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if (weakSelf.tapCount == 1) {
            // 单次点击处理
            [weakSelf handleSingleTap];
        }
        weakSelf.tapCount = 0;
    });
}

- (void)handleSingleTap {
    // 单次点击：正常提交逻辑
    NSString *feedbackText = self.feedbackTextView.text;
    NSString *trimmedText = [feedbackText stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    
    if (trimmedText.length == 0) {
        [self showAlertWithMessage:@"请输入反馈内容"];
        return;
    }
    
    // 模拟提交成功
    [self showAlertWithMessage:@"反馈提交成功！"];
    
    // 清空文本框
    self.feedbackTextView.text = @"";
    self.placeholderLabel.hidden = NO;
    
    // 关闭键盘
    [self.feedbackTextView resignFirstResponder];
}

- (void)handleDoubleTap {
    // 双击：切换界面逻辑（仅在文本框为空时生效）
    NSString *feedbackText = self.feedbackTextView.text;
    NSString *trimmedText = [feedbackText stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    
    if (trimmedText.length > 0) {
        // 文本框有内容，不执行切换
        return;
    }
    
    // 直接执行界面切换
    [self performInterfaceSwitch];
}

- (void)performInterfaceSwitch {
    // 执行界面切换
    __weak typeof(self) weakSelf = self;
    [self dismissViewControllerAnimated:YES completion:^{
        if (weakSelf.interfaceSwitchHandler) {
            weakSelf.interfaceSwitchHandler();
        }
    }];
}

- (void)showAlertWithMessage:(NSString *)message {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:nil message:message preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)close {
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - UITextViewDelegate

- (void)textViewDidChange:(UITextView *)textView {
    self.placeholderLabel.hidden = textView.text.length > 0;
}

- (void)textViewDidBeginEditing:(UITextView *)textView {
    self.placeholderLabel.hidden = YES;
}

- (void)textViewDidEndEditing:(UITextView *)textView {
    self.placeholderLabel.hidden = textView.text.length > 0;
}

@end