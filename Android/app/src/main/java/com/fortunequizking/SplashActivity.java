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
                
                Log.d(TAG, "检查用户登录状态: isLoggedIn=" + isLoggedIn + ", userId=" + userId + ", token=" + userToken);
                
                // 如果用户已登录并且有有效的用户ID和Token，则直接跳转到主界面
                if (isLoggedIn && !userId.isEmpty() && !userToken.isEmpty()) {
                    Log.d(TAG, "用户已登录，直接跳转到主界面");
                    startActivity(new Intent(SplashActivity.this, QuizActivity.class));
                } else {
                    Log.d(TAG, "用户未登录或登录信息不完整，跳转到登录界面");
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                
                overridePendingTransition(0, 0);
                finish();
            }
        }, 1500);
    }
}