package com.police.kb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.kb.domain.entity.User;
import com.police.kb.mapper.UserMapper;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务类
 * <p>
 * 处理用户管理的业务逻辑，包括用户分页查询、创建、修改、密码处理等。
 * 继承ServiceImpl获得通用的CRUD能力。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final UserMapper userMapper;

    /**
     * 分页查询用户列表
     *
     * @param page    分页参数
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    public Page<User> page(Page<User> page, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                   .or()
                   .like(User::getRealName, keyword)
                   .or()
                   .like(User::getPhone, keyword);
        }
        wrapper.orderByDesc(User::getCreatedTime);
        return this.page(page, wrapper);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 创建新用户
     *
     * @param user 用户信息
     * @return 创建的用户
     */
    public User createUser(User user) {
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        userMapper.insert(user);
        return user;
    }

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新后的用户
     */
    public User updateUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        }
        userMapper.updateById(user);
        return userMapper.selectById(user.getId());
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }

    /**
     * 对密码进行加密
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    public String encodePassword(String password) {
        return BCrypt.hashpw(password);
    }
}
