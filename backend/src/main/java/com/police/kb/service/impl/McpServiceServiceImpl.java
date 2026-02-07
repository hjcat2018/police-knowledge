package com.police.kb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.kb.domain.entity.McpService;
import com.police.kb.mapper.McpServiceMapper;
import com.police.kb.service.McpServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpServiceServiceImpl extends ServiceImpl<McpServiceMapper, McpService>
        implements McpServiceService {

    private final McpServiceMapper mcpServiceMapper;

    @Override
    public List<McpService> listActiveServices() {
        LambdaQueryWrapper<McpService> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(McpService::getEnabled, 1)
               .eq(McpService::getDeleted, 0)
               .orderByDesc(McpService::getSort);
        return mcpServiceMapper.selectList(wrapper);
    }

    @Override
    public McpService getById(Long id) {
        McpService service = mcpServiceMapper.selectById(id);
        if (service == null || service.getDeleted() == 1) {
            throw new IllegalArgumentException("MCP服务不存在");
        }
        return service;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(McpService service, Long userId) {
        log.info("新增MCP服务 - 名称: {}, 用户ID: {}", service.getName(), userId);

        service.setCreatedBy(userId);
        service.setCreatedTime(LocalDateTime.now());
        service.setDeleted(0);
        if (service.getEnabled() == null) {
            service.setEnabled(1);
        }
        if (service.getSort() == null) {
            service.setSort(0);
        }
        if (service.getConfigAuthType() == null) {
            service.setConfigAuthType("api_key");
        }
        if (service.getConfigTimeout() == null) {
            service.setConfigTimeout(60);
        }
        if (service.getConfigMethod() == null) {
            service.setConfigMethod("POST");
        }

        mcpServiceMapper.insert(service);
        return service.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, McpService service, Long userId) {
        log.info("更新MCP服务 - ID: {}, 用户ID: {}", id, userId);

        McpService existing = getById(id);

        existing.setName(service.getName());
        existing.setCode(service.getCode());
        existing.setDescription(service.getDescription());
        existing.setEnabled(service.getEnabled());
        existing.setConfigUrl(service.getConfigUrl());
        existing.setConfigAuthType(service.getConfigAuthType());
        // 只在凭证不为空时更新
        if (service.getConfigCredentials() != null && !service.getConfigCredentials().isEmpty()) {
            existing.setConfigCredentials(service.getConfigCredentials());
        }
        existing.setConfigTimeout(service.getConfigTimeout());
        existing.setConfigMethod(service.getConfigMethod());
        existing.setSort(service.getSort());
        existing.setCommand(service.getCommand());
        existing.setArgs(service.getArgs());
        existing.setEnv(service.getEnv());
        existing.setUpdatedBy(userId);
        existing.setUpdatedTime(LocalDateTime.now());

        mcpServiceMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        log.info("删除MCP服务 - ID: {}, 用户ID: {}", id, userId);

        McpService existing = getById(id);

        existing.setDeleted(1);
        existing.setUpdatedBy(userId);
        existing.setUpdatedTime(LocalDateTime.now());

        mcpServiceMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id, Long userId) {
        log.info("切换MCP服务状态 - ID: {}, 用户ID: {}", id, userId);

        McpService existing = getById(id);
        existing.setEnabled(existing.getEnabled() == 1 ? 0 : 1);
        existing.setUpdatedBy(userId);
        existing.setUpdatedTime(LocalDateTime.now());

        mcpServiceMapper.updateById(existing);
    }

    @Override
    public List<McpService> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<McpService> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(McpService::getId, ids)
               .eq(McpService::getEnabled, 1)
               .eq(McpService::getDeleted, 0);
        return mcpServiceMapper.selectList(wrapper);
    }
}
