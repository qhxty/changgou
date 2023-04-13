package com.changgou.goods.dao;

import com.changgou.goods.pojo.Sku;
import com.changgou.order.pojo.OrderItem;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SkuMapper extends Mapper<Sku> {
    @Update("UPDATE tb_sku set num=num-#{num},sale_num=sale_num+#{num} where id = #{skuId} and num>=#{num}")
    int decrCount(OrderItem orderItem);

    @Update("UPDATE tb_sku set num=num+#{num},sale_num=sale_num-#{num} where id = #{skuId}")
    void resumeStockNum(String skuId,Integer num);
}
