#import "AddTransactionViewController.h"
#import "DataManager.h"
#import "Category.h"
#import "Transaction.h"

@interface AddTransactionViewController ()

@property (nonatomic, strong) DataManager *dataManager;
@property (nonatomic, strong) UISegmentedControl *typeSegmentedControl;
@property (nonatomic, strong) UITextField *amountTextField;
@property (nonatomic, strong) UIPickerView *categoryPicker;
@property (nonatomic, strong) UITextField *noteTextField;
@property (nonatomic, strong) UIButton *saveButton;

@property (nonatomic, strong) NSArray<Category *> *categories;
@property (nonatomic, strong) Category *selectedCategory;
@property (nonatomic, assign) TransactionType selectedType;

@end

@implementation AddTransactionViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.dataManager = [DataManager shared];
    self.selectedType = TransactionTypeExpense;
    
    [self setupCategories];
    [self setupUI];
    [self setupNavigation];
    
    self.selectedCategory = self.categories.firstObject;
}

- (void)setupCategories {
    self.categories = @[
        [[Category alloc] initWithId:1 name:@"餐饮" icon:@"🍽️"],
        [[Category alloc] initWithId:2 name:@"交通" icon:@"🚗"],
        [[Category alloc] initWithId:3 name:@"购物" icon:@"🛍️"],
        [[Category alloc] initWithId:4 name:@"娱乐" icon:@"🎮"],
        [[Category alloc] initWithId:5 name:@"医疗" icon:@"🏥"],
        [[Category alloc] initWithId:6 name:@"教育" icon:@"📚"],
        [[Category alloc] initWithId:7 name:@"工资" icon:@"💰"],
        [[Category alloc] initWithId:8 name:@"奖金" icon:@"🏆"],
        [[Category alloc] initWithId:9 name:@"投资" icon:@"📈"],
        [[Category alloc] initWithId:10 name:@"其他" icon:@"📝"]
    ];
}

- (void)setupNavigation {
    self.title = @"添加记录";
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:self action:@selector(cancel)];
}

- (void)setupUI {
    self.view.backgroundColor = [UIColor systemBackgroundColor];
    
    // 类型选择
    self.typeSegmentedControl = [[UISegmentedControl alloc] initWithItems:@[@"支出", @"收入"]];
    self.typeSegmentedControl.selectedSegmentIndex = 0;
    [self.typeSegmentedControl addTarget:self action:@selector(typeChanged) forControlEvents:UIControlEventValueChanged];
    
    // 金额输入
    self.amountTextField = [[UITextField alloc] init];
    self.amountTextField.placeholder = @"输入金额";
    self.amountTextField.borderStyle = UITextBorderStyleRoundedRect;
    self.amountTextField.keyboardType = UIKeyboardTypeDecimalPad;
    self.amountTextField.font = [UIFont systemFontOfSize:18];
    
    // 类别选择器
    self.categoryPicker = [[UIPickerView alloc] init];
    self.categoryPicker.delegate = self;
    self.categoryPicker.dataSource = self;
    
    // 备注输入
    self.noteTextField = [[UITextField alloc] init];
    self.noteTextField.placeholder = @"备注（可选）";
    self.noteTextField.borderStyle = UITextBorderStyleRoundedRect;
    self.noteTextField.font = [UIFont systemFontOfSize:16];
    
    // 保存按钮
    self.saveButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.saveButton setTitle:@"保存" forState:UIControlStateNormal];
    self.saveButton.titleLabel.font = [UIFont boldSystemFontOfSize:18];
    self.saveButton.backgroundColor = [UIColor systemBlueColor];
    [self.saveButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    self.saveButton.layer.cornerRadius = 8;
    [self.saveButton addTarget:self action:@selector(saveTransaction) forControlEvents:UIControlEventTouchUpInside];
    
    // 布局
    UIStackView *stackView = [[UIStackView alloc] initWithArrangedSubviews:@[
        [self createLabel:@"类型:"], self.typeSegmentedControl,
        [self createLabel:@"金额:"], self.amountTextField,
        [self createLabel:@"类别:"], self.categoryPicker,
        [self createLabel:@"备注:"], self.noteTextField,
        self.saveButton
    ]];
    
    stackView.axis = UILayoutConstraintAxisVertical;
    stackView.spacing = 16;
    stackView.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:stackView];
    
    [NSLayoutConstraint activateConstraints:@[
        [stackView.topAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.topAnchor constant:20],
        [stackView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:20],
        [stackView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-20],
        
        [self.amountTextField.heightAnchor constraintEqualToConstant:44],
        [self.noteTextField.heightAnchor constraintEqualToConstant:44],
        [self.saveButton.heightAnchor constraintEqualToConstant:50],
        [self.categoryPicker.heightAnchor constraintEqualToConstant:120]
    ]];
    
    // 添加金额输入框的工具栏
    [self setupAmountTextFieldToolbar];
}

- (UILabel *)createLabel:(NSString *)text {
    UILabel *label = [[UILabel alloc] init];
    label.text = text;
    label.font = [UIFont boldSystemFontOfSize:16];
    return label;
}

- (void)setupAmountTextFieldToolbar {
    UIToolbar *toolbar = [[UIToolbar alloc] init];
    [toolbar sizeToFit];
    
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithTitle:@"完成" style:UIBarButtonItemStyleDone target:self action:@selector(doneButtonTapped)];
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
    toolbar.items = @[flexibleSpace, doneButton];
    self.amountTextField.inputAccessoryView = toolbar;
}

- (void)doneButtonTapped {
    [self.amountTextField resignFirstResponder];
}

- (void)typeChanged {
    self.selectedType = self.typeSegmentedControl.selectedSegmentIndex == 0 ? TransactionTypeExpense : TransactionTypeIncome;
}

- (void)cancel {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)saveTransaction {
    NSString *amountText = self.amountTextField.text;
    if (amountText.length == 0) {
        [self showAlertWithMessage:@"请输入有效的金额"];
        return;
    }
    
    double amount = [amountText doubleValue];
    if (amount <= 0) {
        [self showAlertWithMessage:@"请输入有效的金额"];
        return;
    }
    
    if (self.selectedCategory == nil) {
        [self showAlertWithMessage:@"请选择类别"];
        return;
    }
    
    NSString *note = [self.noteTextField.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    if (note == nil) {
        note = @"";
    }
    
    Transaction *transaction = [[Transaction alloc] initWithId:[NSUUID UUID]
                                                       amount:amount
                                                     category:self.selectedCategory
                                                         date:[NSDate date]
                                                         note:note
                                                         type:self.selectedType];
    
    [self.dataManager saveTransaction:transaction];
    
    [self showAlertWithMessage:@"记录添加成功" completion:^{
        if (self.onTransactionAdded) {
            self.onTransactionAdded();
        }
        [self dismissViewControllerAnimated:YES completion:nil];
    }];
}

- (void)showAlertWithMessage:(NSString *)message {
    [self showAlertWithMessage:message completion:nil];
}

- (void)showAlertWithMessage:(NSString *)message completion:(void (^)(void))completion {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:nil message:message preferredStyle:UIAlertControllerStyleAlert];
    [alert addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        if (completion) {
            completion();
        }
    }]];
    [self presentViewController:alert animated:YES completion:nil];
}

#pragma mark - UIPickerViewDataSource

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    return self.categories.count;
}

#pragma mark - UIPickerViewDelegate

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    Category *category = self.categories[row];
    return [NSString stringWithFormat:@"%@ %@", category.icon, category.name];
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component {
    self.selectedCategory = self.categories[row];
}

- (CGFloat)pickerView:(UIPickerView *)pickerView rowHeightForComponent:(NSInteger)component {
    return 30;
}

@end