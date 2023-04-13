package com.changgou.order.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.config.RabbitMQConfig;
import com.changgou.order.pojo.Task;
import com.changgou.order.service.TaskService;
import org.aspectj.lang.annotation.After;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DelTaskListener {
    @Autowired
    private TaskService taskService;

    @RabbitListener(queues = RabbitMQConfig.CG_BUYING_FINISHADDPOINT)
    public void receiveMessage(String message) {
        Task task = JSON.parseObject(message,Task.class);
        taskService.delTask(task);
    }
}
