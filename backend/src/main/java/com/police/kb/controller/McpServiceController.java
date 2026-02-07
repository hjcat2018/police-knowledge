package com.police.kb.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.police.kb.common.Result;
import com.police.kb.domain.entity.McpService;
import com.police.kb.service.McpServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/mcp/services")
@RequiredArgsConstructor
@Tag(name = "MCP服务管理", description = "MCP服务相关API")
public class McpServiceController {

    private final McpServiceService mcpServiceService;

    @Operation(summary = "获取所有启用的MCP服务列表")
    @GetMapping
    public Result<List<McpService>> listActiveServices() {
        List<McpService> services = mcpServiceService.listActiveServices();
        return Result.success(services);
    }

    @Operation(summary = "获取MCP服务详情")
    @GetMapping("/{id}")
    public Result<McpService> getById(@PathVariable Long id) {
        McpService service = mcpServiceService.getById(id);
        return Result.success(service);
    }

    @Operation(summary = "新增MCP服务")
    @PostMapping
    public Result<Long> add(@RequestBody @Valid CreateMcpServiceRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        McpService service = new McpService();
        service.setName(request.getName());
        service.setCode(request.getCode());
        service.setDescription(request.getDescription());
        service.setEnabled(request.getEnabled() != null ? request.getEnabled() : 1);
        service.setConfigUrl(request.getConfigUrl());
        service.setConfigAuthType(request.getConfigAuthType());
        service.setConfigCredentials(request.getConfigCredentials());
        service.setConfigTimeout(request.getConfigTimeout());
        service.setConfigMethod(request.getConfigMethod());
        service.setSort(request.getSort());
        service.setCommand(request.getCommand());
        service.setArgs(request.getArgs());
        service.setEnv(request.getEnv());

        Long id = mcpServiceService.add(service, userId);
        return Result.success(id);
    }

    @Operation(summary = "更新MCP服务")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid CreateMcpServiceRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        McpService service = new McpService();
        service.setName(request.getName());
        service.setCode(request.getCode());
        service.setDescription(request.getDescription());
        service.setEnabled(request.getEnabled());
        service.setConfigUrl(request.getConfigUrl());
        service.setConfigAuthType(request.getConfigAuthType());
        service.setConfigCredentials(request.getConfigCredentials());
        service.setConfigTimeout(request.getConfigTimeout());
        service.setConfigMethod(request.getConfigMethod());
        service.setSort(request.getSort());
        service.setCommand(request.getCommand());
        service.setArgs(request.getArgs());
        service.setEnv(request.getEnv());

        mcpServiceService.update(id, service, userId);
        return Result.success();
    }

    @Operation(summary = "删除MCP服务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        mcpServiceService.delete(id, userId);
        return Result.success();
    }

    @Operation(summary = "切换MCP服务状态")
    @PutMapping("/{id}/toggle")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        mcpServiceService.toggleStatus(id, userId);
        return Result.success();
    }

    @Data
    public static class CreateMcpServiceRequest {
        @NotBlank(message = "服务名称不能为空")
        private String name;

        @NotBlank(message = "服务编码不能为空")
        private String code;

        private String description;

        private Integer enabled;

        private String configUrl;

        private String configAuthType;

        private String configCredentials;

        private Integer configTimeout;

        private String configMethod;

        private Integer sort;

        private String command;

        private String args;

        private String env;
    }
}
