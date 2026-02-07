package com.police.kb.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 登录响应数据传输对象
 * <p>
 * 用于封装用户登录成功后的返回数据，包含用户信息、Token、角色、权限等。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Data
@Builder
public class LoginResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 认证Token
     */
    private String token;

    /**
     * Token过期时间（秒）
     */
    private Long expiresIn;

    /**
     * Token类型（如：jwt）
     */
    private String tokenType;

    /**
     * 角色编码列表
     */
    private List<String> roles;

    /**
     * 权限编码集合
     */
    private Set<String> permissions;
}
