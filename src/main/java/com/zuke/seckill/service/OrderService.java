package com.zuke.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zuke.seckill.entity.Order;
import com.zuke.seckill.entity.User;
import com.zuke.seckill.vo.GoodsVo;

public interface OrderService extends IService<Order> {

    //秒杀
    Order secKill(User user, GoodsVo goodsVo);

    //生成秒杀路径
    String createPath(User user, Long goodsId);
    //对秒杀路径值进行校验
    boolean checkPath(User user, Long goodsId, String path);

    //校验用户输入的验证码
    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
