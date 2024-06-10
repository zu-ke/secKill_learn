package com.zuke.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zuke.seckill.entity.User;
import com.zuke.seckill.vo.LoginVo;
import com.zuke.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserService extends IService<User> {

    //用户登录校验
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    //根据cookie-ticket获取用户
    User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);

    //更新用户信息
    RespBean updatePassword(User user, String password, String ticket, HttpServletRequest request, HttpServletResponse response);
}
