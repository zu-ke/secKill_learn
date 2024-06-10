package com.zuke.seckill.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuke.seckill.annotation.AccessLimit;
import com.zuke.seckill.entity.User;
import com.zuke.seckill.service.UserService;
import com.zuke.seckill.util.CookieUtil;
import com.zuke.seckill.vo.RespBean;
import com.zuke.seckill.vo.RespBeanEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

//自定义拦截器
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;

    //执行目标方法之前，执行此方法
    //完成：【1】得到user对象,然后放入threadLocal。【2】处理@AccessLimit
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            //获取到登录的user对象
            User user = getUser(request, response);
            //存入threadLocal
            UserContext.setUser(user);
            //把handle转成handleMethod
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //拿到目标方法的AccessLimit注解
            AccessLimit accessLimitAnnotation = handlerMethod.getMethodAnnotation(AccessLimit.class);
            if (accessLimitAnnotation == null) {
                //该接口没有限流防刷业务，直接放行
                return true;
            }
            //获取AccessLimit注解的值
            //生效时间
            int seconds = accessLimitAnnotation.seconds();
            //时间内最大访问次数
            int maxCount = accessLimitAnnotation.maxCount();
            //是否需要登录才能访问该接口
            boolean needLogin = accessLimitAnnotation.needLogin();
            if (needLogin) {
                //需要登录才能访问该接口
                if (user == null) {
                    //说明用户没有登录
                    render(response, RespBeanEnum.NOT_LOGIN);
                    return false;
                }
            }
            //排除只需要登录的接口
            if (seconds == -1 || maxCount == -1){
                return true;
            }
            //业务逻辑：加入redis计数器，完成对用户的限流防刷
            //uri：/seckill/path
            String uri = request.getRequestURI();
            ValueOperations ops = redisTemplate.opsForValue();
            String key = uri + ":" + user.getId();
            Integer count = (Integer) ops.get(key);
            if (count == null) {
                //说明是第一次访问
                ops.set(key, seconds, maxCount, TimeUnit.SECONDS);
            } else if (count >= 1 && count <= maxCount) {
                ops.increment(key);
            } else {
                render(response, RespBeanEnum.FREQUENT_VISITS);
                return false;
            }
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    //构建返回对象 -- 以流的方式
    private void render(HttpServletResponse response, RespBeanEnum respBeanEnum) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        //构建respBean
        RespBean error = RespBean.error(respBeanEnum);
        writer.write(new ObjectMapper().writeValueAsString(error));
        writer.flush();
        writer.close();
    }

    //根据cookie中的userTicket得到user对象
    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        String userTicket = CookieUtil.getCookieValue(request, "userTicket");
        if (StringUtils.isNotEmpty(userTicket)) {
            User user = userService.getUserByCookie(userTicket, request, response);
            return user;
        }
        return null;
    }
}
