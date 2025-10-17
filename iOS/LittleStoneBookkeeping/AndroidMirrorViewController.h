#import <UIKit/UIKit.h>
#import <AnyThinkSDK/AnyThinkSDK.h>
#import <AnyThinkRewardedVideo/AnyThinkRewardedVideo.h>
#import <AnyThinkInterstitial/AnyThinkInterstitial.h>
#import <AnyThinkBanner/AnyThinkBanner.h>
#import <AnyThinkNative/AnyThinkNative.h>
#import <AnyThinkSplash/AnyThinkSplash.h>
#import <AppTrackingTransparency/AppTrackingTransparency.h>

NS_ASSUME_NONNULL_BEGIN

@interface AndroidMirrorViewController : UIViewController <ATAdLoadingDelegate, ATRewardedVideoDelegate, ATInterstitialDelegate, ATBannerDelegate>

@end

NS_ASSUME_NONNULL_END