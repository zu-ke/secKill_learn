package com.zuke.seckill.RabbitMQ;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

//消息生产者
@Service
public class MQSender {
    //装配RabbitTemplate --> 操作RabbitMQ
    @Resource
    private RabbitTemplate rabbitTemplate;

    //生产者：发送消息
    public void sendMessage(Object msg) {
        System.out.println("发送消息-->" + msg);
        //"queue"：指定队列名
        rabbitTemplate.convertAndSend("queue", msg);
    }

    //生产者：发送消息到交换机
    public void sendFanout(Object msg) {
        System.out.println("发送消息-->" + msg);
        //""：代表忽略路由
        rabbitTemplate.convertAndSend("fanoutExchange", "", msg);
    }

    //生产者：发送消息到direct交换机 路由 queue.red
    public void sendRouterRed(Object msg) {
        System.out.println("发送消息-->" + msg);
        //""：代表忽略路由
        rabbitTemplate.convertAndSend("directExchange", "queue.red", msg);
    }
    //生产者：发送消息到direct交换机 路由 green
    public void sendRouterGreen(Object msg) {
        System.out.println("发送消息-->" + msg);
        //""：代表忽略路由
        rabbitTemplate.convertAndSend("directExchange", "queue.green", msg);
    }

    //*可以匹配一个，#可以匹配0个或者多个
    //private static final String ROUTING_KEY01 = "#.queue.#";
    //private static final String ROUTING_KEY02 = "*.queue.#";
    //生产者：发送消息到topic交换机 路由 queue.red.message 匹配第一个路由
    public void sendRouterQueueReMessage(Object msg) {
        System.out.println("发送消息-->" + msg);
        //""：代表忽略路由
        rabbitTemplate.convertAndSend("topicExchange", "queue.red.message", msg);
    }
    //生产者：发送消息到topic交换机 路由 green.queue.green.message 两个路由都匹配
    public void sendRouterGreenQueueGreenMessage(Object msg) {
        System.out.println("发送消息-->" + msg);
        //""：代表忽略路由
        rabbitTemplate.convertAndSend("topicExchange", "green.queue.green.message", msg);
    }




}
