package com.police.kb.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 捕获并处理系统中的各类异常，
 * 返回统一的错误响应格式。
 * </p>
 * <p>
 * 处理的异常类型：
 * <ul>
 *   <li>认证异常 - 未登录、token过期</li>
 *   <li>权限异常 - 角色权限不足</li>
 *   <li>参数异常 - 参数校验失败</li>
 *   <li>系统异常 - 其他未预期的异常</li>
 * </ul>
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理未登录异常
     *
     * @param ex 未登录异常
     * @return 统一响应结果
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleNotLoginException(NotLoginException ex) {
        log.warn("未登录: {}", ex.getMessage());
        return new Result<>(ErrorCode.TOKEN_INVALID, "登录已过期，请重新登录");
    }

    /**
     * 处理角色权限不足异常
     *
     * @param ex 角色权限异常
     * @return 统一响应结果
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotRoleException(NotRoleException ex) {
        log.warn("角色权限不足: {}", ex.getMessage());
        return new Result<>(ErrorCode.FORBIDDEN, "角色权限不足");
    }

    /**
     * 处理权限不足异常
     *
     * @param ex 权限异常
     * @return 统一响应结果
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotPermissionException(NotPermissionException ex) {
        log.warn("权限不足: {}", ex.getMessage());
        return new Result<>(ErrorCode.FORBIDDEN, "权限不足");
    }

    /**
     * 处理方法参数校验异常
     *
     * @param ex 方法参数校验异常
     * @return 统一响应结果（包含错误字段信息）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "",
                        (existing, replacement) -> existing
                ));
        log.warn("参数校验失败: {}", errors);
        return Result.error(400, "参数校验失败");
    }

    /**
     * 处理参数绑定异常
     *
     * @param ex 参数绑定异常
     * @return 统一响应结果（包含错误字段信息）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleBindException(BindException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "",
                        (existing, replacement) -> existing
                ));
        log.warn("参数绑定失败: {}", errors);
        return Result.error(400, "参数绑定失败");
    }

    /**
     * 处理约束校验异常
     *
     * @param ex 约束校验异常
     * @return 统一响应结果（包含错误字段信息）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(field, message);
        });
        log.warn("参数校验失败: {}", errors);
        return Result.error(400, "参数校验失败");
    }

    /**
     * 处理非法参数异常（认证相关）
     *
     * @param ex 非法参数异常
     * @return 统一响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("认证失败: {}", ex.getMessage());
        return new Result<>(ErrorCode.TOKEN_INVALID, "登录已过期，请重新登录");
    }

    /**
     * 处理安全异常
     *
     * @param ex 安全异常
     * @return 统一响应结果
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleSecurityException(SecurityException ex) {
        log.warn("权限不足: {}", ex.getMessage());
        return new Result<>(ErrorCode.FORBIDDEN, "权限不足");
    }

    /**
     * 处理请求方法不支持异常
     *
     * @param ex 请求方法不支持异常
     * @return 统一响应结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("请求方法不支持: {}", ex.getMessage());
        return Result.error(405, "请求方法不支持");
    }

    /**
     * 处理运行时异常
     *
     * @param ex 运行时异常
     * @return 统一响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        if (message != null && (message.contains("token") || message.contains("登录") || message.contains("未授权"))) {
            log.warn("认证异常: {}", message);
            return new Result<>(ErrorCode.TOKEN_INVALID, "登录已过期，请重新登录");
        }
        log.error("业务异常: {}", message, ex);
        return Result.error(message);
    }

    /**
     * 处理通用异常
     *
     * @param ex 异常
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex) {
        String message = ex.getMessage();
        if (message != null && (message.contains("token") || message.contains("登录") || message.contains("未授权"))) {
            log.warn("认证异常: {}", message);
            return new Result<>(ErrorCode.TOKEN_INVALID, "登录已过期，请重新登录");
        }
        log.error("系统异常: {}", message, ex);
        return Result.error("系统异常，请联系管理员");
    }
}
