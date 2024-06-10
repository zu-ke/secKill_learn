package com.zuke.seckill.RabbitMQ;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

//消息消费者
@Service
public class MQReceiver {
    //装配RabbitTemplate --> 操作RabbitMQ
    @Resource
    private RabbitTemplate rabbitTemplate;

    //消费者：接收消息
    //指定接收消息的队列queue
    @RabbitListener(queues = "queue")
    public void receiver(Object msg) {
        System.out.println("从队列queue接收消息-->" + msg);
    }

    //消费者：接收消息
    //指定接收消息的队列queue_fanout01
    @RabbitListener(queues = "queue_fanout01")
    public void receiver1(Object msg) {
        System.out.println("从队列queue_fanout01接收消息-->" + msg);
    }
    //指定接收消息的队列queue_fanout02
    @RabbitListener(queues = "queue_fanout02")
    public void receiver2(Object msg) {
        System.out.println("从队列queue_fanout02接收消息-->" + msg);
    }

    //消费者：接收消息
    //指定接收消息的队列queue_direct01
    @RabbitListener(queues = "queue_direct01")
    public void receiver3(Object msg) {
        System.out.println("从队列queue_direct02接收消息-->" + msg);
    }
    //指定接收消息的队列queue_direct02
    @RabbitListener(queues = "queue_direct02")
    public void receiver4(Object msg) {
        System.out.println("从队列queue_direct02接收消息-->" + msg);
    }

    //消费者：接收消息
    //指定接收消息的队列queue_topic01
    @RabbitListener(queues = "queue_topic01")
    public void receiver5(Object msg) {
        System.out.println("从队列queue_topic01接收消息-->" + msg);
    }
    //指定接收消息的队列queue_topic02
    @RabbitListener(queues = "queue_topic02")
    public void receiver6(Object msg) {
        System.out.println("从队列queue_topic02接收消息-->" + msg);
    }
}
