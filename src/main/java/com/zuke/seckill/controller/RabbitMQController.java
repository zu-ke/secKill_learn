package com.zuke.seckill.controller;

import com.zuke.seckill.RabbitMQ.MQReceiver;
import com.zuke.seckill.RabbitMQ.MQSender;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class RabbitMQController {
    @Resource
    private MQSender mqSender;

    //调用消息生产者，发送消息
    @RequestMapping("/mq")
    @ResponseBody
    public void mq() {
        mqSender.sendMessage("Hi, I'm RabbitMQ!");
    }

    //发送消息到fanoutExchange交换机
    @RequestMapping("/mq/fanout")
    @ResponseBody
    public void sendFanout(Object o) {
        mqSender.sendFanout("Hi, I'm RabbitMQ Fanout!");
    }

    //发送消息到directExchange交换机，并且指定路由
    @RequestMapping("/mq/direct/red")
    @ResponseBody
    public void sendDirectRed(Object o) {
        mqSender.sendRouterRed("Hi, I'm DirectRed!");
    }
    @RequestMapping("/mq/direct/green")
    @ResponseBody
    public void sendDirectGreen(Object o) {
        mqSender.sendRouterGreen("Hi, I'm DirectGreen!");
    }

    //发送消息到topicExchange交换机，并且指定路由【模糊路由】
    //queue.red.message
    @RequestMapping("/mq/topic/01")
    @ResponseBody
    public void sendRouterQueueReMessage(Object o) {
        mqSender.sendRouterQueueReMessage("Hi, I'm queue.red.message!");
    }
    //green.queue.green.message
    @RequestMapping("/mq/topic/02")
    @ResponseBody
    public void sendRouterGreenQueueGreenMessage(Object o) {
        mqSender.sendRouterGreenQueueGreenMessage("Hi, I'm green.queue.green.message!");
    }
}
