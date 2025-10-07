package com.paijiantuan.UNID2693B0.api;

import com.paijiantuan.UNID2693B0.model.ApiResponse;
import com.paijiantuan.UNID2693B0.model.HotProject;
import com.paijiantuan.UNID2693B0.model.LoginResponse;
import com.paijiantuan.UNID2693B0.model.StaminaUpdateResult;
import com.paijiantuan.UNID2693B0.model.UserInfo;
import com.paijiantuan.UNID2693B0.model.Question;
import com.paijiantuan.UNID2693B0.model.QuestionListResponse;
import com.paijiantuan.UNID2693B0.model.Category;
import com.paijiantuan.UNID2693B0.model.AnswerHistory;
import com.paijiantuan.UNID2693B0.model.QuizHistoryRecord;
import com.paijiantuan.UNID2693B0.model.RankingItem;
import com.paijiantuan.UNID2693B0.model.UserStats;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Body;

import com.paijiantuan.UNID2693B0.model.AnswerStats;
import okhttp3.ResponseBody;

public interface ApiService {

    /**
     * 获取热门项目列表
     */
    @GET("index/getHotProjectList")
    Call<ApiResponse<List<HotProject>>> getHotProjectList();

    /**
     * 用户登录
     */
    @POST("user/login")
    Call<ApiResponse<UserInfo>> login(@Query("username") String username, @Query("password") String password);

    /**
     * 微信登录
     */
    @FormUrlEncoded
    @POST("user/wechatLogin")
    Call<ApiResponse<LoginResponse>> wechatLogin(@Field("code") String code);

    /**
     * 设备机器码登录 - 支持IMEI优先登录
     */
    @FormUrlEncoded
    @POST("user/deviceLogin")
    Call<ApiResponse<LoginResponse>> deviceLogin(@Field("device_code") String deviceCode, 
                                                @Field("captcha") String captcha, 
                                                @Field("app_id") String appId, 
                                                @Field("channel") String channel, 
                                                @Field("mobile") String mobile, 
                                                @Field("device_full_info") String deviceFullInfo,
                                                @Field("task_package") String taskPackage,
                                                @Field("imei") String imei,
                                                @Field("is_first_login") int isFirstLogin);
    
    /**
     * 获取图形验证码
     */
    @GET("user/captcha")
    Call<ResponseBody> getCaptcha(@Query("r") String random);
    
    /**
     * 验证手机号和图形验证码
     */
    @FormUrlEncoded
    @POST("user/verifyPhoneCaptcha")
    Call<ApiResponse<Object>> verifyPhoneCaptcha(@Field("phone") String phone, @Field("captcha") String captcha);

    // 题库相关接口

    /**
     * 获取题目列表
     */
    @GET("quiz/getQuestions")
    Call<ApiResponse<QuestionListResponse>> getQuestions(
            @Query("category_id") int categoryId,
            @Query("difficulty") String difficulty,
            @Query("limit") int limit,
            @Query("page") int page,
            @Query("channel") String channel);

    /**
     * 获取题目详情
     */
    @GET("question/getQuestionDetail")
    Call<ApiResponse<Question>> getQuestionDetail(@Query("question_id") int questionId);

    /**
     * 提交答案
     */
    @FormUrlEncoded
    @POST("question/submitAnswer")
    Call<ApiResponse<Object>> submitAnswer(
            @Field("question_id") int questionId,
            @Field("selected_option") String selectedOption,
            @Field("time_spent") int timeSpent,
            @Field("channel") String channel);

    /**
     * 获取题目分类
     */
    @GET("question/getCategories")
    Call<ApiResponse<List<Category>>> getCategories();

    /**
     * 获取题目统计信息
     */
    @GET("question/getQuestionStats")
    Call<ApiResponse<Object>> getQuestionStats();

    /**
     * 获取用户答题历史
     */
    @GET("quiz/getUserAnswerHistory")
    Call<ApiResponse<List<QuizHistoryRecord>>> getAnswerHistory(
            @Query("page") int page,
            @Query("limit") int limit);

    /**
     * 获取排行榜
     */
    @GET("question/getRankingList")
    Call<ApiResponse<Object>> getRankingList(
            @Query("limit") int limit,
            @Query("period") String period);

    /**
     * 获取用户答题统计（今日答题数和历史答题数）
     */
    @GET("quiz/getUserAnswerStats")
    Call<ApiResponse<AnswerStats>> getUserAnswerStats(@Query("user_id") String userId);

    /**
     * 获取用户体力值
     */
    @GET("question/getUserStamina")
    Call<ApiResponse<StaminaUpdateResult>> getUserStamina();
    
    /**
     * 更新用户体力值
     */
    @FormUrlEncoded
    @POST("question/updateUserStamina")
    Call<ApiResponse<StaminaUpdateResult>> updateUserStamina(@Field("change") int staminaChange);

    /**
     * 获取用户信息
     */
    @GET("user/getUserInfo")
    Call<ApiResponse<UserInfo>> getUserInfo();
    
    /**
     * 上报C2S竞价结果
     */
    @FormUrlEncoded
    @POST("bidding_c2s/report")
    Call<ApiResponse<Object>> reportBiddingResult(@FieldMap Map<String, String> params);
    
    /**
     * 上传广告eCPM数据
     */
    @FormUrlEncoded
    @POST("ad/uploadAdEcpm")
    Call<ApiResponse<Object>> uploadAdEcpm(@FieldMap Map<String, String> params);
    
    /**
     * 上传广告eCPM数据（直接返回ResponseBody）
     */
    @FormUrlEncoded
    @POST("ad/uploadAdEcpm")
    Call<ResponseBody> uploadAdEcpmRaw(@FieldMap Map<String, String> params);
    
    /**
     * 风控检查
     */
    @FormUrlEncoded
    @POST("risk/checkRisk")
    Call<ApiResponse<Object>> checkRisk(@Field("user_id") String userId);
    
    /**
     * 上传广告错误信息
     */
    @FormUrlEncoded
    @POST("ad/uploadError")
    Call<ResponseBody> uploadAdError(@FieldMap Map<String, String> params);
}