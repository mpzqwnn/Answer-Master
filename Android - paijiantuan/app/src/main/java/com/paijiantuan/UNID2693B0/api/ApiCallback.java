package com.paijiantuan.UNID2693B0.api;

/**
 * API回调接口，用于处理异步请求的结果
 * @param <T> 成功回调时的数据类型
 */
public interface ApiCallback<T> {
    /**
     * 请求成功时的回调方法
     * @param data 服务器返回的数据
     */
    void onSuccess(T data);

    /**
     * 请求失败时的回调方法
     * @param errorMessage 错误信息
     */
    void onFailure(String errorMessage);
}