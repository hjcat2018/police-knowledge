package com.police.kb.controller;

import com.police.kb.common.Result;
import com.police.kb.config.ChatModelsProperties;
import com.police.kb.domain.vo.ModelVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class ModelController {

  private final ChatModelsProperties chatModelsProperties;

    /**
     * 获取所有启用的模型列表
     * <p>
     * 返回配置中 enabled = true 的模型，供前端展示和选择。
     * </p>
     * 
     * @return 启用的模型列表
     */
    @GetMapping
    public Result<List<ModelVO>> getEnabledModels() {
        try {
            List<ModelVO> models = chatModelsProperties.getModels().stream()
                .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
                .map(this::toVO)
                .collect(Collectors.toList());
            
            log.info("[ModelController] 获取到 {} 个启用的模型", models.size());
            return Result.success(models);
        } catch (Exception e) {
            log.error("[ModelController] 获取模型列表失败", e);
            return Result.error("获取模型列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有模型列表（含启用状态）
     * <p>
     * 返回所有配置的模型，包括禁用的模型。
     * </p>
     * 
     * @return 所有模型列表
     */
    @GetMapping("/all")
    public Result<List<ModelVO>> getAllModels() {
        try {
            List<ModelVO> models = chatModelsProperties.getModels().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
            
            log.info("[ModelController] 获取到 {} 个模型（全部）", models.size());
            return Result.success(models);
        } catch (Exception e) {
            log.error("[ModelController] 获取全部模型列表失败", e);
            return Result.error("获取模型列表失败: " + e.getMessage());
        }
    }

    /**
     * 将配置对象转换为响应对象
     * 
     * @param config 配置对象
     * @return 响应对象
     */
    private ModelVO toVO(ChatModelsProperties.ModelConfig config) {
        ModelVO vo = new ModelVO();
        vo.setId(config.getId());
        vo.setName(config.getName());
        vo.setProvider(config.getProvider());
        vo.setDescription(config.getDescription());
        vo.setEnabled(config.getEnabled());
        vo.setTemperature(config.getTemperature());
        return vo;
    }
}
