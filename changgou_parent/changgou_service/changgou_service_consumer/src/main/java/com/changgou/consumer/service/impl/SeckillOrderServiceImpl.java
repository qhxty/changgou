package com.changgou.consumer.service.impl;

import com.changgou.consumer.dao.SeckillMapper;
import com.changgou.consumer.dao.SeckillOrderMapper;
import com.changgou.consumer.service.SeckillOrderService;
import com.changgou.seckill.pojo.SeckillOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private SeckillMapper seckillMapper;

    //添加订单
    @Override
    @Transactional
    public int createOrder(SeckillOrder seckillOrder) {
        int result = seckillMapper.updateStockCount(seckillOrder.getSeckillId());
        if(result<=0) {
            return 0;
        }
        result = seckillOrderMapper.updateStockCount(seckillOrder.getSeckillId());
        if(result<=0) {
            return 0;
        }
        return 1;
    }
}
