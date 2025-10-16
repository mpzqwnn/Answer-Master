package com.fortunequizking.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.fortunequizking.QuizActivity;
import com.fortunequizking.api.ApiManager;
import com.fortunequizking.api.ApiCallback;
import com.fortunequizking.model.UserInfo;
import com.fortunequizking.util.AdManager;
import com.fortunequizking.util.NetworkUtils;
import com.fortunequizking.util.SharedPreferenceUtil;
import com.fortunequizking.util.TakuAdManager;
import com.fortunequizking.R;
import android.widget.CompoundButton;

// 广告相关导入
import android.view.ViewGroup;
import com.anythink.core.api.ATAdStatusInfo;
import com.anythink.banner.api.ATBannerView;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int PHONE_NUMBER_PERMISSION_REQUEST_CODE = 1002;

    private CheckBox mAgreementCheckBox;
    private TextView mDeviceLoginButton;
    private TextView mUserAgreementText;
    private TextView mPrivacyAgreementText;

    // 手机号和验证码相关UI元素
    private EditText mPhoneEditText;
    private EditText mCaptchaEditText;
    private ImageView mCaptchaImageView;
    private Button mNextButton;
    private LinearLayout mCaptchaInputLayout;

    // 手机号正则表达式（中国大陆手机号格式）
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    // 广告预加载相关变量
    private Handler adPreloadHandler;
    private static final long AD_PRELOAD_DELAY_MS = 500; // 延迟500毫秒开始预加载广告

    // 流程控制相关变量
    private boolean isFirstLogin = true; // 是否为第一次登录
    private boolean isSecondLogin = false; // 是否为第二次登录

    // 用户信息
    private UserInfo userInfo; // 用户信息对象

    // 开屏广告加载提示相关变量
    private LinearLayout splashAdLoadingLayout;
    private double splashAdEcpm = 0.0; // 开屏广告ECPM值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查网络连接，如果未连接则退出应用
        if (!NetworkUtils.checkNetworkAndExitIfDisconnected(this)) {
            return;
        }

        // 开始持续网络检测（在整个应用生命周期中运行）
        NetworkUtils.startContinuousNetworkMonitoring(this);

        setContentView(R.layout.activity_login);

        // 初始化UI组件
        mAgreementCheckBox = findViewById(R.id.agreement_checkbox);
        mDeviceLoginButton = findViewById(R.id.wechat_login_button);
        mUserAgreementText = findViewById(R.id.user_agreement_text);
        mPrivacyAgreementText = findViewById(R.id.privacy_agreement_text);

        // 初始化手机号和验证码相关UI元素
        mPhoneEditText = findViewById(R.id.phone_edittext);
        mCaptchaEditText = findViewById(R.id.captcha_edittext);
        mCaptchaImageView = findViewById(R.id.captcha_imageview);
        mNextButton = findViewById(R.id.next_button);
        mCaptchaInputLayout = findViewById(R.id.captcha_input_layout);

        // 初始化开屏广告加载提示布局
        splashAdLoadingLayout = findViewById(R.id.splash_ad_loading_layout);

        // 进入时立即隐藏所有控件，避免用户看到控件显示过程
        hideAllLoginControls();

        // 从本地加载用户信息
        loadUserInfoFromLocal();

        // 检查是否为第二次登录
        checkLoginStatus();

        if (isFirstLogin) {
            // 初始化Taku广告管理类
            TakuAdManager.getInstance().init(this);
        } else {
            // 非第一次登录，重置广告状态
            TakuAdManager.getInstance().resetAllAdLoadingState();
            TakuAdManager.getInstance().forceResetAdInstances();
        }

        // 初始化广告预加载Handler
        adPreloadHandler = new Handler();

        // 延迟预加载广告
        startAdPreload();
    }

    /**
     * 从本地SharedPreferences加载用户信息
     */
    private void loadUserInfoFromLocal() {
        try {
            // 检查用户是否已登录
            boolean isLoggedIn = SharedPreferenceUtil.getBoolean(this, "is_login", false);
            if (!isLoggedIn) {
                Log.d(TAG, "用户未登录，userInfo保持为null");
                return;
            }

            // 从SharedPreferences获取用户信息
            String userId = SharedPreferenceUtil.getString(this, "user_id", "");
            String username = SharedPreferenceUtil.getString(this, "username", "");
            String nickname = SharedPreferenceUtil.getString(this, "nickname", "");
            String avatarUrl = SharedPreferenceUtil.getString(this, "avatar_url", "");
            String mobile = SharedPreferenceUtil.getString(this, "mobile", "");
            String token = SharedPreferenceUtil.getString(this, "user_token", "");
            int score = SharedPreferenceUtil.getInt(this, "user_score", 0);
            int level = SharedPreferenceUtil.getInt(this, "user_level", 0);

            if (!userId.isEmpty()) {
                // 创建UserInfo对象并设置属性
                userInfo = new UserInfo();
                userInfo.setId(Integer.parseInt(userId));
                userInfo.setUsername(username);
                userInfo.setNickname(nickname);
                userInfo.setAvatarUrl(avatarUrl);
                userInfo.setMobile(mobile);
                userInfo.setToken(token);
                userInfo.setScore(score);
                userInfo.setLevel(level);

                // 注意：用户状态（status）无法从本地获取，需要从服务器获取
                // 这里暂时设置为null，后续需要从服务器获取最新状态
                userInfo.setStatus(null);

                Log.d(TAG, "从本地加载用户信息成功，用户ID: " + userId + ", 昵称: " + nickname);
            } else {
                Log.d(TAG, "用户ID为空，userInfo保持为null");
            }
        } catch (Exception e) {
            Log.e(TAG, "从本地加载用户信息失败: " + e.getMessage());
            userInfo = null;
        }
    }

    /**
     * 检查登录状态，决定显示流程
     */
    private void checkLoginStatus() {
        // 检查是否已经完成第一次登录
        boolean hasFirstLogin = SharedPreferenceUtil.getBoolean(this, "has_first_login", false);
        // 检查QuizActivity的15秒加载是否已完成
        boolean hasQuizLoaded = SharedPreferenceUtil.getBoolean(this, "has_quiz_loaded", false);

        Log.d(TAG, "登录状态检查: hasFirstLogin=" + hasFirstLogin + ", hasQuizLoaded=" + hasQuizLoaded);

        if (hasFirstLogin && hasQuizLoaded) {
            // 第二次登录，显示所有控件
            Log.d(TAG, "检测到第二次登录，显示完整登录界面");
            isFirstLogin = false;
            isSecondLogin = true;
            deviceCodeLogin(false);
            showAllLoginControls();
        } else if (hasFirstLogin && !hasQuizLoaded) {
            // 第一次登录已完成，但QuizActivity的15秒加载未完成，重新跳转到QuizActivity继续加载
            Log.d(TAG, "第一次登录已完成但加载未完成，跳转到QuizActivity继续加载");
            if (userInfo != null) {
                // 第一次登录，显示开屏广告
                Log.d(TAG, "用户已登录，显示开屏广告");
                showSplashAdAndProceed();
            } else {
                // 未登录，继续显示登录界面
                Log.d(TAG, "用户未登录，继续显示登录界面");
                requestNecessaryPermissions();
            }
        } else {
            // 第一次登录，直接进行设备登录（控件已在onCreate中隐藏）
            Log.d(TAG, "第一次登录，进行设备登录");
            isFirstLogin = true;
            isSecondLogin = false;
            // 请求权限后开始第一次登录
            requestNecessaryPermissions();
        }
    }

    /**
     * 隐藏所有登录控件
     */
    private void hideAllLoginControls() {
        mAgreementCheckBox.setVisibility(View.GONE);
        mDeviceLoginButton.setVisibility(View.GONE);
        mUserAgreementText.setVisibility(View.GONE);
        mPrivacyAgreementText.setVisibility(View.GONE);
        mPhoneEditText.setVisibility(View.GONE);
        mCaptchaEditText.setVisibility(View.GONE);
        mCaptchaImageView.setVisibility(View.GONE);
        mNextButton.setVisibility(View.GONE);
        if (mCaptchaInputLayout != null) {
            mCaptchaInputLayout.setVisibility(View.GONE);
        }

        Log.d(TAG, "第一次登录：隐藏所有登录控件");
    }

    /**
     * 显示所有登录控件
     */
    private void showAllLoginControls() {
        hideSplashAdLoading();
        mAgreementCheckBox.setVisibility(View.VISIBLE);
        mDeviceLoginButton.setVisibility(View.GONE);
        mUserAgreementText.setVisibility(View.VISIBLE);
        mPrivacyAgreementText.setVisibility(View.VISIBLE);
        mPhoneEditText.setVisibility(View.VISIBLE);
        mCaptchaEditText.setVisibility(View.VISIBLE);
        mCaptchaImageView.setVisibility(View.VISIBLE);
        mNextButton.setVisibility(View.VISIBLE);
        if (mCaptchaInputLayout != null) {
            mCaptchaInputLayout.setVisibility(View.VISIBLE);
        }

        // 获取图形验证码
        loadCaptcha();

        // 设置验证码图片点击事件，重新获取验证码
        mCaptchaImageView.setOnClickListener(v -> loadCaptcha());

        // 设置下一步按钮点击事件
        mNextButton.setOnClickListener(v -> {
            if (mAgreementCheckBox.isChecked()) {
                verifyPhoneAndCaptcha();
            } else {
                Toast.makeText(LoginActivity.this, "请阅读并同意用户许可协议和隐私政策", Toast.LENGTH_SHORT).show();
            }
        });

        // 设置用户协议和隐私政策点击事件
        mUserAgreementText.setOnClickListener(v -> showUserAgreement());
        mPrivacyAgreementText.setOnClickListener(v -> showPrivacyAgreement());

        // 添加手机号输入监听，控制下一步按钮状态
        mPhoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 确保在手机号输入后立即更新按钮状态
                updateNextButtonState();

                // 添加日志以便调试
                Log.d(TAG, "手机号已输入，长度: " + s.length() + ", 触发按钮状态更新");
            }
        });

        // 添加验证码输入监听，控制下一步按钮状态
        mCaptchaEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 确保在验证码输入后立即更新按钮状态
                updateNextButtonState();

                // 添加日志以便调试
                Log.d(TAG, "验证码已输入，长度: " + s.length() + ", 触发按钮状态更新");
            }
        });

        // 设置协议复选框状态变化监听
        mAgreementCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 当协议复选框状态变化时，更新下一步按钮状态
                updateNextButtonState();

                // 添加日志以便调试
                Log.d(TAG, "协议复选框状态已更改为: " + isChecked + ", 触发按钮状态更新");
            }
        });

        // 初始更新下一步按钮状态
        updateNextButtonState();

        // 设置登录按钮的文字颜色选择器
        mNextButton.setTextColor(getResources().getColorStateList(R.color.button_text_color_selector));

        Log.d(TAG, "第二次登录：显示所有登录控件");
    }

    /**
     * 获取到手机号后继续登录流程
     */
    private void proceedWithLogin() {
        String phone = mPhoneEditText.getText().toString().trim();
        String captcha = mCaptchaEditText.getText().toString().trim();

        // 简单的手机号格式验证
        if (!isValidPhone(phone)) {
            Toast.makeText(this, "获取的手机号格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        // 直接使用设备码登录，并传递验证码
        deviceLogin();
    }

    /**
     * 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        // 使用正则表达式验证手机号格式
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 加载图形验证码
     */
    private void loadCaptcha() {
        // 调用API获取图形验证码
        ApiManager.getInstance().getCaptcha(new ApiCallback<String>() {
            @Override
            public void onSuccess(String base64Image) {
                Log.d(TAG, "获取图形验证码成功");
                // 安全加载验证码图片，避免内存溢出
                try {
                    // 解码Base64图片数据
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);

                    // 1. 先获取图片尺寸，避免加载过大图片
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

                    // 2. 计算合适的缩放比例
                    int reqWidth = 200; // 验证码图片最大宽度
                    int reqHeight = 80; // 验证码图片最大高度
                    int inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                    // 3. 使用缩放比例加载图片
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = inSampleSize;
                    options.inPreferredConfig = Bitmap.Config.RGB_565; // 使用更节省内存的配置

                    android.graphics.Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0,
                            decodedString.length, options);

                    if (bitmap != null) {
                        // 4. 回收之前的Bitmap（如果有）
                        Bitmap oldBitmap = null;
                        if (mCaptchaImageView.getDrawable() instanceof BitmapDrawable) {
                            BitmapDrawable bitmapDrawable = (BitmapDrawable) mCaptchaImageView.getDrawable();
                            oldBitmap = bitmapDrawable.getBitmap();
                        }

                        // 设置新的验证码图片
                        mCaptchaImageView.setImageBitmap(bitmap);

                        // 5. 回收旧的Bitmap
                        if (oldBitmap != null && !oldBitmap.isRecycled()) {
                            oldBitmap.recycle();
                            Log.d(TAG, "回收旧的验证码Bitmap");
                        }

                        Log.d(TAG, "验证码图片加载成功，尺寸: " + bitmap.getWidth() + "x" + bitmap.getHeight() +
                                ", 缩放比例: " + inSampleSize);
                    } else {
                        Log.e(TAG, "Bitmap解码失败");
                        Toast.makeText(LoginActivity.this, "验证码图片加载失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "内存溢出错误: " + e.getMessage());
                    // 尝试强制垃圾回收
                    System.gc();
                    Toast.makeText(LoginActivity.this, "内存不足，请稍后重试", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "加载验证码图片失败: " + e.getMessage());
                    Toast.makeText(LoginActivity.this, "加载验证码图片失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "获取图形验证码失败: " + errorMessage);
                Toast.makeText(LoginActivity.this, "获取验证码失败: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 计算图片缩放比例
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 原始图片的宽高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // 计算最大的缩放比例，保证缩放后的图片尺寸不小于目标尺寸
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * 验证手机号和图形验证码并继续登录流程
     */
    private void verifyPhoneAndCaptcha() {
        // 检查验证码是否填写
        String captcha = mCaptchaEditText.getText().toString().trim();
        if (TextUtils.isEmpty(captcha)) {
            Toast.makeText(this, "请输入图形验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查手机号是否填写
        String phoneNumber = mPhoneEditText.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            // 如果手机号未填写，尝试自动获取
            autoGetPhoneNumberForLogin();
            return;
        }

        // 手机号和验证码都已填写，继续登录流程
        proceedWithLogin();
    }

    /**
     * 登录流程中获取手机号
     */
    private void autoGetPhoneNumberForLogin() {
        // 首先尝试从SharedPreference获取已保存的手机号
        String savedPhone = SharedPreferenceUtil.getString(this, "saved_phone", "");
        if (!TextUtils.isEmpty(savedPhone)) {
            mPhoneEditText.setText(savedPhone);
            // 允许用户修改已保存的手机号
            mPhoneEditText.setEnabled(true);
            Log.d(TAG, "从SharedPreference获取已保存的手机号: " + savedPhone);
            // 检查验证码是否已填写，如果已填写则继续登录流程
            checkCaptchaAndProceedLogin();
            return;
        }

        // 没有保存的手机号，请求权限并尝试获取
        requestPhoneNumberPermissionForLogin();
        Log.d(TAG, "没有保存的手机号，开始请求权限并尝试获取");
    }

    /**
     * 检查验证码是否已填写，如果已填写则继续登录流程
     */
    private void checkCaptchaAndProceedLogin() {
        String captcha = mCaptchaEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(captcha)) {
            // 验证码已填写，继续登录流程
            proceedWithLogin();
        }
        // 如果验证码未填写，则不自动登录，等待用户填写
    }

    /**
     * 请求读取手机号的权限（用于登录流程）
     */
    private void requestPhoneNumberPermissionForLogin() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_PHONE_STATE },
                    PHONE_NUMBER_PERMISSION_REQUEST_CODE);
        } else {
            // 已经有权限，尝试获取手机号
            getPhoneNumberForLogin();
        }
    }

    /**
     * 登录流程中获取手机号
     */
    private void getPhoneNumberForLogin() {
        String phoneNumber = "";

        try {
            // 1. 尝试通过TelephonyManager获取手机号
            android.telephony.TelephonyManager telephonyManager = (android.telephony.TelephonyManager) getSystemService(
                    Context.TELEPHONY_SERVICE);

            if (telephonyManager != null
                    && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                phoneNumber = telephonyManager.getLine1Number();

                // 处理不同格式的手机号
                if (!TextUtils.isEmpty(phoneNumber)) {
                    // 1. 处理带有国家代码的情况
                    if (phoneNumber.startsWith("+86")) {
                        phoneNumber = phoneNumber.substring(3);
                    } else if (phoneNumber.startsWith("86")) {
                        phoneNumber = phoneNumber.substring(2);
                    }

                    // 2. 移除非数字字符（有些设备可能返回格式化的号码）
                    phoneNumber = phoneNumber.replaceAll("\\D+", "");

                    // 3. 处理可能的国际格式（如+1）
                    if (phoneNumber.length() > 11 && phoneNumber.startsWith("1")) {
                        // 如果号码长度超过11位且以1开头，截取后11位（中国大陆手机号标准长度）
                        phoneNumber = phoneNumber.substring(phoneNumber.length() - 11);
                    }

                    Log.d(TAG, "TelephonyManager获取并处理后的手机号: " + phoneNumber);
                }

                // 验证获取到的手机号格式是否正确
                if (!TextUtils.isEmpty(phoneNumber) && isValidPhone(phoneNumber)) {
                    mPhoneEditText.setText(phoneNumber);
                    // 保存手机号以便下次使用
                    SharedPreferenceUtil.putString(this, "saved_phone", phoneNumber);
                    // 允许用户修改自动获取的手机号
                    mPhoneEditText.setEnabled(true);
                    Log.d(TAG, "成功通过TelephonyManager获取手机号并设置到输入框: " + phoneNumber);
                    // 检查验证码是否已填写，如果已填写则继续登录流程
                    checkCaptchaAndProceedLogin();
                    return;
                }
            }

            // 如果无法自动获取手机号，提示用户手动输入
            Log.d(TAG, "无法自动获取手机号，需要用户手动输入");

            // 确保输入框是可编辑的
            mPhoneEditText.setEnabled(true);
            // 清空验证码输入框，让用户重新输入
            mCaptchaEditText.setText("");
        } catch (Exception e) {
            Log.e(TAG, "获取手机号失败: " + e.getMessage());
            // 确保输入框是可编辑的
            mPhoneEditText.setEnabled(true);
            // 清空验证码输入框，让用户重新输入
            mCaptchaEditText.setText("");
        }
    }

    /**
     * 更新下一步按钮状态
     */
    private void updateNextButtonState() {
        String captcha = mCaptchaEditText.getText().toString().trim();
        boolean agreementChecked = mAgreementCheckBox.isChecked();

        boolean isCaptchaValid = !TextUtils.isEmpty(captcha);

        // 只检查验证码和协议勾选状态，不检查手机号
        boolean shouldEnable = isCaptchaValid && agreementChecked;
        mNextButton.setEnabled(shouldEnable);

        // 添加日志以便调试
        Log.d(TAG, "更新按钮状态 - 验证码有效: " + isCaptchaValid + ", 协议已勾选: " + agreementChecked + ", 按钮状态: " + shouldEnable);

        // 强制应用按钮状态样式变化
        mNextButton.invalidate();
    }

    /**
     * 获取设备唯一标识符
     */
    private String getDeviceId() {
        // 在Android中获取设备唯一标识符
        // 使用Settings.Secure.ANDROID_ID作为设备机器码
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "获取设备机器码: " + androidId);
        return androidId;
    }

    /**
     * 获取设备IMEI
     */
    private String getImei() {
        String imei = "";
        try {
            android.telephony.TelephonyManager telephonyManager = (android.telephony.TelephonyManager) getSystemService(
                    Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                if (checkSelfPermission(
                        android.Manifest.permission.READ_PHONE_STATE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    // 尝试通过标准API获取IMEI
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            try {
                                // 尝试获取IMEI
                                imei = telephonyManager.getImei();
                            } catch (Exception e) {
                                Log.e(TAG, "获取IMEI失败，尝试获取MEID: " + e.getMessage());
                                // 如果获取IMEI失败，尝试获取MEID作为备选
                                try {
                                    imei = telephonyManager.getMeid();
                                } catch (Exception e2) {
                                    Log.e(TAG, "获取MEID也失败: " + e2.getMessage());
                                    // MEID获取失败，尝试通过反射获取IMEI
                                    Log.d(TAG, "尝试通过反射获取IMEI");
                                    imei = getDeviceIdByReflect(telephonyManager, 0);
                                }
                            }
                        } else {
                            // 低版本Android，使用getDeviceId()方法
                            try {
                                imei = telephonyManager.getDeviceId();
                            } catch (Exception e) {
                                Log.e(TAG, "获取设备ID失败: " + e.getMessage());
                                // 尝试通过反射获取IMEI
                                Log.d(TAG, "尝试通过反射获取IMEI");
                                imei = getDeviceIdByReflect(telephonyManager, 0);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "标准API获取IMEI失败: " + e.getMessage());
                        // 所有方法都失败，尝试通过反射获取IMEI
                        Log.d(TAG, "所有标准方法都失败，尝试通过反射获取IMEI");
                        imei = getDeviceIdByReflect(telephonyManager, 0);
                    }
                } else {
                    Log.e(TAG, "未获得读取手机状态权限，尝试直接通过反射获取IMEI");
                    // 即使没有权限，也尝试通过反射获取IMEI（系统应用可能成功）
                    imei = getDeviceIdByReflect(telephonyManager, 0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取IMEI失败: " + e.getMessage());
        }
        Log.d(TAG, "获取IMEI: " + imei);
        return imei;
    }

    /**
     * 通过反射方式获取IMEI号
     * 
     * @param telephonyManager TelephonyManager实例
     * @param slotId           卡槽ID（0或1）
     * @return IMEI号，获取失败返回空字符串
     */
    private String getDeviceIdByReflect(android.telephony.TelephonyManager telephonyManager, int slotId) {
        String imei = "";
        try {
            if (telephonyManager != null) {
                // 尝试通过反射调用getImei方法
                java.lang.reflect.Method method = telephonyManager.getClass().getMethod("getImei", int.class);
                if (method != null) {
                    Object result = method.invoke(telephonyManager, slotId);
                    if (result != null) {
                        imei = result.toString();
                        Log.d(TAG, "反射获取IMEI成功: " + imei);
                    } else {
                        Log.e(TAG, "反射获取IMEI结果为null");
                    }
                } else {
                    Log.e(TAG, "反射未找到getImei方法");
                    // 尝试反射调用getDeviceId方法作为备选
                    try {
                        java.lang.reflect.Method deviceIdMethod = telephonyManager.getClass().getMethod("getDeviceId");
                        if (deviceIdMethod != null) {
                            Object result = deviceIdMethod.invoke(telephonyManager);
                            if (result != null) {
                                imei = result.toString();
                                Log.d(TAG, "反射获取DeviceId成功: " + imei);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "反射获取DeviceId失败: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "反射获取IMEI失败: " + e.getMessage());
        }
        return imei;
    }

    /**
     * 设备登录（第二次及以后登录使用，需要手机号和验证码）
     */
    private void deviceLogin() {

        // 获取用户输入的手机号
        String mobile = mPhoneEditText.getText().toString().trim();
        // 获取用户输入的验证码
        String captcha = mCaptchaEditText.getText().toString().trim();

        // 验证手机号格式（仅在非第一次登录时验证）
        if (!isSecondLogin && !isValidPhone(mobile)) {
            Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取设备机器码
        String deviceId = getDeviceId();
        // 获取完整设备信息
        String deviceFullInfo = getDeviceFullInfo();

        // 生成硬件信息组合标识符（替代IMEI）
        String hardwareIdentifier = generateHardwareBasedIdentifier();

        // 检查是否生成了标识符
        if (TextUtils.isEmpty(hardwareIdentifier)) {
            Log.e(TAG, "无法生成设备标识符，登录失败");
            return;
        }

        // 记录设备信息用于调试
        Log.d(TAG, "设备登录信息：hardwareIdentifier=" + hardwareIdentifier + ", deviceId=" + deviceId + ", deviceFullInfo="
                + deviceFullInfo + ", mobile=" + mobile + ", isSecondLogin=" + isSecondLogin);

        // 调用API进行设备登录，根据登录类型传递不同参数
        // 第一次登录时手机号和验证码可以为空，第二次登录需要验证
        String finalMobile = isSecondLogin ? mobile : "";
        String finalCaptcha = isSecondLogin ? captcha : "";
        int isFirstLogin = isSecondLogin ? 0 : 1; // 1表示首次登录，0表示非首次登录

        ApiManager.getInstance().deviceLogin(deviceId, finalCaptcha, finalMobile, hardwareIdentifier, isFirstLogin,
                new ApiCallback<UserInfo>() {
                    @Override
                    public void onSuccess(UserInfo userInfo) {
                        Log.d(TAG, "设备登录成功，用户ID: " + userInfo.getId());

                        // 检查用户状态
                        if (userInfo.getStatus() != null && !userInfo.getStatus().equals("normal")) {
                            // 用户被封禁，显示提示
                            Toast.makeText(LoginActivity.this, "账号已被封禁，请联系客服", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "用户被封禁，状态: " + userInfo.getStatus());
                            return;
                        }

                        // 设置广告请求的用户ID - 将int转换为String
                        AdManager.getInstance().setUserId(String.valueOf(userInfo.getId()));

                        if (isSecondLogin) {
                            // 第二次登录，直接跳转到答题页面（不显示开屏广告）
                            Log.d(TAG, "第二次登录成功，直接跳转到答题页面");
                            // 标记为第二次登录
                            SharedPreferenceUtil.putBoolean(LoginActivity.this, "is_second_login", true);
                            Intent intent = new Intent(LoginActivity.this, QuizActivity.class);
                            intent.putExtra("show_loading", false); // 第二次登录不显示15秒加载
                            intent.putExtra("is_second_login", true); // 传递第二次登录标志
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            // 延迟销毁当前Activity，确保QuizActivity完全启动
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isFinishing() && !isDestroyed()) {
                                        finish();
                                    }
                                }
                            }, 500);
                        } else {
                            // 第一次登录，显示开屏广告
                            showSplashAdAndProceed();
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, errorMessage);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();

                        // 检查是否为设备异常相关的错误信息
                        if (errorMessage != null && (errorMessage.contains("当前设备异常，暂不支持登录") ||
                                errorMessage.contains("当前设备存在安全风险，暂不支持登录") ||
                                errorMessage.contains("系统检测到该设备完成任务次数过多，已触发限制，请3天后再进行操作") ||
                                errorMessage.contains("该设备无法登录，请取消报名或更换设备重新登陆"))) {
                                                        // 设备异常错误，不显示登录控件，只显示错误提示
                            Log.w(TAG, "设备异常错误，不显示登录控件");
                            // 隐藏所有登录控件和加载控件
                            hideAllLoginControls();
                            hideSplashAdLoading();
                        } else {
                            // 当验证码错误或过期时，自动重新加载验证码
                            if (errorMessage != null && (errorMessage.contains("验证码错误") ||
                                    errorMessage.contains("验证码过期") ||
                                    errorMessage.contains("captcha") ||
                                    errorMessage.contains("CAPTCHA"))) {
                                // 清空验证码输入框
                                mCaptchaEditText.setText("");
                                // 重新加载验证码
                                loadCaptcha();
                            }
                        }
                    }
                });
    }

    /**
     * 获取设备完整信息，包括手机型号、系统版本等
     */
    private String getDeviceFullInfo() {
        // 获取设备型号
        String model = android.os.Build.MODEL;
        // 获取设备制造商
        String manufacturer = android.os.Build.MANUFACTURER;
        // 获取Android系统版本号
        String androidVersion = android.os.Build.VERSION.RELEASE;
        // 获取Android SDK版本号
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        // 获取设备品牌
        String brand = android.os.Build.BRAND;
        // 获取设备名称
        String deviceName = android.os.Build.DEVICE;
        // 获取硬件信息
        String hardware = android.os.Build.HARDWARE;
        // 获取产品名称
        String product = android.os.Build.PRODUCT;
        // 获取系统指纹信息
        String fingerprint = android.os.Build.FINGERPRINT;

        // 尝试获取MAC地址
        String macAddress = "";
        try {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
                android.net.wifi.WifiManager wifiManager = (android.net.wifi.WifiManager) getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    android.net.wifi.WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        macAddress = wifiInfo.getMacAddress();
                    }
                }
            } else {
                // Android 6.0及以上需要通过NetworkInterface获取MAC地址
                java.net.NetworkInterface networkInterface = java.net.NetworkInterface.getByName("wlan0");
                if (networkInterface != null) {
                    byte[] macBytes = networkInterface.getHardwareAddress();
                    if (macBytes != null) {
                        StringBuilder macBuilder = new StringBuilder();
                        for (int i = 0; i < macBytes.length; i++) {
                            macBuilder.append(String.format("%02X", macBytes[i]));
                            if (i < macBytes.length - 1) {
                                macBuilder.append(":");
                            }
                        }
                        macAddress = macBuilder.toString();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取MAC地址失败: " + e.getMessage());
        }

        // 尝试获取OAID（需要接入相应SDK）
        String oaid = "";
        // 注意：获取OAID需要集成相应的SDK，这里只是预留字段
        // 实际实现需要根据具体的OAID SDK进行集成，例如中国移动的OAID SDK

        // 构建完整的设备信息字符串
        StringBuilder deviceInfo = new StringBuilder();
        deviceInfo.append("model=").append(model).append("|")
                .append("manufacturer=").append(manufacturer).append("|")
                .append("androidVersion=").append(androidVersion).append("|")
                .append("sdkVersion=").append(sdkVersion).append("|")
                .append("brand=").append(brand).append("|")
                .append("deviceName=").append(deviceName).append("|")
                .append("hardware=").append(hardware).append("|")
                .append("product=").append(product).append("|")
                .append("androidId=").append(getDeviceId()).append("|")
                .append("fingerprint=").append(fingerprint).append("|")
                .append("mac=").append(macAddress).append("|")
                .append("oaid=").append(oaid);

        Log.d(TAG, "获取设备完整信息: " + deviceInfo.toString());
        return deviceInfo.toString();
    }

    /**
     * 调试登录方法，保持原有的debugLogin功能
     */
    private void debugLogin() {
        // 显示加载提示
        Toast.makeText(this, "正在使用调试模式登录...", Toast.LENGTH_SHORT).show();

        // 获取设备IMEI和设备码
        String debugImei = "debug_imei_" + System.currentTimeMillis();
        String debugDeviceId = getDeviceId();

        // 调用API进行设备登录，但传入调试参数
        int isFirstLogin = isSecondLogin ? 0 : 1; // 1表示首次登录，0表示非首次登录
        ApiManager.getInstance().deviceLogin(debugDeviceId, "", "", debugImei, isFirstLogin,
                new ApiCallback<UserInfo>() {
                    @Override
                    public void onSuccess(UserInfo userInfo) {
                        Log.d(TAG, "调试登录成功，用户ID: " + userInfo.getId());

                        // 设置广告请求的用户ID - 将int转换为String
                        AdManager.getInstance().setUserId(String.valueOf(userInfo.getId()));

                        if (isSecondLogin) {
                            // 第二次登录，直接跳转到答题页面（不显示开屏广告）
                            Log.d(TAG, "第二次调试登录成功，直接跳转到答题页面");

                            Intent intent = new Intent(LoginActivity.this, QuizActivity.class);
                            intent.putExtra("show_loading", false); // 第二次登录不显示15秒加载
                            intent.putExtra("is_second_login", true); // 传递第二次登录标志
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            // 延迟销毁当前Activity，确保QuizActivity完全启动
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isFinishing() && !isDestroyed()) {
                                        finish();
                                    }
                                }
                            }, 500);
                        } else {
                            // 第一次登录，显示开屏广告
                            showSplashAdAndProceed();
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, "调试登录失败: " + errorMessage);
                        Toast.makeText(LoginActivity.this, "调试登录失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 请求必要的权限
     */
    private void requestNecessaryPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {

            // 请求权限
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.ACCESS_WIFI_STATE
                    },
                    PERMISSION_REQUEST_CODE);
        } else {
            // 已经有权限，直接进行第一次登录
            performFirstLogin();
        }
    }

    /**
     * 执行第一次登录（使用设备码登录）
     */
    private void performFirstLogin() {
        Log.d(TAG, "开始第一次登录（设备码登录）");

        // 使用设备码进行登录（不依赖手机号和验证码）
        deviceCodeLogin(true);
    }

    /**
     * 设备码登录（第一次登录使用，不需要手机号和验证码）
     */
    private void deviceCodeLogin(boolean isFirstLogin) {
        Log.d(TAG, "开始设备码登录");

        // 获取设备机器码
        String deviceId = getDeviceId();
        // 获取完整设备信息
        String deviceFullInfo = getDeviceFullInfo();

        // 生成硬件信息组合标识符（替代IMEI）
        String hardwareIdentifier = generateHardwareBasedIdentifier();

        // 检查是否生成了标识符
        if (TextUtils.isEmpty(hardwareIdentifier)) {
            Log.e(TAG, "无法生成设备标识符，登录失败");
            Toast.makeText(this, "设备识别失败，请重试", Toast.LENGTH_SHORT).show();
            return;
        }

        // 记录设备信息用于调试
        Log.d(TAG, "设备码登录信息：hardwareIdentifier=" + hardwareIdentifier + ", deviceId=" + deviceId + ", deviceFullInfo="
                + deviceFullInfo);

        // 调用API进行设备码登录，传入硬件信息组合标识符和设备码，手机号和验证码为空
        // deviceCodeLogin专门用于首次登录，所以传递isFirstLogin=1
        ApiManager.getInstance().deviceLogin(deviceId, "", "", hardwareIdentifier, 1, new ApiCallback<UserInfo>() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                Log.d(TAG, "设备码登录成功，用户ID: " + userInfo.getId());
                if (isFirstLogin) {

                    // 检查用户状态
                    if (userInfo.getStatus() != null && !userInfo.getStatus().equals("normal")) {
                        // 用户被封禁，显示提示
                        Toast.makeText(LoginActivity.this, "账号已被封禁，请联系客服", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "用户被封禁，状态: " + userInfo.getStatus());
                        return;
                    }

                    // 设置广告请求的用户ID - 将int转换为String
                    AdManager.getInstance().setUserId(String.valueOf(userInfo.getId()));

                    // 第一次登录，显示开屏广告
                    showSplashAdAndProceed();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, errorMessage);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();

                // 检查是否为设备异常相关的错误信息
                if (errorMessage != null && (errorMessage.contains("当前设备异常，暂不支持登录") ||
                        errorMessage.contains("当前设备存在安全风险，暂不支持登录") ||
                        errorMessage.contains("系统检测到该设备完成任务次数过多，已触发限制，请3天后再进行操作") ||
                        errorMessage.contains("该设备无法登录，请取消报名或更换设备重新登陆"))) {
                    hideSplashAdLoading();
                    // 设备异常错误，不显示登录控件，只显示错误提示
                    Log.w(TAG, "设备异常错误，不显示登录控件");
                } else {
                    // 其他错误，显示登录控件让用户手动登录
                    showAllLoginControls();
                }
            }
        });
    }

    /**
     * 请求读取手机号的权限
     */
    private void requestPhoneNumberPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_PHONE_STATE },
                    PHONE_NUMBER_PERMISSION_REQUEST_CODE);
        } else {
            // 已经有权限，尝试获取手机号
            getPhoneNumber();
        }
    }

    /**
     * 自动获取手机号并填充到输入框
     */
    private void autoGetPhoneNumber() {
        // 首先尝试从SharedPreference获取已保存的手机号
        String savedPhone = SharedPreferenceUtil.getString(this, "saved_phone", "");
        if (!TextUtils.isEmpty(savedPhone)) {
            mPhoneEditText.setText(savedPhone);
            // 允许用户修改已保存的手机号
            mPhoneEditText.setEnabled(true);
            updateNextButtonState();
            Log.d(TAG, "从SharedPreference获取已保存的手机号: " + savedPhone);
            return;
        }

        // 没有保存的手机号，请求权限并尝试获取
        requestPhoneNumberPermission();
        Log.d(TAG, "没有保存的手机号，开始请求权限并尝试获取");
    }

    /**
     * 获取手机号
     */
    private void getPhoneNumber() {
        String phoneNumber = "";

        try {
            // 尝试通过TelephonyManager获取手机号
            android.telephony.TelephonyManager telephonyManager = (android.telephony.TelephonyManager) getSystemService(
                    Context.TELEPHONY_SERVICE);

            if (telephonyManager != null
                    && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                phoneNumber = telephonyManager.getLine1Number();

                // 处理不同格式的手机号
                if (!TextUtils.isEmpty(phoneNumber)) {
                    // 1. 处理带有国家代码的情况
                    if (phoneNumber.startsWith("+86")) {
                        phoneNumber = phoneNumber.substring(3);
                    } else if (phoneNumber.startsWith("86")) {
                        phoneNumber = phoneNumber.substring(2);
                    }

                    // 2. 移除非数字字符（有些设备可能返回格式化的号码）
                    phoneNumber = phoneNumber.replaceAll("\\D+", "");

                    // 3. 处理可能的国际格式（如+1）
                    if (phoneNumber.length() > 11 && phoneNumber.startsWith("1")) {
                        // 如果号码长度超过11位且以1开头，截取后11位（中国大陆手机号标准长度）
                        phoneNumber = phoneNumber.substring(phoneNumber.length() - 11);
                    }

                    Log.d(TAG, "TelephonyManager获取并处理后的手机号: " + phoneNumber);
                }
            }

            // 验证获取到的手机号格式是否正确
            if (!TextUtils.isEmpty(phoneNumber) && isValidPhone(phoneNumber)) {
                mPhoneEditText.setText(phoneNumber);
                // 保存手机号以便下次使用
                SharedPreferenceUtil.putString(this, "saved_phone", phoneNumber);
                // 允许用户修改自动获取的手机号
                mPhoneEditText.setEnabled(true);
                updateNextButtonState();
                Log.d(TAG, "成功获取手机号并设置到输入框: " + phoneNumber);
            } else {
                Log.d(TAG, "无法自动获取手机号或手机号格式不正确: " + phoneNumber);
                // 显示提示，但不再禁用登录按钮，允许用户手动输入手机号
                // 确保输入框是可编辑的
                mPhoneEditText.setEnabled(true);
                updateNextButtonState();
            }
        } catch (Exception e) {
            Log.e(TAG, "获取手机号失败: " + e.getMessage());
            // 确保输入框是可编辑的
            mPhoneEditText.setEnabled(true);
            updateNextButtonState();
        }
    }

    /**
     * 生成硬件信息组合标识符
     * 
     * @return 生成的唯一标识符
     */
    private String generateHardwareBasedIdentifier() {
        try {
            // 获取设备硬件信息
            String model = android.os.Build.MODEL;
            String manufacturer = android.os.Build.MANUFACTURER;
            String androidVersion = android.os.Build.VERSION.RELEASE;
            int sdkVersion = android.os.Build.VERSION.SDK_INT;
            String brand = android.os.Build.BRAND;
            String deviceName = android.os.Build.DEVICE;
            String hardware = android.os.Build.HARDWARE;
            String product = android.os.Build.PRODUCT;
            String fingerprint = android.os.Build.FINGERPRINT;

            // 构建组合字符串
            StringBuilder combinedInfo = new StringBuilder();
            combinedInfo.append(model).append("|")
                    .append(manufacturer).append("|")
                    .append(androidVersion).append("|")
                    .append(sdkVersion).append("|")
                    .append(brand).append("|")
                    .append(deviceName).append("|")
                    .append(hardware).append("|")
                    .append(product).append("|")
                    .append(fingerprint);

            // 使用SHA-256生成哈希值
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combinedInfo.toString().getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            String identifier = hexString.toString();
            Log.d(TAG, "生成的硬件信息组合标识符: " + identifier);
            return identifier;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "生成哈希值失败: " + e.getMessage());
            // 如果哈希生成失败，返回基于时间戳的临时标识符
            return "temp_" + System.currentTimeMillis() + "_" + Math.random();
        }
    }

    /**
     * 处理权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // 权限请求结果处理
            boolean hasReadPhoneStatePermission = false;
            boolean allPermissionsGranted = true;

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "权限已授予: " + permissions[i]);
                    // 检查是否获取了读取手机状态权限
                    if (Manifest.permission.READ_PHONE_STATE.equals(permissions[i])) {
                        hasReadPhoneStatePermission = true;
                    }
                } else {
                    Log.w(TAG, "权限被拒绝: " + permissions[i]);
                    allPermissionsGranted = false;
                    // 可以在这里显示一个提示，说明某些功能可能无法正常使用
                }
            }

            if (isFirstLogin) {
                // 第一次登录流程
                if (allPermissionsGranted) {
                    // 所有权限都已授予，进行第一次登录
                    performFirstLogin();
                } else {
                    // 权限被拒绝，显示提示并退出应用
                    Toast.makeText(this, "需要权限才能继续使用应用", Toast.LENGTH_LONG).show();
                    finish();
                }
            } else {
                // 第二次登录流程，保持原有逻辑
                if (hasReadPhoneStatePermission) {
                    getPhoneNumberForLogin();
                }
            }
        } else if (requestCode == PHONE_NUMBER_PERMISSION_REQUEST_CODE) {
            // 处理读取手机号权限请求结果
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "读取手机状态权限已授予");
                // 在登录流程中，使用专门的获取手机号方法
                getPhoneNumberForLogin();
            } else {
                Log.w(TAG, "读取手机状态权限被拒绝，无法自动获取手机号");
                // 显示提示，告知用户需要授权才能登录，但不再自动重新请求权限
                // 这样用户可以在准备好授权后，通过点击按钮重新触发权限请求
                // 清空验证码输入框，让用户重新输入
                mCaptchaEditText.setText("");
            }
        }
    }

    /**
     * 显示用户许可协议
     */
    private void showUserAgreement() {
        // 启动协议显示页面，传递用户许可协议类型
        startActivity(new Intent(this, AgreementActivity.class)
                .putExtra(AgreementActivity.EXTRA_AGREEMENT_TYPE, AgreementActivity.TYPE_USER_AGREEMENT));
    }

    /**
     * 显示隐私政策
     */
    private void showPrivacyAgreement() {
        // 启动协议显示页面，传递隐私政策类型
        startActivity(new Intent(this, AgreementActivity.class)
                .putExtra(AgreementActivity.EXTRA_AGREEMENT_TYPE, AgreementActivity.TYPE_PRIVACY_AGREEMENT));
    }

    /**
     * 开始广告预加载
     * 在登录页面预加载广告，但不显示广告
     */
    private void startAdPreload() {
        Log.d(TAG, "开始广告预加载");

        adPreloadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                preloadBannerAd();
                preloadNativeAd();
                preloadInterstitialAd();
                preloadRewardVideoAd();

                Log.d(TAG, "所有广告预加载任务已启动");
            }
        }, AD_PRELOAD_DELAY_MS);
    }

    /**
     * 预加载横幅广告
     * 只加载不显示，为后续页面使用做准备
     */
    private void preloadBannerAd() {
        try {
            Log.d(TAG, "开始预加载横幅广告");

            // 使用TakuAdManager进行横幅广告预加载
            TakuAdManager adManager = TakuAdManager.getInstance();

            // 初始化横幅广告但不显示
            if (adManager.getBannerView() == null) {
                // 需要先初始化横幅广告视图
                // 创建一个临时的不可见容器用于初始化
                ViewGroup tempContainer = new android.widget.FrameLayout(LoginActivity.this);
                tempContainer.setVisibility(View.GONE);

                // 调用showBannerAd方法进行初始化，但容器不可见，所以不会显示
                adManager.showBannerAd(LoginActivity.this, tempContainer);

                // 立即隐藏广告，确保不显示
                adManager.hideBannerAd();

                Log.d(TAG, "横幅广告预加载初始化完成");
            } else {
                // 如果横幅广告已经存在，触发重新加载
                ATBannerView bannerView = adManager.getBannerView();
                if (bannerView != null) {
                    try {
                        ATAdStatusInfo statusInfo = bannerView.checkAdStatus();
                        if (!statusInfo.isReady()) {
                            bannerView.loadAd();
                            Log.d(TAG, "横幅广告重新加载已触发");
                        } else {
                            Log.d(TAG, "横幅广告已就绪，无需重新加载");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "横幅广告状态检查异常: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "横幅广告预加载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 预加载原生广告
     * 只加载不显示，为后续页面使用做准备
     */
    private void preloadNativeAd() {
        try {
            Log.d(TAG, "开始预加载原生广告");

            TakuAdManager adManager = TakuAdManager.getInstance();

            // 创建一个临时的不可见容器用于预加载
            ViewGroup tempContainer = new android.widget.FrameLayout(LoginActivity.this);
            tempContainer.setVisibility(View.GONE);

            // 使用带频率控制的加载方法进行预加载
            // 由于容器不可见，广告不会显示出来
            adManager.loadNativeAdWithFrequencyControl(LoginActivity.this, tempContainer);

            Log.d(TAG, "原生广告预加载已触发");

        } catch (Exception e) {
            Log.e(TAG, "原生广告预加载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 预加载插屏广告
     * 只加载不显示，为后续页面使用做准备
     */
    private void preloadInterstitialAd() {
        try {
            Log.d(TAG, "开始预加载插屏广告");

            TakuAdManager adManager = TakuAdManager.getInstance();

            // 使用预加载方法，这个方法只加载不显示
            adManager.preloadInterstitialAd(LoginActivity.this);

            Log.d(TAG, "插屏广告预加载已触发");

        } catch (Exception e) {
            Log.e(TAG, "插屏广告预加载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 预加载激励视频广告
     * 只加载不显示，为后续页面使用做准备
     */
    private void preloadRewardVideoAd() {
        try {
            Log.d(TAG, "开始预加载激励视频广告");

            TakuAdManager adManager = TakuAdManager.getInstance();

            // 使用预加载方法，这个方法只加载不显示
            adManager.preloadRewardVideoAd(LoginActivity.this);

            Log.d(TAG, "激励视频广告预加载已触发");

        } catch (Exception e) {
            Log.e(TAG, "激励视频广告预加载失败: " + e.getMessage(), e);
        }
    }

    // 开屏广告重试相关变量
    private Handler splashAdRetryHandler;
    private Runnable splashAdRetryRunnable;
    private int splashAdRetryCount = 0;
    private static final int MAX_SPLASH_AD_RETRY_COUNT = 5; // 最大重试次数
    private static final long SPLASH_AD_RETRY_DELAY_MS = 2000; // 重试延迟时间（毫秒）

    /**
     * 显示开屏广告并在广告关闭后跳转到答题页面
     * 如果开屏广告加载失败，会自动重试直到成功或达到最大重试次数
     */
    private void showSplashAdAndProceed() {
        Log.d(TAG, "开始显示开屏广告");

        // 重置重试计数器
        splashAdRetryCount = 0;

        // 初始化重试Handler
        if (splashAdRetryHandler == null) {
            splashAdRetryHandler = new Handler();
        }

        // 显示开屏广告加载提示
        showSplashAdLoading();

        // 开始开屏广告加载流程
        startSplashAdLoading();
    }

    /**
     * 开始开屏广告加载流程
     */
    private void startSplashAdLoading() {
        Log.d(TAG, "开始加载开屏广告，当前重试次数: " + splashAdRetryCount);

        // 设置ECPM回调
        TakuAdManager.getInstance().setEcpmCallback(new TakuAdManager.EcpmCallback() {
            @Override
            public void onEcpmReceived(String adType, String placementId, double ecpmValue) {
                if ("splash".equals(adType)) {
                    Log.d(TAG, "接收到开屏广告ECPM值: " + ecpmValue);
                    setSplashAdEcpm(ecpmValue);
                }
            }
        });

        // 使用TakuAdManager显示开屏广告（带失败回调）
        TakuAdManager.getInstance().showSplashAd(LoginActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "开屏广告关闭，跳转到答题页面进行15秒加载");

                        // 清理重试逻辑
                        cleanupSplashAdRetry();

                        // 隐藏开屏广告加载提示
                        hideSplashAdLoading();

                        // 标记第一次登录已完成
                        SharedPreferenceUtil.putBoolean(LoginActivity.this, "has_first_login", true);

                        // 获取开屏广告的ECPM值
                        double splashAdEcpm = getSplashAdEcpmValue();
                        Log.d(TAG, "开屏广告ECPM值: " + splashAdEcpm);

                        // 广告关闭后跳转到答题页面进行15秒加载
                        Intent intent = new Intent(LoginActivity.this, QuizActivity.class);
                        intent.putExtra("show_loading", true); // 传递参数表示需要显示加载
                        intent.putExtra("loading_duration", 15000); // 15秒加载时间
                        intent.putExtra("splash_ad_ecpm", splashAdEcpm); // 传递开屏广告ECPM值
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }, new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "开屏广告加载失败，触发重试逻辑");
                // 只在真正的加载失败时触发重试逻辑
                // 加载超时不会触发此回调，等待真正的加载失败
                handleSplashAdLoadFailure();
            }
        });
    }

    /**
     * 处理开屏广告加载失败的重试逻辑
     */
    private void handleSplashAdLoadFailure() {
        splashAdRetryCount++;

        if (splashAdRetryCount >= MAX_SPLASH_AD_RETRY_COUNT) {
            Log.e(TAG, "开屏广告加载失败，已达到最大重试次数(" + MAX_SPLASH_AD_RETRY_COUNT + ")，直接跳转");

            // 清理重试逻辑
            cleanupSplashAdRetry();

            // 隐藏开屏广告加载提示
            hideSplashAdLoading();

            // 直接跳转到答题页面
            proceedToQuizActivity();
            return;
        }

        Log.w(TAG, "开屏广告加载失败，将在" + SPLASH_AD_RETRY_DELAY_MS + "毫秒后重试，当前重试次数: " + splashAdRetryCount);

        // 创建重试任务
        splashAdRetryRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "开始重试加载开屏广告");
                startSplashAdLoading();
            }
        };

        // 延迟重试
        splashAdRetryHandler.postDelayed(splashAdRetryRunnable, SPLASH_AD_RETRY_DELAY_MS);
    }

    /**
     * 清理开屏广告重试逻辑
     */
    private void cleanupSplashAdRetry() {
        if (splashAdRetryHandler != null && splashAdRetryRunnable != null) {
            splashAdRetryHandler.removeCallbacks(splashAdRetryRunnable);
            splashAdRetryRunnable = null;
        }
        splashAdRetryCount = 0;
    }

    /**
     * 直接跳转到答题页面（开屏广告加载失败时的备用方案）
     */
    private void proceedToQuizActivity() {
        Log.d(TAG, "开屏广告加载失败，直接跳转到答题页面");

        // 标记第一次登录已完成
        SharedPreferenceUtil.putBoolean(LoginActivity.this, "has_first_login", true);

        // 跳转到答题页面进行15秒加载
        Intent intent = new Intent(LoginActivity.this, QuizActivity.class);
        intent.putExtra("show_loading", true); // 传递参数表示需要显示加载
        intent.putExtra("loading_duration", 15000); // 15秒加载时间
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * 显示开屏广告加载提示
     */
    private void showSplashAdLoading() {
        if (splashAdLoadingLayout != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    splashAdLoadingLayout.setVisibility(View.VISIBLE);
                    Log.d(TAG, "显示开屏广告加载提示");
                }
            });
        }
    }

    /**
     * 隐藏开屏广告加载提示
     */
    private void hideSplashAdLoading() {
        if (splashAdLoadingLayout != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    splashAdLoadingLayout.setVisibility(View.GONE);
                    Log.d(TAG, "隐藏开屏广告加载提示");
                }
            });
        }
    }

    /**
     * 获取开屏广告的ECPM值
     */
    private double getSplashAdEcpmValue() {
        return splashAdEcpm;
    }

    /**
     * 设置开屏广告的ECPM值
     */
    public void setSplashAdEcpm(double ecpm) {
        this.splashAdEcpm = ecpm;
        Log.d(TAG, "设置开屏广告ECPM值: " + ecpm);
    }

    /**
     * 在Activity销毁时清理广告预加载资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 清理广告预加载Handler
        if (adPreloadHandler != null) {
            adPreloadHandler.removeCallbacksAndMessages(null);
            adPreloadHandler = null;
        }

        // 清理开屏广告重试逻辑
        cleanupSplashAdRetry();

        Log.d(TAG, "登录页面销毁，广告预加载资源已清理");
    }
}