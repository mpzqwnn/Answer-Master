package com.fortunequizking.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.fortunequizking.MyApplication;
import com.fortunequizking.model.ApiResponse;
import com.fortunequizking.model.HotProject;
import com.fortunequizking.model.StaminaUpdateResult;
import com.fortunequizking.model.UserInfo;
import com.fortunequizking.api.ApiCallback;
import com.fortunequizking.model.AnswerStats;
import com.fortunequizking.model.AnswerHistory;
import com.fortunequizking.model.Category;
import com.fortunequizking.model.Question;
import com.fortunequizking.model.QuizHistoryRecord;
import com.fortunequizking.model.UserStats;
import com.fortunequizking.model.LoginResponse;
import com.fortunequizking.util.NetworkConfig;
import com.fortunequizking.util.SharedPreferenceUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import com.fortunequizking.model.QuestionListResponse;
import okhttp3.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.os.Handler;
import android.os.Looper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiManager {

    private static final String TAG = "ApiManager";
    private static ApiManager instance;
    private ApiService apiService;

    private ApiManager() {
        // 初始化API服务
        apiService = NetworkConfig.getRetrofitInstance().create(ApiService.class);
    }

    public static synchronized ApiManager getInstance() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

    // 应用ID，从AndroidManifest.xml中获取的Taku广告APP_ID
    private static final String APP_ID = "a68bab61c2bd06";

    /**
     * 设备机器码登录
     */
    public void deviceLogin(String deviceCode, String captcha, String mobile, final ApiCallback<UserInfo> callback) {
        Log.d(TAG, "执行设备登录: deviceCode=" + deviceCode + ", captcha=" + captcha + ", mobile=" + mobile);

        // 仍然使用原来的方式传递参数，但deviceCode现在包含了完整的设备信息
        Call<ApiResponse<LoginResponse>> call = apiService.deviceLogin(deviceCode, captcha, APP_ID, getChannel(),
                mobile);
        call.enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call,
                    Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LoginResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        LoginResponse loginResponse = apiResponse.getData();
                        if (loginResponse != null && loginResponse.getUserInfo() != null) {
                            UserInfo userInfo = loginResponse.getUserInfo();
                            // 保存用户信息和Token
                            saveUserInfo(userInfo);
                            callback.onSuccess(userInfo);
                        } else {
                            callback.onFailure("用户信息为空");
                        }
                    } else {
                        callback.onFailure(apiResponse.getMessage());
                    }
                } else {
                    callback.onFailure("登录失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                Log.e(TAG, "设备登录请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 获取渠道信息
     */
    public String getChannel() {
        try {
            return "应用宝";
            // return "赏帮赚";
            // return "乐助客";
        } catch (Exception e) {
            Log.e(TAG, "获取渠道信息异常: " + e.getMessage());
            return "default";
        }
    }

    /**
     * 获取图形验证码
     */
    public void getCaptcha(final ApiCallback<String> callback) {
        // 生成随机数参数，防止缓存
        String random = String.valueOf(Math.random());

        Call<ResponseBody> call = apiService.getCaptcha(random);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // 将图片响应转换为Base64字符串
                        byte[] imageBytes = response.body().bytes();
                        String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                        callback.onSuccess(base64Image);
                    } catch (IOException e) {
                        Log.e(TAG, "解析验证码图片失败: " + e.getMessage());
                        callback.onFailure("解析验证码图片失败: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("获取验证码失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "获取图形验证码请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 验证手机号和图形验证码
     */
    public void verifyPhoneCaptcha(String phone, String captcha, final ApiCallback<Object> callback) {
        Call<ApiResponse<Object>> call = apiService.verifyPhoneCaptcha(phone, captcha);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onFailure(apiResponse.getMessage());
                    }
                } else {
                    callback.onFailure("验证失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e(TAG, "验证手机号和验证码请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 获取热门项目列表
     */
    public void getHotProjectList(final ApiCallback<List<HotProject>> callback) {
        Call<ApiResponse<List<HotProject>>> call = apiService.getHotProjectList();
        call.enqueue(new Callback<ApiResponse<List<HotProject>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<HotProject>>> call,
                    Response<ApiResponse<List<HotProject>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<HotProject>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onFailure(apiResponse.getMessage());
                    }
                } else {
                    callback.onFailure("获取项目列表失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<HotProject>>> call, Throwable t) {
                Log.e(TAG, "获取热门项目列表请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    // 题库相关方法

    /**
     * 获取题目列表
     */
    /**
     * 获取题目列表
     */
    public void getQuestions(int categoryId, String difficulty, int limit, int page,
            final ApiCallback<List<Question>> callback) {
        // 获取渠道信息
        String channel = getChannel();
        Call<ApiResponse<QuestionListResponse>> call = apiService.getQuestions(categoryId, difficulty, limit, page,
                channel);
        call.enqueue(new Callback<ApiResponse<QuestionListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<QuestionListResponse>> call,
                    Response<ApiResponse<QuestionListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<QuestionListResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        QuestionListResponse questionListResponse = apiResponse.getData();
                        if (questionListResponse != null && questionListResponse.getQuestions() != null) {
                            callback.onSuccess(questionListResponse.getQuestions());
                        } else {
                            // 返回空列表，QuizActivity会使用本地题目
                            callback.onSuccess(new ArrayList<Question>());
                        }
                    } else {
                        callback.onFailure(apiResponse.getMessage());
                    }
                } else {
                    callback.onFailure("获取题目列表失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<QuestionListResponse>> call, Throwable t) {
                Log.e(TAG, "获取题目列表请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 获取题目详情
     */
    public void getQuestionDetail(int questionId, final ApiCallback<Question> callback) {
        Call<ApiResponse<Question>> call = apiService.getQuestionDetail(questionId);
        call.enqueue(new Callback<ApiResponse<Question>>() {
            @Override
            public void onResponse(Call<ApiResponse<Question>> call, Response<ApiResponse<Question>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Question> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onFailure(apiResponse.getMessage());
                    }
                } else {
                    callback.onFailure("获取题目详情失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Question>> call, Throwable t) {
                Log.e(TAG, "获取题目详情请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 提交答案
     */
    public void submitAnswer(int questionId, String selectedOption, int timeSpent, final ApiCallback<Object> callback) {
        // 获取渠道信息
        String channel = getChannel();
        Call<ApiResponse<Object>> call = apiService.submitAnswer(questionId, selectedOption, timeSpent, channel);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onFailure(apiResponse.getMessage());
                    }
                } else {
                    callback.onFailure("提交答案失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e(TAG, "提交答案请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 获取题目分类
     */
    public void getCategories(final ApiCallback<List<Category>> callback) {
        Call<ApiResponse<List<Category>>> call = apiService.getCategories();
        call.enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call,
                    Response<ApiResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Category>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onFailure(apiResponse.getMessage());
                    }
                } else {
                    callback.onFailure("获取分类失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                Log.e(TAG, "获取分类请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 获取用户答题历史
     */
    public void getAnswerHistory(int page, int limit, final ApiCallback<List<QuizHistoryRecord>> callback) {
        Call<ApiResponse<List<QuizHistoryRecord>>> call = apiService.getAnswerHistory(page, limit);
        call.enqueue(new Callback<ApiResponse<List<QuizHistoryRecord>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuizHistoryRecord>>> call,
                    Response<ApiResponse<List<QuizHistoryRecord>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<QuizHistoryRecord>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onFailure(apiResponse.getMessage());
                    }
                } else {
                    callback.onFailure("获取答题历史失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuizHistoryRecord>>> call, Throwable t) {
                Log.e(TAG, "获取答题历史请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 获取排行榜
     */
    public void getRankingList(int limit, String period, final ApiCallback<Object> callback) {
        Call<ApiResponse<Object>> call = apiService.getRankingList(limit, period);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onFailure(apiResponse.getMessage());
                    }
                } else {
                    callback.onFailure("获取排行榜失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e(TAG, "获取排行榜请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 获取用户答题统计（今日答题数和历史答题数）
     */
    public void getUserAnswerStats(final ApiCallback<AnswerStats> callback) {
        // 从SharedPreference获取用户ID
        String userId = SharedPreferenceUtil.getString(MyApplication.getInstance(), "user_id", "");
        Log.d(TAG, "获取答题统计，用户ID: " + userId);

        Call<ApiResponse<AnswerStats>> call = apiService.getUserAnswerStats(userId);
        call.enqueue(new Callback<ApiResponse<AnswerStats>>() {
            @Override
            public void onResponse(Call<ApiResponse<AnswerStats>> call, Response<ApiResponse<AnswerStats>> response) {
                Log.d(TAG, "获取答题统计响应码: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AnswerStats> apiResponse = response.body();
                    Log.d(TAG, "获取答题统计响应数据: code=" + apiResponse.getCode() + ", message=" + apiResponse.getMessage());
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "答题统计数据: todayCount=" + apiResponse.getData().getTodayCount() + ", totalCount="
                                + apiResponse.getData().getTotalCount());
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "获取答题统计失败: " + apiResponse.getMessage());
                        callback.onFailure(apiResponse.getMessage());
                    }
                } else {
                    String errorMsg = "获取答题统计失败，服务器响应异常";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ": " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "解析错误响应失败", e);
                        }
                    }
                    Log.e(TAG, errorMsg);
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AnswerStats>> call, Throwable t) {
                Log.e(TAG, "获取答题统计请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 获取用户体力值
     */
    public void getUserStamina(final ApiCallback<Integer> callback) {
        Call<ApiResponse<StaminaUpdateResult>> call = apiService.getUserStamina();
        call.enqueue(new Callback<ApiResponse<StaminaUpdateResult>>() {
            @Override
            public void onResponse(Call<ApiResponse<StaminaUpdateResult>> call,
                    Response<ApiResponse<StaminaUpdateResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<StaminaUpdateResult> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData().getStamina());
                    } else {
                        callback.onFailure(apiResponse.getMessage());
                    }
                } else {
                    callback.onFailure("获取体力失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<StaminaUpdateResult>> call, Throwable t) {
                Log.e(TAG, "获取体力请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 获取当前用户信息
     */
    public void getCurrentUserInfo(final ApiCallback<UserInfo> callback) {
        // 获取用户ID
        String userId = SharedPreferenceUtil.getString(MyApplication.getInstance(), "user_id", "");
        if (userId.isEmpty()) {
            callback.onFailure("用户未登录");
            return;
        }

        // 先执行风控检查
        checkRisk(userId, new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object data) {
                // 风控检查通过后，调用getUserInfo接口获取用户信息
                Call<ApiResponse<UserInfo>> call = apiService.getUserInfo();
                call.enqueue(new Callback<ApiResponse<UserInfo>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserInfo>> call, Response<ApiResponse<UserInfo>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<UserInfo> apiResponse = response.body();
                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                // 保存用户信息到本地
                                UserInfo userInfo = apiResponse.getData();
                                saveUserInfo(userInfo); // 使用完整的保存方法，确保保存所有字段

                                callback.onSuccess(userInfo);
                            } else {
                                callback.onFailure(apiResponse.getMessage());
                            }
                        } else {
                            callback.onFailure("获取用户信息失败，服务器响应异常");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserInfo>> call, Throwable t) {
                        Log.e(TAG, "获取用户信息请求失败: " + t.getMessage());
                        callback.onFailure("网络请求失败: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(String errorMsg) {
                // 风控检查失败，设置用户状态为风控异常
                SharedPreferenceUtil.putBoolean(MyApplication.getInstance(), "risk_control_triggered", true);
                callback.onFailure("风控检查失败: " + errorMsg);
            }
        });
    }

    /**
     * 更新用户体力值
     */
    public void updateUserStamina(int staminaChange, final ApiCallback<StaminaUpdateResult> callback) {
        Call<ApiResponse<StaminaUpdateResult>> call = apiService.updateUserStamina(staminaChange);
        call.enqueue(new Callback<ApiResponse<StaminaUpdateResult>>() {
            @Override
            public void onResponse(Call<ApiResponse<StaminaUpdateResult>> call,
                    Response<ApiResponse<StaminaUpdateResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<StaminaUpdateResult> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onFailure(apiResponse.getMessage());
                    }
                } else {
                    callback.onFailure("更新体力失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<StaminaUpdateResult>> call, Throwable t) {
                Log.e(TAG, "更新体力请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 上报C2S竞价结果（带回调）
     */
    public void reportBiddingResult(java.util.Map<String, String> params, final ApiCallback<Object> callback) {
        Call<ApiResponse<Object>> call = apiService.reportBiddingResult(params);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        if (callback != null) {
                            callback.onSuccess(apiResponse.getData());
                        }
                    } else {
                        Log.w(TAG, "C2S竞价结果上报失败: " + apiResponse.getMessage());
                        if (callback != null) {
                            callback.onFailure(apiResponse.getMessage());
                        }
                    }
                } else {
                    Log.w(TAG, "C2S竞价结果上报失败，服务器响应异常");
                    if (callback != null) {
                        callback.onFailure("C2S竞价结果上报失败，服务器响应异常");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e(TAG, "C2S竞价结果上报网络请求失败: " + t.getMessage());
                // 这里不回调失败，因为上报只是统计功能，不应影响主流程
            }
        });
    }

    /**
     * 上报C2S竞价结果（无回调，简化调用）
     */
    public void reportBiddingResult(java.util.Map<String, String> params) {
        // 直接调用带回调的方法，但传入null作为回调
        reportBiddingResult(params, null);
    }

    /**
     * 上传广告eCPM数据（带回调）
     */
    public void uploadAdEcpm(java.util.Map<String, String> params, final ApiCallback<Object> callback) {
        // 检查是否有用户Token
        String token = SharedPreferenceUtil.getString(MyApplication.getInstance(), "user_token", "");
        Log.d(TAG, "准备上传广告eCPM数据: 是否有Token=" + (!token.isEmpty() ? "是" : "否") + ", 参数=" + params.toString());

        // 如果有token，添加到请求参数中
        if (!token.isEmpty()) {
            params.put("token", token);
            Log.d(TAG, "已将用户token添加到请求参数中");
        }

        // 添加渠道信息
        String channel = getChannel();
        if (!channel.isEmpty()) {
            params.put("channel", channel);
            Log.d(TAG, "已添加渠道信息到eCPM请求参数中: " + channel);
        }

        // 这里直接使用ResponseBody而不是ApiResponse<Object>，因为服务器返回的格式可能不匹配
        Call<ResponseBody> call = apiService.uploadAdEcpmRaw(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Log.d(TAG, "广告eCPM数据上传成功，响应: " + responseBody);

                        // 尝试解析响应内容，即使失败也记录成功
                        if (callback != null) {
                            callback.onSuccess(null);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "广告eCPM数据上传响应解析异常: " + e.getMessage());
                        if (callback != null) {
                            callback.onSuccess(null); // 即使解析失败也返回成功，因为上报只是统计功能
                        }
                    }
                } else {
                    Log.w(TAG, "广告eCPM数据上传失败，服务器响应异常");
                    if (callback != null) {
                        callback.onFailure("广告eCPM数据上传失败，服务器响应异常");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "广告eCPM数据上传网络请求失败: " + t.getMessage());
                // 这里不回调失败，因为上报只是统计功能，不应影响主流程
            }
        });
    }

    /**
     * 上传广告eCPM数据（无回调，简化调用）
     */
    public void uploadAdEcpm(java.util.Map<String, String> params) {
        // 直接调用带回调的方法，但传入null作为回调
        uploadAdEcpm(params, null);
    }

    /**
     * 保存用户信息到SharedPreferences
     */
    private void saveUserInfo(UserInfo userInfo) {
        Context context = MyApplication.getInstance();
        SharedPreferenceUtil.putString(context, "user_id", String.valueOf(userInfo.getId()));
        SharedPreferenceUtil.putString(context, "username", userInfo.getUsername());
        SharedPreferenceUtil.putString(context, "nickname", userInfo.getNickname());
        SharedPreferenceUtil.putString(context, "avatar_url", userInfo.getAvatarUrl());
        SharedPreferenceUtil.putString(context, "user_token", userInfo.getToken());
        SharedPreferenceUtil.putString(context, "mobile", userInfo.getMobile() != null ? userInfo.getMobile() : "");

        // 保存用户的分数和等级
        SharedPreferenceUtil.putInt(context, "user_score", userInfo.getScore());
        SharedPreferenceUtil.putInt(context, "user_level", userInfo.getLevel());
        
        // 保存注册时间yy
        if (userInfo.getCreateTime() > 0) {
            // 处理时间戳：用户提供的createtime:1758679856是秒级时间戳
            // 需要乘以1000转换为毫秒级时间戳后再格式化
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            Date registerDate = new Date(userInfo.getCreateTime() * 1000); // 转换为毫秒
            String formattedRegisterTime = sdf.format(registerDate);
            SharedPreferenceUtil.putString(context, "register_time", formattedRegisterTime);
        }
    }

    /**
     * 执行风控检查
     */
    public void checkRisk(String userId, final ApiCallback<Object> callback) {
        Call<ApiResponse<Object>> call = apiService.checkRisk(userId);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    Log.d(TAG, "风控检查响应: code=" + apiResponse.getCode() + ", msg=" + apiResponse.getMessage() + ", data="
                            + (apiResponse.getData() != null ? apiResponse.getData().toString() : "null"));

                    // 判断是否触发风控：根据data中的risk_triggered值和msg字段综合判断
                    boolean isRiskTriggered = false;
                    boolean shouldBanUser = false; // 是否应该封禁用户
                    String banReason = "触发风控条件";

                    try {
                        // 首先检查msg字段是否包含明确的'未触发风控条件'
                        if (apiResponse.getMessage() != null && apiResponse.getMessage().contains("未触发风控条件")) {
                            Log.d(TAG, "msg包含'未触发风控条件'，设置isRiskTriggered=false");
                            isRiskTriggered = false;
                        } else {
                            // 优先从data中获取risk_triggered值
                            if (apiResponse.getData() != null) {
                                Log.d(TAG, "data类型: " + apiResponse.getData().getClass().getName());

                                if (apiResponse.getData() instanceof Map) {
                                    Map<String, Object> data = (Map<String, Object>) apiResponse.getData();
                                    Log.d(TAG, "data内容: " + data.toString());

                                    if (data.containsKey("risk_triggered")) {
                                        Object riskTriggeredObj = data.get("risk_triggered");
                                        Log.d(TAG,
                                                "risk_triggered类型: " + (riskTriggeredObj != null
                                                        ? riskTriggeredObj.getClass().getName()
                                                        : "null"));
                                        Log.d(TAG, "risk_triggered值: " + String.valueOf(riskTriggeredObj));

                                        // 增强类型处理，确保任何形式的1都能被正确识别
                                        if (riskTriggeredObj instanceof Number) {
                                            isRiskTriggered = ((Number) riskTriggeredObj).intValue() == 1;
                                            Log.d(TAG, "risk_triggered是Number类型，转换后值: " + isRiskTriggered);
                                        } else if (riskTriggeredObj instanceof Boolean) {
                                            isRiskTriggered = (Boolean) riskTriggeredObj;
                                            Log.d(TAG, "risk_triggered是Boolean类型，值: " + isRiskTriggered);
                                        } else if (riskTriggeredObj instanceof String) {
                                            String riskTriggeredStr = (String) riskTriggeredObj;
                                            isRiskTriggered = "1".equals(riskTriggeredStr)
                                                    || "true".equalsIgnoreCase(riskTriggeredStr);
                                            Log.d(TAG, "risk_triggered是String类型，转换后值: " + isRiskTriggered);
                                        } else {
                                            // 尝试将其他类型转换为字符串进行检查
                                            String riskTriggeredStr = String.valueOf(riskTriggeredObj);
                                            isRiskTriggered = "1".equals(riskTriggeredStr)
                                                    || "true".equalsIgnoreCase(riskTriggeredStr);
                                            Log.d(TAG, "risk_triggered是未知类型，转换为String后值: " + isRiskTriggered);
                                        }
                                    } else {
                                        Log.w(TAG, "data中不包含risk_triggered字段");
                                    }

                                    // 尝试从data中获取封禁原因
                                    if (data.containsKey("ban_reason")) {
                                        banReason = String.valueOf(data.get("ban_reason"));
                                    } else if (data.containsKey("risk_name")) {
                                        banReason = String.valueOf(data.get("risk_name"));
                                    }

                                    // 检查风控类型，决定是否需要封禁用户
                                    // 如果result_type是ban_user，则执行封禁；如果是risk_triggered_hard_question，则只标记但不封禁
                                    if (data.containsKey("result_type")) {
                                        String resultType = String.valueOf(data.get("result_type"));
                                        Log.d(TAG, "风控结果类型: " + resultType);

                                        if ("ban_user".equals(resultType)) {
                                            shouldBanUser = true;
                                        } else if ("risk_triggered_hard_question".equals(resultType)) {
                                            // 体力冷却增加类型的风控，不执行封禁
                                            shouldBanUser = false;
                                            // 可以在这里添加标记用户需要回答难题的逻辑
                                        }
                                    }

                                    // 检查risk_type，避免对体力冷却增加类型风控执行封禁
                                    if (data.containsKey("risk_type")) {
                                        String riskType = String.valueOf(data.get("risk_type"));
                                        if ("hard_question".equals(riskType)) {
                                            shouldBanUser = false;
                                        }
                                    }

                                    // 检查risk_name是否包含"难题"，如果包含则不执行封禁
                                    if (data.containsKey("risk_name")) {
                                        String riskName = String.valueOf(data.get("risk_name"));
                                        if (riskName.contains("难题")) {
                                            shouldBanUser = false;
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "data不是Map类型，无法解析risk_triggered值");
                                }
                            } else {
                                Log.w(TAG, "data为null，无法解析risk_triggered值");
                            }

                            // 如果msg不为空且不包含'未触发风控条件'，使用msg作为封禁原因
                            if (apiResponse.getMessage() != null && !apiResponse.getMessage().isEmpty()) {
                                banReason = apiResponse.getMessage();
                                Log.d(TAG, "使用msg作为封禁原因: " + banReason);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "解析风控数据失败: " + e.getMessage());
                        // 发生异常时，默认认为未触发风控
                        isRiskTriggered = false;
                        shouldBanUser = false;
                    }

                    Log.d(TAG, "最终风控判断结果: isRiskTriggered=" + isRiskTriggered + ", shouldBanUser=" + shouldBanUser
                            + ", banReason=" + banReason);

                    // 根据shouldBanUser标志来决定是否执行封禁操作
                    if (shouldBanUser) {
                        Log.d(TAG, "触发需要封禁的风控条件，执行强制退出操作");
                        // 触发风控时，设置风控标志并强制退出登录
                        SharedPreferenceUtil.putBoolean(MyApplication.getInstance(), "risk_control_triggered", true);
                        logoutAndRedirectToLogin(banReason);
                        callback.onFailure(banReason);
                    } else {
                        Log.d(TAG, "未触发需要封禁的风控条件，继续正常流程");
                        // 未触发需要封禁的风控条件，调用onSuccess回调
                        callback.onSuccess(apiResponse.getData());
                    }
                } else {
                    callback.onFailure("风控检查失败，服务器响应异常");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e(TAG, "风控检查网络请求失败: " + t.getMessage());
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 清除用户登录信息并跳转到登录页面
     */
    private void logoutAndRedirectToLogin(final String banReason) {
        // 清除本地保存的用户登录信息
        SharedPreferenceUtil.putString(MyApplication.getInstance(), "user_id", "");
        SharedPreferenceUtil.putString(MyApplication.getInstance(), "token", "");
        SharedPreferenceUtil.putString(MyApplication.getInstance(), "nickname", "");
        SharedPreferenceUtil.putString(MyApplication.getInstance(), "register_time", "");
        SharedPreferenceUtil.putBoolean(MyApplication.getInstance(), "is_login", false);

        // 在UI线程中显示提示并跳转到登录页面
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyApplication.getInstance(), banReason, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MyApplication.getInstance(),
                        com.fortunequizking.activity.LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                MyApplication.getInstance().startActivity(intent);
            }
        });
    }

    // 添加一个格式化当前时间的辅助方法
    private String formatCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // 导入外部AnswerStats类，在实际代码中不需要这行，因为已经在文件顶部导入
}