package com.police.kb.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 用户信息数据传输对象
 * <p>
 * 用于封装用户详细信息，包含用户基本信息、角色、权限等。
 * 用于登录后返回给前端展示。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Data
@Builder
public class UserInfoDTO {

    /**
     * 用户ID
     */
    private Long id;

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
     * 性别（0-未知，1-男，2-女）
     */
    private Integer gender;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 警员编号
     */
    private String policeNo;

    /**
     * 角色编码列表
     */
    private List<String> roles;

    /**
     * 权限编码集合
     */
    private Set<String> permissions;
}
