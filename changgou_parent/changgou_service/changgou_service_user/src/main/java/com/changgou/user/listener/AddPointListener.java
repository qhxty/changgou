package com.changgou.user.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.pojo.Task;
import com.changgou.user.config.RabbitMQConfig;
import com.changgou.user.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.stereotype.Component;

@Component
public class AddPointListener {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.CG_BUYING_ADDPOINT)
    public void receiveAddPointMessage(String message) {
        System.out.println("用户服务接收到消息");
        Task task = JSON.parseObject(message, Task.class);
        if(task == null || StringUtils.isEmpty(task.getRequestBody())) {
            return;
        }
        Object value = redisTemplate.boundValueOps(task.getId()).get();
        if(value != null) {
            return;
        }
        int result = userService.updateUserPoint(task);
        if(result == 0) {
            return;
        }
        rabbitTemplate.convertAndSend(RabbitMQConfig.EX_BUYING_ADDPOINTUSER,
                RabbitMQConfig.CG_BUYING_FINISHADDPOINT_KEY,JSON.toJSONString(task));
        System.out.println("用户服务完成添加积分的操作并向队列发送了一条消息");

    }
}
