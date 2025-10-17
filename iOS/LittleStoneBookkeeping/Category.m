#import "Category.h"

@implementation Category

- (instancetype)initWithId:(NSInteger)id name:(NSString *)name icon:(NSString *)icon {
    self = [super init];
    if (self) {
        _id = id;
        _name = [name copy];
        _icon = [icon copy];
    }
    return self;
}

#pragma mark - NSCoding

- (instancetype)initWithCoder:(NSCoder *)coder {
    self = [super init];
    if (self) {
        _id = [coder decodeIntegerForKey:@"id"];
        _name = [coder decodeObjectOfClass:[NSString class] forKey:@"name"];
        _icon = [coder decodeObjectOfClass:[NSString class] forKey:@"icon"];
    }
    return self;
}

- (void)encodeWithCoder:(NSCoder *)coder {
    [coder encodeInteger:self.id forKey:@"id"];
    [coder encodeObject:self.name forKey:@"name"];
    [coder encodeObject:self.icon forKey:@"icon"];
}

#pragma mark - Equality

- (BOOL)isEqual:(id)object {
    if (self == object) {
        return YES;
    }
    
    if (![object isKindOfClass:[Category class]]) {
        return NO;
    }
    
    Category *other = (Category *)object;
    return self.id == other.id;
}

- (NSUInteger)hash {
    return self.id;
}

@end