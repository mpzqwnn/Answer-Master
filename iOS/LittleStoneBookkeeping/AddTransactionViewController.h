#import <UIKit/UIKit.h>
#import "Transaction.h"

NS_ASSUME_NONNULL_BEGIN

@class DataManager;
@class Category;

@interface AddTransactionViewController : UIViewController <UIPickerViewDelegate, UIPickerViewDataSource>

@property (nonatomic, copy, nullable) void (^onTransactionAdded)(void);

@end

NS_ASSUME_NONNULL_END