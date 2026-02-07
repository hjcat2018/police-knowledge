package com.police.kb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.police.kb.domain.dto.DictDTO;
import com.police.kb.domain.entity.SysDict;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 字典服务接口
 */
public interface DictService {

    // ==================== 字典类型管理 ====================

    /**
     * 获取字典类型列表
     *
     * @return 字典类型列表
     */
    List<SysDict> getDictTypes();

    /**
     * 获取字典类型详情
     *
     * @param id 字典类型ID
     * @return 字典类型
     */
    SysDict getDictTypeById(Long id);

    /**
     * 新增字典类型
     *
     * @param dto 字典类型信息
     */
    void createDictType(DictDTO.TypeDTO dto);

    /**
     * 更新字典类型
     *
     * @param id  字典类型ID
     * @param dto 字典类型信息
     */
    void updateDictType(Long id, DictDTO.TypeDTO dto);

    /**
     * 删除字典类型（级联删除该类型下所有数据）
     *
     * @param id 字典类型ID
     */
    void deleteDictType(Long id);

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
    IPage<SysDict> pageDicts(int pageNum, int pageSize, String kind, String keyword);

    /**
     * 获取字典树
     *
     * @param kind 字典类型
     * @return 字典树
     */
    List<SysDict> getDictTree(String kind);

    /**
     * 获取父级选项（下拉用，只返回一级字典）
     *
     * @param kind 字典类型
     * @return 一级字典列表
     */
    List<SysDict> getParentOptions(String kind);

    /**
     * 获取某个节点的直接子节点
     *
     * @param kind       字典类型
     * @param parentCode 父级编码（为空则获取一级节点）
     * @return 直接子节点列表
     */
    List<SysDict> getChildren(String kind, String parentCode);

    /**
     * 获取某个节点的直接子节点（按父ID查询）
     *
     * @param kind      字典类型
     * @param parentId  父级ID（为0则获取一级节点）
     * @return 直接子节点列表
     */
    List<SysDict> getChildrenById(String kind, Long parentId);

    /**
     * 获取字典详情
     *
     * @param id 字典ID
     * @return 字典详情
     */
    SysDict getDictById(Long id);

    /**
     * 新增字典
     *
     * @param dto 字典信息
     */
    void createDict(DictDTO.DataDTO dto);

    /**
     * 更新字典
     *
     * @param id  字典ID
     * @param dto 字典信息
     */
    void updateDict(Long id, DictDTO.DataDTO dto);

    /**
     * 删除字典（级联删除子节点）
     *
     * @param id 字典ID
     */
    void deleteDict(Long id);

    // ==================== 导入导出 ====================

    /**
     * 导入字典数据
     *
     * @param kind  字典类型
     * @param file  Excel文件
     * @return 导入结果
     */
    DictDTO.ImportResult importDict(String kind, MultipartFile file);

    /**
     * 导出字典数据
     *
     * @param kind    字典类型
     * @param keyword 搜索关键词
     * @return 字典数据列表
     */
    List<DictDTO.ExportDTO> exportDict(String kind, String keyword);

    /**
     * 获取导入模板
     *
     * @return 模板数据
     */
    List<DictDTO.ExportDTO> getImportTemplate();

    /**
     * 获取知识库分类树（指定父节点，支持状态过滤）
     *
     * @param kind       字典类型
     * @param parentCode 父节点编码
     * @param status     状态过滤（可选）
     * @return 知识库分类树
     */
    List<SysDict> getKbCategoryTree(String kind, String parentCode, Integer status);
}
