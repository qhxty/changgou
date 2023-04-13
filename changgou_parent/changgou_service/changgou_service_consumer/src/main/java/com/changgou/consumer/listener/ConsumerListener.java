package com.changgou.consumer.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.consumer.config.RabbitMQConfig;
import com.changgou.consumer.service.SeckillOrderService;
import com.changgou.seckill.pojo.SeckillOrder;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsumerListener {
    @Autowired
    private SeckillOrderService seckillOrderService;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_ORDER_KEY)
    public void receiveSeckillOrderMessage(Channel channel, Message message) {
        //流量削峰
        try {
            //抓取请求的总数
            channel.basicQos(300);
        }catch (Exception e) {
            e.printStackTrace();
        }

        //消息转换
        SeckillOrder seckillOrder = JSON.parseObject(message.getBody(),SeckillOrder.class);

        //同步订单到mysql
        int rows = seckillOrderService.createOrder(seckillOrder);
        if(rows>0) {
            //发送成功通知
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            //发送失败通知
            try {
                //第一个false：true所有消费者会拒绝接收，false只有当前消费者拒绝接收
                //第二个false：true进入死信队列，false回到原队列
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
