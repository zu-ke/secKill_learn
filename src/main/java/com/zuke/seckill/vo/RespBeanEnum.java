package com.zuke.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RespBeanEnum {

    //通用信息
    success(200, "SUCCESS"),
    ERROR(500, "服务端异常"),

    //登录信息
    LOGIN_ERROR(500210, "用户账号或者密码错误"),
    MOBILE_ERROR(500211, "手机号格式不正确"),
    MOBILE_NOT_EXISTS(500212, "手机号不存在"),
    BING_ERROR(500213, "参数绑定异常"),
    NOT_LOGIN(500213, "参数绑定异常"),

    //秒杀模块返回信息
    ENTRY_STOCK(500500, "库存不足"),
    REPEAT_STOCK(500501, "该商品每人限购一件"),

    //用户操作
    PASSWORD_UPDATE_ERROR(500502, "该商品每人限购一件"),
    CAPTCHA_CHECK_ERROR(500505, "验证码校验失败"),
    SEC_KILL_RETRY(500506, "本次抢购失败，请再次抢购"),

    //非法操作
    REQUEST_ILLEGAL(500502,"请求非法"),
    SESSION_ERROR(500503,"用户信息有误"),
    SEC_KILL_WAIT(500504,"秒杀排队中"),
    FREQUENT_VISITS(500505,"访问过于频繁，请稍后再试");

    private final Integer code;
    private final String msg;
    }
