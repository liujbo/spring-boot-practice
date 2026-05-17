package com.example.demoexcel.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;
import java.util.Map;

/**
 * Sheet信息类
 */
@Setter
@Getter
public class SheetInfo {
    private String sheetName;
    private List<Row> headerRows;
    private List<CellRangeAddress> mergedRegions;
    private Map<Integer, Integer> columnWidths;
    private int columnCount;
    private int dataStartRow;
}
