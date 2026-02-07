package com.police.kb.service.impl;

import com.police.kb.service.FileParseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class FileParseServiceImpl implements FileParseService {

  @Override
  public String parse(String fileName, byte[] fileData) {
    if (fileName == null || fileData == null || fileData.length == 0) {
      log.warn("文件名为空或文件数据为空");
      return "";
    }

    String extension = getFileExtension(fileName).toLowerCase();
    log.info("开始解析文件: {}, 类型: {}", fileName, extension);

    try {
      String content = switch (extension) {
        case "docx" -> parseDocx(fileData);
        case "doc" -> parseDoc(fileData);
        case "xlsx", "xls" -> parseExcel(fileData);
        case "pdf" -> parsePdf(fileData);
        case "txt", "md" -> parseText(fileData);
        default -> {
          log.warn("不支持的文件类型: {}", extension);
          yield "";
        }
      };

      log.info("文件解析完成: {}, 内容长度: {}", fileName, content != null ? content.length() : 0);
      return content != null ? content : "";
    } catch (Exception e) {
      log.error("文件解析失败: {}, error: {}", fileName, e.getMessage(), e);
      throw new RuntimeException("文件解析失败: " + e.getMessage(), e);
    }
  }

  private String parseDocx(byte[] fileData) throws Exception {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
        org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(bis);
        org.apache.poi.xwpf.extractor.XWPFWordExtractor extractor = new org.apache.poi.xwpf.extractor.XWPFWordExtractor(
            doc)) {
      return extractor.getText();
    }
  }

  private String parseDoc(byte[] fileData) throws Exception {
    log.warn("旧版.doc格式解析需要额外依赖，当前版本暂不支持完整解析");
    return "旧版Word文档(.doc)内容: " + fileData.length + " 字节";
  }

  private String parseExcel(byte[] fileData) throws Exception {
    StringBuilder content = new StringBuilder();
    try (ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
        org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(bis)) {
      int sheetCount = workbook.getNumberOfSheets();
      for (int i = 0; i < sheetCount; i++) {
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(i);
        if (sheet == null)
          continue;
        content.append("=== Sheet: ").append(sheet.getSheetName()).append(" ===\n");
        for (int rowIdx = 0; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
          org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIdx);
          if (row == null)
            continue;
          StringBuilder rowContent = new StringBuilder();
          int cellCount = row.getLastCellNum();
          for (int cellIdx = 0; cellIdx < cellCount; cellIdx++) {
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(cellIdx);
            rowContent.append(cell == null ? "" : getCellValue(cell)).append("\t");
          }
          if (rowContent.length() > 0) {
            content.append(rowContent.toString().trim()).append("\n");
          }
        }
        content.append("\n");
      }
    }
    return content.toString();
  }

  private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
    if (cell == null)
      return "";
    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue();
      case NUMERIC -> {
        if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
          yield cell.getLocalDateTimeCellValue().toString();
        }
        yield String.valueOf(cell.getNumericCellValue());
      }
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      case FORMULA -> cell.getCellFormula();
      default -> "";
    };
  }

  private String parsePdf(byte[] fileData) throws Exception {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
             PDDocument document = org.apache.pdfbox.Loader.loadPDF(bis.readAllBytes())) {

            if (document.isEncrypted()) {
                return "[PDF文件已加密，无法解析内容]";
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setAddMoreFormatting(true);

            return stripper.getText(document);
        }
    }

  private String parseText(byte[] fileData) {
    StringBuilder content = new StringBuilder();
    try (ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
        InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(isr)) {
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line).append("\n");
      }
    } catch (Exception e) {
      log.error("文本文件解析失败: {}", e.getMessage());
      return new String(fileData, StandardCharsets.UTF_8);
    }
    return content.toString();
  }
}
