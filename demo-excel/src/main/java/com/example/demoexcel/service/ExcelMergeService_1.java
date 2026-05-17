package com.example.demoexcel.service;

import com.example.demoexcel.dto.SheetInfo_1;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * Excel多sheet合并
 */
@Service
@Slf4j
public class ExcelMergeService_1 {


    // 批次处理大小，避免一次性加载太多数据
    private static final int BATCH_SIZE = 1000;

    /**
     * 优化的合并方法，避免内存溢出
     */
    public byte[] mergeExcelSheetsToZipOptimized(List<MultipartFile> excelFiles) throws IOException {
        // 1. 轻量级分析sheet结构（只分析第一个文件的第一个sheet作为模板）
        Map<String, SheetInfo_1> sheetStructure = lightAnalysisSheetStructure(excelFiles);

        // 2. 创建输出工作簿
        try (XSSFWorkbook mergedWorkbook = new XSSFWorkbook()) {

            // 创建样式缓存（减少重复创建）
            Map<String, CellStyle> globalStyleCache = new HashMap<>();

            // 3. 创建sheet模板（只创建一次）
            for (Map.Entry<String, SheetInfo_1> entry : sheetStructure.entrySet()) {
                String sheetName = entry.getKey();
                SheetInfo_1 sheetInfo = entry.getValue();

                XSSFSheet mergedSheet = mergedWorkbook.createSheet(sheetName);
                // 轻量级复制表头结构
                lightCopyHeaderToSheet(sheetInfo, mergedSheet, mergedWorkbook, globalStyleCache);
            }

            // 4. 流式读取数据并写入（不缓存Row对象）
            for (MultipartFile file : excelFiles) {
                processFileData(file, sheetStructure, mergedWorkbook, globalStyleCache);
            }

            // 5. 创建ZIP文件
            return createZipFile(mergedWorkbook);
        }
    }

    /**
     * 轻量级分析sheet结构（不缓存Row对象）
     */
    private Map<String, SheetInfo_1> lightAnalysisSheetStructure(List<MultipartFile> excelFiles) throws IOException {
        Map<String, SheetInfo_1> sheetStructure = new LinkedHashMap<>();

        // 只分析第一个文件获取结构
        if (!excelFiles.isEmpty()) {
            try (InputStream is = excelFiles.get(0).getInputStream();
                 Workbook workbook = WorkbookFactory.create(is)) {

                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    String sheetName = sheet.getSheetName();

                    // 只分析一次
                    if (!sheetStructure.containsKey(sheetName)) {
                        SheetInfo_1 sheetInfo = new SheetInfo_1();
                        sheetInfo.setSheetName(sheetName);

                        int dataStartRow = findDataStartRow(sheet);
                        sheetInfo.setDataStartRow(dataStartRow);

                        // 收集表头信息（只保存数据，不保存Row对象）
                        List<List<Object>> headerData = collectHeaderData(sheet, dataStartRow);
                        sheetInfo.setHeaderData(headerData);

                        // 收集合并单元格信息
                        List<CellRangeAddress> mergedRegions = getMergedRegions(sheet, dataStartRow);
                        sheetInfo.setMergedRegions(mergedRegions);

                        // 收集列宽
                        Map<Integer, Integer> columnWidths = getColumnWidths(sheet, dataStartRow);
                        sheetInfo.setColumnWidths(columnWidths);

                        // 获取列数
                        sheetInfo.setColumnCount(getMaxColumnCount(sheet, dataStartRow));

                        sheetStructure.put(sheetName, sheetInfo);
                    }
                }
            }
        }

        return sheetStructure;
    }

    /**
     * 收集表头数据（不保存Row对象）
     */
    private List<List<Object>> collectHeaderData(Sheet sheet, int dataStartRow) {
        List<List<Object>> headerData = new ArrayList<>();

        for (int rowNum = 0; rowNum < dataStartRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                List<Object> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(getCellValue(cell));
                }
                headerData.add(rowData);
            }
        }

        return headerData;
    }

    /**
     * 获取单元格值（通用方法）
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                // 尝试获取公式结果
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            case BLANK:
                return "";
            case ERROR:
                return cell.getErrorCellValue();
            default:
                return "";
        }
    }

    /**
     * 轻量级复制表头（根据数据重建，不复制Row对象）
     */
    private void lightCopyHeaderToSheet(SheetInfo_1 sheetInfo, XSSFSheet targetSheet, Workbook targetWorkbook, Map<String, CellStyle> styleCache) {
        // 设置列宽
        if (sheetInfo.getColumnWidths() != null) {
            for (Map.Entry<Integer, Integer> entry : sheetInfo.getColumnWidths().entrySet()) {
                targetSheet.setColumnWidth(entry.getKey(), entry.getValue());
            }
        }

        // 根据headerData重建表头
        List<List<Object>> headerData = sheetInfo.getHeaderData();
        for (int i = 0; i < headerData.size(); i++) {
            Row targetRow = targetSheet.createRow(i);
            List<Object> rowData = headerData.get(i);

            for (int j = 0; j < rowData.size(); j++) {
                Cell targetCell = targetRow.createCell(j);
                Object value = rowData.get(j);
                setCellValue(targetCell, value);

                // 设置默认样式
                CellStyle style = getDefaultHeaderStyle(targetWorkbook, styleCache);
                targetCell.setCellStyle(style);
            }
        }

        // 复制合并单元格
        if (sheetInfo.getMergedRegions() != null) {
            for (CellRangeAddress mergedRegion : sheetInfo.getMergedRegions()) {
                targetSheet.addMergedRegion(mergedRegion);
            }
        }
    }

    /**
     * 获取默认表头样式
     */
    private CellStyle getDefaultHeaderStyle(Workbook workbook, Map<String, CellStyle> styleCache) {
        String styleKey = "default_header_style";

        return styleCache.computeIfAbsent(styleKey, key -> {
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 12);
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);

            // 设置边框
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);

            return style;
        });
    }

    /**
     * 设置单元格值
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
            return;
        }

        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 处理单个文件的数据（流式处理）
     */
    private void processFileData(MultipartFile file, Map<String, SheetInfo_1> sheetStructure,
                                 XSSFWorkbook mergedWorkbook, Map<String, CellStyle> styleCache) throws IOException {
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                if (!sheetStructure.containsKey(sheetName)) {
                    continue;
                }

                SheetInfo_1 sheetInfo = sheetStructure.get(sheetName);
                XSSFSheet mergedSheet = mergedWorkbook.getSheet(sheetName);

                // 获取当前数据写入位置
                int currentRow = mergedSheet.getLastRowNum() + 1;
                if (currentRow <= sheetInfo.getHeaderData().size()) {
                    currentRow = sheetInfo.getHeaderData().size();
                }

                // 流式读取数据行并写入
                writeDataRowsStreaming(sheet, sheetInfo, mergedSheet, currentRow, mergedWorkbook, styleCache);
            }
        }
    }

    /**
     * 流式写入数据行（避免缓存所有Row）
     */
    private void writeDataRowsStreaming(Sheet sourceSheet, SheetInfo_1 sheetInfo, XSSFSheet targetSheet, int startRow, Workbook targetWorkbook, Map<String, CellStyle> styleCache) {
        int dataStartRow = sheetInfo.getDataStartRow();
        int columnCount = sheetInfo.getColumnCount();

        // 创建数据样式
        CellStyle dataStyle = getOrCreateDataStyle(targetWorkbook, styleCache);

        for (int rowNum = dataStartRow; rowNum <= sourceSheet.getLastRowNum(); rowNum++) {
            Row sourceRow = sourceSheet.getRow(rowNum);
            if (sourceRow == null) {
                continue;
            }

            Row targetRow = targetSheet.createRow(startRow++);

            // 设置行高
            if (sourceRow.getHeight() >= 0) {
                targetRow.setHeight(sourceRow.getHeight());
            }

            // 复制单元格值
            for (int colNum = 0; colNum < columnCount; colNum++) {
                Cell sourceCell = sourceRow.getCell(colNum);
                Cell targetCell = targetRow.createCell(colNum);

                if (sourceCell != null) {
                    // 复制值
                    Object cellValue = getCellValue(sourceCell);
                    setCellValue(targetCell, cellValue);

                    // 应用数据样式（简化，避免样式复制开销）
                    targetCell.setCellStyle(dataStyle);
                }
            }

            // 批量处理检查，定期刷新
            if (startRow % BATCH_SIZE == 0) {
                log.debug("已处理 {} 行数据", startRow);
            }
        }
    }

    /**
     * 获取或创建数据样式
     */
    private CellStyle getOrCreateDataStyle(Workbook workbook, Map<String, CellStyle> styleCache) {
        String styleKey = "default_data_style";

        return styleCache.computeIfAbsent(styleKey, key -> {
            CellStyle style = workbook.createCellStyle();

            // 设置边框
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);

            // 设置对齐
            style.setVerticalAlignment(VerticalAlignment.CENTER);

            return style;
        });
    }

    // ============ 以下方法保持原有逻辑，但进行优化 ============

    /**
     * 检测数据开始行号
     */
    private int findDataStartRow(Sheet sheet) {
        // 优化：最多检查前100行
        int maxCheckRows = Math.min(100, sheet.getLastRowNum());

        for (int rowNum = 0; rowNum <= maxCheckRows; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                Cell cell = row.getCell(0);
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell);
                    if (cellValue != null && cellValue.matches("\\d{5}")) {
                        return rowNum;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellValueAsString(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    /**
     * 获取合并单元格信息
     */
    private List<CellRangeAddress> getMergedRegions(Sheet sheet, int dataStartRow) {
        List<CellRangeAddress> headerMergedRegions = new ArrayList<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.getLastRow() < dataStartRow) {
                headerMergedRegions.add(mergedRegion);
            }
        }
        return headerMergedRegions;
    }

    /**
     * 获取列宽信息
     */
    private Map<Integer, Integer> getColumnWidths(Sheet sheet, int dataStartRow) {
        Map<Integer, Integer> columnWidths = new HashMap<>();
        int maxCols = getMaxColumnCount(sheet, dataStartRow);

        for (int colNum = 0; colNum <= maxCols; colNum++) {
            int width = sheet.getColumnWidth(colNum);
            columnWidths.put(colNum, width);
        }
        return columnWidths;
    }

    /**
     * 获取最大列数（优化版）
     */
    private int getMaxColumnCount(Sheet sheet, int dataStartRow) {
        int maxCols = 0;

        // 只检查前几行来提高性能
        int checkRows = Math.min(dataStartRow + 10, sheet.getLastRowNum());
        for (int rowNum = 0; rowNum <= checkRows; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                maxCols = Math.max(maxCols, row.getLastCellNum());
            }
        }
        return maxCols;
    }

    /**
     * 创建ZIP文件
     */
    private byte[] createZipFile(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ByteArrayOutputStream workbookBaos = new ByteArrayOutputStream();
            workbook.write(workbookBaos);
            workbook.close();

            ZipEntry entry = new ZipEntry("merged_data.xlsx");
            zos.putNextEntry(entry);
            zos.write(workbookBaos.toByteArray());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}