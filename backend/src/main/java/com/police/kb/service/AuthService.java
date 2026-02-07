package com.police.kb.service;

import com.police.kb.domain.dto.ChangePasswordRequest;
import com.police.kb.domain.dto.LoginRequest;
import com.police.kb.domain.dto.LoginResponse;
import com.police.kb.domain.dto.UserInfoDTO;
import com.police.kb.domain.dto.*;
import com.police.kb.domain.entity.Permission;
import com.police.kb.domain.entity.Role;
import com.police.kb.domain.entity.User;
import com.police.kb.mapper.PermissionMapper;
import com.police.kb.mapper.RoleMapper;
import com.police.kb.mapper.UserMapper;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 认证服务
 * <p>
 * 提供用户认证和授权相关的业务逻辑。
 * 包括登录、登出、获取用户信息、修改密码等功能。
 * </p>
 * <p>
 * 技术特性：
 * <ul>
 *   <li>Sa-Token认证框架集成</li>
 *   <li>BCrypt密码加密</li>
 *   <li>RBAC角色权限控制</li>
 * </ul>
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Slf4j
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    /**
     * 构造函数
     *
     * @param userMapper       用户Mapper
     * @param roleMapper       角色Mapper
     * @param permissionMapper 权限Mapper
     */
    public AuthService(UserMapper userMapper, RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    /**
     * Token名称配置
     */
    @Value("${sa-token.token-name:X-Token}")
    private String tokenName;

    /**
     * Token超时时间配置
     */
    @Value("${sa-token.timeout:1800}")
    private Long tokenTimeout;

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    private boolean matchesPassword(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }

    /**
     * 对密码进行加密
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    private String encodePassword(String password) {
        return BCrypt.hashpw(password);
    }

    /**
     * 用户登录
     * <p>
     * 验证用户名和密码，创建登录会话，返回Token和用户信息。
     * </p>
     *
     * @param request 登录请求参数
     * @return 登录响应结果
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!matchesPassword(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用，请联系管理员");
        }

        String loginType = request.getDeviceType() != null ? request.getDeviceType() : "PC";
        StpUtil.login(user.getId(), loginType);

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String token = tokenInfo.getTokenValue();

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(getClientIp());
        userMapper.updateById(user);

        List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        Set<String> permissions = new HashSet<>();
        for (Role role : roles) {
            List<Permission> rolePermissions = permissionMapper.selectPermissionsByRoleId(role.getId());
            permissions.addAll(rolePermissions.stream()
                    .map(Permission::getCode)
                    .collect(Collectors.toSet()));
        }

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .email(user.getEmail())
                .token(token)
                .expiresIn(tokenTimeout)
                .tokenType("jwt")
                .roles(roleCodes)
                .permissions(permissions)
                .build();
    }

    /**
     * 用户登出
     * <p>
     * 清除当前用户的登录会话。
     * </p>
     */
    public void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    /**
     * 获取当前用户信息
     * <p>
     * 从会话中获取当前登录用户的信息，包括角色和权限。
     * </p>
     *
     * @return 用户信息DTO
     */
    public UserInfoDTO getCurrentUserInfo() {
        if (!StpUtil.isLogin()) {
            throw new RuntimeException("用户未登录");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        List<String> roleCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        Set<String> permissions = new HashSet<>();
        for (Role role : roles) {
            List<Permission> rolePermissions = permissionMapper.selectPermissionsByRoleId(role.getId());
            permissions.addAll(rolePermissions.stream()
                    .map(Permission::getCode)
                    .collect(Collectors.toSet()));
        }

        return UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .email(user.getEmail())
                .gender(user.getGender())
                .idCard(user.getIdCard())
                .policeNo(user.getPoliceNo())
                .roles(roleCodes)
                .permissions(permissions)
                .build();
    }

    /**
     * 修改密码
     * <p>
     * 允许已登录用户修改自己的密码，需要验证旧密码。
     * </p>
     *
     * @param request 修改密码请求参数
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!StpUtil.isLogin()) {
            throw new RuntimeException("用户未登录");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);

        if (!matchesPassword(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        user.setPassword(encodePassword(request.getNewPassword()));
        userMapper.updateById(user);

        StpUtil.logout(userId);
    }

    /**
     * 获取客户端IP地址
     *
     * @return IP地址
     */
    private String getClientIp() {
        return "127.0.0.1";
    }
}
