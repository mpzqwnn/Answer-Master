#import <UIKit/UIKit.h>
#import <AnyThinkiOS/AnyThinkSDK.h>
#import <AnyThinkiOS/AnyThinkRewardedVideo.h>
#import <AnyThinkiOS/AnyThinkInterstitial.h>
#import <AnyThinkiOS/AnyThinkBanner.h>
#import <AnyThinkiOS/AnyThinkNative.h>
#import <AnyThinkiOS/AnyThinkSplash.h>
#import <AppTrackingTransparency/AppTrackingTransparency.h>

NS_ASSUME_NONNULL_BEGIN

@interface AndroidMirrorViewController : UIViewController <ATAdLoadingDelegate, ATRewardedVideoDelegate, ATInterstitialDelegate, ATBannerDelegate>

@end

NS_ASSUME_NONNULL_END