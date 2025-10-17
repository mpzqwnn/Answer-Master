#import <UIKit/UIKit.h>
#import "AnyThinkSDK.h"
#import "AnyThinkSplash.h"

@interface AppDelegate : UIResponder <UIApplicationDelegate, ATAdLoadingDelegate, ATSplashDelegate>

@property (strong, nonatomic) UIWindow *window;

@end