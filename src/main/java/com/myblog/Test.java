package com.myblog;

import cn.hutool.core.date.DateUtil;

public class Test {
    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis()/1000);
        System.out.println(DateUtil.currentSeconds());
    }
}
