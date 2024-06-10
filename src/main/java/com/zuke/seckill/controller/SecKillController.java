package com.zuke.seckill.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ramostear.captcha.HappyCaptcha;
import com.ramostear.captcha.common.Fonts;
import com.ramostear.captcha.support.CaptchaStyle;
import com.ramostear.captcha.support.CaptchaType;
import com.zuke.seckill.RabbitMQ.SecKillMQSender;
import com.zuke.seckill.annotation.AccessLimit;
import com.zuke.seckill.entity.*;
import com.zuke.seckill.service.GoodsService;
import com.zuke.seckill.service.OrderService;
import com.zuke.seckill.service.SeckillGoodsService;
import com.zuke.seckill.service.SeckillOrderService;
import com.zuke.seckill.vo.GoodsVo;
import com.zuke.seckill.vo.RespBean;
import com.zuke.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

//implements InitializingBean和实现的方法afterPropertiesSet()解读：
//这个类的属性初始化完毕后，自动调用方法afterPropertiesSet()
@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean {

    //SecKillController类属性初始化完成后自动执行
    //将所有秒杀商品的库存量保存到redis【为了防止redis和mysql数据数据不一致，可以在每次修改商品/库存操作时，设置/更新一下redis中存储的本数据】
    @Override
    public void afterPropertiesSet() throws Exception {
        //查询所有秒杀商品的库存
        List<GoodsVo> goodsVoList = goodsService.findGoodsVo();
        //如果商品为空就返回
        if (CollectionUtils.isEmpty(goodsVoList)) {
            return;
        }
        goodsVoList.forEach(i -> {
            redisTemplate.opsForValue().set("secKillGoods:" + i.getId(), i.getStockCount());

            //初始化entryStockMap
            //设计申明：false表示有库存，true表示没有库存
            entryStockMap.put(i.getId(), false);
        });

    }

    @Resource
    private OrderService orderService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private SecKillMQSender secKillMQSender;
    //装配RedisScript
    @Resource
    private RedisScript<Long> script;

    //定义map，记录秒杀商品是否还有库存
    //使用线程安全的ConcurrentHashMap
    private ConcurrentHashMap<Long, Boolean> entryStockMap = new ConcurrentHashMap<>();

    //处理用户抢购/秒杀请求
    //{path}: 加入秒杀安全
    @RequestMapping("/{path}/doSeckill")
    @ResponseBody
    public RespBean doSecKill(@PathVariable("path") String path, Model model, User user, Long goodsId) {

        //判断是否登录
        //if (user == null) {
        //    return "login";
        //}
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        //校验用户携带的路径是否正确
        boolean checkPath = orderService.checkPath(user, goodsId, path);
        if (!checkPath) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        //model.addAttribute("user", user);

        GoodsVo goodsVo = goodsService.toDetailByGoodsId(goodsId);

        //判断用户是否复购
        //SeckillOrder seckillOrder = seckillOrderService.getOne(
        //        new QueryWrapper<SeckillOrder>()
        //                .eq("user_id", user.getId())
        //                .eq("goods_id", goodsId)
        //);
        //增加代码：【OrderServiceImpl】已经将秒杀订单放入redis，这里直接到redis查询，可以起到优化效果
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get(("order:" + user.getId() + ":" + goodsVo.getId()));
        if (seckillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEAT_STOCK);
            //return "secKillFail";
            return RespBean.error(RespBeanEnum.REPEAT_STOCK);
        }

        //【判断库存应该放在判断是否复购前，防止先预减后，才发现他已经购买过了，导致库存遗留】
        //判断是否还有库存
        //if (goodsVo.getStockCount() < 1) {
        //    model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK);
        //    return "secKillFail";
        //}
        //如果在短时间内，大量抢购冲击DB，造成洪峰，容易压垮数据库，
        //使用redis完成预见库存，如果没有库存了，直接返回，减小对DB的压力。在【SecKillController】完成该操作

        //对entryStockMap尽行判断【内存标记】，如果该商品标记为没有库存，则直接返回，无需进行redis预减
        if (entryStockMap.get(goodsId)) {
            model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK);
            //return "secKillFail";
            return RespBean.error(RespBeanEnum.ENTRY_STOCK);
        }
        //返回减一后的结构【此方法具有原子性】
        //Long decrement = redisTemplate.opsForValue().decrement("secKillGoods:" + goodsId);
        ////当库存还有1时，来了一个用户，预减1后库存为0，此时这个用户购买到最后一个，所以判断应该 decrement <0 而不是 decrement <1
        //if (decrement < 0) {
        //    //库存不足
        //    entryStockMap.put(goodsId, true);
        //    model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK);
        //    //return "secKillFail";
        //    return RespBean.error(RespBeanEnum.ENTRY_STOCK);
        //}

        //====使用redis分布式锁===
        //目前已经完成了正常售卖，由于只使用了一行代码Long decrement = redisTemplate.opsForValue().decrement("secKillGoods:" + goodsId);
        //而且这行代码还具有原子性，所以简单解决了超卖等问题
        //当业务复杂时，一行代码无法处理，就需要分布式锁，扩大隔离范围

        //生成uuid，作为锁的值
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        if (lock) {
            //获取锁成功
            //准备删除锁的脚本
            //String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            //DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            //redisScript.setScriptText(script);
            //redisScript.setResultType(Long.class);

            Long decrement = redisTemplate.opsForValue().decrement("secKillGoods:" + goodsId);
            //当库存还有1时，来了一个用户，预减1后库存为0，此时这个用户购买到最后一个，所以判断应该 decrement <0 而不是 decrement <1
            if (decrement < 0) {
                //库存不足
                entryStockMap.put(goodsId, true);
                //【释放锁 -- lua脚本】
                redisTemplate.execute(script, Arrays.asList("lock"), uuid);
                model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK);
                //return "secKillFail";
                return RespBean.error(RespBeanEnum.ENTRY_STOCK);
            }
            //【释放锁 -- lua脚本】
            redisTemplate.execute(script, Arrays.asList("lock"), uuid);
        } else {
            //获取锁失败，返回信息【本次抢购失败，请再次抢购
            RespBean.error(RespBeanEnum.SEC_KILL_RETRY);
        }

        //抢购/秒杀
        //Order order = orderService.secKill(user, goodsVo);
        //if (order == null) {
        //    model.addAttribute("errmsg", RespBeanEnum.ENTRY_STOCK);
        //    return "secKillFail";
        //}
        //model.addAttribute("order", order);
        //model.addAttribute("goods", goodsVo);
        //return "orderDetail";
        //加入RabbitMQ消息队列，实现秒杀异步请求【防止并发导致线程堆积】。给队列发送消息后立即给请求临时结果“开始秒杀”
        //创建SecKillMessage对象
        SecKillMessage secKillMessage = new SecKillMessage(user, goodsId);
        secKillMQSender.SenKillSendMessage(JSONUtil.toJsonStr(secKillMessage));
        model.addAttribute("errmsg", "排队中...");
        //return "secKillFail";
        return RespBean.error(RespBeanEnum.SEC_KILL_WAIT);
    }

    //获取秒杀路径
    @RequestMapping("/path")
    @ResponseBody
    //使用自定义注解完成对用户的限流防刷
    //5秒超时时间，最大访问次数5，需要登录
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    public RespBean getSecKillPath(HttpServletRequest request, User user, Long goodsId, String captcha) {
        if (user == null || goodsId == null || goodsId < 0) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        //增加业务逻辑：加入redis计数器，完成对用户的限流防刷
        //5s内，访问次数超过5次，认为是在刷接口
        //这里先把代码写在方法中，后面我们使用注解，提高通用性
        //uri：/seckill/path
        //String uri = request.getRequestURI();
        //ValueOperations ops = redisTemplate.opsForValue();
        //String key = uri + ":" + user.getId();
        //Integer count = (Integer) ops.get(key);
        //if (count == null) {
        //    //说明是第一次访问
        //    ops.set(key, 1, 5, TimeUnit.SECONDS);
        //} else if (count >= 1 && count <= 5) {
        //    ops.increment(key);
        //}else {
        //    return RespBean.error(RespBeanEnum.FREQUENT_VISITS);
        //}

        //增加业务逻辑：校验用户输入的验证码是否正确
        boolean captchaCheckResult = orderService.checkCaptcha(user, goodsId, captcha);
        if (!captchaCheckResult) {
            return RespBean.error(RespBeanEnum.CAPTCHA_CHECK_ERROR);
        }

        String path = orderService.createPath(user, goodsId);
        return RespBean.success(path);
    }

    //开源项目 -- happyCaptcha -- 生成验证码
    @RequestMapping("/captcha")
    public void happyCaptcha(HttpServletRequest request, HttpServletResponse response, User user, Long goodsId) {
        HappyCaptcha.require(request, response)
                .style(CaptchaStyle.ANIM)            //设置展现样式为动画
                .type(CaptchaType.NUMBER)            //设置验证码内容为汉字
                .length(6)                            //设置字符长度为6
                .width(220)                            //设置动画宽度为220
                .height(80)                            //设置动画高度为80
                .font(Fonts.getInstance().zhFont())    //设置汉字的字体
                .build().finish();                //生成并输出验证码
        //生成验证码并输出【该验证码默认保存到了session中，key默认为happy-captcha】
        redisTemplate.opsForValue().set(
                "captcha:" + user.getId() + ":" + goodsId,
                request.getSession().getAttribute("happy-captcha"),
                100,
                TimeUnit.SECONDS
        );
    }
}
