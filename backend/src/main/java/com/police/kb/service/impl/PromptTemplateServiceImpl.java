package com.police.kb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.kb.domain.entity.PromptTemplate;
import com.police.kb.mapper.PromptTemplateMapper;
import com.police.kb.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateServiceImpl extends ServiceImpl<PromptTemplateMapper, PromptTemplate>
        implements PromptTemplateService {

    private final PromptTemplateMapper templateMapper;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    @Override
    public List<PromptTemplate> listTemplates(String type, Long userId) {
        log.debug("查询模板列表 - 类型: {}, 用户ID: {}", type, userId);

        switch (type.toLowerCase()) {
            case "system":
                return templateMapper.selectSystemTemplates();
            case "shared":
                return templateMapper.selectSharedTemplates();
            case "my":
            default:
                return templateMapper.selectMyTemplates(userId);
        }
    }

    @Override
    public PromptTemplate getById(Long id) {
        PromptTemplate template = templateMapper.selectById(id);
        if (template == null || template.getDeleted() == 1) {
            throw new IllegalArgumentException("模板不存在");
        }
        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PromptTemplate template, Long userId) {
        log.info("创建模板 - 名称: {}, 用户ID: {}", template.getName(), userId);

        template.setCreatedBy(userId);
        template.setCreatedTime(LocalDateTime.now());
        template.setDeleted(0);

        if (template.getSort() == null) {
            template.setSort(0);
        }
        if (template.getIsDefault() == null) {
            template.setIsDefault(0);
        }
        if (template.getIsSystem() == null) {
            template.setIsSystem(0);
        }

        templateMapper.insert(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PromptTemplate template, Long userId) {
        log.info("更新模板 - ID: {}, 用户ID: {}", id, userId);

        PromptTemplate existing = getById(id);

        if (existing.getIsSystem() == 1) {
            throw new IllegalArgumentException("系统模板不可修改");
        }

        if (!existing.getCreatedBy().equals(userId)) {
            throw new IllegalArgumentException("只能修改自己创建的模板");
        }

        template.setId(id);
        template.setUpdatedBy(userId);
        template.setUpdatedTime(LocalDateTime.now());

        templateMapper.updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        log.info("删除模板 - ID: {}, 用户ID: {}", id, userId);

        PromptTemplate existing = getById(id);

        if (existing.getIsSystem() == 1) {
            throw new IllegalArgumentException("系统模板不可删除");
        }

        if (!existing.getCreatedBy().equals(userId)) {
            throw new IllegalArgumentException("只能删除自己创建的模板");
        }

        existing.setDeleted(1);
        existing.setUpdatedBy(userId);
        existing.setUpdatedTime(LocalDateTime.now());
        templateMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id, Long userId) {
        log.info("设置默认模板 - ID: {}, 用户ID: {}", id, userId);

        PromptTemplate template = getById(id);

        if (template.getIsSystem() == 1 && template.getCreatedBy() != userId) {
            throw new IllegalArgumentException("只能设置自己创建的模板为默认");
        }

        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getCreatedBy, userId)
               .eq(PromptTemplate::getIsDefault, 1)
               .eq(PromptTemplate::getDeleted, 0);

        PromptTemplate oldDefault = templateMapper.selectOne(wrapper);
        if (oldDefault != null && !oldDefault.getId().equals(id)) {
            oldDefault.setIsDefault(0);
            oldDefault.setUpdatedTime(LocalDateTime.now());
            oldDefault.setUpdatedBy(userId);
            templateMapper.updateById(oldDefault);
        }

        template.setIsDefault(1);
        template.setUpdatedTime(LocalDateTime.now());
        template.setUpdatedBy(userId);
        templateMapper.updateById(template);
    }

    @Override
    public String render(String template, Map<String, Object> context) {
        if (template == null || context == null) {
            return template;
        }

        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = context.get(key);
            String replacement = value != null ? value.toString() : "";
            result = result.replace("{{" + key + "}}", replacement);
        }

        return result;
    }
}
