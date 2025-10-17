#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface Category : NSObject <NSCoding>

@property (nonatomic, assign) NSInteger id;
@property (nonatomic, copy) NSString *name;
@property (nonatomic, copy) NSString *icon;

- (instancetype)initWithId:(NSInteger)id name:(NSString *)name icon:(NSString *)icon;

@end

NS_ASSUME_NONNULL_END