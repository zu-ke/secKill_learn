package com.zuke.seckill.controller;

import com.zuke.seckill.annotation.AccessLimit;
import com.zuke.seckill.entity.User;
import com.zuke.seckill.service.GoodsService;
import com.zuke.seckill.service.UserService;
import com.zuke.seckill.vo.GoodsVo;
import com.zuke.seckill.vo.RespBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.javassist.compiler.ast.Variable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private UserService userService;

    @Resource
    private GoodsService goodsService;

    //进入到商品列表页 -- 原生到DB获取
    //@RequestMapping("toList")
    ////public String index(Model model,
    ////                    @CookieValue("userTicket") String ticket,
    ////                    HttpServletRequest request,
    ////                    HttpServletResponse response) {
    //public String index(Model model, User user) {
    //    //没有ticket
    //    //if (!StringUtils.isNotEmpty(ticket)) {
    //    //    return "login";
    //    //}
    //
    //    //没登陆过
    //    //User user = (User)session.getAttribute(ticket);
    //    //User user = (User) redisTemplate.opsForValue().get("user:" + ticket);
    //    //User user = userService.getUserByCookie(ticket, request, response);
    //    if (user == null) {
    //        return "login";
    //    }
    //
    //    //携带给model使用
    //    model.addAttribute("user", user);
    //
    //    //将商品列表信息放入到model，携带给下一个模板使用
    //    model.addAttribute("goodsList", goodsService.findGoodsVo());
    //
    //    return "goodsList";
    //}

    //进入到商品列表页 -- 加入redis缓存优化\

    //装配redis模板
    @Resource
    private RedisTemplate redisTemplate;
    //手动渲染需要的模板解析器
    @Resource
    private ThymeleafViewResolver thymeleafViewResolver;

    @RequestMapping(value = "toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    @AccessLimit(seconds = -1, maxCount = -1, needLogin = true)
    public String index(Model model,
                        User user,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        //if (user == null) {
        //    return "login";
        //}
        //不使用上面的方式进行是否登录验证，使用自定义注解

        //先到redis获取页面，如果有就直接返回
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String goodsList = (String) valueOperations.get("goodsList");
        if (StringUtils.isNotEmpty(goodsList)) {
            return goodsList;
        }

        //将user放入到model, 携带该下一个模板使用
        model.addAttribute("user", user);
        //将商品列表信息,放入到model,携带该下一个模板使用
        model.addAttribute("goodsList", goodsService.findGoodsVo());

        //从redis没有获取到页面，手动渲染页面，并且存入到redis
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        goodsList = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if (StringUtils.isNotEmpty(goodsList)) {
            //将该页面保存到redis，并设置60秒失效时间，redis会清除该页面，尽量防止数据被修该
            valueOperations.set("goodsList", goodsList, 60, TimeUnit.SECONDS);
            return goodsList;
        }

        return "goodsList";
    }

    //到DB查询
    //@RequestMapping("/toDetail/{goodsId}")
    //public String findGoodsVoByGoodsId(Model model, User user, @PathVariable("goodsId") Long goodsId) {
    //    if (user == null) {
    //        return "login";
    //    }
    //    GoodsVo goodsVo = goodsService.toDetailByGoodsId(goodsId);
    //    model.addAttribute("goods", goodsVo);
    //    model.addAttribute("user", user);
    //
    //    //当返回秒杀商品详情时，同时返回该商品的秒杀状态和剩余时间
    //    //1.变量secKillStatus 秒杀状态：0 - 秒杀未开始，1 - 秒杀进行中，2 - 秒杀已经结束
    //    //2.变量remainSeconds 剩余次数：>0 - 表示还有多久开始秒杀，0 - 秒杀进行中
    //    Date startDate = goodsVo.getStartDate();
    //    Date endDate = goodsVo.getEndDate();
    //    Date nowDate = new Date();
    //
    //    int secKillStatus = 0;
    //    int remainSeconds = 0;
    //
    //    if (nowDate.before(startDate)) {
    //        //现在的时间(比如9：00)早于开始时间(10:00) --> 还未开始秒杀
    //        remainSeconds = (int) (startDate.getTime() - nowDate.getTime()) / 1000;
    //    } else if (nowDate.after(endDate)) {
    //        //现在的时间(比如10：00)晚于开始时间(9:00) --> 秒杀结束
    //        secKillStatus = 2;
    //        remainSeconds = -1;
    //    } else {
    //        //秒杀进行中
    //        secKillStatus = 1;
    //    }
    //
    //    //放入model，携带给模板使用
    //    model.addAttribute("secKillStatus", secKillStatus);
    //    model.addAttribute("remainSeconds", remainSeconds);
    //
    //    return "goodsDetail";
    //}

    //使用redis进行优化
    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String findGoodsVoByGoodsId(Model model,
                                       User user,
                                       @PathVariable("goodsId") Long goodsId,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        if (user == null) {
            return "login";
        }

        //先到redis获取页面，如果有就直接返回
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String goodsDetail = (String) valueOperations.get("goodsDetail:" + goodsId);
        if (StringUtils.isNotEmpty(goodsDetail)) {
            return goodsDetail;
        }

        GoodsVo goodsVo = goodsService.toDetailByGoodsId(goodsId);
        model.addAttribute("goods", goodsVo);
        model.addAttribute("user", user);

        //当返回秒杀商品详情时，同时返回该商品的秒杀状态和剩余时间
        //1.变量secKillStatus 秒杀状态：0 - 秒杀未开始，1 - 秒杀进行中，2 - 秒杀已经结束
        //2.变量remainSeconds 剩余次数：>0 - 表示还有多久开始秒杀，0 - 秒杀进行中
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();

        int secKillStatus = 0;
        int remainSeconds = 0;

        if (nowDate.before(startDate)) {
            //现在的时间(比如9：00)早于开始时间(10:00) --> 还未开始秒杀
            remainSeconds = (int) (startDate.getTime() - nowDate.getTime()) / 1000;
        } else if (nowDate.after(endDate)) {
            //现在的时间(比如10：00)晚于开始时间(9:00) --> 秒杀结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            //秒杀进行中
            secKillStatus = 1;
        }

        //放入model，携带给模板使用
        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        //从redis没有获取到页面，手动渲染页面，并且存入到redis
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        goodsDetail = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);
        if (StringUtils.isNotEmpty(goodsDetail)) {
            //将该页面保存到redis，并设置60秒失效时间，redis会清除该页面，尽量防止数据被修该
            valueOperations.set("goodsDetail:" + goodsId, goodsDetail, 60, TimeUnit.SECONDS);
            return goodsDetail;
        }

        return "goodsDetail";
    }
}
