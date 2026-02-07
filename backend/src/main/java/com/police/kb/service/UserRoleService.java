package com.police.kb.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.kb.domain.entity.RolePermission;
import com.police.kb.domain.entity.UserRole;
import com.police.kb.mapper.RolePermissionMapper;
import com.police.kb.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户角色服务
 * <p>
 * 提供用户与角色关联、角色与权限关联的管理功能。
 * 实现基于RBAC的角色权限控制。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>为用户分配/移除角色</li>
 *   <li>查询用户的角色列表</li>
 *   <li>为角色分配/移除权限</li>
 *   <li>查询角色的权限列表</li>
 * </ul>
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Service
@RequiredArgsConstructor
public class UserRoleService extends ServiceImpl<UserRoleMapper, UserRole> {

    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 为用户分配角色
     * <p>
     * 先清除用户的所有现有角色，再批量分配新角色。
     * </p>
     *
     * @param userId 用户ID
     * @param roleIds 要分配的角色ID列表
     */
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(null);

        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    /**
     * 获取用户的角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    public List<Long> getUserRoleIds(Long userId) {
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }

    /**
     * 获取角色的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    public List<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    /**
     * 批量分配角色权限
     * <p>
     * 先清除角色的所有现有权限，再批量分配新权限。
     * </p>
     *
     * @param roleId 角色ID
     * @param permissionIds 要分配的权限ID列表
     */
    @Transactional
    public void assignRolePermissions(Long roleId, List<Long> permissionIds) {
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissionMapper.insert(rolePermission);
        }
    }
}
