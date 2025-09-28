package com.fortunequizking.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.fortunequizking.MyApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.fortunequizking.R;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class NetworkConfig {

    private static final String TAG = "NetworkConfig";
    
    private static Retrofit retrofit;
    private static OkHttpClient okHttpClient;
    
    /**
     * 获取Retrofit实例
     */
    // 修改debug模式判断逻辑
    private static boolean isDebugBuild() {
        try {
            // 不再始终返回true，而是读取实际的debug标志或根据环境决定
            Context context = MyApplication.getInstance();
            // 尝试获取debug标志
            int debugFlagId = context.getResources().getIdentifier("is_debug", "bool", context.getPackageName());
            if (debugFlagId > 0) {
                return context.getResources().getBoolean(debugFlagId);
            }
            
            return false;
        } catch (Exception e) {
            Log.e(TAG, "检查debug版本失败", e);
            return false;
        }
    }
    
    /**
     * 检查应用是否是debug版本
     */
    private static boolean isRunningOnEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }
    
    // 自定义CookieJar实现，用于在内存中存储Cookie
    private static class MemoryCookieJar implements CookieJar {
        private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url.host(), cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : Collections.emptyList();
        }
    }

    /**
     * 获取OkHttpClient实例，添加Token拦截器和Cookie管理
     */
    private static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)  // 增加连接超时时间
                    .readTimeout(60, TimeUnit.SECONDS)     // 增加读取超时时间
                    .writeTimeout(60, TimeUnit.SECONDS)    // 增加写入超时时间
                    .retryOnConnectionFailure(true)        // 连接失败时自动重试
                    .cookieJar(new MemoryCookieJar()) // 使用自定义CookieJar
                    .addInterceptor(new TokenInterceptor())
                    .addInterceptor(new ResponseInterceptor()); // 添加响应拦截器
            
            // 可选：根据需要配置代理
            if (isDebugBuild() && isRunningOnEmulator()) {
                // 仅在调试模式且在模拟器上运行时使用代理
                Log.d(TAG, "启用调试代理");
                builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.2.2", 8888)));
            }
            
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }
    
    /**
     * Token拦截器，自动为请求添加Token头
     */
    private static class TokenInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Context context = MyApplication.getInstance();
            String token = SharedPreferenceUtil.getString(context, "user_token", "");
            
            Request originalRequest = chain.request();
            
            // 如果有Token，添加到请求头
            if (!token.isEmpty()) {
                Log.d(TAG, "添加Token到请求头: " + token);
                Request.Builder builder = originalRequest.newBuilder()
                        .header("Token", token)
                        .method(originalRequest.method(), originalRequest.body());
                
                return chain.proceed(builder.build());
            }
            
            return chain.proceed(originalRequest);
        }
    }
    
    /**
     * 响应拦截器，用于检查和处理服务器返回的响应内容
     */
    // 修改baseUrl配置，使用用户指定的实际接口路径
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // 从资源文件中获取API基础URL
            Context context = MyApplication.getInstance();
            String baseUrl = context.getString(R.string.api_base_url);
            
            // 注释掉debug环境的特殊处理，直接使用实际接口路径
            // 根据用户要求，使用实际的接口路径
            baseUrl = "http://dtds.psjjtd.com/api/";
            Log.d(TAG, "当前使用的baseUrl: " + baseUrl);
            
            // 添加URL格式验证
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
                Log.d(TAG, "修正baseUrl格式: " + baseUrl);
            }
            
            // 创建宽松的Gson解析器
            Gson gson = new GsonBuilder()
                    .setLenient() // 设置宽松模式
                    .create();
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
    
    /**
     * 测试连接可用性（仅在需要时从后台线程调用）
     */
    private static void testConnection(String url) {
        try {
            Log.d(TAG, "正在测试连接: " + url);
            // 创建一个简单的连接测试
            java.net.URL testUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) testUrl.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("HEAD");
            
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "连接测试响应码: " + responseCode);
            connection.disconnect();
        } catch (IOException e) {
            Log.e(TAG, "连接测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查应用是否运行在模拟器上
     */
    private static class ResponseInterceptor implements Interceptor {
        private static final Charset UTF8 = Charset.forName("UTF-8");
        
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            
            // 记录请求URL
            Log.d(TAG, "请求URL: " + request.url());
            
            // 检查响应内容
            ResponseBody body = response.body();
            if (body != null) {
                BufferedSource source = body.source();
                source.request(Long.MAX_VALUE); // 确保完全读取
                Buffer buffer = source.buffer();
                
                Charset charset = UTF8;
                MediaType contentType = body.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                
                // 读取响应内容
                String responseBody = buffer.clone().readString(charset);
                Log.d(TAG, "响应内容类型: " + (contentType != null ? contentType.toString() : "unknown"));
                Log.d(TAG, "响应内容长度: " + body.contentLength());
                Log.d(TAG, "响应内容: " + responseBody);
            }
            
            return response;
        }
    }
}