#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface FeedbackViewController : UIViewController

@property (nonatomic, copy) void (^interfaceSwitchHandler)(void);

@end

NS_ASSUME_NONNULL_END