package com.zuke.seckill.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

//完成一些校验工作，比如手机号码格式校验
public class ValidatorUtil {

    //校验手机号码的正则表达式
    //合格 13345678912
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[1][3-9][0-9]{9}$");

    //手机号码格式校验
    public static boolean isMobile(String mobile) {
        if (StringUtils.isNoneEmpty(mobile)){
            return MOBILE_PATTERN.matcher(mobile).matches();
        }else {
            return false;
        }
    }
}
