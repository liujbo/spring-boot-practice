package com.tsgs.demomybatispluseasyexcel.listener;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.tsgs.demomybatispluseasyexcel.domain.KyrjNonBasicUseListDomain;
import com.tsgs.demomybatispluseasyexcel.util.BizException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 非基础类使用清单导入监听器
 */
@Slf4j
@Getter
public class NonBasicUseListDomainListener extends AnalysisEventListener<KyrjNonBasicUseListDomain> {

    private final List<KyrjNonBasicUseListDomain> nonBasicUseListDomainList = new ArrayList<>();

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        int count = 0;
        Field[] declaredFields = KyrjNonBasicUseListDomain.class.getDeclaredFields();
        for (Field field : declaredFields) {
            ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
            if (annotation != null) {
                count = count + 1;
                String headName = headMap.get(annotation.index());
                if (StringUtils.isEmpty(headName) || !headName.equals(annotation.value()[0])) {
                    throw new BizException("模板错误，请检查导入模板");
                }
            }
        }
        if (count != headMap.size()) {
            throw new BizException("模板错误，请检查导入模板");
        }
    }

    @Override
    public void invoke(KyrjNonBasicUseListDomain data, AnalysisContext context) {
        data.setStartUseDate(new Date());
        data.setOperateState("1");
        nonBasicUseListDomainList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("全部解析完成");
    }
}
