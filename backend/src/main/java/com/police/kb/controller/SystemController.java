package com.police.kb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统控制器
 * <p>
 * 提供系统健康检查和信息查询接口。
 * 用于监控系统运行状态和获取系统基本信息。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemController {

    /**
     * 健康检查接口
     *
     * @return 系统健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());
        result.put("service", "Police Knowledge Base System");
        result.put("version", "1.0.0");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取系统信息
     *
     * @return 系统信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", "公安专网知识库系统");
        result.put("version", "1.0.0");
        result.put("description", "基层民警智能知识库系统");
        return ResponseEntity.ok(result);
    }
}
