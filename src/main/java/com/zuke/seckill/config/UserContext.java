package com.zuke.seckill.config;

import com.zuke.seckill.entity.User;

public class UserContext {
    //每个线程都有自己的threadLocal，把共享数据存放到这里，保证线程安全
    private static ThreadLocal<User> userThreadLocal = new ThreadLocal<User>();

    public static void setUser(User user) {
        userThreadLocal.set(user);
    }

    public static User getUser() {
        return userThreadLocal.get();
    }
}
