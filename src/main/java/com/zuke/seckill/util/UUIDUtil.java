package com.zuke.seckill.util;

import java.util.UUID;

//生成uuid的工具类
public class UUIDUtil {

    public static String uuid(){
        // .replace("-","")： 去除默认生成的uuid中的“-”符号
        return UUID.randomUUID().toString().replace("-","");
    }
}
