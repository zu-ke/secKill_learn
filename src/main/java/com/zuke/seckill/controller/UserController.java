package com.zuke.seckill.controller;

import com.zuke.seckill.entity.User;
import com.zuke.seckill.service.UserService;
import com.zuke.seckill.util.CookieUtil;
import com.zuke.seckill.vo.RespBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    //返回登录的用户信息，同时接收请求携带的参数 -- jmeter
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user) {
        return RespBean.success(user);
    }

    //修改用户密码
    @RequestMapping("/updatePass")
    @ResponseBody
    public RespBean updatePass(String password,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               User user) {
        String userTicket = CookieUtil.getCookieValue(request, "userTicket");
        RespBean respBean = userService.updatePassword(user, password, userTicket, request, response);
        return RespBean.success(respBean);
    }
}
