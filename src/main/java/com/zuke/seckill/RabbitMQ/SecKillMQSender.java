package com.zuke.seckill.RabbitMQ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

//消息生产者
@Service
@Slf4j
public class SecKillMQSender {
    @Resource
    private RabbitTemplate rabbitTemplate;

    //发送秒杀消息
    public void SenKillSendMessage(String msg){
        log.info("发送消息 --> {}", msg);
        rabbitTemplate.convertAndSend("secKillExchange","secKill.message",msg);
    }

}
