package com.police.kb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.police.kb.common.Result;
import com.police.kb.domain.entity.User;
import com.police.kb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * <p>
 * 处理用户管理的CRUD操作，包括用户列表查询、创建、修改、删除、状态管理等。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     *
     * @param pageNum  页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @param keyword  搜索关键词
     * @return 分页用户列表
     */
    @GetMapping
    public Result<Page<User>> listUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userService.page(page, keyword);
        return Result.success(result);
    }

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 创建新用户
     *
     * @param user 用户信息
     * @return 创建的用户信息
     */
    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return Result.success(createdUser);
    }

    /**
     * 更新用户信息
     *
     * @param id  用户ID
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updatedUser = userService.updateUser(user);
        return Result.success(updatedUser);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        Map<String, String> result = new HashMap<>();
        result.put("message", "删除成功");
        return Result.success(result);
    }

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result<Map<String, String>> batchDeleteUsers(@RequestBody List<Long> ids) {
        userService.removeByIds(ids);
        Map<String, String> result = new HashMap<>();
        result.put("message", "批量删除成功");
        return Result.success(result);
    }

    /**
     * 修改用户状态
     *
     * @param id     用户ID
     * @param status 状态（0-禁用，1-启用）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<Map<String, String>> changeUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        User user = userService.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        userService.updateById(user);
        Map<String, String> result = new HashMap<>();
        result.put("message", "状态修改成功");
        return Result.success(result);
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/reset-password")
    public Result<Map<String, String>> resetPassword(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(userService.encodePassword("123456"));
        userService.updateById(user);
        Map<String, String> result = new HashMap<>();
        result.put("message", "密码已重置为123456");
        return Result.success(result);
    }
}
