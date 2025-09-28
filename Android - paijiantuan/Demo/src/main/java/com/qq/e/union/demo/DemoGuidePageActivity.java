package com.qq.e.union.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.qq.e.comm.managers.GDTAdSdk;
import com.qq.e.union.demo.util.ToastUtil;

/**
 * Demo启动时首屏展示一个引导页
 * 引导页中含有两个button，如下：
 * button1：【第一步：初始化SDK】
 * button2：【第二步：开始展示广告】
 */
public class DemoGuidePageActivity extends FragmentActivity {

    private boolean mIsSdkStartSuccess = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_guide_page);

        // button1：【第一步：初始化SDK】
        Button initSdkButton = findViewById(R.id.button_init_sdk);

        // button2：【第二步：开始展示广告】
        Button startDemoButton = findViewById(R.id.button_start_demo);

        // 这里调用GDTAdSdk.start方法，原有InnerDemoApplication中的start调用逻辑删除
        initSdkButton.setOnClickListener(v -> GDTAdSdk.start(new GDTAdSdk.OnStartListener() {
            @Override
            public void onStartSuccess() {
                mIsSdkStartSuccess = true;
                ToastUtil.l("初始化SDK成功");
            }

            @Override
            public void onStartFailed(Exception e) {
                mIsSdkStartSuccess = false;
                ToastUtil.l("初始化SDK失败");
                Log.e("gdt onStartFailed:", e.toString());
            }
        }));

        startDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSdkStartSuccess) {
                    Intent intent = new Intent(getBaseContext(), SplashActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    ToastUtil.l("未初始化SDK，请先进行初始化");
                }
            }
        });
    }
}
