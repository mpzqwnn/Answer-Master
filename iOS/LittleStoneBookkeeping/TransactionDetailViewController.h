#import <UIKit/UIKit.h>
#import "Transaction.h"

@interface TransactionDetailViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, strong) Transaction *transaction;

@end