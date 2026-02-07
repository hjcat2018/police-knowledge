package com.police.kb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.police.kb.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 * <p>
 * 继承BaseMapper，提供用户实体的数据库操作方法。
 * 同时定义了用户相关的自定义查询方法。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND status = 1")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND status = 1")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND status = 1")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据关键词搜索用户
     *
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_user WHERE status = 1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR real_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR phone LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "</script>")
    List<User> selectByKeyword(@Param("keyword") String keyword);
}
