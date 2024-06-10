package com.zuke.seckill.controller;

import com.zuke.seckill.service.UserService;
import com.zuke.seckill.vo.LoginVo;
import com.zuke.seckill.vo.RespBean;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Resource
    private UserService userService;

    //前往登录页面
    @RequestMapping("/toLogin")
    public String index() {
        //前往 /template/login.html
        return "login";
    }

    //登录
    @RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(@Validated LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        RespBean respBean = userService.doLogin(loginVo, request, response);
        return respBean;
    }
}
