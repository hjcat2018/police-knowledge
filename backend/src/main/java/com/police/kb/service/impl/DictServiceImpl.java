package com.police.kb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.police.kb.domain.dto.DictDTO;
import com.police.kb.domain.entity.SysDict;
import com.police.kb.mapper.SysDictMapper;
import com.police.kb.service.DictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictMapper sysDictMapper;
    private final JdbcTemplate jdbcTemplate;

    // 允许的字典类型
    private static final List<String> ALLOWED_KINDS = Arrays.asList(
        "origin_scope", "origin_department", "kb_category"
    );

    // 预置模板数据
    private static final List<DictDTO.ExportDTO> TEMPLATE_DATA = Arrays.asList(
        new DictDTO.ExportDTO("origin_scope", "national", "国家级", null, null, null, 1, 1, 0, 1),
        new DictDTO.ExportDTO("origin_scope", "ministerial", "部级", null, null, null, 1, 2, 0, 1),
        new DictDTO.ExportDTO("origin_scope", "provincial", "省级", null, null, null, 1, 3, 0, 1),
        new DictDTO.ExportDTO("origin_scope", "municipal", "市级", null, null, null, 1, 4, 0, 1),
        new DictDTO.ExportDTO("origin_scope", "county", "县级", null, null, null, 1, 5, 0, 1),
        new DictDTO.ExportDTO("origin_scope", "unit", "单位级", null, null, null, 1, 6, 0, 1),
        new DictDTO.ExportDTO("origin_department", "zazhi", "治安管理支队", null, null, null, 1, 1, 0, 1),
        new DictDTO.ExportDTO("origin_department", "xingzheng", "刑侦支队", null, null, null, 1, 2, 0, 1),
        new DictDTO.ExportDTO("origin_department", "zazhi_1", "一大队", null, null, "zazhi", 2, 1, 0, 1),
        new DictDTO.ExportDTO("origin_department", "zazhi_2", "二大队", null, null, "zazhi", 2, 2, 0, 1)
    );

    // ==================== 字典类型管理 ====================

    @Override
    public List<SysDict> getDictTypes() {
        log.info("获取字典类型列表");
        try {
            // 获取所有字典数据用于构建树
            return sysDictMapper.selectList(
                new LambdaQueryWrapper<SysDict>()
                    .eq(SysDict::getDeleted, 0)
                    .orderByAsc(SysDict::getKind)
                    .orderByAsc(SysDict::getLevel)
                    .orderByAsc(SysDict::getSort)
            );
        } catch (Exception e) {
            log.error("获取字典类型列表失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public SysDict getDictTypeById(Long id) {
        SysDict dict = sysDictMapper.selectById(id);
        if (dict == null || dict.getDeleted() == 1) {
            throw new IllegalArgumentException("字典类型不存在");
        }
        return dict;
    }

    @Override
    @Transactional
    public void createDictType(DictDTO.TypeDTO dto) {
        log.info("新增字典类型: kind={}, detail={}", dto.getKind(), dto.getDetail());

        // 校验kind是否允许
        if (!ALLOWED_KINDS.contains(dto.getKind())) {
            throw new IllegalArgumentException("不允许的字典类型: " + dto.getKind());
        }

        // 校验kind是否已存在
        long count = sysDictMapper.selectCount(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getKind, dto.getKind())
                .eq(SysDict::getLevel, 1)
                .eq(SysDict::getDeleted, 0)
        );
        if (count > 0) {
            throw new IllegalArgumentException("字典类型编码已存在: " + dto.getKind());
        }

        SysDict dict = new SysDict();
        dict.setKind(dto.getKind());
        dict.setCode(dto.getKind());
        dict.setDetail(dto.getDetail());
        dict.setAlias(dto.getAlias());
        dict.setDescription(dto.getDescription());
        dict.setIcon(dto.getIcon());
        dict.setColor(dto.getColor());
        dict.setLevel(1);
        dict.setSort(dto.getSort() != null ? dto.getSort() : 1);
        dict.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        dict.setIsSystem(1);
        dict.setIsPublic(1);
        dict.setDeleted(0);
        dict.setCreatedTime(LocalDateTime.now());
        dict.setUpdatedTime(LocalDateTime.now());

        sysDictMapper.insert(dict);
    }

    @Override
    @Transactional
    public void updateDictType(Long id, DictDTO.TypeDTO dto) {
        log.info("更新字典类型: id={}, detail={}", id, dto.getDetail());

        SysDict dict = getDictTypeById(id);

        dict.setDetail(dto.getDetail());
        dict.setSort(dto.getSort() != null ? dto.getSort() : dict.getSort());
        dict.setUpdatedTime(LocalDateTime.now());

        sysDictMapper.updateById(dict);
    }

    @Override
    @Transactional
    public void deleteDictType(Long id) {
        log.info("删除字典类型: id={}", id);

        SysDict dict = getDictTypeById(id);

        // 级联删除该类型下所有数据
        deleteWithChildren(id);

        log.info("字典类型删除完成: id={}", id);
    }

    // ==================== 字典数据管理 ====================

    @Override
    public IPage<SysDict> pageDicts(int pageNum, int pageSize, String kind, String keyword) {
        log.info("分页查询字典数据: kind={}, keyword={}", kind, keyword);

        Page<SysDict> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<SysDict>()
            .eq(SysDict::getDeleted, 0);

        if (StringUtils.hasText(kind)) {
            wrapper.eq(SysDict::getKind, kind);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(SysDict::getCode, keyword)
                .or()
                .like(SysDict::getDetail, keyword)
            );
        }

        wrapper.orderByAsc(SysDict::getKind)
               .orderByAsc(SysDict::getLevel)
               .orderByAsc(SysDict::getSort);

        return sysDictMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysDict> getDictTree(String kind) {
        log.info("获取字典树: kind={}", kind);

        List<SysDict> allDicts = sysDictMapper.selectList(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getKind, kind)
                .eq(SysDict::getDeleted, 0)
                .eq(SysDict::getStatus, 1)
                .orderByAsc(SysDict::getLevel)
                .orderByAsc(SysDict::getSort)
        );

        // 过滤掉一级根节点（如"知识库字典"），将其子节点提升为根节点
        Set<Long> filteredParentIds = new HashSet<>();
        List<SysDict> filteredDicts = new ArrayList<>();
        
        for (SysDict dict : allDicts) {
            // 过滤掉 level=1 且 parent_id=0 的根节点（如"知识库字典"）
            if (dict.getLevel() == 1 && (dict.getParentId() == null || dict.getParentId() == 0)) {
                filteredParentIds.add(dict.getId());
            } else {
                filteredDicts.add(dict);
            }
        }
        
        // 构建树时，将被过滤节点的子节点视为根节点
        return buildTreeWithPromotedRoots(filteredDicts, filteredParentIds);
    }
    
    /**
     * 构建树形结构，被过滤节点的子节点提升为根节点
     */
    private List<SysDict> buildTreeWithPromotedRoots(List<SysDict> allDicts, Set<Long> filteredParentIds) {
        Map<Long, SysDict> dictMap = allDicts.stream()
            .collect(Collectors.toMap(SysDict::getId, d -> d));
        
        List<SysDict> roots = new ArrayList<>();
        
        for (SysDict dict : allDicts) {
            // 如果父节点被过滤了，或者没有父节点，则作为根节点
            if (dict.getParentId() == null || dict.getParentId() == 0 || filteredParentIds.contains(dict.getParentId())) {
                roots.add(dict);
            } else {
                SysDict parent = dictMap.get(dict.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dict);
                }
            }
        }
        
        return roots;
    }

    @Override
    public List<SysDict> getParentOptions(String kind) {
        // 只返回一级字典作为父级选项
        return sysDictMapper.selectList(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getKind, kind)
                .eq(SysDict::getLevel, 1)
                .eq(SysDict::getDeleted, 0)
                .eq(SysDict::getStatus, 1)
                .orderByAsc(SysDict::getSort)
        );
    }

    @Override
    public List<SysDict> getChildren(String kind, String parentCode) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<SysDict>()
            .eq(SysDict::getKind, kind)
            .eq(SysDict::getDeleted, 0);
        
        if (StringUtils.hasText(parentCode)) {
            wrapper.eq(SysDict::getParentCode, parentCode);
        } else {
            wrapper.isNull(SysDict::getParentCode);
        }
        
        wrapper.orderByAsc(SysDict::getSort);
        
        return sysDictMapper.selectList(wrapper);
    }

    @Override
    public List<SysDict> getChildrenById(String kind, Long parentId) {
        log.info("getChildrenById called: kind={}, parentId={}", kind, parentId);
        
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<SysDict>()
            .eq(SysDict::getKind, kind)
            .eq(SysDict::getDeleted, 0);
        
        if (parentId != null && parentId > 0) {
            wrapper.eq(SysDict::getParentId, parentId);
        } else {
            wrapper.eq(SysDict::getParentId, 0);
        }
        
        wrapper.orderByAsc(SysDict::getSort);
        
        List<SysDict> result = sysDictMapper.selectList(wrapper);
        log.info("getChildrenById result: count={}", result.size());
        return result;
    }

    @Override
    public SysDict getDictById(Long id) {
        SysDict dict = sysDictMapper.selectById(id);
        if (dict == null || dict.getDeleted() == 1) {
            throw new IllegalArgumentException("字典不存在");
        }
        return dict;
    }

    @Override
    @Transactional
    public void createDict(DictDTO.DataDTO dto) {
        log.info("新增字典: kind={}, code={}, detail={}", dto.getKind(), dto.getCode(), dto.getDetail());

        // 校验kind是否允许
        if (!ALLOWED_KINDS.contains(dto.getKind())) {
            throw new IllegalArgumentException("不允许的字典类型: " + dto.getKind());
        }

        // 校验层级
        if (dto.getLevel() == 2 && !StringUtils.hasText(dto.getParentCode())) {
            throw new IllegalArgumentException("二级字典必须选择父级");
        }

        // 校验编码唯一性
        if (existsCode(dto.getKind(), dto.getCode())) {
            throw new IllegalArgumentException("字典编码已存在: " + dto.getCode());
        }

        // 校验父级是否存在
        if (dto.getLevel() == 2) {
            SysDict parent = sysDictMapper.selectOne(
                new LambdaQueryWrapper<SysDict>()
                    .eq(SysDict::getKind, dto.getKind())
                    .eq(SysDict::getCode, dto.getParentCode())
                    .eq(SysDict::getDeleted, 0)
            );
            if (parent == null) {
                throw new IllegalArgumentException("父级字典不存在: " + dto.getParentCode());
            }
        }

        SysDict dict = new SysDict();
        dict.setKind(dto.getKind());
        dict.setCode(dto.getCode());
        dict.setDetail(dto.getDetail());
        dict.setAlias(dto.getAlias());
        dict.setDescription(dto.getDescription());
        dict.setIcon(dto.getIcon());
        dict.setColor(dto.getColor());
        dict.setExtConfig(dto.getExtConfig());
        dict.setLevel(dto.getLevel());
        dict.setParentId(dto.getParentId());
        dict.setParentCode(dto.getParentCode());
        dict.setSort(dto.getSort() != null ? dto.getSort() : 1);
        dict.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        dict.setLeaf(dto.getLeaf() != null ? dto.getLeaf() : 0);
        dict.setIsSystem(dto.getIsSystem() != null ? dto.getIsSystem() : 0);
        dict.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : 1);
        dict.setRemark(dto.getRemark());
        dict.setDeleted(0);
        dict.setCreatedTime(LocalDateTime.now());
        dict.setUpdatedTime(LocalDateTime.now());

        sysDictMapper.insert(dict);
    }

    @Override
    @Transactional
    public void updateDict(Long id, DictDTO.DataDTO dto) {
        log.info("更新字典: id={}, detail={}", id, dto.getDetail());

        SysDict dict = getDictById(id);

        // 如果修改了编码，校验唯一性
        if (!dict.getCode().equals(dto.getCode())) {
            if (existsCode(dto.getKind(), dto.getCode())) {
                throw new IllegalArgumentException("字典编码已存在: " + dto.getCode());
            }
        }

        dict.setDetail(dto.getDetail());
        dict.setAlias(dto.getAlias());
        dict.setDescription(dto.getDescription());
        dict.setIcon(dto.getIcon());
        dict.setColor(dto.getColor());
        dict.setExtConfig(dto.getExtConfig());
        dict.setLevel(dto.getLevel());
        dict.setParentCode(dto.getParentCode());
        dict.setSort(dto.getSort() != null ? dto.getSort() : dict.getSort());
        dict.setStatus(dto.getStatus() != null ? dto.getStatus() : dict.getStatus());
        dict.setLeaf(dto.getLeaf() != null ? dto.getLeaf() : dict.getLeaf());
        dict.setIsSystem(dto.getIsSystem() != null ? dto.getIsSystem() : dict.getIsSystem());
        dict.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : dict.getIsPublic());
        dict.setRemark(dto.getRemark());
        dict.setUpdatedTime(LocalDateTime.now());

        sysDictMapper.updateById(dict);
    }

    @Override
    @Transactional
    public void deleteDict(Long id) {
        log.info("删除字典: id={}", id);

        SysDict dict = getDictById(id);

        // 级联删除子节点
        deleteWithChildren(id);

        log.info("字典删除完成: id={}", id);
    }

    // ==================== 导入导出 ====================

    @Override
    @Transactional
    public DictDTO.ImportResult importDict(String kind, MultipartFile file) {
        log.info("导入字典数据: kind={}, filename={}", kind, file.getOriginalFilename());

        if (!ALLOWED_KINDS.contains(kind)) {
            throw new IllegalArgumentException("不允许的字典类型: " + kind);
        }

        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int skipCount = 0;

        try {
            // 解析Excel（简化处理，实际应使用EasyExcel）
            List<DictDTO.ImportDTO> dataList = parseExcel(file);

            for (int i = 0; i < dataList.size(); i++) {
                DictDTO.ImportDTO data = dataList.get(i);
                try {
                    // 校验kind
                    if (!ALLOWED_KINDS.contains(data.getKind())) {
                        errors.add(String.format("第%d行：字典类型[%s]不允许", i + 2, data.getKind()));
                        skipCount++;
                        continue;
                    }

                    // 校验必填字段
                    if (!StringUtils.hasText(data.getCode())) {
                        errors.add(String.format("第%d行：编码不能为空", i + 2));
                        skipCount++;
                        continue;
                    }
                    if (!StringUtils.hasText(data.getDetail())) {
                        errors.add(String.format("第%d行：名称不能为空", i + 2));
                        skipCount++;
                        continue;
                    }
                    if (data.getLevel() == null) {
                        errors.add(String.format("第%d行：层级不能为空", i + 2));
                        skipCount++;
                        continue;
                    }

                    // 校验层级
                    if (data.getLevel() < 1 || data.getLevel() > 2) {
                        errors.add(String.format("第%d行：层级必须为1或2", i + 2));
                        skipCount++;
                        continue;
                    }

                    // 校验二级字典的父级
                    if (data.getLevel() == 2) {
                        if (!StringUtils.hasText(data.getParentCode())) {
                            errors.add(String.format("第%d行：二级字典必须填写父级编码", i + 2));
                            skipCount++;
                            continue;
                        }
                        SysDict parent = sysDictMapper.selectOne(
                            new LambdaQueryWrapper<SysDict>()
                                .eq(SysDict::getKind, data.getKind())
                                .eq(SysDict::getCode, data.getParentCode())
                                .eq(SysDict::getDeleted, 0)
                        );
                        if (parent == null) {
                            errors.add(String.format("第%d行：父级编码[%s]不存在", i + 2, data.getParentCode()));
                            skipCount++;
                            continue;
                        }
                    }

                    // 校验编码唯一性
                    if (existsCode(data.getKind(), data.getCode())) {
                        errors.add(String.format("第%d行：编码[%s]已存在", i + 2, data.getCode()));
                        skipCount++;
                        continue;
                    }

                    // 插入数据
                    SysDict dict = new SysDict();
                    dict.setKind(data.getKind());
                    dict.setCode(data.getCode());
                    dict.setDetail(data.getDetail());
                    dict.setAlias(data.getAlias());
                    dict.setDescription(data.getDescription());
                    dict.setLevel(data.getLevel());
                    dict.setParentCode(data.getParentCode());
                    dict.setSort(data.getSort() != null ? data.getSort() : 1);
                    dict.setStatus(1);
                    dict.setLeaf(data.getLeaf() != null ? data.getLeaf() : 0);
                    dict.setDeleted(0);
                    dict.setCreatedTime(LocalDateTime.now());
                    dict.setUpdatedTime(LocalDateTime.now());

                    sysDictMapper.insert(dict);
                    successCount++;

                } catch (Exception e) {
                    errors.add(String.format("第%d行：%s", i + 2, e.getMessage()));
                    skipCount++;
                }
            }

        } catch (Exception e) {
            log.error("解析Excel失败: {}", e.getMessage(), e);
            throw new IllegalArgumentException("解析Excel失败: " + e.getMessage());
        }

        log.info("导入完成: success={}, skip={}", successCount, skipCount);
        return new DictDTO.ImportResult(successCount, skipCount, errors);
    }

    @Override
    public List<DictDTO.ExportDTO> exportDict(String kind, String keyword) {
        log.info("导出字典数据: kind={}, keyword={}", kind, keyword);

        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<SysDict>()
            .eq(SysDict::getDeleted, 0);

        if (StringUtils.hasText(kind)) {
            wrapper.eq(SysDict::getKind, kind);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(SysDict::getCode, keyword)
                .or()
                .like(SysDict::getDetail, keyword)
            );
        }

        wrapper.orderByAsc(SysDict::getKind)
               .orderByAsc(SysDict::getLevel)
               .orderByAsc(SysDict::getSort);

        List<SysDict> dataList = sysDictMapper.selectList(wrapper);

        return dataList.stream()
            .map(d -> new DictDTO.ExportDTO(
                d.getKind(), d.getCode(), d.getDetail(),
                d.getAlias(), d.getDescription(), d.getParentCode(), d.getLevel(), d.getSort(), d.getLeaf(), d.getStatus()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public List<DictDTO.ExportDTO> getImportTemplate() {
        return TEMPLATE_DATA;
    }

    @Override
    public List<SysDict> getKbCategoryTree(String kind, String parentCode, Integer status) {
        log.info("获取知识库分类树: kind={}, parentCode={}, status={}", kind, parentCode, status);

        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<SysDict>()
            .eq(SysDict::getKind, kind)
            .eq(SysDict::getDeleted, 0);

        if (StringUtils.hasText(parentCode)) {
            wrapper.eq(SysDict::getParentCode, parentCode);
        }

        if (status != null) {
            wrapper.eq(SysDict::getStatus, status);
        }

        wrapper.orderByAsc(SysDict::getSort);

        List<SysDict> children = sysDictMapper.selectList(wrapper);
        log.info("查询到 {} 个子节点", children.size());

        // 构建子节点的树形结构
        return buildChildrenTree(children);
    }

    /**
     * 构建子节点的树形结构（不包含父节点）
     */
    private List<SysDict> buildChildrenTree(List<SysDict> children) {
        if (children.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, SysDict> childrenMap = children.stream()
            .collect(Collectors.toMap(SysDict::getId, d -> d));

        List<SysDict> result = new ArrayList<>();

        for (SysDict child : children) {
            if (child.getParentId() == null || child.getParentId() == 0 ||
                !childrenMap.containsKey(child.getParentId())) {
                result.add(child);
            }
        }

        for (SysDict child : children) {
            if (child.getParentId() != null && child.getParentId() > 0) {
                SysDict parent = childrenMap.get(child.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(child);
                }
            }
        }

        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 检查编码是否存在
     */
    private boolean existsCode(String kind, String code) {
        return sysDictMapper.selectCount(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getKind, kind)
                .eq(SysDict::getCode, code)
                .eq(SysDict::getDeleted, 0)
        ) > 0;
    }

    /**
     * 物理删除节点及其子节点（使用原生SQL绕过@TableLogic）
     */
    private void deleteWithChildren(Long id) {
        // 查询所有子节点
        List<SysDict> children = sysDictMapper.selectList(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getParentId, id)
        );

        // 递归删除子节点
        for (SysDict child : children) {
            deleteWithChildren(child.getId());
        }

        // 物理删除当前节点（使用原生SQL）
        jdbcTemplate.update("DELETE FROM sys_dict WHERE id = ?", id);
        log.info("物理删除字典: id={}", id);
    }

    /**
     * 构建树形结构
     */
    private List<SysDict> buildTree(List<SysDict> allDicts) {
        Map<Long, SysDict> dictMap = allDicts.stream()
            .collect(Collectors.toMap(SysDict::getId, d -> d));

        List<SysDict> roots = new ArrayList<>();

        for (SysDict dict : allDicts) {
            if (dict.getParentId() == null || dict.getParentId() == 0) {
                roots.add(dict);
            } else {
                SysDict parent = dictMap.get(dict.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dict);
                }
            }
        }

        return roots;
    }

    /**
     * 解析Excel（简化实现，实际应使用EasyExcel）
     */
    private List<DictDTO.ImportDTO> parseExcel(MultipartFile file) {
        // 这里简化处理，实际应使用EasyExcel解析
        // 暂时返回空列表，由调用方处理
        return List.of();
    }
}
