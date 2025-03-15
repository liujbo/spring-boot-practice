package com.tsgs.demomybatispluseasyexcel.util;

import java.util.UUID;

public class CommonUtil {

    /**
     * 获取UUID
     *
     * @param whetherSimplify 是否去除中划线
     * @return 结果uuid
     */
    public static String generateUuid(boolean whetherSimplify) {
        String uuid = UUID.randomUUID().toString();
        if (whetherSimplify) {
            return uuid.replaceAll("-", "");
        } else {
            return uuid;
        }
    }
}