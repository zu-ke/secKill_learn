package com.zuke.seckill.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
public @interface AccessLimit {
    //时间范围
    int seconds();
    //访问最大次数
    int maxCount();
    //是否登录
    boolean needLogin() default true;
}
