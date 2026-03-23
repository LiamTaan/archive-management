package com.electronic.archive.vo;

import lombok.Data;

/**
 * 统一响应结果
 * @param <T> 响应数据类型
 */
@Data
public class ResponseResult<T> {
    /**
     * 状态码（200：成功，500：失败）
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 默认构造方法
     */
    public ResponseResult() {
    }

    /**
     * 全参构造方法
     * @param code 状态码
     * @param message 响应消息
     * @param data 响应数据
     */
    public ResponseResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应（带数据）
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>(200, message, data);
    }

    /**
     * 成功响应（带数据，不带消息）
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(200, null, data);
    }

    /**
     * 成功响应（不带数据）
     * @param message 响应消息
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public static <T> ResponseResult<T> success(String message) {
        return new ResponseResult<>(200, message, null);
    }

    /**
     * 成功响应（不带数据，不带消息）
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>(200, null, null);
    }

    /**
     * 失败响应
     * @param message 响应消息
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public static <T> ResponseResult<T> fail(String message) {
        return new ResponseResult<>(500, message, null);
    }

    /**
     * 失败响应（带数据）
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public static <T> ResponseResult<T> fail(String message, T data) {
        return new ResponseResult<>(500, message, data);
    }
}