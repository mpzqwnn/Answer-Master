#import "AIAssistantViewController.h"

@interface AIAssistantViewController () <UIImagePickerControllerDelegate, UINavigationControllerDelegate>

@property (nonatomic, strong) UIView *contentView;
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UILabel *subtitleLabel;
@property (nonatomic, strong) UIButton *chooseImageButton;
@property (nonatomic, strong) UIButton *takePhotoButton;
@property (nonatomic, strong) UIImageView *previewImageView;
@property (nonatomic, strong) UIActivityIndicatorView *loadingIndicator;

@property (nonatomic, strong) AVCaptureSession *captureSession;
@property (nonatomic, strong) AVCaptureVideoPreviewLayer *previewLayer;
@property (nonatomic, strong) UIView *cameraContainerView;
@property (nonatomic, strong) UIButton *captureButton;
@property (nonatomic, strong) UIButton *closeCameraButton;

@end

@implementation AIAssistantViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = @"AI智能记账";
    self.view.backgroundColor = [UIColor systemBackgroundColor];
    
    [self setupUI];
    [self setupCameraSession];
}

- (void)setupUI {
    self.contentView = [[UIView alloc] init];
    self.contentView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 标题
    self.titleLabel = [[UILabel alloc] init];
    self.titleLabel.text = @"智能识别模式";
    self.titleLabel.font = [UIFont systemFontOfSize:24 weight:UIFontWeightMedium];
    self.titleLabel.textAlignment = NSTextAlignmentCenter;
    self.titleLabel.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 副标题
    self.subtitleLabel = [[UILabel alloc] init];
    self.subtitleLabel.text = @"选择图片或拍照，AI一键识别账单信息！";
    self.subtitleLabel.font = [UIFont systemFontOfSize:16];
    self.subtitleLabel.textColor = [UIColor systemGrayColor];
    self.subtitleLabel.textAlignment = NSTextAlignmentCenter;
    self.subtitleLabel.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 选择图片按钮
    self.chooseImageButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.chooseImageButton.backgroundColor = [UIColor systemBlueColor];
    [self.chooseImageButton setTitle:@"选择图片" forState:UIControlStateNormal];
    [self.chooseImageButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    self.chooseImageButton.titleLabel.font = [UIFont systemFontOfSize:18 weight:UIFontWeightMedium];
    self.chooseImageButton.layer.cornerRadius = 12;
    [self.chooseImageButton addTarget:self action:@selector(chooseImage) forControlEvents:UIControlEventTouchUpInside];
    self.chooseImageButton.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 设置选择图片按钮图标和文字布局
    [self.chooseImageButton setImage:[UIImage systemImageNamed:@"photo"] forState:UIControlStateNormal];
    [self.chooseImageButton setImageEdgeInsets:UIEdgeInsetsMake(0, -10, 0, 0)];
    [self.chooseImageButton setTitleEdgeInsets:UIEdgeInsetsMake(0, 10, 0, 0)];
    self.chooseImageButton.tintColor = [UIColor whiteColor];
    
    // 拍照按钮
    self.takePhotoButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.takePhotoButton.backgroundColor = [UIColor systemBlueColor];
    [self.takePhotoButton setTitle:@"拍照" forState:UIControlStateNormal];
    [self.takePhotoButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    self.takePhotoButton.titleLabel.font = [UIFont systemFontOfSize:18 weight:UIFontWeightMedium];
    self.takePhotoButton.layer.cornerRadius = 12;
    [self.takePhotoButton addTarget:self action:@selector(takePhoto) forControlEvents:UIControlEventTouchUpInside];
    self.takePhotoButton.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 设置拍照按钮图标和文字布局
    [self.takePhotoButton setImage:[UIImage systemImageNamed:@"camera"] forState:UIControlStateNormal];
    [self.takePhotoButton setImageEdgeInsets:UIEdgeInsetsMake(0, -10, 0, 0)];
    [self.takePhotoButton setTitleEdgeInsets:UIEdgeInsetsMake(0, 10, 0, 0)];
    self.takePhotoButton.tintColor = [UIColor whiteColor];
    
    // 预览图片
    self.previewImageView = [[UIImageView alloc] init];
    self.previewImageView.contentMode = UIViewContentModeScaleAspectFit;
    self.previewImageView.backgroundColor = [UIColor systemGray5Color];
    self.previewImageView.layer.cornerRadius = 8;
    self.previewImageView.clipsToBounds = YES;
    self.previewImageView.hidden = YES;
    self.previewImageView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 加载指示器
    self.loadingIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleLarge];
    self.loadingIndicator.hidesWhenStopped = YES;
    self.loadingIndicator.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 摄像头容器
    self.cameraContainerView = [[UIView alloc] init];
    self.cameraContainerView.backgroundColor = [UIColor blackColor];
    self.cameraContainerView.hidden = YES;
    self.cameraContainerView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 拍照按钮
    self.captureButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.captureButton.backgroundColor = [UIColor whiteColor];
    self.captureButton.layer.cornerRadius = 30;
    self.captureButton.layer.borderWidth = 4;
    self.captureButton.layer.borderColor = [UIColor systemGrayColor].CGColor;
    [self.captureButton addTarget:self action:@selector(capturePhoto) forControlEvents:UIControlEventTouchUpInside];
    self.captureButton.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 关闭摄像头按钮
    self.closeCameraButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [self.closeCameraButton setImage:[UIImage systemImageNamed:@"xmark"] forState:UIControlStateNormal];
    self.closeCameraButton.tintColor = [UIColor whiteColor];
    [self.closeCameraButton addTarget:self action:@selector(closeCamera) forControlEvents:UIControlEventTouchUpInside];
    self.closeCameraButton.translatesAutoresizingMaskIntoConstraints = NO;
    
    // 添加子视图
    [self.view addSubview:self.contentView];
    [self.contentView addSubview:self.titleLabel];
    [self.contentView addSubview:self.subtitleLabel];
    [self.contentView addSubview:self.chooseImageButton];
    [self.contentView addSubview:self.takePhotoButton];
    [self.contentView addSubview:self.previewImageView];
    [self.contentView addSubview:self.loadingIndicator];
    [self.view addSubview:self.cameraContainerView];
    [self.cameraContainerView addSubview:self.captureButton];
    [self.cameraContainerView addSubview:self.closeCameraButton];
    
    // 布局
    [self setupLayout];
}

- (void)setupLayout {
    [NSLayoutConstraint activateConstraints:@[
        // 主内容视图
        [self.contentView.topAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.topAnchor],
        [self.contentView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor],
        [self.contentView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor],
        [self.contentView.bottomAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.bottomAnchor],
        
        // 标题
        [self.titleLabel.topAnchor constraintEqualToAnchor:self.contentView.topAnchor constant:40],
        [self.titleLabel.leadingAnchor constraintEqualToAnchor:self.contentView.leadingAnchor constant:20],
        [self.titleLabel.trailingAnchor constraintEqualToAnchor:self.contentView.trailingAnchor constant:-20],
        
        // 副标题
        [self.subtitleLabel.topAnchor constraintEqualToAnchor:self.titleLabel.bottomAnchor constant:16],
        [self.subtitleLabel.leadingAnchor constraintEqualToAnchor:self.contentView.leadingAnchor constant:20],
        [self.subtitleLabel.trailingAnchor constraintEqualToAnchor:self.contentView.trailingAnchor constant:-20],
        
        // 预览图片
        [self.previewImageView.topAnchor constraintEqualToAnchor:self.subtitleLabel.bottomAnchor constant:32],
        [self.previewImageView.leadingAnchor constraintEqualToAnchor:self.contentView.leadingAnchor constant:20],
        [self.previewImageView.trailingAnchor constraintEqualToAnchor:self.contentView.trailingAnchor constant:-20],
        [self.previewImageView.heightAnchor constraintEqualToConstant:250],
        
        // 选择图片按钮
        [self.chooseImageButton.topAnchor constraintEqualToAnchor:self.previewImageView.bottomAnchor constant:32],
        [self.chooseImageButton.leadingAnchor constraintEqualToAnchor:self.contentView.leadingAnchor constant:20],
        [self.chooseImageButton.trailingAnchor constraintEqualToAnchor:self.contentView.trailingAnchor constant:-20],
        [self.chooseImageButton.heightAnchor constraintEqualToConstant:56],
        
        // 拍照按钮
        [self.takePhotoButton.topAnchor constraintEqualToAnchor:self.chooseImageButton.bottomAnchor constant:16],
        [self.takePhotoButton.leadingAnchor constraintEqualToAnchor:self.contentView.leadingAnchor constant:20],
        [self.takePhotoButton.trailingAnchor constraintEqualToAnchor:self.contentView.trailingAnchor constant:-20],
        [self.takePhotoButton.heightAnchor constraintEqualToConstant:56],
        
        // 加载指示器
        [self.loadingIndicator.centerXAnchor constraintEqualToAnchor:self.contentView.centerXAnchor],
        [self.loadingIndicator.centerYAnchor constraintEqualToAnchor:self.contentView.centerYAnchor],
        
        // 摄像头容器
        [self.cameraContainerView.topAnchor constraintEqualToAnchor:self.view.topAnchor],
        [self.cameraContainerView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor],
        [self.cameraContainerView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor],
        [self.cameraContainerView.bottomAnchor constraintEqualToAnchor:self.view.bottomAnchor],
        
        // 拍照按钮
        [self.captureButton.centerXAnchor constraintEqualToAnchor:self.cameraContainerView.centerXAnchor],
        [self.captureButton.bottomAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.bottomAnchor constant:-40],
        [self.captureButton.widthAnchor constraintEqualToConstant:60],
        [self.captureButton.heightAnchor constraintEqualToConstant:60],
        
        // 关闭摄像头按钮
        [self.closeCameraButton.topAnchor constraintEqualToAnchor:self.view.safeAreaLayoutGuide.topAnchor constant:20],
        [self.closeCameraButton.trailingAnchor constraintEqualToAnchor:self.cameraContainerView.trailingAnchor constant:-20],
        [self.closeCameraButton.widthAnchor constraintEqualToConstant:40],
        [self.closeCameraButton.heightAnchor constraintEqualToConstant:40]
    ]];
}

- (void)setupCameraSession {
    self.captureSession = [[AVCaptureSession alloc] init];
    
    // 获取后置摄像头
    AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    
    NSError *error = nil;
    AVCaptureDeviceInput *input = [AVCaptureDeviceInput deviceInputWithDevice:device error:&error];
    
    if (input && [self.captureSession canAddInput:input]) {
        [self.captureSession addInput:input];
    }
    
    // 添加视频预览层
    self.previewLayer = [AVCaptureVideoPreviewLayer layerWithSession:self.captureSession];
    self.previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
}

- (void)startCameraSession {
    if (!self.captureSession.running) {
        // 设置预览层
        self.previewLayer.frame = self.cameraContainerView.bounds;
        [self.cameraContainerView.layer insertSublayer:self.previewLayer atIndex:0];
        
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{            [self.captureSession startRunning];
        });
        
        // 显示摄像头视图
        self.cameraContainerView.hidden = NO;
        self.contentView.hidden = YES;
    }
}

- (void)stopCameraSession {
    if (self.captureSession.running) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{            [self.captureSession stopRunning];
        });
        
        // 移除预览层
        [self.previewLayer removeFromSuperlayer];
        
        // 隐藏摄像头视图
        self.cameraContainerView.hidden = YES;
        self.contentView.hidden = NO;
    }
}

#pragma mark - 按钮事件

- (void)chooseImage {
    // 请求相册权限
    [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
        if (status == PHAuthorizationStatusAuthorized) {
            dispatch_async(dispatch_get_main_queue(), ^{                UIImagePickerController *picker = [[UIImagePickerController alloc] init];
                picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
                picker.delegate = self;
                picker.allowsEditing = YES;
                [self presentViewController:picker animated:YES completion:nil];
            });
        } else {
            [self showAlertWithTitle:@"权限提示" message:@"请在设置中允许访问相册以选择图片"];
        }
    }];
}

- (void)takePhoto {
    // 请求相机权限
    [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
        if (granted) {
            dispatch_async(dispatch_get_main_queue(), ^{                [self startCameraSession];
            });
        } else {
            [self showAlertWithTitle:@"权限提示" message:@"请在设置中允许访问相机以拍照"];
        }
    }];
}

- (void)capturePhoto {
    // 捕获照片
    AVCapturePhotoOutput *output = [[AVCapturePhotoOutput alloc] init];
    if ([self.captureSession canAddOutput:output]) {
        [self.captureSession addOutput:output];
    }
    
    AVCapturePhotoSettings *settings = [[AVCapturePhotoSettings alloc] init];
    [output capturePhotoWithSettings:settings delegate:self];
}

- (void)closeCamera {
    [self stopCameraSession];
}

#pragma mark - UIImagePickerControllerDelegate

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<UIImagePickerControllerInfoKey,id> *)info {
    [picker dismissViewControllerAnimated:YES completion:^{        UIImage *selectedImage = info[UIImagePickerControllerEditedImage] ?: info[UIImagePickerControllerOriginalImage];
        [self processImage:selectedImage];
    }];
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [picker dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - AVCapturePhotoCaptureDelegate

- (void)captureOutput:(AVCapturePhotoOutput *)output didFinishProcessingPhoto:(AVCapturePhoto *)photo error:(NSError *)error {
    if (error) {
        [self showAlertWithTitle:@"拍照失败" message:error.localizedDescription];
        return;
    }
    
    NSData *imageData = [photo fileDataRepresentation];
    UIImage *capturedImage = [UIImage imageWithData:imageData];
    
    [self stopCameraSession];
    [self processImage:capturedImage];
}

#pragma mark - 图像处理

- (void)processImage:(UIImage *)image {
    // 显示预览图片
    self.previewImageView.image = image;
    self.previewImageView.hidden = NO;
    
    // 显示加载指示器
    [self.loadingIndicator startAnimating];
    
    // 模拟AI处理过程
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{        [self.loadingIndicator stopAnimating];
        
        // 模拟识别结果
        [self showRecognitionResult];
    });
}

- (void)showRecognitionResult {
    // 显示识别结果弹窗
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"识别完成" 
                                                                   message:@"已识别到金额: ¥222.00\n类别: 购物\n日期: 2023-10-11" 
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction *saveAction = [UIAlertAction actionWithTitle:@"保存记录" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        // 保存识别结果到记账记录
        [self showAlertWithTitle:@"保存成功" message:@"已保存到交易记录"];
    }];
    
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil];
    
    [alert addAction:saveAction];
    [alert addAction:cancelAction];
    
    [self presentViewController:alert animated:YES completion:nil];
}

#pragma mark - 辅助方法

- (void)showAlertWithTitle:(NSString *)title message:(NSString *)message {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title 
                                                                   message:message 
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil];
    [alert addAction:okAction];
    
    [self presentViewController:alert animated:YES completion:nil];
}

@end