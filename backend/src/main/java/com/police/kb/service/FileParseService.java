package com.police.kb.service;

/**
 * 文件解析服务接口
 * <p>
 * 定义文件解析的业务操作方法，支持多种文档格式的文本提取。
 * 支持Word、Excel、PDF、TXT、Markdown等格式。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
public interface FileParseService {

    /**
     * 支持的文件类型
     */
    String[] SUPPORTED_TYPES = {
        "doc", "docx",    // Word文档
        "xls", "xlsx",    // Excel表格
        "pdf",           // PDF文档
        "txt",           // 纯文本
        "md"             // Markdown
    };

    /**
     * 解析文件内容
     * <p>
     * 根据文件类型调用相应的解析器提取文本内容。
     * </p>
     *
     * @param fileName 文件名
     * @param fileData 文件字节数组
     * @return 解析后的文本内容
     */
    String parse(String fileName, byte[] fileData);

    /**
     * 检查文件类型是否支持
     * <p>
     * 通过文件扩展名判断是否支持该类型。
     * </p>
     *
     * @param fileName 文件名
     * @return 是否支持
     */
    default boolean isSupported(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String extension = getFileExtension(fileName).toLowerCase();
        for (String type : SUPPORTED_TYPES) {
            if (type.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    default String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
