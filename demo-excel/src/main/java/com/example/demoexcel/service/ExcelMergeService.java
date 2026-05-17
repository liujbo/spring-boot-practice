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
public class ExcelMergeService {

    /**
     * 合并多个Excel文件的sheet数据
     */
    public byte[] mergeExcelSheetsToZip(List<MultipartFile> excelFiles) throws IOException {
        // 1. 检测所有文件的sheet结构
        Map<String, SheetInfo> sheetStructure = analysisSheetStructure(excelFiles);

        // 2. 创建新的工作簿
        try (XSSFWorkbook mergedWorkbook = new XSSFWorkbook()) {

            // 为每个sheet创建一个样式缓存，避免重复创建
            Map<CellStyle, CellStyle> styleCache = new HashMap<>();

            // 3. 为每个sheet创建模板
            for (Map.Entry<String, SheetInfo> entry : sheetStructure.entrySet()) {
                String sheetName = entry.getKey();
                SheetInfo sheetInfo = entry.getValue();

                // 创建sheet
                XSSFSheet mergedSheet = mergedWorkbook.createSheet(sheetName);

                // 复制表头（包括样式、合并单元格、列宽）
                copyHeaderToSheet(sheetInfo, mergedSheet, mergedWorkbook, styleCache);
            }

            // 4. 收集并合并所有数据
            Map<String, List<Row>> allDataRows = new HashMap<>();
            for (MultipartFile file : excelFiles) {
                try (InputStream is = file.getInputStream()) {
                    XSSFWorkbook workbook = new XSSFWorkbook(is);

                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                        XSSFSheet sheet = workbook.getSheetAt(i);
                        String sheetName = sheet.getSheetName();

                        int dataStartRow = findDataStartRow(sheet);
                        List<Row> dataRows = getDataRows(sheet, dataStartRow);

                        if (!allDataRows.containsKey(sheetName)) {
                            allDataRows.put(sheetName, new ArrayList<>());
                        }
                        allDataRows.get(sheetName).addAll(dataRows);
                    }
                }
            }

            // 5. 将数据写入合并后的sheet
            for (Map.Entry<String, SheetInfo> entry : sheetStructure.entrySet()) {
                String sheetName = entry.getKey();
                SheetInfo sheetInfo = entry.getValue();
                XSSFSheet mergedSheet = mergedWorkbook.getSheet(sheetName);

                if (allDataRows.containsKey(sheetName)) {
                    List<Row> sheetDataRows = allDataRows.get(sheetName);
                    writeDataToSheet(sheetDataRows, mergedSheet, mergedWorkbook, sheetInfo, styleCache);
                }
            }

            // 6. 将工作簿写入ZIP文件
            return createZipFile(mergedWorkbook);
        }
    }

    /**
     * 检测数据开始行号
     */
    private int findDataStartRow(Sheet sheet) {
        // 优化：最多检查前10行
        int maxCheckRows = Math.min(10, sheet.getLastRowNum());

        for (int rowNum = 0; rowNum <= maxCheckRows; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                // 第一列
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

    /**
     * 获取指定sheet的所有数据行对象集合
     */
    private List<Row> getDataRows(Sheet sheet, int dataStartRow) {
        List<Row> dataRows = new ArrayList<>();
        for (int rowNum = dataStartRow; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                dataRows.add(row);
            }
        }
        return dataRows;
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
     * 将数据写入sheet（带样式）
     */
    private void writeDataToSheet(List<Row> dataRows, XSSFSheet targetSheet,
                                  Workbook targetWorkbook, SheetInfo sheetInfo, Map<CellStyle, CellStyle> styleCache) {
        int startRowNum = sheetInfo.getHeaderRows().size();

        for (int i = 0; i < dataRows.size(); i++) {
            Row sourceRow = dataRows.get(i);
            Row targetRow = targetSheet.createRow(startRowNum + i);

            // 设置行高
            if (sourceRow.getHeight() >= 0) {
                targetRow.setHeight(sourceRow.getHeight());
            }

            // 复制每个单元格
            for (int colNum = 0; colNum < sheetInfo.getColumnCount(); colNum++) {
                Cell sourceCell = sourceRow.getCell(colNum);
                if (sourceCell != null) {
                    Cell targetCell = targetRow.createCell(colNum);
                    copyCellWithStyle(sourceCell, targetCell, targetWorkbook, styleCache);
                }
            }
        }
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