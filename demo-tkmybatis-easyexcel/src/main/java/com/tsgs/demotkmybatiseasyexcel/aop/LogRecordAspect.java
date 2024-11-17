package com.tsgs.demotkmybatiseasyexcel.aop;

import com.tsgs.demotkmybatiseasyexcel.annotation.LogRecord;
import com.tsgs.demotkmybatiseasyexcel.entity.TbLogRecord;
import com.tsgs.demotkmybatiseasyexcel.mapper.LogRecordMapper;
import jakarta.annotation.Resource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;

@Aspect
@Component
public class LogRecordAspect {

    @Resource
    private LogRecordMapper logRecordMapper;

    @After("@annotation(com.tsgs.demotkmybatiseasyexcel.annotation.LogRecord)")
    public void logRecord(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogRecord logRecord = method.getAnnotation(LogRecord.class);
        TbLogRecord tbLogRecord = new TbLogRecord(method.getName(), logRecord.methodName());
        tbLogRecord.setCreateDate(new Date());
        logRecordMapper.insertUseGeneratedKeys(tbLogRecord);
    }
}
