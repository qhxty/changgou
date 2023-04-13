package com.changgou.seckill.config;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ConfirmMessageSender implements RabbitTemplate.ConfirmCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String MESSAGE_CONFIRM_KEY = "message_confirm_";

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack){
            //成功通知
            //删除redis里的相关数据
            redisTemplate.delete(correlationData.getId());
            redisTemplate.delete(MESSAGE_CONFIRM_KEY+correlationData.getId());
        }else {
            //失败通知
            //从redis里获取刚才消息的内容
            Map<String,String> map = redisTemplate.opsForHash().entries(MESSAGE_CONFIRM_KEY+correlationData.getId());
            //重新发送
            String exchange = map.get("exchange");
            String routingkey = map.get("routing");
            String message = map.get("message");
            rabbitTemplate.convertAndSend(exchange,routingkey, JSON.toJSONString(message));
        }
    }

    //自定义消息发送的方法
    public void sendMessage(String exchange,String routingKey,String message){

        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        redisTemplate.opsForValue().set(correlationData.getId(),message);

        Map<String,String> map = new HashMap<>();
        map.put("exchange",exchange);
        map.put("routingKey",routingKey);
        map.put("message",message);
        redisTemplate.opsForHash().putAll(MESSAGE_CONFIRM_KEY+correlationData.getId(),map);

        rabbitTemplate.convertAndSend(exchange,routingKey,message,correlationData);
    }
}
