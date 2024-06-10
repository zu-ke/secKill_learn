package com.zuke.seckill.util;

import org.apache.commons.codec.digest.DigestUtils;

//工具类，根据前面的密码设计方案，提供响应的方法
public class MD5Util {

    //md5加密
    public static String md5(String str) {
        return DigestUtils.md5Hex(str);
    }

    //准备一个salt(盐)
    public static final String SALT = "FdRTBjf69ZmgsQ"; //长度14

    //加密加salt(盐) -> 生成中间密码
    public static String inputPassToMidPass(String inputPass) {
        String str = SALT.charAt(0) + inputPass + SALT.charAt(13);
        return md5(str);
    }

    //第二次加密加盐 -> db中的密码
    public static String midPassToDbPass(String midPass, String salt) {
        String str = salt.charAt(1) + midPass + salt.charAt(5);
        return md5(str);
    }

    //可以将password明文，直接转成DB中的密码
    public static String inputPassToDbPass(String inputPass, String salt) {
        String midPass = inputPassToMidPass(inputPass);
        String dbPass = midPassToDbPass(midPass, salt);
        return dbPass;
    }
}
