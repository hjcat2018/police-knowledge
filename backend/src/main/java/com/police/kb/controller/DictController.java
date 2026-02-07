package com.police.kb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.police.kb.common.Result;
import com.police.kb.domain.dto.DictDTO;
import com.police.kb.domain.entity.SysDict;
import com.police.kb.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 字典控制器
 * <p>
 * 提供字典数据的查询和管理接口。
 * </p>
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/api/v1/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // ==================== 字典类型管理 ====================

    /**
     * 获取字典类型列表
     *
     * @return 字典类型列表
     */
    @Operation(summary = "获取字典类型列表")
    @GetMapping("/types")
    public Result<List<SysDict>> getDictTypes() {
        return Result.success(dictService.getDictTypes());
    }

    /**
     * 获取字典类型详情
     *
     * @param id 字典类型ID
     * @return 字典类型
     */
    @Operation(summary = "获取字典类型详情")
    @GetMapping("/types/{id}")
    public Result<SysDict> getDictTypeById(@PathVariable Long id) {
        return Result.success(dictService.getDictTypeById(id));
    }

    /**
     * 新增字典类型
     *
     * @param dto 字典类型信息
     * @return 操作结果
     */
    @Operation(summary = "新增字典类型")
    @PostMapping("/types")
    public Result<Void> createDictType(@Valid @RequestBody DictDTO.TypeDTO dto) {
        dictService.createDictType(dto);
        return Result.success();
    }

    /**
     * 更新字典类型
     *
     * @param id  字典类型ID
     * @param dto 字典类型信息
     * @return 操作结果
     */
    @Operation(summary = "更新字典类型")
    @PutMapping("/types/{id}")
    public Result<Void> updateDictType(
            @PathVariable Long id,
            @Valid @RequestBody DictDTO.TypeDTO dto) {
        dictService.updateDictType(id, dto);
        return Result.success();
    }

    /**
     * 删除字典类型（级联删除）
     *
     * @param id 字典类型ID
     * @return 操作结果
     */
    @Operation(summary = "删除字典类型")
    @DeleteMapping("/types/{id}")
    public Result<Void> deleteDictType(@PathVariable Long id) {
        dictService.deleteDictType(id);
        return Result.success();
    }

    // ==================== 字典数据管理 ====================

    /**
     * 分页查询字典数据
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param kind     字典类型
     * @param keyword  搜索关键词
     * @return 分页结果
     */
    @Operation(summary = "分页查询字典数据")
    @GetMapping
    public Result<IPage<SysDict>> pageDicts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String keyword) {
        return Result.success(dictService.pageDicts(pageNum, pageSize, kind, keyword));
    }

    /**
     * 获取字典树
     *
     * @param kind 字典类型
     * @return 字典树
     */
    @Operation(summary = "获取字典树")
    @GetMapping("/tree")
    public Result<List<SysDict>> getDictTree(@RequestParam String kind) {
        return Result.success(dictService.getDictTree(kind));
    }

    /**
     * 获取父级选项（下拉用）
     *
     * @param kind 字典类型
     * @return 一级字典列表
     */
    @Operation(summary = "获取父级选项")
    @GetMapping("/options")
    public Result<List<SysDict>> getParentOptions(@RequestParam String kind) {
        return Result.success(dictService.getParentOptions(kind));
    }

    /**
     * 获取知识库分类列表
     *
     * @param parentCode 父节点编码（默认20000000）
     * @param status     状态过滤
     * @return 知识库分类列表
     */
    @Operation(summary = "获取知识库分类列表")
    @GetMapping("/kb-category")
    public Result<List<SysDict>> getKbCategoryList(
            @RequestParam(required = false, defaultValue = "20000000") String parentCode,
            @RequestParam(required = false) Integer status) {
        return Result.success(dictService.getKbCategoryTree("kb_category", parentCode, status));
    }

    /**
     * 获取知识库分类完整树
     *
     * @param kind   字典类型
     * @param status 状态过滤
     * @return 知识库分类完整树
     */
    @Operation(summary = "获取知识库分类完整树")
    @GetMapping("/kb-category/tree")
    public Result<List<SysDict>> getKbCategoryTree(
            @RequestParam(required = false, defaultValue = "kb_category") String kind,
            @RequestParam(required = false) Integer status) {
        return Result.success(dictService.getDictTree(kind));
    }

    /**
     * 获取来源层级列表
     *
     * @param parentCode 父节点编码（默认10000000）
     * @param status     状态过滤
     * @return 来源层级列表
     */
    @Operation(summary = "获取来源层级列表")
    @GetMapping("/origin-scope")
    public Result<List<SysDict>> getOriginScopeList(
            @RequestParam(required = false, defaultValue = "10000000") String parentCode,
            @RequestParam(required = false) Integer status) {
        return Result.success(dictService.getKbCategoryTree("origin_scope", parentCode, status));
    }

    /**
     * 获取来源部门列表
     *
     * @param parentCode 父节点编码（默认30000000）
     * @param status     状态过滤
     * @return 来源部门列表
     */
    @Operation(summary = "获取来源部门列表")
    @GetMapping("/origin-department")
    public Result<List<SysDict>> getOriginDepartmentList(
            @RequestParam(required = false, defaultValue = "30000000") String parentCode,
            @RequestParam(required = false) Integer status) {
        return Result.success(dictService.getKbCategoryTree("origin_department", parentCode, status));
    }

    /**
     * 获取来源部门完整树
     *
     * @param status 状态过滤
     * @return 来源部门完整树
     */
    @Operation(summary = "获取来源部门完整树")
    @GetMapping("/origin-department/tree")
    public Result<List<SysDict>> getOriginDepartmentTree(
            @RequestParam(required = false) Integer status) {
        return Result.success(dictService.getDictTree("origin_department"));
    }

    /**
     * 获取直接子节点（按父ID）
     *
     * @param kind      字典类型
     * @param parentId  父级ID（为空或0则获取一级节点）
     * @return 直接子节点列表
     */
    @Operation(summary = "获取直接子节点（按父ID）")
    @GetMapping("/children-by-id")
    public Result<List<SysDict>> getChildrenById(
            @RequestParam String kind,
            @RequestParam(defaultValue = "0") String parentId) {
        Long parentIdLong = "0".equals(parentId) ? null : Long.parseLong(parentId);
        return Result.success(dictService.getChildrenById(kind, parentIdLong));
    }

    // ==================== 导入导出 ====================

    /**
     * 导入字典数据
     *
     * @param kind 字典类型
     * @param file Excel文件
     * @return 导入结果
     */
    @Operation(summary = "导入字典数据")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<DictDTO.ImportResult> importDict(
            @RequestParam String kind,
            @RequestParam("file") MultipartFile file) {
        DictDTO.ImportResult result = dictService.importDict(kind, file);
        return Result.success(result);
    }

    /**
     * 导出字典数据
     *
     * @param kind    字典类型
     * @param keyword 搜索关键词
     * @return 导出数据
     */
    @Operation(summary = "导出字典数据")
    @GetMapping("/export")
    public Result<List<DictDTO.ExportDTO>> exportDict(
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) String keyword) {
        return Result.success(dictService.exportDict(kind, keyword));
    }

    /**
     * 获取导入模板
     *
     * @return 模板数据
     */
    @Operation(summary = "获取导入模板")
    @GetMapping("/template")
    public Result<List<DictDTO.ExportDTO>> getImportTemplate() {
        return Result.success(dictService.getImportTemplate());
    }

    /**
     * 新增字典
     *
     * @param dto 字典信息
     * @return 操作结果
     */
    @Operation(summary = "新增字典")
    @PostMapping
    public Result<Void> createDict(@Valid @RequestBody DictDTO.DataDTO dto) {
        dictService.createDict(dto);
        return Result.success();
    }

    /**
     * 更新字典
     *
     * @param id  字典ID
     * @param dto 字典信息
     * @return 操作结果
     */
    @Operation(summary = "更新字典")
    @PutMapping("/{id}")
    public Result<Void> updateDict(
            @PathVariable Long id,
            @Valid @RequestBody DictDTO.DataDTO dto) {
        dictService.updateDict(id, dto);
        return Result.success();
    }

    /**
     * 获取字典详情
     *
     * @param id 字典ID
     * @return 字典详情
     */
    @Operation(summary = "获取字典详情")
    @GetMapping("/{id}")
    public Result<SysDict> getDictById(@PathVariable Long id) {
        return Result.success(dictService.getDictById(id));
    }

    /**
     * 删除字典
     *
     * @param id 字典ID
     * @return 操作结果
     */
    @Operation(summary = "删除字典")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDict(@PathVariable Long id) {
        dictService.deleteDict(id);
        return Result.success();
    }
}
