package com.changgou.seckill.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    //秒杀商品信息
    public static final String SECKILL_ORDER_KEY = "seckill_order";

    @Bean
    public Queue queue(){
        return new Queue(SECKILL_ORDER_KEY,true);
    }
}
