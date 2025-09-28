package com.fortunequizking.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.TextView;
import com.fortunequizking.R;

public class AgreementActivity extends AppCompatActivity {

    public static final String EXTRA_AGREEMENT_TYPE = "agreement_type";
    public static final String TYPE_USER_AGREEMENT = "user_agreement";
    public static final String TYPE_PRIVACY_AGREEMENT = "privacy_agreement";

    // 隐私政策HTML文件路径
    private static final String PRIVACY_POLICY_HTML_PATH = "http://dtds.psjjtd.com/PrivacyPolicy.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_agreement);

        // 设置窗口属性，使其看起来像一个弹窗
        if (getWindow() != null) {
            getWindow().setGravity(android.view.Gravity.CENTER);
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            getWindow().setAttributes(params);
        }

        // 获取类型参数
        String agreementType = getIntent().getStringExtra(EXTRA_AGREEMENT_TYPE);
        if (agreementType == null) {
            agreementType = TYPE_USER_AGREEMENT; // 默认显示用户许可协议
        }

        // 初始化UI组件
        TextView titleTextView = findViewById(R.id.dialog_title);
        Button closeButton = findViewById(R.id.close_button);

        // 设置标题
        if (TYPE_PRIVACY_AGREEMENT.equals(agreementType)) {
            titleTextView.setVisibility(View.GONE); // 隐藏标题
            // 加载隐私政策HTML内容
            loadPrivacyPolicyFromHtmlFile();
        } else {
            titleTextView.setText("用户许可协议");
            TextView contentTextView = findViewById(R.id.agreement_content);
            contentTextView.setText(getUserAgreementContent());
        }

        // 设置关闭按钮点击事件
        closeButton.setOnClickListener(v -> finish());
    }

    /**
     * 从网址加载隐私政策内容
     */
    private void loadPrivacyPolicyFromHtmlFile() {
        try {
            // 先找到TextView，然后获取其父级ScrollView
            TextView contentTextView = findViewById(R.id.agreement_content);
            ScrollView scrollView = (ScrollView) contentTextView.getParent();
            
            // 创建WebView
            WebView webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDefaultTextEncodingName("UTF-8");
            webView.getSettings().setTextZoom(50); // 将字体大小设置为默认的80%
            
            // 设置WebViewClient监控加载状态
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    // 页面加载完成后隐藏加载提示
                    contentTextView.setVisibility(View.GONE);
                }
                
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    // 加载失败时显示默认内容
                    contentTextView.setText(getPrivacyAgreementContent());
                    contentTextView.setVisibility(View.VISIBLE);
                    if (webView.getParent() != null) {
                        ((
                                ViewGroup) webView.getParent()).removeView(webView);
                    }
                    Toast.makeText(AgreementActivity.this, "加载隐私政策失败", Toast.LENGTH_SHORT).show();
                }
            });
            
            // 将WebView添加到ScrollView的父布局中，替换ScrollView
            ViewGroup parent = (ViewGroup) scrollView.getParent();
            if (parent != null) {
                int index = parent.indexOfChild(scrollView);
                parent.removeView(scrollView);
                
                // 使用原来ScrollView的布局参数，保持弹窗尺寸不变
                ViewGroup.LayoutParams params = scrollView.getLayoutParams();
                
                parent.addView(webView, index, params);
                
                // 直接加载网址
                webView.loadUrl(PRIVACY_POLICY_HTML_PATH);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 发生异常时显示默认内容
            TextView contentTextView = findViewById(R.id.agreement_content);
            contentTextView.setText(getPrivacyAgreementContent());
            contentTextView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "加载隐私政策失败", Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * 获取用户许可协议内容
     */
    private String getUserAgreementContent() {
        return "用户许可协议\n\n" +
                "一、服务条款的接受\n" +
                "通过注册、登录、使用答题大师平台（以下简称\"平台\"）的服务，您确认并同意本协议的所有条款和条件。\n\n" +
                "二、用户注册与账户安全\n" +
                "1. 用户应提供真实、准确、完整的个人资料，并及时更新。\n" +
                "2. 用户应妥善保管账户和密码，对使用其账户进行的所有操作负全部责任。\n\n" +
                "三、用户行为规范\n" +
                "1. 用户不得利用本平台从事任何违法违规活动。\n" +
                "2. 用户不得干扰平台的正常运行，不得破坏平台的系统安全。\n\n" +
                "四、知识产权\n" +
                "平台内的所有内容，包括但不限于文字、图片、音频、视频等，均受著作权法等相关法律法规保护。\n\n" +
                "五、免责声明\n" +
                "1. 平台不对用户因使用本平台而产生的任何间接、偶然、特殊及后续的损害承担责任。\n" +
                "2. 平台有权在必要时修改本协议，并通过平台公告的方式通知用户。\n\n" +
                "六、协议的修改\n" +
                "平台有权在必要时修改本协议，修改后的协议将在平台上公布，用户继续使用平台服务视为接受修改后的协议。\n\n" +
                "七、法律适用与争议解决\n" +
                "本协议的订立、执行、解释及争议的解决均应适用中华人民共和国大陆地区法律。\n\n" +
                "更新日期：2025年9月";
    }

    /**
     * 获取默认隐私政策内容（当HTML文件无法加载时使用）
     */
    private String getPrivacyAgreementContent() {
        return "隐私政策\n\n" +
                "一、隐私政策的接受\n" +
                "通过使用答题大师平台（以下简称\"平台\"）的服务，您确认并同意我们按照本隐私政策收集、使用和存储您的个人信息。\n\n" +
                "二、我们收集的信息\n" +
                "1. 您提供的信息：包括但不限于您在注册时提供的手机号、昵称等。\n" +
                "2. 设备信息：包括但不限于设备型号、操作系统版本、设备标识符等。\n" +
                "3. 使用信息：包括但不限于您在平台上的浏览记录、答题记录等。\n\n" +
                "三、我们如何使用信息\n" +
                "1. 提供、维护和改进我们的服务。\n" +
                "2. 发送服务通知和更新。\n" +
                "3. 进行用户验证和安全保护。\n" +
                "4. 进行数据分析和服务优化。\n\n" +
                "四、信息的共享与披露\n" +
                "1. 我们不会向第三方出售您的个人信息。\n" +
                "2. 在以下情况下，我们可能会共享您的信息：\n" +
                "   a. 经您明确同意；\n" +
                "   b. 遵守法律法规要求；\n" +
                "   c. 保护我们的合法权益。\n\n" +
                "五、信息的存储与安全\n" +
                "1. 我们采取各种安全措施保护您的信息，防止信息泄露、丢失或损坏。\n" +
                "2. 您的信息将存储在中华人民共和国境内。\n\n" +
                "六、您的权利\n" +
                "您有权访问、更正、删除您的个人信息，也有权限制或反对我们对您信息的处理。\n\n" +
                "七、隐私政策的更新\n" +
                "我们可能会不时更新本隐私政策，并通过平台公告的方式通知您。\n\n" +
                "更新日期：2025年9月";
    }
}