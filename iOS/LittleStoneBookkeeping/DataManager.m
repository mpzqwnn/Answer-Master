#import "DataManager.h"
#import "Category.h"
#import "Transaction.h"

@interface DataManager ()

@property (nonatomic, strong) NSArray<Transaction *> *cachedTransactions;
@property (nonatomic, strong) NSArray<Category *> *cachedCategories;

@end

@implementation DataManager

+ (instancetype)shared {
    static DataManager *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        [self loadCategories];
    }
    return self;
}

#pragma mark - Transaction Management

- (void)saveTransaction:(Transaction *)transaction {
    NSMutableArray<Transaction *> *transactions = [[self getAllTransactions] mutableCopy];
    [transactions addObject:transaction];
    [self saveTransactions:transactions];
}

- (void)deleteTransaction:(Transaction *)transaction {
    NSMutableArray<Transaction *> *transactions = [[self getAllTransactions] mutableCopy];
    [transactions removeObject:transaction];
    [self saveTransactions:transactions];
}

- (NSArray<Transaction *> *)getAllTransactions {
    if (self.cachedTransactions) {
        return self.cachedTransactions;
    }
    
    NSData *data = [[NSUserDefaults standardUserDefaults] objectForKey:@"transactions"];
    if (!data) {
        self.cachedTransactions = @[];
        return @[];
    }
    
    NSError *error;
    NSArray *transactionsArray = [NSKeyedUnarchiver unarchivedObjectOfClasses:[NSSet setWithArray:@[[NSArray class], [Transaction class], [Category class], [NSUUID class], [NSDate class], [NSString class]]] fromData:data error:&error];
    
    if (error) {
        NSLog(@"Failed to decode transactions: %@", error);
        self.cachedTransactions = @[];
        return @[];
    }
    
    // 按时间倒序排列
    NSArray<Transaction *> *sortedTransactions = [transactionsArray sortedArrayUsingComparator:^NSComparisonResult(Transaction *t1, Transaction *t2) {
        return [t2.date compare:t1.date];
    }];
    
    self.cachedTransactions = sortedTransactions;
    return sortedTransactions;
}

- (NSArray<Transaction *> *)getTransactionsForMonth:(NSDate *)date {
    NSArray<Transaction *> *allTransactions = [self getAllTransactions];
    NSCalendar *calendar = [NSCalendar currentCalendar];
    
    NSMutableArray<Transaction *> *monthlyTransactions = [NSMutableArray array];
    
    for (Transaction *transaction in allTransactions) {
        if ([calendar isDate:transaction.date equalToDate:date toUnitGranularity:NSCalendarUnitMonth]) {
            [monthlyTransactions addObject:transaction];
        }
    }
    
    return [monthlyTransactions copy];
}

- (double)getTotalIncomeForMonth:(NSDate *)date {
    NSArray<Transaction *> *transactions = [self getTransactionsForMonth:date];
    double totalIncome = 0.0;
    
    for (Transaction *transaction in transactions) {
        if (transaction.type == TransactionTypeIncome) {
            totalIncome += transaction.amount;
        }
    }
    
    return totalIncome;
}

- (double)getTotalExpenseForMonth:(NSDate *)date {
    NSArray<Transaction *> *transactions = [self getTransactionsForMonth:date];
    double totalExpense = 0.0;
    
    for (Transaction *transaction in transactions) {
        if (transaction.type == TransactionTypeExpense) {
            totalExpense += transaction.amount;
        }
    }
    
    return totalExpense;
}

- (NSArray<Category *> *)getCategoriesForType:(TransactionType)type {
    NSArray<Category *> *allCategories = [self getDefaultCategories];
    
    if (type == TransactionTypeIncome) {
        return [allCategories filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(Category *category, NSDictionary *bindings) {
            return category.id >= 7 && category.id <= 9; // 收入类别: 工资、奖金、投资
        }]];
    } else {
        return [allCategories filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(Category *category, NSDictionary *bindings) {
            return category.id >= 1 && category.id <= 6; // 支出类别: 餐饮、交通、购物、娱乐、医疗、教育
        }]];
    }
}

#pragma mark - Category Management

- (void)loadCategories {
    self.cachedCategories = [self getDefaultCategories];
}

- (NSArray<Category *> *)getDefaultCategories {
    return @[
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

#pragma mark - Data Export/Import

- (void)exportDataToCSVWithCompletion:(void (^)(NSString * _Nullable, NSError * _Nullable))completion {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSArray<Transaction *> *transactions = [self getAllTransactions];
        
        NSMutableString *csvString = [NSMutableString stringWithString:@"日期,类型,类别,金额,备注\n"];
        
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
        
        for (Transaction *transaction in transactions) {
            NSString *dateString = [dateFormatter stringFromDate:transaction.date];
            NSString *typeString = transaction.type == TransactionTypeIncome ? @"收入" : @"支出";
            NSString *amountString = [NSString stringWithFormat:@"%.2f", transaction.amount];
            
            NSString *row = [NSString stringWithFormat:@"\"%@\",\"%@\",\"%@\",\"%@\",\"%@\"\n",
                            dateString, typeString, transaction.category.name, amountString, transaction.note];
            [csvString appendString:row];
        }
        
        // 保存到临时文件
        NSString *tempDir = NSTemporaryDirectory();
        NSString *fileName = [NSString stringWithFormat:@"stone_bookkeeping_%@.csv", [dateFormatter stringFromDate:[NSDate date]]];
        NSString *filePath = [tempDir stringByAppendingPathComponent:fileName];
        
        NSError *error;
        BOOL success = [csvString writeToFile:filePath atomically:YES encoding:NSUTF8StringEncoding error:&error];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if (success) {
                completion(filePath, nil);
            } else {
                completion(nil, error);
            }
        });
    });
}

- (void)importDataFromCSV:(NSString *)filePath completion:(void (^)(BOOL, NSError * _Nullable))completion {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSError *error;
        NSString *csvString = [NSString stringWithContentsOfFile:filePath encoding:NSUTF8StringEncoding error:&error];
        
        if (error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(NO, error);
            });
            return;
        }
        
        NSArray *lines = [csvString componentsSeparatedByCharactersInSet:[NSCharacterSet newlineCharacterSet]];
        if (lines.count <= 1) {
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(NO, [NSError errorWithDomain:@"DataManager" code:1001 userInfo:@{NSLocalizedDescriptionKey: @"CSV文件格式错误"}]);
            });
            return;
        }
        
        NSMutableArray<Transaction *> *importedTransactions = [NSMutableArray array];
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
        
        for (NSUInteger i = 1; i < lines.count; i++) {
            NSString *line = lines[i];
            if (line.length == 0) continue;
            
            NSArray *components = [self parseCSVLine:line];
            if (components.count < 5) continue;
            
            NSString *dateString = components[0];
            NSString *typeString = components[1];
            NSString *categoryName = components[2];
            NSString *amountString = components[3];
            NSString *note = components[4];
            
            NSDate *date = [dateFormatter dateFromString:dateString];
            double amount = [amountString doubleValue];
            
            if (!date || amount <= 0) continue;
            
            TransactionType type = [typeString isEqualToString:@"收入"] ? TransactionTypeIncome : TransactionTypeExpense;
            
            Category *category = nil;
            for (Category *cat in [self getDefaultCategories]) {
                if ([cat.name isEqualToString:categoryName]) {
                    category = cat;
                    break;
                }
            }
            
            if (!category) {
                category = [[Category alloc] initWithId:10 name:@"其他" icon:@"📝"];
            }
            
            Transaction *transaction = [[Transaction alloc] initWithId:[NSUUID UUID]
                                                              amount:amount
                                                            category:category
                                                                date:date
                                                                note:note
                                                                type:type];
            
            [importedTransactions addObject:transaction];
        }
        
        // 保存导入的交易
        if (importedTransactions.count > 0) {
            NSMutableArray<Transaction *> *existingTransactions = [[self getAllTransactions] mutableCopy];
            [existingTransactions addObjectsFromArray:importedTransactions];
            [self saveTransactions:existingTransactions];
        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            completion(YES, nil);
        });
    });
}

#pragma mark - Private Methods

- (void)saveTransactions:(NSArray<Transaction *> *)transactions {
    NSError *error;
    NSData *data = [NSKeyedArchiver archivedDataWithRootObject:transactions requiringSecureCoding:YES error:&error];
    
    if (error) {
        NSLog(@"Failed to encode transactions: %@", error);
        return;
    }
    
    [[NSUserDefaults standardUserDefaults] setObject:data forKey:@"transactions"];
    [[NSUserDefaults standardUserDefaults] synchronize];
    
    // 清除缓存
    self.cachedTransactions = nil;
}

- (NSArray<NSString *> *)parseCSVLine:(NSString *)line {
    NSMutableArray *components = [NSMutableArray array];
    NSMutableString *currentComponent = [NSMutableString string];
    BOOL inQuotes = NO;
    
    for (NSUInteger i = 0; i < line.length; i++) {
        unichar currentChar = [line characterAtIndex:i];
        
        if (currentChar == '"') {
            inQuotes = !inQuotes;
        } else if (currentChar == ',' && !inQuotes) {
            [components addObject:[currentComponent copy]];
            [currentComponent setString:@""];
        } else {
            [currentComponent appendFormat:@"%C", currentChar];
        }
    }
    
    [components addObject:[currentComponent copy]];
    
    return [components copy];
}

#pragma mark - Computed Properties

- (NSArray<Transaction *> *)transactions {
    return [self getAllTransactions];
}

- (NSArray<Category *> *)categories {
    return [self getDefaultCategories];
}

@end