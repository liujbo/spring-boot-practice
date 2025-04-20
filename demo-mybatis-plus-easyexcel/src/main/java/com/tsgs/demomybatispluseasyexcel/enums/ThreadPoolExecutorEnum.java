package com.tsgs.demomybatispluseasyexcel.enums;

import lombok.Getter;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Getter
public enum ThreadPoolExecutorEnum {

    IO(Runtime.getRuntime().availableProcessors() + 1, 2 * Runtime.getRuntime().availableProcessors());

    private final ThreadPoolExecutor threadPoolExecutor;

    ThreadPoolExecutorEnum(Integer corePoolSize, Integer maximumPoolSize) {
        CustomizableThreadFactory customizableThreadFactory = new CustomizableThreadFactory("pool-exec-");
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100), customizableThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

}
