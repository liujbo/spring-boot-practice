package com.example.demoexcel.service;

import com.example.demoexcel.dto.SheetInfo;
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
public class ExcelMergeService_3 {

    /*
        在 2 的基础上优化
        主要问题分析
        1. LRU缓存实现错误
        问题: 原代码中LRU缓存的初始容量设置不当
        修复: 使用合理的初始容量(16)，让LinkedHashMap自动扩容
        2. 样式缓存清理逻辑冲突
        问题: 同时使用LRU自动清理和手动清理，造成逻辑冲突
        修复: 移除手动清理，完全依赖LRU机制
        3. 无效的引用清理
        问题: sourceRow = null和 targetRow = null是无效操作
        修复: 移除这些无效的GC"帮助"代码
        4. 重复的批次大小限制
        问题: 在循环内外都有批次大小限制，逻辑重复
        修复: 统一在循环内进行批次大小控制
        5. 资源泄漏风险
        问题: workbookBaos没有使用try-with-resources
        修复: 确保所有流资源正确关闭
        6. 缺少必要的异常处理
        问题: 代码中缺少对空值和边界条件的检查
        建议: 添加适当的空值检查和异常处理
 */

    private static final int MAX_CACHE_SIZE = 1000;

    /**
     * 优化版合并方法，减少内存占用
     */
    public byte[] mergeExcelSheetsToZip_3(List<MultipartFile> excelFiles) throws IOException {
        // 1. 先分析结构，不加载数据
        Map<String, SheetInfo> sheetStructure = analysisSheetStructure(excelFiles);

        try (XSSFWorkbook mergedWorkbook = new XSSFWorkbook()) {
            // 使用LRU缓存限制样式缓存大小
            Map<CellStyle, CellStyle> styleCache = createStyleCache();

            // 2. 创建模板sheet
            for (Map.Entry<String, SheetInfo> entry : sheetStructure.entrySet()) {
                String sheetName = entry.getKey();
                SheetInfo sheetInfo = entry.getValue();
                XSSFSheet mergedSheet = mergedWorkbook.createSheet(sheetName);
                copyHeaderToSheet(sheetInfo, mergedSheet, mergedWorkbook, styleCache);
            }

            // 3. 流式处理每个文件，避免一次性加载所有数据
            for (MultipartFile file : excelFiles) {
                processFileIncrementally(file, mergedWorkbook, sheetStructure, styleCache);
            }

            // 4. 创建ZIP文件
            return createZipFile(mergedWorkbook);
        }
    }

    // 修复后的LRU缓存实现
    private Map<CellStyle, CellStyle> createStyleCache() {
        return new LinkedHashMap<CellStyle, CellStyle>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<CellStyle, CellStyle> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
    }


    /**
     * 检测Excel文件中的sheet结构
     */
    public Map<String, SheetInfo> analysisSheetStructure(List<MultipartFile> excelFiles) throws IOException {
        Map<String, SheetInfo> sheetStructure = new LinkedHashMap<>();

        for (MultipartFile file : excelFiles) {
            try (InputStream is = file.getInputStream()) {
                XSSFWorkbook workbook = new XSSFWorkbook(is);

                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    XSSFSheet sheet = workbook.getSheetAt(i);
                    String sheetName = sheet.getSheetName();

                    int dataStartRow = findDataStartRow(sheet);

                    if (!sheetStructure.containsKey(sheetName)) {
                        SheetInfo sheetInfo = new SheetInfo();
                        // 获取sheet名称
                        sheetInfo.setSheetName(sheetName);
                        // 收集表头信息
                        sheetInfo.setHeaderRows(getHeaderRows(sheet, dataStartRow));
                        // 收集合并单元格信息
                        sheetInfo.setMergedRegions(getMergedRegions(sheet, dataStartRow));
                        // 收集列宽
                        sheetInfo.setColumnWidths(getColumnWidths(sheet, dataStartRow));
                        // 获取列数
                        sheetInfo.setColumnCount(getMaxColumnCount(sheet, dataStartRow));
                        // 获取 数据开始行号
                        sheetInfo.setDataStartRow(dataStartRow);
                        sheetStructure.put(sheetName, sheetInfo);
                    }
                }
            }
        }

        return sheetStructure;
    }


    /**
     * 获取表头行范围
     */
    private List<Row> getHeaderRows(Sheet sheet, int dataStartRow) {
        List<Row> headerRows = new ArrayList<>();
        for (int rowNum = 0; rowNum < dataStartRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                headerRows.add(row);
            }
        }
        return headerRows;
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
        // 获取表头区域的列宽
        for (int colNum = 0; colNum <= getMaxColumnCount(sheet, dataStartRow); colNum++) {
            int width = sheet.getColumnWidth(colNum);
            columnWidths.put(colNum, width);
        }
        return columnWidths;
    }

    /**
     * 获取最大列数
     */
    private int getMaxColumnCount(Sheet sheet, int dataStartRow) {
        int maxCols = 0;

        // 检查数据行（只检查前几行来提高性能）
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
     * 复制表头到新sheet（完整样式）
     */
    private void copyHeaderToSheet(SheetInfo sheetInfo, XSSFSheet targetSheet, Workbook targetWorkbook, Map<CellStyle, CellStyle> styleCache) {
        List<Row> headerRows = sheetInfo.getHeaderRows();
        List<CellRangeAddress> mergedRegions = sheetInfo.getMergedRegions();
        Map<Integer, Integer> columnWidths = sheetInfo.getColumnWidths();

        // 复制列宽
        for (Map.Entry<Integer, Integer> entry : columnWidths.entrySet()) {
            targetSheet.setColumnWidth(entry.getKey(), entry.getValue());
        }

        // 复制表头行
        for (int i = 0; i < headerRows.size(); i++) {
            Row sourceRow = headerRows.get(i);
            Row targetRow = targetSheet.createRow(i);
            copyRowWithStyle(sourceRow, targetRow, targetWorkbook, styleCache);
        }

        // 复制合并单元格
        for (CellRangeAddress mergedRegion : mergedRegions) {
            targetSheet.addMergedRegion(mergedRegion);
        }
    }


    /**
     * 复制行（包括所有单元格和样式）
     */
    private void copyRowWithStyle(Row sourceRow, Row targetRow, Workbook targetWorkbook, Map<CellStyle, CellStyle> styleCache) {
        if (sourceRow == null || targetRow == null) {
            return;
        }

        // 设置行高
        targetRow.setHeight(sourceRow.getHeight());

        // 复制每个单元格
        for (int colNum = 0; colNum <= sourceRow.getLastCellNum(); colNum++) {
            Cell sourceCell = sourceRow.getCell(colNum);
            if (sourceCell != null) {
                Cell targetCell = targetRow.createCell(colNum);
                copyCellWithStyle(sourceCell, targetCell, targetWorkbook, styleCache);
            }
        }
    }


    /**
     * 增量处理单个文件
     */
    private void processFileIncrementally(MultipartFile file, XSSFWorkbook mergedWorkbook,
                                          Map<String, SheetInfo> sheetStructure,
                                          Map<CellStyle, CellStyle> styleCache) throws IOException {
        try (InputStream is = file.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(is)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                XSSFSheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                if (!sheetStructure.containsKey(sheetName)) {
                    continue;
                }

                SheetInfo sheetInfo = sheetStructure.get(sheetName);
                XSSFSheet mergedSheet = mergedWorkbook.getSheet(sheetName);

                // 分批处理数据行，避免内存溢出
                processSheetDataInBatches(sheet, mergedSheet, mergedWorkbook, sheetInfo, styleCache);
            }
        }
    }

    /*
     * 修复问题3: 样式缓存清理逻辑冲突
     */

    /**
     * 分批处理sheet数据
     */
    private void processSheetDataInBatches(Sheet sourceSheet, XSSFSheet targetSheet,
                                           Workbook targetWorkbook, SheetInfo sheetInfo,
                                           Map<CellStyle, CellStyle> styleCache) throws IOException {
        int currentRow = sheetInfo.getDataStartRow();
        int batchSize = 1000; // 每批处理1000行

        while (currentRow <= sourceSheet.getLastRowNum()) {
            int endRow = Math.min(currentRow + batchSize - 1, sourceSheet.getLastRowNum());
            List<Row> batchRows = getDataRowsBatch(sourceSheet, currentRow, endRow);

            if (!batchRows.isEmpty()) {
                writeDataToSheet(batchRows, targetSheet, targetWorkbook, sheetInfo, styleCache);

                // 定期清理样式缓存，防止内存泄漏
                // if (styleCache.size() > MAX_CACHE_SIZE * 0.8) {
                //     styleCache.clear();
                // }

                // 修复: 移除冲突的缓存清理逻辑，让LRU自动管理
            }

            currentRow = endRow + 1;
        }
    }


    /*
     * 修复问题5: 重复的批次大小限制
     */

    /**
     * 获取批次数据行
     */
    private List<Row> getDataRowsBatch(Sheet sheet, int startRow, int endRow) {
        List<Row> batchRows = new ArrayList<>();
        for (int rowNum = startRow; rowNum <= endRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                batchRows.add(row);
                // 限制单批次大小，防止内存溢出
                // if (batchRows.size() >= 1000) {
                //     break;
                // }
                // 修复: 移除重复的限制，外层已经通过batchSize控制
            }
            // 添加合理的批次大小控制
            if (batchRows.size() >= 1000) {
                break;
            }
        }
        return batchRows;
    }


    /*
     * 修复问题4: 无效的引用清理
     */

    /**
     * 优化的数据写入方法
     */
    private void writeDataToSheet(List<Row> dataRows, XSSFSheet targetSheet, Workbook targetWorkbook, SheetInfo sheetInfo, Map<CellStyle, CellStyle> styleCache) {
        int startRowNum = targetSheet.getLastRowNum() + 1;

        for (int i = 0; i < dataRows.size(); i++) {
            Row sourceRow = dataRows.get(i);
            Row targetRow = targetSheet.createRow(startRowNum + i);

            if (sourceRow.getHeight() >= 0) {
                targetRow.setHeight(sourceRow.getHeight());
            }

            for (int colNum = 0; colNum < sheetInfo.getColumnCount(); colNum++) {
                Cell sourceCell = sourceRow.getCell(colNum);
                if (sourceCell != null) {
                    Cell targetCell = targetRow.createCell(colNum);
                    copyCellWithStyle(sourceCell, targetCell, targetWorkbook, styleCache);
                }
            }

            // 及时清理引用，帮助GC
            // if (i % 100 == 0) {
            //     sourceRow = null;
            //     targetRow = null;
            // }

            // 修复: 移除无效的引用清理，这不会帮助GC
            // sourceRow = null;  // 这行无效，因为sourceRow是局部变量
            // targetRow = null; // 这行无效，因为targetRow是局部变量
        }
    }

    private int findDataStartRow(Sheet sheet) {
        int maxCheckRows = Math.min(10, sheet.getLastRowNum());
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

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }


    /**
     * 复制单元格及其样式
     */
    private void copyCellWithStyle(Cell sourceCell, Cell targetCell, Workbook targetWorkbook, Map<CellStyle, CellStyle> styleCache) {
        if (sourceCell == null || targetCell == null) {
            return;
        }
        // 复制值
        copyCellValue(sourceCell, targetCell);

        // 复制单元格样式
        CellStyle sourceStyle = sourceCell.getCellStyle();
        CellStyle targetStyle = styleCache.get(sourceStyle);

        if (targetStyle == null) {
            targetStyle = targetWorkbook.createCellStyle();
            targetStyle.cloneStyleFrom(sourceStyle);

            // 复制字体
            Font sourceFont = sourceCell.getSheet().getWorkbook().getFontAt(sourceStyle.getFontIndex());
            Font targetFont = targetWorkbook.createFont();
            copyFont(sourceFont, targetFont);
            targetStyle.setFont(targetFont);

            styleCache.put(sourceStyle, targetStyle);
        }
        targetCell.setCellStyle(targetStyle);
    }


    /**
     * 复制字体
     *
     * @param source 原始字体对象
     * @param target 目标字体对象
     */
    private void copyFont(Font source, Font target) {
        target.setBold(source.getBold());
        target.setItalic(source.getItalic());
        target.setUnderline(source.getUnderline());
        target.setFontHeightInPoints(source.getFontHeightInPoints());
        target.setFontName(source.getFontName());
        target.setColor(source.getColor());
    }

    /**
     * 复制单元格值
     */
    private void copyCellValue(Cell sourceCell, Cell targetCell) {
        switch (sourceCell.getCellType()) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(sourceCell)) {
                    targetCell.setCellValue(sourceCell.getDateCellValue());
                } else {
                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                try {
                    targetCell.setCellFormula(String.valueOf(sourceCell.getNumericCellValue()));
                } catch (Exception e) {
                    targetCell.setCellFormula(sourceCell.getCellFormula());
                }
                break;
            case BLANK:
                targetCell.setBlank();
                break;
            case ERROR:
                targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
                break;
            default:
                // 使用通用方法获取值
                targetCell.setCellValue(getCellValueAsString(sourceCell));
        }
    }

    private byte[] createZipFile(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            try (ByteArrayOutputStream workbookBaos = new ByteArrayOutputStream()) {
                workbook.write(workbookBaos);

                ZipEntry entry = new ZipEntry("merged_data.xlsx");
                zos.putNextEntry(entry);
                zos.write(workbookBaos.toByteArray());
                zos.closeEntry();
            }

        }
        return baos.toByteArray();
    }
}