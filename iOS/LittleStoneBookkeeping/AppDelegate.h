#import <UIKit/UIKit.h>
#import <AnyThinkSDK/AnyThinkSDK.h>
#import <AnyThinkSplash/AnyThinkSplash.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate, ATAdLoadingDelegate, ATSplashDelegate>

@property (strong, nonatomic) UIWindow *window;

@end