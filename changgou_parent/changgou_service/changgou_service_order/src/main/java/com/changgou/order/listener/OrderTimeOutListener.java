package com.changgou.order.listener;

import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderTimeOutListener {
    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = "queue.ordertimeout")
    public void closeOrder(String orderId) {
        System.out.println("接收到关闭订单的消息:" + orderId);
        try {
            orderService.closeOrder(orderId);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
