package com.police.kb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.police.kb.domain.entity.McpService;

import java.util.List;

public interface McpServiceService extends IService<McpService> {

    List<McpService> listActiveServices();

    McpService getById(Long id);

    Long add(McpService service, Long userId);

    void update(Long id, McpService service, Long userId);

    void delete(Long id, Long userId);

    void toggleStatus(Long id, Long userId);

    List<McpService> listByIds(List<Long> ids);
}
