package com.fortunequizking;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.fortunequizking.activity.LoginActivity;
import com.fortunequizking.QuizActivity;
import com.fortunequizking.util.SharedPreferenceUtil;
import com.fortunequizking.R;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 检查用户是否已经登录
                boolean isLoggedIn = SharedPreferenceUtil.getBoolean(SplashActivity.this, "is_login", false);
                String userId = SharedPreferenceUtil.getString(SplashActivity.this, "user_id", "");
                String userToken = SharedPreferenceUtil.getString(SplashActivity.this, "user_token", "");
                
                // 检查是否是第二次登录（通过检查是否有登录次数记录）
                boolean isSecondLogin = SharedPreferenceUtil.getBoolean(SplashActivity.this, "is_second_login", false);
                
                Log.d(TAG, "检查用户登录状态: isLoggedIn=" + isLoggedIn + ", userId=" + userId + ", token=" + userToken + ", isSecondLogin=" + isSecondLogin);
                
                // 只有第二次登录以后的用户才能直接跳转到答题页
                if (isLoggedIn && !userId.isEmpty() && !userToken.isEmpty() && isSecondLogin) {
                    Log.d(TAG, "用户已登录且是第二次登录，直接跳转到主界面");
                    startActivity(new Intent(SplashActivity.this, QuizActivity.class));
                } else {
                    Log.d(TAG, "用户未登录或登录信息不完整或第一次登录，跳转到登录界面");
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                
                overridePendingTransition(0, 0);
                finish();
            }
        }, 1500);
    }
}