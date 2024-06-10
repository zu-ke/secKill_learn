package com.zuke.seckill.RabbitMQ;

import cn.hutool.json.JSONUtil;
import com.zuke.seckill.entity.SecKillMessage;
import com.zuke.seckill.entity.User;
import com.zuke.seckill.service.GoodsService;
import com.zuke.seckill.service.OrderService;
import com.zuke.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

//消息消费者
@Service
@Slf4j
public class SecKillMQReceiver {
    @Resource
    private GoodsService goodsService;
    @Resource
    private OrderService orderService;

    //接收消息，并且完成下单操作
    @RabbitListener(queues = "secKillQueue")
    public void queue(String msg) {
        log.info("接收到的消息 --> {}", msg);
        //将msg转成SecKillMessage对象
        SecKillMessage secKillMessage = JSONUtil.toBean(msg, SecKillMessage.class);
        //参与秒杀的用户
        User user = secKillMessage.getUser();
        //秒杀的商品id
        long goodsId = secKillMessage.getGoodsId();
        //得到对应的商品信息
        GoodsVo goodsVo = goodsService.toDetailByGoodsId(goodsId);
        //【下单】
        orderService.secKill(user, goodsVo);
    }

}
