#import <Foundation/Foundation.h>
#import "Category.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, TransactionType) {
    TransactionTypeIncome,
    TransactionTypeExpense
};

@interface Transaction : NSObject <NSCoding>

@property (nonatomic, strong) NSUUID *id;
@property (nonatomic, assign) double amount;
@property (nonatomic, strong) Category *category;
@property (nonatomic, strong) NSDate *date;
@property (nonatomic, copy) NSString *note;
@property (nonatomic, assign) TransactionType type;

- (instancetype)initWithId:(NSUUID *)id 
                    amount:(double)amount 
                  category:(Category *)category 
                      date:(NSDate *)date 
                      note:(NSString *)note 
                      type:(TransactionType)type;

@end

NS_ASSUME_NONNULL_END