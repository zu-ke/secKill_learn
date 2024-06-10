package com.zuke.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//秒杀相关
@Configuration
public class RabbitMQSecKillConfig {
    //定义队列、交换机名
    private static final String QUEUE = "secKillQueue";
    private static final String EXCHANGE = "secKillExchange";

    //创建队列
    @Bean
    public Queue secKillQueue() {
        return new Queue(QUEUE);
    }

    //创建交换机
    @Bean
    public TopicExchange secKillExchange(){
        return new TopicExchange(EXCHANGE);
    }

    //将队列和交换机绑定，并且指定路由
    @Bean
    public Binding binding(){
        return BindingBuilder.bind(secKillQueue()).to(secKillExchange()).with("secKill.#");
    }
}
