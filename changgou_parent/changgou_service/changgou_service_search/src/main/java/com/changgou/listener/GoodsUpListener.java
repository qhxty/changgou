package com.changgou.listener;

import com.changgou.config.RabbitMqConfig;
import com.changgou.service.ESManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsUpListener {
    @Autowired
    private ESManagerService esManagerService;

    @RabbitListener(queues = RabbitMqConfig.SEARCH_ADD_QUEUE)
    public void receiverMessage(String spuId) {
        System.out.println(spuId);
        esManagerService.importDataBySpuId(spuId);
    }
}
