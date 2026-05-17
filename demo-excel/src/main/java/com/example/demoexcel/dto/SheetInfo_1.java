package com.example.demoexcel.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;
import java.util.Map;

/**
 * Sheet信息类
 */
@Setter
@Getter
public class SheetInfo_1 {
    private String sheetName;
    private int dataStartRow;
    private int columnCount;

    // 轻量级存储：不保存Row对象，只保存数据
    private List<List<Object>> headerData;

    // 其他元数据
    private List<CellRangeAddress> mergedRegions;
    private Map<Integer, Integer> columnWidths;

    // 原方法兼容
    public void setHeaderRows(List<?> headerRows) {
        // 空实现，不再保存Row对象
    }
}
