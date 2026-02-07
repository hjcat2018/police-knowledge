package com.police.kb.common;

/**
 * 错误码常量类
 * <p>
 * 定义系统统一的错误码常量，用于API接口的错误处理。
 * 错误码采用HTTP状态码规范，配合业务子码进行细分。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
public class ErrorCode {

    /**
     * 操作成功
     */
    public static final int SUCCESS = 200;

    /**
     * 操作失败
     */
    public static final int FAIL = 500;

    /**
     * 未授权
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * Token已过期
     */
    public static final int TOKEN_EXPIRED = 40101;

    /**
     * Token无效
     */
    public static final int TOKEN_INVALID = 40102;

    /**
     * 禁止访问
     */
    public static final int FORBIDDEN = 403;

    /**
     * 资源不存在
     */
    public static final int NOT_FOUND = 404;

    /**
     * 根据错误码获取对应的错误信息
     *
     * @param code 错误码
     * @return 错误信息
     */
    public static String getMessage(int code) {
        switch (code) {
            case SUCCESS:
                return "操作成功";
            case FAIL:
                return "操作失败";
            case UNAUTHORIZED:
            case TOKEN_EXPIRED:
            case TOKEN_INVALID:
                return "未授权，请重新登录";
            case FORBIDDEN:
                return "禁止访问";
            case NOT_FOUND:
                return "资源不存在";
            default:
                return "未知错误";
        }
    }
}
