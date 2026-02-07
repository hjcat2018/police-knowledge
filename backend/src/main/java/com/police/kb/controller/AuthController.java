package com.police.kb.controller;

import com.police.kb.common.Result;
import com.police.kb.domain.dto.ChangePasswordRequest;
import com.police.kb.domain.dto.LoginRequest;
import com.police.kb.domain.dto.LoginResponse;
import com.police.kb.domain.dto.UserInfoDTO;
import com.police.kb.domain.dto.*;
import com.police.kb.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * <p>
 * 处理用户认证相关的请求，包括登录、登出、密码修改、获取用户信息等。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * <p>
     * 验证用户凭据，返回登录凭证（Token）和用户信息。
     * </p>
     *
     * @param request 登录请求参数
     * @return 登录响应结果
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户登出
     * <p>
     * 清除当前用户的登录状态。
     * </p>
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    /**
     * 修改密码
     * <p>
     * 允许已登录用户修改自己的密码。
     * </p>
     *
     * @param request 修改密码请求参数
     * @return 操作结果
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     * <p>
     * 获取已登录用户的详细信息。
     * </p>
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoDTO> getCurrentUserInfo() {
        UserInfoDTO userInfo = authService.getCurrentUserInfo();
        return Result.success(userInfo);
    }

    /**
     * 检查登录状态
     * <p>
     * 检查当前用户是否已登录。
     * </p>
     *
     * @return 登录状态信息
     */
    @GetMapping("/check-status")
    public Result<Map<String, Object>> checkLoginStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("isLogin", authService.getCurrentUserInfo() != null);
        return Result.success(result);
    }
}
