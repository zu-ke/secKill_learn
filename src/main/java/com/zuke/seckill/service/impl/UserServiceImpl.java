package com.zuke.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zuke.seckill.entity.User;
import com.zuke.seckill.exception.GlobalException;
import com.zuke.seckill.mapper.UserMapper;
import com.zuke.seckill.service.UserService;
import com.zuke.seckill.util.CookieUtil;
import com.zuke.seckill.util.MD5Util;
import com.zuke.seckill.util.UUIDUtil;
import com.zuke.seckill.vo.LoginVo;
import com.zuke.seckill.vo.RespBean;
import com.zuke.seckill.vo.RespBeanEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

    //用户登录校验
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        //这里已经是中间密码 midPass
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        //判断手机号是否为空
        //if (!StringUtils.isNotEmpty(mobile) || !StringUtils.isNotEmpty(password)) {
        //    return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        //}

        //校验手机号码是否合格
        //if (!ValidatorUtil.isMobile(mobile)) {
        //    return RespBean.error(RespBeanEnum.MOBILE_ERROR);
        //}

        //查询db，用户是否存在
        User user = userMapper.selectById(mobile);
        //说明用户不存在
        if (user == null) {
            //return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            //也可以这样
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        //用户存在，比对密码
        //密码错误
        if (!MD5Util.midPassToDbPass(password, user.getSlat()).equals(user.getPassword())) {
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }

        //用户存在，密码正确，登录成功
        //给每个用户生成票据【唯一】
        String ticket = UUIDUtil.uuid();
        //HttpSession session = request.getSession();
        //session.setAttribute(ticket, user);

        //为了实现分布式session，把登录的用户存放到redis
        System.out.println("使用--> redisTemplate:" + redisTemplate.hashCode());
        redisTemplate.opsForValue().set("user:" + ticket, user);

        //将ticket保存到cookie
        CookieUtil.setCookie(request, response, "userTicket", ticket);

        return RespBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {

        if (!StringUtils.isNotEmpty(userTicket)) {
            return null;
        }

        //根据userTicket到redis获取user
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);

        //如果用户不为空，则刷新cookie
        if (user != null) {
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }

        return user;
    }

    @Override
    public RespBean updatePassword(User user,
                                   String password,
                                   String ticket,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        if (user == null) {
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXISTS);
        }
        user.setPassword(MD5Util.inputPassToDbPass(password, user.getSlat()));
        if (userMapper.updateById(user) == 1) {
            //清除redis该用户的缓存
            redisTemplate.delete("user:" + ticket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_ERROR);
    }

}
