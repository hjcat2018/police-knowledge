package com.police.kb.config;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import cn.dev33.satoken.stp.StpUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus自动填充处理器
 * <p>
 * 实现了MetaObjectHandler接口，用于在插入和更新操作时自动填充公共字段。
 * 包括创建时间、更新时间、创建人、修改人等字段的自动填充。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时自动填充字段
     * <p>
     * 自动填充：createdTime、updatedTime、createdBy、updatedBy
     * 时间字段填充为当前时间，用户ID从Sa-Token会话中获取
     * </p>
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
        
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        }
    }

    /**
     * 更新操作时自动填充字段
     * <p>
     * 自动填充：updatedTime、updatedBy
     * 时间字段填充为当前时间，用户ID从Sa-Token会话中获取
     * </p>
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
        
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
        }
    }
}
