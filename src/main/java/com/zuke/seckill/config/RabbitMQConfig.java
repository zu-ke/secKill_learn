package com.zuke.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//创建队列、交换机......
@Configuration
public class RabbitMQConfig {
    //定义队列名
    private static final String QUEUE = "queue";

    //创建队列
    //true: 表示队列持久化【队列在默认情况下，是放在内存的，RabbitMQ重启后就不在了，如果希望重启后数据队列还能使用，就需要持久化。Erlang自带Mnesia数据库
    // 当RabbitMQ重启后，会读取该数据库，从而恢复数据】

    //fanout
    private static final String QUEUE1 = "queue_fanout01";
    private static final String QUEUE2 = "queue_fanout02";
    private static final String EXCHANGE = "fanoutExchange";

    //--direct---
    private static final String QUEUE_DIRECT1 = "queue_direct01";
    private static final String QUEUE_DIRECT2 = "queue_direct02";
    private static final String EXCHANGE_DIRECT = "directExchange";

    //路由
    private static final String ROUTING_KEY01 = "queue.red";
    private static final String ROUTING_KEY02 = "queue.green";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true);
    }

    //创建队列 QUEUE1 (queue_fanout01)
    @Bean
    public Queue queue1() {
        return new Queue(QUEUE1);
    }


    //创建队列 QUEUE2 (queue_fanout02)
    @Bean
    public Queue queue2() {
        return new Queue(QUEUE2);
    }

    //配置交换机 EXCHANGE(fanoutExchange)
    @Bean
    public FanoutExchange exchange() {
        return new FanoutExchange(EXCHANGE);
    }

    //将QUEUE1 (queue_fanout01)绑定到交换机EXCHANGE(fanoutExchange)
    @Bean
    public Binding binding01() {
        return BindingBuilder.bind(queue1()).to(exchange());
    }

    //将QUEUE2 (queue_fanout02)绑定到交换机EXCHANGE(fanoutExchange)
    @Bean
    public Binding binding02() {
        return BindingBuilder.bind(queue2()).to(exchange());
    }

    //-----direct-----

    //创建/配置队列 QUEUE_DIRECT1 (queue_direct01)
    @Bean
    public Queue queue_direct1() {
        return new Queue(QUEUE_DIRECT1);
    }

    //创建/配置队列 QUEUE_DIRECT2 (queue_direct02)
    @Bean
    public Queue queue_direct2() {
        return new Queue(QUEUE_DIRECT2);
    }

    //创建/配置 交换机 EXCHANGE_DIRECT(directExchange)
    @Bean
    public DirectExchange exchange_direct() {
        return new DirectExchange(EXCHANGE_DIRECT);
    }

     //将队列QUEUE_DIRECT1绑定到指定的交换机EXCHANGE_DIRECT(directExchange)
     //同时声明了/关联路由ROUTING_KEY01(queue.red)
     //队列：queue_direct1()
     //交换机: exchange_direct()
     //路由 ROUTING_KEY01
    @Bean
    public Binding binding_direct1() {
        return BindingBuilder
                .bind(queue_direct1()).to(exchange_direct()).with(ROUTING_KEY01);
    }

    @Bean
    public Binding binding_direct2() {
        return BindingBuilder
                .bind(queue_direct2()).to(exchange_direct()).with(ROUTING_KEY02);
    }

}
