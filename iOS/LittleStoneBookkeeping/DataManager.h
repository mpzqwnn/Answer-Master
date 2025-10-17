#import <Foundation/Foundation.h>
#import "Transaction.h"
#import "Category.h"

NS_ASSUME_NONNULL_BEGIN

@interface DataManager : NSObject

@property (nonatomic, strong, readonly) NSArray<Transaction *> *transactions;
@property (nonatomic, strong, readonly) NSArray<Category *> *categories;

+ (instancetype)shared;

- (void)saveTransaction:(Transaction *)transaction;
- (void)deleteTransaction:(Transaction *)transaction;
- (NSArray<Transaction *> *)getTransactionsForMonth:(NSDate *)date;
- (double)getTotalIncomeForMonth:(NSDate *)date;
- (double)getTotalExpenseForMonth:(NSDate *)date;
- (NSArray<Category *> *)getCategoriesForType:(TransactionType)type;
- (void)exportDataToCSVWithCompletion:(void (^)(NSString * _Nullable filePath, NSError * _Nullable error))completion;
- (void)importDataFromCSV:(NSString *)filePath completion:(void (^)(BOOL success, NSError * _Nullable error))completion;

@end

NS_ASSUME_NONNULL_END