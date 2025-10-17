#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class DataManager;
@class Category;
@class Transaction;

typedef NS_ENUM(NSInteger, TransactionType) {
    TransactionTypeExpense,
    TransactionTypeIncome
};

@interface AddTransactionViewController : UIViewController <UIPickerViewDelegate, UIPickerViewDataSource>

@property (nonatomic, copy, nullable) void (^onTransactionAdded)(void);

@end

NS_ASSUME_NONNULL_END