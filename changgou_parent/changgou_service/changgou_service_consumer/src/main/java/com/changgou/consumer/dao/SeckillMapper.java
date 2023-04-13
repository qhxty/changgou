package com.changgou.consumer.dao;

import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SeckillMapper extends Mapper<SeckillGoods> {
    @Update("update tb_seckill_goods set stock_count=stock_count-1 where id=#{id} and stock_count>=1")
    int updateStockCount(@Param("seckillId") Long seckillOrder);
}
