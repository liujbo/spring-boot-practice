package com.example.demoexcel.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Excel多sheet拆分
 */
@Service
public class ExcelSplitService {

    /**
     * 拆分Excel
     *
     * @param file           原始文件对象
     * @param targetOrgCodes 目标机构号
     * @return 新工作簿字节输出流
     * @throws Exception 异常信息
     */
    public ByteArrayOutputStream splitExcelByOrgCodes(MultipartFile file, List<String> targetOrgCodes) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             Workbook originalWorkbook = WorkbookFactory.create(inputStream)) {

            // 预先读取所有sheet的结构信息
            Map<Integer, SheetStructure> sheetStructures = readSheetStructures(originalWorkbook);

            ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(zipOutputStream);

            // 为每个目标机构号创建独立的Excel文件
            for (String orgCode : targetOrgCodes) {
                Workbook newWorkbook = createWorkbookForOrg(originalWorkbook, sheetStructures, orgCode);

                // 将工作簿写入ZIP
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                newWorkbook.write(bos);
                newWorkbook.close();

                String filename = orgCode + "_" + file.getOriginalFilename();
                ZipEntry zipEntry = new ZipEntry(filename);
                zipOut.putNextEntry(zipEntry);
                zipOut.write(bos.toByteArray());
                zipOut.closeEntry();
            }

            zipOut.finish();
            zipOut.close();

            return zipOutputStream;
        }
    }

    /**
     * 读取sheet数据结构信息
     *
     * @param workbook 工作簿对象
     * @return sheet数据机构信息map
     */
    private Map<Integer, SheetStructure> readSheetStructures(Workbook workbook) {
        Map<Integer, SheetStructure> structures = new HashMap<>();

        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            SheetStructure structure = analyzeSheetStructure(sheet);
            structures.put(sheetIndex, structure);
        }

        return structures;
    }

    /**
     * 分析sheet结构信息
     *
     * @param sheet sheet对象
     * @return sheet结构信息对象
     */
    private SheetStructure analyzeSheetStructure(Sheet sheet) {
        SheetStructure structure = new SheetStructure();

        // 1. 查找数据开始行
        structure.dataStartRow = findDataStartRow(sheet);

        // 2. 记录列宽（Excel最多256列）
        for (int i = 0; i < 256; i++) {
            int columnWidth = sheet.getColumnWidth(i);
            if (columnWidth > 0) {
                structure.columnWidths.put(i, columnWidth);
            }
        }

        // 3. 分析表头部分
        if (structure.dataStartRow != -1) {
            for (int rowNum = 0; rowNum < structure.dataStartRow; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row != null) {
                    HeaderRowInfo headerRowInfo = new HeaderRowInfo();
                    headerRowInfo.rowHeight = row.getHeight();
                    headerRowInfo.rowIndex = rowNum;

                    // 复制行中每个单元格
                    for (int colNum = 0; colNum < row.getLastCellNum(); colNum++) {
                        Cell cell = row.getCell(colNum);
                        if (cell != null) {
                            headerRowInfo.cells.put(colNum, cell);
                        }
                    }

                    structure.headerRows.add(headerRowInfo);
                }
            }
        }

        // 4. 记录合并单元格信息
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            structure.mergedRegions.add(sheet.getMergedRegion(i));
        }

        return structure;
    }

    /**
     * 创建新的工作簿
     *
     * @param originalWorkbook 原始工作簿对象
     * @param sheetStructures  sheet数据机构信息
     * @param targetOrgCode    目标机构号
     * @return 工作簿对象
     */
    private Workbook createWorkbookForOrg(Workbook originalWorkbook,
                                          Map<Integer, SheetStructure> sheetStructures,
                                          String targetOrgCode) {
        Workbook newWorkbook = new XSSFWorkbook();

        // 为每个sheet创建一个样式缓存，避免重复创建
        Map<CellStyle, CellStyle> styleCache = new HashMap<>();

        for (int sheetIndex = 0; sheetIndex < originalWorkbook.getNumberOfSheets(); sheetIndex++) {
            Sheet originalSheet = originalWorkbook.getSheetAt(sheetIndex);
            SheetStructure structure = sheetStructures.get(sheetIndex);
            Sheet newSheet = newWorkbook.createSheet(originalSheet.getSheetName());

            // 应用sheet结构
            applySheetStructure(originalSheet, newSheet, structure, targetOrgCode, styleCache);
        }

        return newWorkbook;
    }

    /**
     * 应用sheet数据结构信息
     *
     * @param originalSheet 原始工作簿sheet
     * @param newSheet      新工作簿sheet
     * @param structure     sheet数据结构信息
     * @param targetOrgCode 目标机构号
     * @param styleCache    样式信息
     */
    private void applySheetStructure(Sheet originalSheet, Sheet newSheet, SheetStructure structure, String targetOrgCode, Map<CellStyle, CellStyle> styleCache) {

        // 1. 设置列宽
        for (Map.Entry<Integer, Integer> entry : structure.columnWidths.entrySet()) {
            newSheet.setColumnWidth(entry.getKey(), entry.getValue());
        }

        // 2. 复制表头行（包含所有样式）
        for (HeaderRowInfo headerRowInfo : structure.headerRows) {
            Row newRow = newSheet.createRow(headerRowInfo.rowIndex);
            newRow.setHeight(headerRowInfo.rowHeight);

            for (Map.Entry<Integer, Cell> cellEntry : headerRowInfo.cells.entrySet()) {
                Cell originalCell = cellEntry.getValue();
                Cell newCell = newRow.createCell(cellEntry.getKey());

                // 复制单元格内容和样式
                copyCellWithStyle(originalCell, newCell, newSheet.getWorkbook(), styleCache);
            }
        }

        // 3. 复制合并单元格（只复制表头部分的）
        for (CellRangeAddress mergedRegion : structure.mergedRegions) {
            // 只复制表头部分的合并区域
            if (mergedRegion.getLastRow() < structure.dataStartRow) {
                newSheet.addMergedRegion(mergedRegion);
            }
        }

        // 4. 复制符合机构号的数据行
        if (structure.dataStartRow != -1) {
            int newRowIndex = structure.dataStartRow;

            for (int rowNum = structure.dataStartRow; rowNum <= originalSheet.getLastRowNum(); rowNum++) {
                Row originalRow = originalSheet.getRow(rowNum);
                if (originalRow == null) continue;

                Cell orgCodeCell = originalRow.getCell(0);
                if (orgCodeCell == null) continue;

                String orgCode = getCellValueAsString(orgCodeCell);
                if (orgCode == null || !orgCode.equals(targetOrgCode)) continue;

                // 创建新行并复制
                Row newRow = newSheet.createRow(newRowIndex);
                newRow.setHeight(originalRow.getHeight());

                for (int colNum = 0; colNum < originalRow.getLastCellNum(); colNum++) {
                    Cell originalCell = originalRow.getCell(colNum);
                    if (originalCell != null) {
                        Cell newCell = newRow.createCell(colNum);
                        copyCellWithStyle(originalCell, newCell, newSheet.getWorkbook(), styleCache);
                    }
                }

                newRowIndex++;
            }
        }
    }

    /**
     * 复制单元格数据以及样式
     *
     * @param sourceCell     原始工作簿单元格对象
     * @param targetCell     新工作簿单元格对象
     * @param targetWorkbook 新工作簿对象
     * @param styleCache     样式信息
     */
    private void copyCellWithStyle(Cell sourceCell, Cell targetCell, Workbook targetWorkbook, Map<CellStyle, CellStyle> styleCache) {
        // 复制单元格值
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
     * 复制单元格数据
     *
     * @param sourceCell 原始单元格对象
     * @param targetCell 目标单元格对象
     */
    private void copyCellValue(Cell sourceCell, Cell targetCell) {
        if (sourceCell == null) return;

        switch (sourceCell.getCellType()) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(sourceCell)) {
                    targetCell.setCellValue(sourceCell.getDateCellValue());
                    // 复制日期格式
                    CellStyle style = targetCell.getCellStyle();
                    style.setDataFormat(sourceCell.getCellStyle().getDataFormat());
                } else {
                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                targetCell.setCellFormula(sourceCell.getCellFormula());
                break;
            case BLANK:
                targetCell.setBlank();
                break;
            default:
                targetCell.setCellValue("");
        }
    }

    /**
     * 获取当前sheet数据开始行好
     *
     * @param sheet sheet对象
     * @return 数据开始行号
     */
    private int findDataStartRow(Sheet sheet) {
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(0);
                if (cell != null) {
                    String value = getCellValueAsString(cell);
                    if (value != null && value.matches("\\d{5}")) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 获取Cell单元格数据字符串
     *
     * @param cell 单元格对象
     * @return 单元格值字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    /**
     * 内部类：sheet数据结构类
     */
    static class SheetStructure {
        int dataStartRow = -1;
        Map<Integer, Integer> columnWidths = new HashMap<>();
        List<HeaderRowInfo> headerRows = new ArrayList<>();
        List<CellRangeAddress> mergedRegions = new ArrayList<>();
    }

    /**
     * 内部类：表头信息类
     */
    static class HeaderRowInfo {
        int rowIndex;
        short rowHeight;
        Map<Integer, Cell> cells = new HashMap<>();
    }

}