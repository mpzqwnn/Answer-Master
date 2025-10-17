#import "AddTransactionViewController.h"
#import "DataManager.h"
#import "Transaction.h"
#import "Category.h"

@interface AddTransactionViewController () <UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate>

@property (nonatomic, strong) UISegmentedControl *typeSegment;
@property (nonatomic, strong) UILabel *amountLabel;
@property (nonatomic, strong) UIView *categoryContainer;
@property (nonatomic, strong) UIPickerView *categoryPicker;
@property (nonatomic, strong) UITextField *noteField;
@property (nonatomic, strong) UIButton *dateButton;
@property (nonatomic, strong) UIView *keyboardView;

@property (nonatomic, strong) NSMutableArray *categoryButtons;
@property (nonatomic, strong) NSArray<Category *> *expenseCategories;
@property (nonatomic, strong) NSArray<Category *> *incomeCategories;
@property (nonatomic, strong) NSArray<Category *> *currentCategories;

@property (nonatomic, strong) Category *selectedCategory;
@property (nonatomic, strong) NSDate *selectedDate;
@property (nonatomic, assign) TransactionType transactionType;
@property (nonatomic, strong) NSMutableString *amountString;

@property (nonatomic, strong) DataManager *dataManager;

@end

@implementation AddTransactionViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.dataManager = [DataManager shared];
    self.transactionType = TransactionTypeExpense;
    self.selectedDate = [NSDate date];
    self.amountString = [NSMutableString stringWithString:@"0.00"];
    
    [self setupUI];
    [self loadCategories];
}

- (void)setupUI {
    self.view.backgroundColor = [UIColor whiteColor];
    self.title = @"记一笔";
    
    // 设置导航栏
    UIBarButtonItem *cancelButton = [[UIBarButtonItem alloc] initWithTitle:@"取消" style:UIBarButtonItemStylePlain target:self action:@selector(cancelAction)];
    UIBarButtonItem *saveButton = [[UIBarButtonItem alloc] initWithTitle:@"保存" style:UIBarButtonItemStyleDone target:self action:@selector(saveAction)];
    self.navigationItem.leftBarButtonItem = cancelButton;
    self.navigationItem.rightBarButtonItem = saveButton;
    
    // 交易类型选择器
    [self setupTypeSegment];
    
    // 金额显示
    [self setupAmountLabel];
    
    // 类别选择区域
    [self setupCategoryContainer];
    
    // 备注输入
    [self setupNoteField];
    
    // 日期选择按钮
    [self setupDateButton];
    
    // 键盘视图
    [self setupKeyboardView];
    
    // 布局
    [self setupLayout];
}

- (void)setupTypeSegment {
    self.typeSegment = [[UISegmentedControl alloc] initWithItems:@[@"支出", @"收入"]];
    self.typeSegment.selectedSegmentIndex = 0;
    self.typeSegment.tintColor = [UIColor systemBlueColor];
    [self.typeSegment addTarget:self action:@selector(typeChanged:) forControlEvents:UIControlEventValueChanged];
    self.typeSegment.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:self.typeSegment];
}

- (void)setupAmountLabel {
    self.amountLabel = [[UILabel alloc] init];
    self.amountLabel.text = @"¥0.00";
    self.amountLabel.font = [UIFont systemFontOfSize:48 weight:UIFontWeightMedium];
    self.amountLabel.textAlignment = NSTextAlignmentCenter;
    self.amountLabel.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:self.amountLabel];
}

- (void)setupCategoryContainer {
    self.categoryContainer = [[UIView alloc] init];
    self.categoryContainer.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:self.categoryContainer];
}

- (void)setupNoteField {
    self.noteField = [[UITextField alloc] init];
    self.noteField.placeholder = @"输入备注";    
    self.noteField.borderStyle = UITextBorderStyleRoundedRect;
    self.noteField.font = [UIFont systemFontOfSize:16];
    self.noteField.translatesAutoresizingMaskIntoConstraints = NO;
    
    [self.view addSubview:self.noteField];
}

- (void)setupDateButton {
    self.dateButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.dateButton.titleLabel.font = [UIFont systemFontOfSize:16];
    [self.dateButton setImage:[UIImage systemImageNamed:@"calendar"] forState:UIControlStateNormal];
    [self.dateButton setTitleColor:[UIColor systemBlueColor] forState:UIControlStateNormal];
    [self.dateButton addTarget:self action:@selector(showDatePicker) forControlEvents:UIControlEventTouchUpInside];
    self.dateButton.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 更新日期显示
    [self updateDateButtonTitle];
    
    [self.view addSubview:self.dateButton];
}

- (void)updateDateButtonTitle {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"MM-dd"];
    NSString *dateString = [formatter stringFromDate:self.selectedDate];
    [self.dateButton setTitle:dateString forState:UIControlStateNormal];
}

- (void)setupKeyboardView {
    self.keyboardView = [[UIView alloc] init];
    self.keyboardView.backgroundColor = [UIColor colorWithRed:0.96 green:0.96 blue:0.98 alpha:1.0];
    self.keyboardView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 创建数字按钮网格
    NSArray *numbers = @[@"1", @"2", @"3", @"4", @"5", @"6", @"7", @"8", @"9", @".", @"0", @"←"];
    UIStackView *rowStack = nil;
    
    for (int i = 0; i < numbers.count; i++) {
        if (i % 3 == 0) {
            rowStack = [[UIStackView alloc] init];
            rowStack.axis = UILayoutConstraintAxisHorizontal;
            rowStack.distribution = UIStackViewDistributionFillEqually;
            rowStack.spacing = 1;
            rowStack.translatesAutoresizingMaskIntoConstraints = NO;
            [self.keyboardView addSubview:rowStack];
        }
        
        UIButton *button = [UIButton buttonWithType:UIButtonTypeSystem];
        [button setTitle:numbers[i] forState:UIControlStateNormal];
        button.titleLabel.font = [UIFont systemFontOfSize:24];
        button.backgroundColor = [UIColor whiteColor];
        button.layer.cornerRadius = 5;
        
        if ([numbers[i] isEqualToString:@"←"]) {
            [button setImage:[UIImage systemImageNamed:@"delete.left"] forState:UIControlStateNormal];
            [button setTitle:@"" forState:UIControlStateNormal];
        }
        
        [button addTarget:self action:@selector(keyboardButtonTapped:) forControlEvents:UIControlEventTouchUpInside];
        [rowStack addArrangedSubview:button];
        
        [NSLayoutConstraint activateConstraints:@[
            [button.heightAnchor constraintEqualToConstant:50]
        ]];
    }
    
    // 布局键盘行
    NSArray *keyboardRows = self.keyboardView.subviews;
    for (int i = 0; i < keyboardRows.count; i++) {
        UIStackView *row = keyboardRows[i];
        [NSLayoutConstraint activateConstraints:@[
            [row.leadingAnchor constraintEqualToAnchor:self.keyboardView.leadingAnchor],
            [row.trailingAnchor constraintEqualToAnchor:self.keyboardView.trailingAnchor],
        ]];
        
        if (i == 0) {
            [row.topAnchor constraintEqualToAnchor:self.keyboardView.topAnchor constant:5].active = YES;
        } else {
            [row.topAnchor constraintEqualToAnchor:keyboardRows[i-1].bottomAnchor constant:1].active = YES;
        }
        
        if (i == keyboardRows.count - 1) {
            [row.bottomAnchor constraintEqualToAnchor:self.keyboardView.bottomAnchor constant:-5].active = YES;
        }
    }
    
    [self.view addSubview:self.keyboardView];
}

- (void)setupLayout {
    [NSLayoutConstraint activateConstraints:@[
        // 交易类型选择器
        [self.typeSegment.topAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.topAnchor constant:16],
        [self.typeSegment.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:16],
        [self.typeSegment.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-16],
        
        // 金额显示
        [self.amountLabel.topAnchor constraintEqualToAnchor:self.typeSegment.bottomAnchor constant:32],
        [self.amountLabel.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:16],
        [self.amountLabel.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-16],
        
        // 类别选择区域
        [self.categoryContainer.topAnchor constraintEqualToAnchor:self.amountLabel.bottomAnchor constant:32],
        [self.categoryContainer.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor],
        [self.categoryContainer.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor],
        [self.categoryContainer.heightAnchor constraintEqualToConstant:220], // 4行类别
        
        // 备注输入
        [self.noteField.topAnchor constraintEqualToAnchor:self.categoryContainer.bottomAnchor constant:16],
        [self.noteField.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:16],
        [self.noteField.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-16],
        [self.noteField.heightAnchor constraintEqualToConstant:44],
        
        // 日期选择按钮
        [self.dateButton.topAnchor constraintEqualToAnchor:self.noteField.bottomAnchor constant:16],
        [self.dateButton.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:16],
        
        // 键盘视图
        [self.keyboardView.topAnchor constraintEqualToAnchor:self.dateButton.bottomAnchor constant:24],
        [self.keyboardView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor],
        [self.keyboardView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor],
        [self.keyboardView.bottomAnchor constraintEqualToAnchor:self.view.bottomAnchor]
    ]];
}

- (void)loadCategories {
    // 加载支出类别
    self.expenseCategories = [self.dataManager getExpenseCategories];
    
    // 加载收入类别
    self.incomeCategories = [self.dataManager getIncomeCategories];
    
    // 设置当前类别为支出类别
    self.currentCategories = self.expenseCategories;
    
    // 初始选择第一个类别
    if (self.currentCategories.count > 0) {
        self.selectedCategory = self.currentCategories[0];
    }
    
    // 更新类别按钮
    [self updateCategoryButtons];
}

- (void)updateCategoryButtons {
    // 清除现有的类别按钮
    for (UIView *view in self.categoryContainer.subviews) {
        [view removeFromSuperview];
    }
    
    self.categoryButtons = [NSMutableArray new];
    
    // 创建类别按钮网格
    UIStackView *columnStack = nil;
    int itemsPerRow = 4;
    
    for (int i = 0; i < self.currentCategories.count; i++) {
        Category *category = self.currentCategories[i];
        
        if (i % itemsPerRow == 0) {
            columnStack = [[UIStackView alloc] init];
            columnStack.axis = UILayoutConstraintAxisHorizontal;
            columnStack.distribution = UIStackViewDistributionFillEqually;
            columnStack.spacing = 8;
            columnStack.translatesAutoresizingMaskIntoConstraints = NO;
            [self.categoryContainer addSubview:columnStack];
        }
        
        // 类别按钮容器
        UIView *categoryItemView = [[UIView alloc] init];
        categoryItemView.translatesAutoresizingMaskIntoConstraints = NO;
        
        // 图标按钮
        UIButton *iconButton = [UIButton buttonWithType:UIButtonTypeSystem];
        iconButton.tag = i;
        [iconButton setTitle:@"" forState:UIControlStateNormal];
        iconButton.backgroundColor = [UIColor colorWithRed:0.92 green:0.92 blue:0.96 alpha:1.0];
        iconButton.layer.cornerRadius = 25;
        iconButton.tintColor = [UIColor systemBlueColor];
        
        // 设置系统图标
        UIImage *categoryImage = [self getCategoryImageForName:category.name];
        [iconButton setImage:categoryImage forState:UIControlStateNormal];
        
        // 选中状态
        if ([category isEqual:self.selectedCategory]) {
            iconButton.backgroundColor = [UIColor systemBlueColor];
            iconButton.tintColor = [UIColor whiteColor];
        }
        
        [iconButton addTarget:self action:@selector(categoryButtonTapped:) forControlEvents:UIControlEventTouchUpInside];
        iconButton.translatesAutoresizingMaskIntoConstraints = NO;
        
        // 类别名称
        UILabel *nameLabel = [[UILabel alloc] init];
        nameLabel.text = category.name;
        nameLabel.font = [UIFont systemFontOfSize:12];
        nameLabel.textAlignment = NSTextAlignmentCenter;
        nameLabel.translatesAutoresizingMaskIntoConstraints = NO;
        
        // 布局类别项
        [categoryItemView addSubview:iconButton];
        [categoryItemView addSubview:nameLabel];
        
        [NSLayoutConstraint activateConstraints:@[
            [iconButton.topAnchor constraintEqualToAnchor:categoryItemView.topAnchor],
            [iconButton.centerXAnchor constraintEqualToAnchor:categoryItemView.centerXAnchor],
            [iconButton.widthAnchor constraintEqualToConstant:50],
            [iconButton.heightAnchor constraintEqualToConstant:50],
            
            [nameLabel.topAnchor constraintEqualToAnchor:iconButton.bottomAnchor constant:4],
            [nameLabel.centerXAnchor constraintEqualToAnchor:categoryItemView.centerXAnchor],
            [nameLabel.bottomAnchor constraintEqualToAnchor:categoryItemView.bottomAnchor]
        ]];
        
        [columnStack addArrangedSubview:categoryItemView];
        [self.categoryButtons addObject:iconButton];
    }
    
    // 布局类别行
    NSArray *categoryRows = self.categoryContainer.subviews;
    for (int i = 0; i < categoryRows.count; i++) {
        UIStackView *row = categoryRows[i];
        [NSLayoutConstraint activateConstraints:@[
            [row.leadingAnchor constraintEqualToAnchor:self.categoryContainer.leadingAnchor constant:8],
            [row.trailingAnchor constraintEqualToAnchor:self.categoryContainer.trailingAnchor constant:-8],
        ]];
        
        if (i == 0) {
            [row.topAnchor constraintEqualToAnchor:self.categoryContainer.topAnchor constant:8].active = YES;
        } else {
            [row.topAnchor constraintEqualToAnchor:categoryRows[i-1].bottomAnchor constant:16].active = YES;
        }
        
        if (i == categoryRows.count - 1) {
            [row.bottomAnchor constraintEqualToAnchor:self.categoryContainer.bottomAnchor constant:-8].active = YES;
        }
    }
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
    } else if ([name isEqualToString:@"转账"]) {
        return [UIImage systemImageNamed:@"arrow.right.arrow.left"];
    } else if ([name isEqualToString:@"其他"]) {
        return [UIImage systemImageNamed:@"ellipsis"];
    } else if ([name isEqualToString:@"房租"]) {
        return [UIImage systemImageNamed:@"house"];
    } else if ([name isEqualToString:@"水电费"]) {
        return [UIImage systemImageNamed:@"drop"];
    } else if ([name isEqualToString:@"通讯费"]) {
        return [UIImage systemImageNamed:@"iphone"];
    } else {
        return [UIImage systemImageNamed:@"ellipsis"];
    }
}

- (void)typeChanged:(UISegmentedControl *)sender {
    self.transactionType = (TransactionType)sender.selectedSegmentIndex;
    
    // 切换类别列表
    if (self.transactionType == TransactionTypeExpense) {
        self.currentCategories = self.expenseCategories;
    } else {
        self.currentCategories = self.incomeCategories;
    }
    
    // 重新选择第一个类别
    if (self.currentCategories.count > 0) {
        self.selectedCategory = self.currentCategories[0];
    }
    
    // 更新类别按钮
    [self updateCategoryButtons];
}

- (void)categoryButtonTapped:(UIButton *)sender {
    // 更新选中的类别
    self.selectedCategory = self.currentCategories[sender.tag];
    
    // 更新按钮选中状态
    for (int i = 0; i < self.categoryButtons.count; i++) {
        UIButton *button = self.categoryButtons[i];
        if (button.tag == sender.tag) {
            button.backgroundColor = [UIColor systemBlueColor];
            button.tintColor = [UIColor whiteColor];
        } else {
            button.backgroundColor = [UIColor colorWithRed:0.92 green:0.92 blue:0.96 alpha:1.0];
            button.tintColor = [UIColor systemBlueColor];
        }
    }
}

- (void)keyboardButtonTapped:(UIButton *)sender {
    NSString *title = [sender titleForState:UIControlStateNormal];
    
    if ([title isEqualToString:@"←"]) {
        // 删除键
        if (self.amountString.length > 1) {
            [self.amountString deleteCharactersInRange:NSMakeRange(self.amountString.length - 1, 1)];
        } else {
            self.amountString = [NSMutableString stringWithString:@"0"];
        }
    } else if ([title isEqualToString:@"."]) {
        // 小数点，确保只有一个小数点
        if ([self.amountString rangeOfString:@"."].location == NSNotFound) {
            [self.amountString appendString:"."];
        }
    } else {