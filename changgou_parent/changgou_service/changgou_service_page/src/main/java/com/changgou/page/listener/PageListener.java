package com.changgou.page.listener;

import com.changgou.page.config.RabbitMqConfig;
import com.changgou.page.service.PageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListeners;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PageListener {
    @Autowired
    private PageService pageService;

    @RabbitListener(queues = RabbitMqConfig.PAGE_CREATE_QUEUE)
    public void receiveMessage(String spuId) {
        System.out.println("spuId:" + spuId);
        pageService.generateItemPage(spuId);
    }
}
