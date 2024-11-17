package com.tsgs.demotkmybatiseasyexcel.util;

import java.util.Random;

public class UserIdGenerator {

    public static String generateUserId() {
        long timeMillis = System.currentTimeMillis();

        Random random = new Random();
        int randomSuffix = random.nextInt(999999);
        // 格式化为六位数字字符串，不足六位前面补0
        String randomSuffixStr = String.format("%06d", randomSuffix);

        // 拼接字符串并返回
        return String.valueOf(timeMillis) + randomSuffixStr;
    }
}
