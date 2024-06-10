package com.zuke.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQTopicConfig {
    //定义队列名和交换机以及路由
    private static final String QUEUE01 = "queue_topic01";
    private static final String QUEUE02 = "queue_topic02";
    private static final String EXCHANGE = "topicExchange";
    //*可以匹配一个，#可以匹配0个或者多个
    private static final String ROUTING_KEY01 = "#.queue.#";
    private static final String ROUTING_KEY02 = "*.queue.#";

    //创建队列
    @Bean
    public Queue queue_topic01(){
        return new Queue(QUEUE01);
    }
    @Bean
    public Queue queue_topic02(){
        return new Queue(QUEUE02);
    }

    //创建交换机
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(EXCHANGE);
    }

    //将队列绑定到交换机,同时指定路由
    @Bean
    public Binding binding_topic01(){
        return BindingBuilder.bind(queue_topic01()).to(topicExchange()).with(ROUTING_KEY01);
    }
    @Bean
    public Binding binding_topic02(){
        return BindingBuilder.bind(queue_topic02()).to(topicExchange()).with(ROUTING_KEY02);
    }
}
