#import <UIKit/UIKit.h>
#import "AnyThinkSDK.h"
#import "AnyThinkRewardedVideo.h"
#import "AnyThinkInterstitial.h"
#import "AnyThinkBanner.h"
#import "AnyThinkNative.h"
#import "AnyThinkSplash.h"
#import <AppTrackingTransparency/AppTrackingTransparency.h>

NS_ASSUME_NONNULL_BEGIN

@interface AndroidMirrorViewController : UIViewController <ATAdLoadingDelegate, ATRewardedVideoDelegate, ATInterstitialDelegate, ATBannerDelegate>

@end

NS_ASSUME_NONNULL_END