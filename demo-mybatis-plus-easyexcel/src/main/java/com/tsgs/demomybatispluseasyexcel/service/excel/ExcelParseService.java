package com.tsgs.demomybatispluseasyexcel.service.excel;

import com.alibaba.excel.EasyExcel;
import com.tsgs.demomybatispluseasyexcel.listener.excel.ExcelParseListener;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author liujunbo
 * @version 1.0
 * @description Excel解析service
 * @since 2025/4/19 20:44
 */
@Service
@Slf4j
public class ExcelParseService {

    public Set<String> parseExcel(MultipartFile file) {
        Set<String> result = new HashSet<>();

        try (InputStream inputStream = file.getInputStream()) {
            EasyExcel.read(inputStream)
                    .headRowNumber(1)
                    .registerReadListener(new ExcelParseListener(result))
                    .sheet().doRead();
        } catch (Exception e) {
            log.info("解析Excel异常" + e.getMessage());
        }
        return result;
    }

    /**
     * 解析Excel同时导出过滤后的Excel文件
     *
     * @param file     原始文件
     * @param response 响应对象
     */
    public void parseAndDownload(MultipartFile file, HttpServletResponse response) {
        try (InputStream inputStream = file.getInputStream();
             Workbook originalWorkbook = WorkbookFactory.create(inputStream);
             SXSSFWorkbook filteredWorkbook = new SXSSFWorkbook(200)) {


            // 处理所有sheet
            for (int i = 0; i < originalWorkbook.getNumberOfSheets(); i++) {
                Sheet originalSheet = originalWorkbook.getSheetAt(i);
                String sheetName = originalSheet.getSheetName();

                if (i == 0) { // 假设第一个sheet是需要过滤的
                    Set<Integer> filteredRowIndices = new HashSet<>();
                    int headRowIndex = 0;

                    // 获取数据开始行号（最多查询 10行）
                    for (int j = 0; j < 10; j++) {
                        Row headRow = originalSheet.getRow(j);
                        if (headRow == null) {
                            continue;
                        }
                        Cell headRowCell = headRow.getCell(0);
                        if (headRowCell == null) {
                            continue;
                        }
                        if ("机构编号".equals(headRowCell.getStringCellValue())) {
                            headRowIndex = j + 1;
                            break;
                        }
                    }

                    // 遍历第一列(假设机构编号在第一列，索引0)
                    for (Row row : originalSheet) {
                        Cell cell = row.getCell(0);
                        if (cell != null) {
                            String cellValue = getCellValueAsString(cell);
                            if ("10025".equals(cellValue)) {
                                filteredRowIndices.add(row.getRowNum());
                            }
                        }
                    }

                    // 创建新sheet
                    Sheet newSheet = filteredWorkbook.createSheet(sheetName);
                    copyDataValidations(originalSheet, newSheet);

                    // 过滤数据放入新sheet中
                    filterSheet(originalSheet, newSheet, headRowIndex, filteredRowIndices);
                } else {
                    // 其他sheet直接复制
                    Sheet newSheet = filteredWorkbook.createSheet(sheetName);
                    copySheet(originalSheet, newSheet);
                }
            }

            // 直接写入响应输出流
            filteredWorkbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.info("解析Excel异常：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 过滤sheet，只保留机构编号为targetOrgCode的行
     *
     * @param originalSheet 原始Excel第一个sheet
     * @param newSheet      新文件 sheet
     * @param headRowIndex  原始Excel第一个sheet的标题行号
     * @param targetOrgCode 需要过滤的机构号
     */
    private void filterSheet(Sheet originalSheet, Sheet newSheet, int headRowIndex, Set<Integer> filteredRowIndices) {
        int rowNum = 0;
        // 首先复制标题行（假设第一行是标题）
        if (originalSheet.getPhysicalNumberOfRows() > 0) {
            if (0 == headRowIndex) {
                Row headerRow = originalSheet.getRow(0);
                if (headerRow != null) {
                    Row newHeaderRow = newSheet.createRow(rowNum++);
                    copyRow(headerRow, newHeaderRow, originalSheet, newSheet);
                }
            } else {
                // 存在多级标题行
                for (int i = 0; i < headRowIndex; i++) {
                    Row headerRow = originalSheet.getRow(i);
                    if (headerRow != null) {
                        Row newHeaderRow = newSheet.createRow(rowNum++);
                        copyRow(headerRow, newHeaderRow, originalSheet, newSheet);
                    }
                }
            }
        }

        // 过滤数据行
        Row originalRow;
        Row newRow;
        for (int i = 1; i <= originalSheet.getLastRowNum(); i++) {
            originalRow = originalSheet.getRow(i);
            if (originalRow == null) continue;
            if (filteredRowIndices.contains(originalRow.getRowNum())) {
                // 匹配到相同机构号的数据行，进行复制
                newRow = newSheet.createRow(rowNum++);
                // 复制行数据
                copyRow(originalRow, newRow, originalSheet, newSheet);
            }
        }
    }

    /**
     * 复制整个sheet
     */
    private void copySheet(Sheet originalSheet, Sheet newSheet) {
        int rowNum = 0;
        for (Row originalRow : originalSheet) {
            Row newRow = newSheet.createRow(rowNum++);
            copyRow(originalRow, newRow, originalSheet, newSheet);
        }
    }

    /**
     * 获取单元格的字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理整数和小数情况
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (int) numValue) {
                        return String.valueOf((int) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // 对于公式单元格，可以计算其值或直接返回公式
                switch (cell.getCachedFormulaResultType()) {
                    case STRING:
                        return cell.getStringCellValue().trim();
                    case NUMERIC:
                        return String.valueOf(cell.getNumericCellValue());
                    case BOOLEAN:
                        return String.valueOf(cell.getBooleanCellValue());
                    default:
                        return cell.getCellFormula();
                }
            default:
                return "";
        }
    }

    /**
     * 复制行数据
     *
     * @param originalRow   原始Excel第一个sheet的行
     * @param newRow        新Excel的sheet的行
     * @param originalSheet 原始Excel的第一个sheet
     * @param newSheet      新Excel的sheet
     */
    private void copyRow(Row originalRow, Row newRow, Sheet originalSheet, Sheet newSheet) {
        // 复制行高
        newRow.setHeight(originalRow.getHeight());

        // 处理合并区域（有效果）
        for (int i = 0; i < originalSheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = originalSheet.getMergedRegion(i);
            if (mergedRegion.getFirstRow() == originalRow.getRowNum()) {
                // 创建新的合并区域（调整行号）
                CellRangeAddress newMergedRegion = new CellRangeAddress(
                        newRow.getRowNum(),
                        newRow.getRowNum() + (mergedRegion.getLastRow() - mergedRegion.getFirstRow()),
                        mergedRegion.getFirstColumn(),
                        mergedRegion.getLastColumn()
                );
                newSheet.addMergedRegion(newMergedRegion);
            }
        }

        // 复制单元格内容
        for (int i = 0; i < originalRow.getLastCellNum(); i++) {
            Cell originalCell = originalRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Cell newCell = newRow.createCell(i);
            copyCell(originalCell, newCell);
        }
    }

    /**
     * 复制单元格数据 和 样式
     */
    private void copyCell(Cell originalCell, Cell newCell) {
        switch (originalCell.getCellType()) {
            case STRING:
                newCell.setCellValue(originalCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(originalCell)) {
                    newCell.setCellValue(originalCell.getDateCellValue());
                } else {
                    newCell.setCellValue(originalCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                newCell.setCellValue(originalCell.getBooleanCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(originalCell.getCellFormula());
                break;
            default:
                newCell.setCellValue("");
        }

        // 复制单元格样式(有效果)
        CellStyle newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();
        newCellStyle.cloneStyleFrom(originalCell.getCellStyle());
        newCell.setCellStyle(newCellStyle);
    }

    private void copyDataValidations(Sheet srcSheet, Sheet destSheet) {
        DataValidationHelper destHelper = destSheet.getDataValidationHelper();

        for (DataValidation srcValidation : srcSheet.getDataValidations()) {
            DataValidationConstraint srcConstraint = srcValidation.getValidationConstraint();
            DataValidationConstraint destConstraint = null;

            // 根据不同类型创建对应的约束
            switch (srcConstraint.getValidationType()) {
                case DataValidationConstraint.ValidationType.LIST:
                    if (srcConstraint.getFormula1() != null && srcConstraint.getFormula1().startsWith("\"")) {
                        // 处理显式列表值 (如 "值1,值2,值3")
                        String[] explicitValues = srcConstraint.getExplicitListValues();
                        destConstraint = destHelper.createExplicitListConstraint(explicitValues);
                    } else {
                        // 处理公式列表 (如 "=Sheet1!$A$1:$A$10")
                        destConstraint = destHelper.createFormulaListConstraint(srcConstraint.getFormula1());
                    }
                    break;

                case DataValidationConstraint.ValidationType.INTEGER:
                case DataValidationConstraint.ValidationType.DECIMAL:
                    destConstraint = destHelper.createNumericConstraint(
                            srcConstraint.getValidationType(),
                            srcConstraint.getOperator(),
                            srcConstraint.getFormula1(),
                            srcConstraint.getFormula2()
                    );
                    break;

                case DataValidationConstraint.ValidationType.TEXT_LENGTH:
                    destConstraint = destHelper.createTextLengthConstraint(
                            srcConstraint.getOperator(),
                            srcConstraint.getFormula1(),
                            srcConstraint.getFormula2()
                    );
                    break;

                case DataValidationConstraint.ValidationType.DATE:
                    destConstraint = destHelper.createDateConstraint(
                            srcConstraint.getOperator(),
                            srcConstraint.getFormula1(),
                            srcConstraint.getFormula2(),
                            "yyyy-MM-dd" // 可根据需要调整日期格式
                    );
                    break;

                case DataValidationConstraint.ValidationType.TIME:
                    destConstraint = destHelper.createTimeConstraint(
                            srcConstraint.getOperator(),
                            srcConstraint.getFormula1(),
                            srcConstraint.getFormula2()
                    );
                    break;

                default:
                    // 其他类型使用通用方法
                    destConstraint = destHelper.createCustomConstraint(
                            srcConstraint.getFormula1()
                    );
            }

            // 创建数据验证对象
            DataValidation destValidation = destHelper.createValidation(
                    destConstraint,
                    srcValidation.getRegions()
            );

            // 复制验证设置
            destValidation.setEmptyCellAllowed(srcValidation.getEmptyCellAllowed());
            destValidation.setSuppressDropDownArrow(srcValidation.getSuppressDropDownArrow());
            destValidation.setShowErrorBox(srcValidation.getShowErrorBox());
            destValidation.setShowPromptBox(srcValidation.getShowPromptBox());


            destSheet.addValidationData(destValidation);
        }
    }
}
