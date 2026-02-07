package com.police.kb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.police.kb.domain.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息数据访问接口
 * <p>
 * 提供消息（Message）数据的数据库操作。
 * 继承BaseMapper接口，自动获得基础的CRUD能力。
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
