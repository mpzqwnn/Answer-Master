package com.fortunequizking;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.fortunequizking.activity.LoginActivity;
import com.fortunequizking.activity.AgreementActivity;
import com.fortunequizking.util.SharedPreferenceUtil;
import com.fortunequizking.R;
import com.fortunequizking.api.ApiManager;
import com.fortunequizking.api.ApiCallback;

public class SplashActivity extends AppCompatActivity {

    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_PRIVACY_POLICY_ACCEPTED = "privacy_policy_accepted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    
        // 检查是否是首次启动且未接受隐私政策
        boolean isFirstLaunch = SharedPreferenceUtil.getBoolean(this, KEY_FIRST_LAUNCH, true);
        boolean hasAcceptedPrivacyPolicy = SharedPreferenceUtil.getBoolean(this, KEY_PRIVACY_POLICY_ACCEPTED, false);
    
        if (isFirstLaunch || !hasAcceptedPrivacyPolicy) {
            // 首次启动或未接受隐私政策，显示隐私政策弹窗
            showPrivacyPolicyDialog();
            // 标记为非首次启动
            SharedPreferenceUtil.putBoolean(this, KEY_FIRST_LAUNCH, false);
        } else {
            // 非首次启动且已接受隐私政策，直接进入登录页
            proceedToNextStep();
        }
    }
    
    /**
     * 显示隐私政策弹窗
     */
    private void showPrivacyPolicyDialog() {
        Intent intent = new Intent(SplashActivity.this, AgreementActivity.class);
        intent.putExtra(AgreementActivity.EXTRA_AGREEMENT_TYPE, AgreementActivity.TYPE_PRIVACY_AGREEMENT);
        startActivityForResult(intent, 1001);
    }
    
    /**
     * 处理隐私政策弹窗的返回结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001) {
            // 用户关闭隐私政策弹窗，标记为已接受
            SharedPreferenceUtil.putBoolean(this, KEY_PRIVACY_POLICY_ACCEPTED, true);
            // 继续原有逻辑
            proceedToNextStep();
        }
    }
    
    /**
     * 继续原有逻辑：直接跳转到登录页
     */
    private void proceedToNextStep() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 不管用户是否登录过，都直接跳转到登录页
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        }, 500);
    }
}