package com.changgou.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.config.ConfirmMessageSender;
import com.changgou.seckill.config.RabbitMQConfig;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.util.IdWorker;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ConfirmMessageSender confirmMessageSender;
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    private static final String SECKILL_GOODS_KEY = "seckill_goods_";
    private static final String SECKILL_GOODS_STOCK_COUNT_KEY = "seckill_goods_stock_count_";

    @Override
    public boolean add(Long id, String time, String username) {
        //防止恶意刷单
        String preventRepeatCommit = this.preventRepeatCommit(username,id);
        if("fail".equals(preventRepeatCommit)) {
            return false;
        }

        //判断用户是否购买过
        SeckillOrder seckillOrder = seckillOrderMapper.getSeckillOrderByUsernameAndGoodsId(username,id);
        if(seckillOrder != null) {
            //买过，不能再买
            return false;
        }

        //获取商品数据
        SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps(SECKILL_GOODS_KEY+time).get(id);
        //获取库存
        String redisStock = (String) redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_COUNT_KEY+id);

        //库存为0
        if(StringUtils.isEmpty(redisStock)) {
            return false;
        }
        int value = Integer.parseInt(redisStock);
        if(goods==null||value<=0) {
            return false;
        }

        //redis预扣库存
        Long stockCount = redisTemplate.boundValueOps(SECKILL_GOODS_STOCK_COUNT_KEY+id).decrement();
        if(stockCount<=0) {
            //库存没了
            //在redis删除
            redisTemplate.boundHashOps(SECKILL_GOODS_KEY+time).delete(id);
            //删除对应库存信息
            redisTemplate.delete(SECKILL_GOODS_STOCK_COUNT_KEY+id);
        }
        //有库存,创建秒杀订单
//        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(id);
        seckillOrder.setMoney(goods.getCostPrice());
        seckillOrder.setUserId(username);
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");

        confirmMessageSender.sendMessage("", RabbitMQConfig.SECKILL_ORDER_KEY, JSON.toJSONString(seckillOrder));
        return true;
    }

    //防止重复提交
    private String preventRepeatCommit(String username, Long id) {
        String redisKey = "seckill_user_" + username + "_id_" + id;
        long count = redisTemplate.opsForValue().increment(redisKey,1);
        if(count == 1) {
            redisTemplate.expire(redisKey,5, TimeUnit.MINUTES);
            return "success";
        }
        if(count > 1) {
            return "fail";
        }
        return "fail";
    }
}
