package com.tsgs.demomybatispluseasypoi.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liujunbo
 * @version 1.0
 * @description 测试解析Excel如何避免OOM
 * @since 2025/4/19 13:31
 */
@Service
@Slf4j
public class ExcelParseService {

    private static final int ROW_ACCESS_WINDOW_SIZE = 100; // 流式处理行窗口大小

    public Set<String> parseExcel(MultipartFile file) {
        // 使用LinkedHashSet保持插入顺序
        Set<String> institutionCodes = new HashSet<>();
        AtomicInteger rowCount = new AtomicInteger(0);

        Workbook workbook = null;
        try (InputStream inputStream = file.getInputStream()) {
            // 使用WorkbookFactory自动判断Excel版本
            workbook = WorkbookFactory.create(inputStream);

            // 对于XLSX大文件，转换为流式处理模式
            if (workbook instanceof XSSFWorkbook) {
                workbook = new SXSSFWorkbook((XSSFWorkbook) workbook, ROW_ACCESS_WINDOW_SIZE);
                log.info("启用SXSSF流式处理模式");
            }

            Sheet sheet = workbook.getSheetAt(0);
            int targetColumnIndex = findTargetColumnIndex(sheet);

            // 并行流处理提高大文件处理效率
            sheet.forEach(row -> {
                if (row.getRowNum() == 0) return;

                processRow(row, targetColumnIndex, institutionCodes, rowCount);

                // 定期日志输出处理进度
                if (rowCount.get() % 100000 == 0) {
                    log.info("已处理 {} 行数据", rowCount.get());
                }
            });
            log.info("文件解析完成，总行数: {}, 机构编号数: {}", rowCount.get(), institutionCodes.size());
        } catch (Exception e) {
            log.info("解析Excel发生异常{}", e.getMessage());
        } finally {
            // 清理临时文件（针对SXSSF）
            if (workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) workbook).dispose();
            }
        }


        return institutionCodes;
    }

    /**
     * 【私有方法】获取目标列行号
     *
     * @param sheet sheet页对象
     * @return 目标标题列所在行号
     */
    private int findTargetColumnIndex(Sheet sheet) {
        int firstRowNum = sheet.getFirstRowNum();
        Row headerRow = sheet.getRow(firstRowNum);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel文件没有标题行");
        }

        // 遍历标题行查找目标列
        for (Cell cell : headerRow) {
            String cellValue = getCellStringValue(cell);
            if ("机构编号".equals(cellValue)) {
                log.info("找到目标列 '机构编号'，索引: {}", cell.getColumnIndex());
                return cell.getColumnIndex();
            }
        }

        // 处理合并单元格情况
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String cellValue = cell.getStringCellValue().trim();
                if ("机构编号".equals(cellValue)) {
                    log.info("找到目标列 '机构编号'，索引: {}", i);
                    return i;
                }
            }
        }

        throw new IllegalArgumentException("未找到标题为 '" + "机构编号" + "' 的列");
    }

    /**
     * 【私有方法】处理数据
     *
     * @param row               Excel行对象
     * @param targetColumnIndex 目标数据行号
     * @param institutionCodes  机构编号集合
     * @param rowCount          行
     */
    private void processRow(Row row, int targetColumnIndex, Set<String> institutionCodes, AtomicInteger rowCount) {
        try {
            Cell cell = row.getCell(targetColumnIndex);
            if (cell != null) {
                String code = getCellStringValue(cell);
                if (code != null && !code.trim().isEmpty()) {
                    institutionCodes.add(code.trim());
                }
            }
            rowCount.incrementAndGet();
        } catch (Exception e) {
            log.info("处理第 {} 行数据时发生错误: {}", row.getRowNum() + 1, e.getMessage());
        }
    }

    /**
     * 【私有方法】获取单元格数据
     *
     * @param cell 单元格对象
     * @return 单元格数据
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理数值型机构编号，避免科学计数法
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }
}
