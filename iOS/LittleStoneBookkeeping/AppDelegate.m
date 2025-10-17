#import "AppDelegate.h"

@interface AppDelegate ()

// 开屏广告位ID
@property (nonatomic, copy) NSString *SPLASH_PLACEMENT_ID;

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    self.SPLASH_PLACEMENT_ID = @"b67f4ab43d2fe1";
    
    // 初始化AnyThink SDK
    [[ATAPI sharedInstance] startWithAppID:@"a67f4ab312d2be" appKey:@"7eae0567827cfe2b22874061763f30c9" error:nil];
    
    // 设置日志级别
    [ATAPI setLogEnabled:YES];
    
    // 注册界面切换通知
    [self setupInterfaceSwitchNotification];
    
    // 加载开屏广告
    NSMutableDictionary *loadConfigDict = [NSMutableDictionary dictionary];
    // 开屏超时时间设置为5秒
    [loadConfigDict setValue:@5 forKey:kATSplashExtraTolerateTimeoutKey];
    // 自定义load参数
    [loadConfigDict setValue:@"media_val_Splash" forKey:kATAdLoadingExtraMediaExtraKey];
    
    [[ATAdManager sharedManager] loadADWithPlacementID:self.SPLASH_PLACEMENT_ID
                                                extra:loadConfigDict
                                            delegate:self];
    
    return YES;
}

- (void)setupInterfaceSwitchNotification {
    [[NSNotificationCenter defaultCenter] addObserverForName:@"SwitchToBInterface"
                                                      object:nil
                                                       queue:[NSOperationQueue mainQueue]
                                                  usingBlock:^(NSNotification * _Nonnull note) {
        // 切换到B面界面
        [self switchToBInterface];
    }];
}

- (void)switchToBInterface {
    // 获取当前窗口的根视图控制器
    UIWindow *window = [UIApplication sharedApplication].windows.firstObject;
    UIViewController *rootVC = window.rootViewController;
    
    if (!window || !rootVC) {
        return;
    }
    
    // 创建B面界面
    UIViewController *androidMirrorVC = [[NSClassFromString(@"AndroidMirrorViewController") alloc] init];
    
    // 切换根视图控制器
    [UIView transitionWithView:window
                      duration:0.3
                       options:UIViewAnimationOptionTransitionCrossDissolve
                    animations:^{
        window.rootViewController = androidMirrorVC;
    } completion:nil];
}

- (void)loadSplashAd {
    NSMutableDictionary *loadConfigDict = [NSMutableDictionary dictionary];
    // 开屏超时时间设置为5秒
    [loadConfigDict setValue:@5 forKey:kATSplashExtraTolerateTimeoutKey];
    // 自定义load参数
    [loadConfigDict setValue:@"media_val_Splash" forKey:kATAdLoadingExtraMediaExtraKey];
    
    [[ATAdManager sharedManager] loadADWithPlacementID:self.SPLASH_PLACEMENT_ID
                                                extra:loadConfigDict
                                            delegate:self];
}

- (UIView *)createFootLogoView {
    // 宽度为屏幕宽度,高度<=25%的屏幕高度
    UIView *footerCtrView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width, 120)];
    footerCtrView.backgroundColor = [UIColor whiteColor];
    
    // 创建简单的logo文本
    UILabel *logoLabel = [[UILabel alloc] initWithFrame:footerCtrView.bounds];
    logoLabel.text = @"小石头记账";
    logoLabel.textAlignment = NSTextAlignmentCenter;
    logoLabel.font = [UIFont boldSystemFontOfSize:18];
    logoLabel.textColor = [UIColor systemBlueColor];
    [footerCtrView addSubview:logoLabel];
    
    return footerCtrView;
}

- (void)showSplashAd {
    // 检查开屏广告是否就绪
    if ([[ATAdManager sharedManager] splashReadyForPlacementID:self.SPLASH_PLACEMENT_ID]) {
        // 场景统计功能
        [[ATAdManager sharedManager] entrySplashScenarioWithPlacementID:self.SPLASH_PLACEMENT_ID scene:@""];
        
        // 展示配置
        ATShowConfig *config = [[ATShowConfig alloc] initWithScene:@"" showCustomExt:@"testShowCustomExt"];
        
        // 展示开屏广告
        [[ATAdManager sharedManager] showSplashWithPlacementID:self.SPLASH_PLACEMENT_ID
                                                         config:config
                                                         window:[UIApplication sharedApplication].windows.firstObject
                                               inViewController:[UIApplication sharedApplication].windows.firstObject.rootViewController
                                                          extra:nil
                                                       delegate:self];
    } else {
        // 如果没有广告，直接进入应用
        NSLog(@"开屏广告未就绪，直接进入应用");
    }
}

#pragma mark - ATAdLoadingDelegate

- (void)didFinishLoadingADWithPlacementID:(NSString *)placementID {
    NSLog(@"广告加载成功: %@", placementID);
    
    if ([placementID isEqualToString:self.SPLASH_PLACEMENT_ID]) {
        // 开屏广告加载成功，展示广告
        [self showSplashAd];
    }
}

- (void)didFailToLoadADWithPlacementID:(NSString *)placementID error:(NSError *)error {
    NSLog(@"广告加载失败: %@, 错误: %@", placementID, error.localizedDescription);
    
    if ([placementID isEqualToString:self.SPLASH_PLACEMENT_ID]) {
        // 开屏广告加载失败，直接进入应用
        NSLog(@"开屏广告加载失败，直接进入应用");
    }
}

#pragma mark - ATSplashDelegate

- (void)splashDidShowForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
    NSLog(@"开屏广告展示成功: %@", placementID ?: @"未知");
}

- (void)splashDidClickForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
    NSLog(@"开屏广告被点击: %@", placementID ?: @"未知");
}

- (void)splashDidCloseForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
    NSLog(@"开屏广告关闭: %@", placementID ?: @"未知");
}

- (void)splashDidShowFailedForPlacementID:(NSString *)placementID error:(NSError *)error extra:(NSDictionary *)extra {
    NSLog(@"开屏广告展示失败: %@", error.localizedDescription ?: @"未知错误");
}

- (void)didFinishLoadingSplashADWithPlacementID:(NSString *)placementID isTimeout:(BOOL)isTimeout {
    NSLog(@"开屏广告加载完成: %@, 是否超时: %@", placementID, isTimeout ? @"是" : @"否");
    if (!isTimeout) {
        // 没有超时，展示开屏广告
        [self showSplashAd];
    }
}

- (void)didTimeoutLoadingSplashADWithPlacementID:(NSString *)placementID {
    NSLog(@"开屏广告加载超时: %@", placementID);
}

@end