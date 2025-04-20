package com.tsgs.demomybatispluseasyexcel.listener.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author liujunbo
 * @version 1.0
 * @description Excel解析监听器
 * @since 2025/4/19 20:44
 */
@Slf4j
public class ExcelParseListener extends AnalysisEventListener<Map<Integer, String>> {

    private final Set<String> resultSet;

    public ExcelParseListener(Set<String> resultSet) {
        this.resultSet = resultSet;
    }

    // 每批处理的大小
    private static final int BATCH_SIZE = 5000;
    // 当前批次临时存储
    private List<String> batchList = new ArrayList<>(BATCH_SIZE);
    private int totalCount = 0;
    long startTime = System.currentTimeMillis();

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        batchList.add(data.get(0));
        totalCount++;

        // 达到批处理大小时处理并清空
        if (batchList.size() >= BATCH_SIZE) {

            // 聚合到总集合
            resultSet.addAll(batchList);
            batchList = new ArrayList<>(BATCH_SIZE);

            // 每处理一批记录一次进度
            if (totalCount % 10000 == 0) {
                log.info("已处理 {} 条记录", totalCount);
            }
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!batchList.isEmpty()) {
            resultSet.addAll(batchList);
        }
        long endTime = System.currentTimeMillis();

        log.info("Excel解析完成，共处理 {} 条记录，耗时 {} 秒", totalCount, (endTime - startTime) / 1000.0);
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        log.info("解析下一行");
    }
}
