package com.police.kb.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果
 * <p>
 * 用于所有API接口的标准化响应格式。
 * 包含状态码、消息、数据和时间戳。
 * </p>
 *
 * @param <T> 数据类型
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
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
     * 时间戳
     */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    /**
     * 创建成功响应
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Result实例
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 创建成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return Result实例
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 创建错误响应
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return Result实例
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 创建错误响应（指定状态码）
     *
     * @param code 状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return Result实例
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 判断是否成功
     *
     * @return true-成功
     */
    public boolean isSuccess() {
        return 200 == code;
    }
}
