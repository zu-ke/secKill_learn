package com.zuke.seckill.config;


import com.zuke.seckill.entity.User;
import com.zuke.seckill.service.UserService;
import com.zuke.seckill.util.CookieUtil;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    //装配UserService
    @Resource
    private UserService userService;

    //判断你当前要解析的参数类型是不是你需要的?
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //获取参数是不是user类型
        Class<?> aClass = parameter.getParameterType();
        //如果为t, 就执行resolveArgument
        return aClass == User.class;
    }

    //如果上面supportsParameter,返回T,就执行下面的resolveArgument方法
    //到底怎么解析，是由程序员根据业务来编写
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        //HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        //HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        //
        //String ticket = CookieUtil.getCookieValue(request, "userTicket");
        //if (!StringUtils.hasText(ticket)) {
        //    return null;
        //}
        //////从Redis来获取用户
        //User user = userService.getUserByCookie(ticket, request, response);
        //
        //return user;
        return UserContext.getUser();
    }
}
