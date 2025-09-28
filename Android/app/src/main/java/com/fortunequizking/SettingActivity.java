package com.fortunequizking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fortunequizking.api.ApiManager;
import com.fortunequizking.api.ApiCallback;
import com.fortunequizking.model.AnswerStats;
import com.fortunequizking.util.SharedPreferenceUtil;
import com.fortunequizking.activity.LoginActivity;
import com.fortunequizking.R;

public class SettingActivity extends AppCompatActivity {

    private TextView mNicknameText;
    private TextView mRegisterTimeText;
    private ApiManager mApiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        
        try {
            // 初始化UI组件
            mNicknameText = findViewById(R.id.nickname_text);
            mRegisterTimeText = findViewById(R.id.register_time_text);
            
            // 初始化API管理器
            mApiManager = ApiManager.getInstance();
            
            // 加载用户数据
            loadUserData();
            
            // 加载答题统计
            loadUserAnswerStats();
        } catch (Exception e) {
            e.printStackTrace();
            // 防止崩溃，返回上一页
            finish();
        }
    }

    private void loadUserData() {
        try {
            String userId = SharedPreferenceUtil.getString(this, "user_id", "2581800015");
            String nickname = SharedPreferenceUtil.getString(this, "nickname", "头发长出来了吗");
            String registerTime = SharedPreferenceUtil.getString(this, "register_time", "2025/08/18 00:04:49");
            
            // 设置用户信息
            mNicknameText.setText(nickname);
            mRegisterTimeText.setText(registerTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载用户答题统计并在UI上显示
     */
    private void loadUserAnswerStats() {
        if (mApiManager != null) {
            mApiManager.getUserAnswerStats(new ApiCallback<AnswerStats>() {
                @Override
                public void onSuccess(AnswerStats stats) {
                    // 保存统计数据到本地
                    SharedPreferenceUtil.putInt(SettingActivity.this, "today_answer_count", stats.getTodayCount());
                    SharedPreferenceUtil.putInt(SettingActivity.this, "total_answer_count", stats.getTotalCount());
                    SharedPreferenceUtil.putInt(SettingActivity.this, "today_correct_count", stats.getTodayCorrectCount());
                    
                    // 更新UI显示
                    TextView todayCorrectText = findViewById(R.id.today_correct_text);
                    TextView todayTotalText = findViewById(R.id.today_total_text);
                    
                    if (todayCorrectText != null) {
                        todayCorrectText.setText("今日答对: " + stats.getTodayCorrectCount() + "题");
                    }
                    
                    if (todayTotalText != null) {
                        todayTotalText.setText("今日答题: " + stats.getTodayCount() + "题");
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e("SettingActivity", "加载答题统计失败: " + error);
                    
                    // 加载失败时，使用本地缓存或默认值
                    int todayCount = SharedPreferenceUtil.getInt(SettingActivity.this, "today_answer_count", 0);
                    int todayCorrectCount = SharedPreferenceUtil.getInt(SettingActivity.this, "today_correct_count", 0);
                    
                    TextView todayCorrectText = findViewById(R.id.today_correct_text);
                    TextView todayTotalText = findViewById(R.id.today_total_text);
                    
                    if (todayCorrectText != null) {
                        todayCorrectText.setText("今日答对: " + todayCorrectCount + "题");
                    }
                    
                    if (todayTotalText != null) {
                        todayTotalText.setText("今日答题: " + todayCount + "题");
                    }
                }
            });
        }
    }
}
