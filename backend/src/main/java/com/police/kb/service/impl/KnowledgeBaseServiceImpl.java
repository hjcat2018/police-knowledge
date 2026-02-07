package com.police.kb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.kb.domain.dto.KnowledgeBaseDTO;
import com.police.kb.domain.entity.KnowledgeBase;
import com.police.kb.mapper.KnowledgeBaseMapper;
import com.police.kb.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public IPage<KnowledgeBase> pageKnowledgeBases(int pageNum, int pageSize, String keyword) {
        Page<KnowledgeBase> page = new Page<>(pageNum, pageSize);
        if (StringUtils.hasText(keyword)) {
            return knowledgeBaseMapper.selectPageByKeyword(page, keyword);
        }
        return knowledgeBaseMapper.selectPage(page, null);
    }

    @Override
    public List<KnowledgeBase> listKnowledgeBases() {
        return knowledgeBaseMapper.selectAll();
    }

    @Override
    public KnowledgeBase getKnowledgeBaseById(Long id) {
        KnowledgeBase knowledgeBase = this.getById(id);
        if (knowledgeBase == null) {
            throw new IllegalArgumentException("知识库不存在");
        }
        return knowledgeBase;
    }

    @Override
    @Transactional
    public KnowledgeBase createKnowledgeBase(KnowledgeBaseDTO dto) {
        if (dto.getCode() == null || dto.getCode().isEmpty()) {
            throw new IllegalArgumentException("知识库编码不能为空");
        }

        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getCode, dto.getCode());
        if (this.count(wrapper) > 0) {
            throw new IllegalArgumentException("知识库编码已存在");
        }

        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setName(dto.getName());
        knowledgeBase.setCode(dto.getCode());
        knowledgeBase.setDescription(dto.getDescription());
        knowledgeBase.setCover(dto.getCover());
        knowledgeBase.setCategoryId(dto.getCategoryId());
        knowledgeBase.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        knowledgeBase.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : 0);
        knowledgeBase.setViewCount(0);
        knowledgeBase.setFavoriteCount(0);
        knowledgeBase.setDocCount(0);

        this.save(knowledgeBase);
        return knowledgeBase;
    }

    @Override
    @Transactional
    public KnowledgeBase updateKnowledgeBase(Long id, KnowledgeBaseDTO dto) {
        KnowledgeBase knowledgeBase = this.getById(id);
        if (knowledgeBase == null) {
            throw new IllegalArgumentException("知识库不存在");
        }

        if (StringUtils.hasText(dto.getCode()) && !dto.getCode().equals(knowledgeBase.getCode())) {
            LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(KnowledgeBase::getCode, dto.getCode());
            if (this.count(wrapper) > 0) {
                throw new IllegalArgumentException("知识库编码已存在");
            }
        }

        if (StringUtils.hasText(dto.getName())) {
            knowledgeBase.setName(dto.getName());
        }
        if (StringUtils.hasText(dto.getCode())) {
            knowledgeBase.setCode(dto.getCode());
        }
        if (dto.getDescription() != null) {
            knowledgeBase.setDescription(dto.getDescription());
        }
        if (dto.getCover() != null) {
            knowledgeBase.setCover(dto.getCover());
        }
        if (dto.getCategoryId() != null) {
            knowledgeBase.setCategoryId(dto.getCategoryId());
        }
        if (dto.getStatus() != null) {
            knowledgeBase.setStatus(dto.getStatus());
        }
        if (dto.getIsPublic() != null) {
            knowledgeBase.setIsPublic(dto.getIsPublic());
        }

        this.updateById(knowledgeBase);
        return knowledgeBase;
    }

    @Override
    @Transactional
    public void deleteKnowledgeBase(Long id) {
        KnowledgeBase knowledgeBase = this.getById(id);
        if (knowledgeBase == null) {
            throw new IllegalArgumentException("知识库不存在");
        }

        this.removeById(id);
    }

    @Override
    @Transactional
    public void batchDeleteKnowledgeBases(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("请选择要删除的知识库");
        }
        this.removeByIds(ids);
    }

    @Override
    @Transactional
    public void changeStatus(Long id, Integer status) {
        KnowledgeBase knowledgeBase = this.getById(id);
        if (knowledgeBase == null) {
            throw new IllegalArgumentException("知识库不存在");
        }

        knowledgeBase.setStatus(status);
        this.updateById(knowledgeBase);
    }
}
