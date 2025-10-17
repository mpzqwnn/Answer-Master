#import "Transaction.h"

@implementation Transaction

- (instancetype)initWithId:(NSUUID *)id 
                    amount:(double)amount 
                  category:(Category *)category 
                      date:(NSDate *)date 
                      note:(NSString *)note 
                      type:(TransactionType)type {
    self = [super init];
    if (self) {
        _id = id;
        _amount = amount;
        _category = category;
        _date = date;
        _note = [note copy];
        _type = type;
    }
    return self;
}

#pragma mark - NSCoding

- (instancetype)initWithCoder:(NSCoder *)coder {
    self = [super init];
    if (self) {
        _id = [coder decodeObjectOfClass:[NSUUID class] forKey:@"id"];
        _amount = [coder decodeDoubleForKey:@"amount"];
        _category = [coder decodeObjectOfClass:[Category class] forKey:@"category"];
        _date = [coder decodeObjectOfClass:[NSDate class] forKey:@"date"];
        _note = [coder decodeObjectOfClass:[NSString class] forKey:@"note"];
        _type = [coder decodeIntegerForKey:@"type"];
    }
    return self;
}

- (void)encodeWithCoder:(NSCoder *)coder {
    [coder encodeObject:self.id forKey:@"id"];
    [coder encodeDouble:self.amount forKey:@"amount"];
    [coder encodeObject:self.category forKey:@"category"];
    [coder encodeObject:self.date forKey:@"date"];
    [coder encodeObject:self.note forKey:@"note"];
    [coder encodeInteger:self.type forKey:@"type"];
}

@end