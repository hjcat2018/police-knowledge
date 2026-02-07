package com.police.kb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.police.kb.domain.entity.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {

    @Select("SELECT * FROM prompt_template WHERE id = #{id} AND deleted = 0")
    PromptTemplate selectById(@Param("id") Long id);

    @Select("SELECT * FROM prompt_template WHERE is_system = 0 AND created_by = #{userId} AND deleted = 0 ORDER BY sort ASC")
    List<PromptTemplate> selectMyTemplates(@Param("userId") Long userId);

    @Select("SELECT * FROM prompt_template WHERE is_system = 1 AND deleted = 0 ORDER BY sort ASC")
    List<PromptTemplate> selectSystemTemplates();

    @Select("SELECT * FROM prompt_template WHERE is_system = 2 AND deleted = 0 ORDER BY sort ASC")
    List<PromptTemplate> selectSharedTemplates();
}
