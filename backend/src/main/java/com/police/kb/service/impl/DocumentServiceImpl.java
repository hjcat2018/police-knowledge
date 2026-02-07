package com.police.kb.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.kb.domain.dto.DocumentDTO;
import com.police.kb.domain.entity.Document;
import com.police.kb.domain.entity.SysDict;
import com.police.kb.mapper.DocumentMapper;
import com.police.kb.mapper.KnowledgeBaseMapper;
import com.police.kb.mapper.SysDictMapper;
import com.police.kb.service.DocumentService;
import com.police.kb.service.VectorService;
import com.police.kb.domain.vo.DocumentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

  private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final DocumentMapper documentMapper;
  private final KnowledgeBaseMapper knowledgeBaseMapper;
  private final SysDictMapper sysDictMapper;
  private final VectorService vectorService;

  @Qualifier("vectorExecutor")
  @Autowired
  private Executor vectorExecutor;

  public DocumentServiceImpl(DocumentMapper documentMapper, KnowledgeBaseMapper knowledgeBaseMapper,
      SysDictMapper sysDictMapper, VectorService vectorService) {
    this.documentMapper = documentMapper;
    this.knowledgeBaseMapper = knowledgeBaseMapper;
    this.sysDictMapper = sysDictMapper;
    this.vectorService = vectorService;
  }

  @Override
  public IPage<DocumentVO> pageDocuments(int pageNum, int pageSize, Long kbId, String keyword, String originScope, String originDepartment) {
    Page<Document> page = new Page<>(pageNum, pageSize);
    IPage<Document> result;

    if (kbId != null) {
      if (StringUtils.hasText(keyword)) {
        result = documentMapper.selectPageByKbIdAndKeyword(page, kbId, keyword, originScope, originDepartment);
      } else {
        result = documentMapper.selectPageByKbId(page, kbId, originScope, originDepartment);
      }
    } else {
      result = documentMapper.selectPageAll(page, originScope, originDepartment);
    }

    Page<DocumentVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
    voPage.setRecords(result.getRecords().stream().map(doc -> {
      DocumentVO vo = new DocumentVO();
      vo.setId(doc.getId());
      vo.setTitle(doc.getTitle());
      vo.setContent(doc.getContent());
      vo.setSummary(doc.getSummary());
      vo.setCover(doc.getCover());
      vo.setKbId(doc.getKbId());
      vo.setCategoryId(doc.getCategoryId());
      vo.setTags(doc.getTags());
      vo.setStatus(doc.getStatus());
      vo.setViewCount(doc.getViewCount());
      vo.setFavoriteCount(doc.getFavoriteCount());
      vo.setIsTop(doc.getIsTop());
      vo.setIsHot(doc.getIsHot());
      vo.setCreatedTime(doc.getCreatedTime());
      vo.setCreatedBy(doc.getCreatedBy());
      vo.setUpdatedTime(doc.getUpdatedTime());
      vo.setSourceTime(doc.getSourceTime());
      vo.setDocUrl(doc.getDocUrl());
      vo.setOriginScope(doc.getOriginScope());
      vo.setOriginDepartment(doc.getOriginDepartment());
      vo.setKbName(doc.getKbName());

      return vo;
    }).toList());

    return voPage;
  }

  @Override
  public DocumentVO getDocumentById(Long id) {
    Document doc = documentMapper.selectById(id);
    if (doc == null) {
      throw new IllegalArgumentException("文档不存在");
    }

    DocumentVO vo = new DocumentVO();
    vo.setId(doc.getId());
    vo.setTitle(doc.getTitle());
    vo.setContent(doc.getContent());
    vo.setSummary(doc.getSummary());
    vo.setCover(doc.getCover());
    vo.setKbId(doc.getKbId());
    vo.setCategoryId(doc.getCategoryId());
    vo.setTags(doc.getTags());
    vo.setStatus(doc.getStatus());
    vo.setViewCount(doc.getViewCount());
    vo.setFavoriteCount(doc.getFavoriteCount());
    vo.setIsTop(doc.getIsTop());
    vo.setIsHot(doc.getIsHot());
    vo.setCreatedTime(doc.getCreatedTime());
    vo.setCreatedBy(doc.getCreatedBy());
    vo.setUpdatedTime(doc.getUpdatedTime());
    vo.setSourceTime(doc.getSourceTime());
      vo.setDocUrl(doc.getDocUrl());
      vo.setKbName(doc.getKbName());

    return vo;
  }

    @Override
    @Transactional
    public Document createDocument(DocumentDTO dto) {
      Document doc = new Document();
      doc.setTitle(dto.getTitle());
      doc.setContent(dto.getContent());
      doc.setSummary(dto.getSummary());
      doc.setKbId(dto.getKbId());
      doc.setKbPath(buildKbPath(dto.getKbId()));
      doc.setCategoryId(dto.getCategoryId());
      doc.setTags(dto.getTags());
      doc.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
      doc.setCover(dto.getCover());
      doc.setSource(dto.getSource());
      doc.setAuthor(dto.getAuthor());
      doc.setSourceTime(parseSourceTime(dto.getSourceTime()));
      doc.setDocUrl(dto.getDocUrl());
      doc.setOriginScope(dto.getOriginScope());
      doc.setOriginDepartment(dto.getOriginDepartment());
      doc.setViewCount(0);
      doc.setFavoriteCount(0);
      doc.setIsTop(0);
      doc.setIsHot(0);

      documentMapper.insert(doc);

      // 异步向量化，不阻塞文档创建返回
      vectorizeDocumentAsync(doc.getId());

      return doc;
    }

    /**
     * 异步向量化文档
     * 在文档创建后立即返回，向量化任务在后台异步执行
     */
    @Async("vectorExecutor")
    public void vectorizeDocumentAsync(Long documentId) {
      try {
        log.info("开始异步向量化文档: id={}", documentId);
        vectorService.vectorizeDocument(documentId);
        vectorService.syncVectorMetadata(documentId, null, null);
        log.info("异步向量化完成: id={}", documentId);
      } catch (Exception e) {
        log.error("异步向量化失败: id={}, error={}", documentId, e.getMessage(), e);
      }
    }

    @Override
    @Transactional
    public Document updateDocument(Long id, DocumentDTO dto) {
      Document doc = documentMapper.selectById(id);
      if (doc == null) {
        throw new IllegalArgumentException("文档不存在");
      }

      boolean needSyncVector = false;
      String newOriginScope = doc.getOriginScope();
      String newOriginDepartment = doc.getOriginDepartment();

      if (StringUtils.hasText(dto.getTitle())) {
        doc.setTitle(dto.getTitle());
      }
      if (dto.getContent() != null) {
        doc.setContent(dto.getContent());
      }
      if (dto.getSummary() != null) {
        doc.setSummary(dto.getSummary());
      }
      if (dto.getKbId() != null) {
        doc.setKbId(dto.getKbId());
        doc.setKbPath(buildKbPath(dto.getKbId()));
      }
      if (dto.getCategoryId() != null) {
        doc.setCategoryId(dto.getCategoryId());
      }
      if (dto.getTags() != null) {
        doc.setTags(dto.getTags());
      }
      if (dto.getStatus() != null) {
        doc.setStatus(dto.getStatus());
      }
      if (dto.getCover() != null) {
        doc.setCover(dto.getCover());
      }
      if (dto.getSource() != null) {
        doc.setSource(dto.getSource());
      }
      if (dto.getAuthor() != null) {
        doc.setAuthor(dto.getAuthor());
      }
      if (dto.getSourceTime() != null) {
        doc.setSourceTime(parseSourceTime(dto.getSourceTime()));
      }
      if (dto.getDocUrl() != null) {
        doc.setDocUrl(dto.getDocUrl());
      }
      if (dto.getOriginScope() != null) {
        if (!dto.getOriginScope().equals(doc.getOriginScope())) {
          needSyncVector = true;
        }
        doc.setOriginScope(dto.getOriginScope());
        newOriginScope = dto.getOriginScope();
      }
      if (dto.getOriginDepartment() != null) {
        if (!dto.getOriginDepartment().equals(doc.getOriginDepartment())) {
          needSyncVector = true;
        }
        doc.setOriginDepartment(dto.getOriginDepartment());
        newOriginDepartment = dto.getOriginDepartment();
      }

      documentMapper.updateById(doc);

      // 内容变更时异步重新向量化
      if (dto.getContent() != null) {
        vectorizeDocumentAsync(id);
      } else {
        // 仅元数据变更，直接同步
        vectorService.revectorizeDocument(id);
      }

      if (needSyncVector) {
        vectorService.syncVectorMetadata(id, newOriginScope, newOriginDepartment);
      }

      return doc;
    }

  @Override
  @Transactional
  public void deleteDocument(Long id) {
    Document doc = documentMapper.selectById(id);
    if (doc == null) {
      throw new IllegalArgumentException("文档不存在");
    }

    // 删除文档向量
    vectorService.deleteDocumentVectors(id);

    documentMapper.deleteById(id);
  }

  @Override
  @Transactional
  public void batchDeleteDocuments(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      throw new IllegalArgumentException("请选择要删除的文档");
    }

    // 删除文档向量
    for (Long id : ids) {
      vectorService.deleteDocumentVectors(id);
    }

    documentMapper.deleteBatchIds(ids);
  }

  @Override
  @Transactional
  public void changeStatus(Long id, Integer status) {
    Document doc = documentMapper.selectById(id);
    if (doc == null) {
      throw new IllegalArgumentException("文档不存在");
    }
    doc.setStatus(status);
    documentMapper.updateById(doc);
  }

  @Override
  @Transactional
  public void incrementViewCount(Long id) {
    Document doc = documentMapper.selectById(id);
    if (doc != null) {
      doc.setViewCount(doc.getViewCount() + 1);
      documentMapper.updateById(doc);
    }
  }

  @Override
  public List<DocumentVO> getHotDocuments() {
    return documentMapper.selectHotDocuments().stream().map(this::convertToVO).toList();
  }

  @Override
  public List<DocumentVO> getRecentDocuments() {
    return documentMapper.selectRecentDocuments().stream().map(this::convertToVO).toList();
  }

  private DocumentVO convertToVO(Document doc) {
    DocumentVO vo = new DocumentVO();
    vo.setId(doc.getId());
    vo.setTitle(doc.getTitle());
    vo.setContent(doc.getContent());
    vo.setSummary(doc.getSummary());
    vo.setCover(doc.getCover());
    vo.setKbId(doc.getKbId());
    vo.setCategoryId(doc.getCategoryId());
    vo.setTags(doc.getTags());
    vo.setStatus(doc.getStatus());
    vo.setViewCount(doc.getViewCount());
    vo.setFavoriteCount(doc.getFavoriteCount());
    vo.setIsTop(doc.getIsTop());
    vo.setIsHot(doc.getIsHot());
    vo.setCreatedTime(doc.getCreatedTime());
    vo.setSourceTime(doc.getSourceTime());
    vo.setDocUrl(doc.getDocUrl());
    return vo;
  }

  private LocalDateTime parseSourceTime(String sourceTime) {
    if (sourceTime == null || sourceTime.isEmpty()) {
      return null;
    }
    try {
      return LocalDateTime.parse(sourceTime, DATETIME_FORMATTER);
    } catch (Exception e) {
      try {
        return LocalDateTime.parse(sourceTime.replace("T", " ").split("\\.")[0], DATETIME_FORMATTER);
      } catch (Exception e2) {
        return null;
      }
    }
  }

  /**
   * 计算知识库路径
   * <p>从当前知识库ID向上递归，获取完整的父级ID链</p>
   * <p>如：法律法规(16) -> 刑法(17)，则刑法的kbPath为"16,17"</p>
   *
   * @param kbId 知识库ID
   * @return 逗号分隔的知识库路径
   */
  private String buildKbPath(Long kbId) {
    if (kbId == null) {
      return null;
    }

    List<Long> pathIds = new ArrayList<>();
    Long currentId = kbId;

    while (currentId != null && currentId > 0) {
      pathIds.add(currentId);
      SysDict dict = sysDictMapper.selectById(currentId);
      if (dict == null) {
        break;
      }
      currentId = dict.getParentId();
    }

    if (pathIds.isEmpty()) {
      return String.valueOf(kbId);
    }

    return pathIds.stream().map(String::valueOf).collect(Collectors.joining(","));
  }
}
