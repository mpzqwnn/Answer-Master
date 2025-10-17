#import <UIKit/UIKit.h>
#import <AnyThinkiOS/AnyThinkSDK.h>
#import <AnyThinkiOS/AnyThinkSplash.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate, ATAdLoadingDelegate, ATSplashDelegate>

@property (strong, nonatomic) UIWindow *window;

@end