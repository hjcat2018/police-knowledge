package com.police.kb.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 派出所ID
     */
    private Long stationId;

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
     * 状态（0-禁用，1-正常）
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Role> roles;

    /**
     * 角色ID列表（非数据库字段）
     */
     @TableField(exist = false)
     private List<Long> roleIds;
}
