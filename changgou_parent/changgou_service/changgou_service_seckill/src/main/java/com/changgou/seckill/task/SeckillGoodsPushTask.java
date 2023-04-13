package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.stylesheets.LinkStyle;
import tk.mybatis.mapper.entity.Example;

import java.sql.DatabaseMetaData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillGoodsPushTask {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillMapper seckillMapper;

    private static final String SECKILL_GOODS_KEY = "seckill_goods_";

    @Scheduled(cron = "0/30 * * * * ?")
    public void loadSeckillGoodsToRedis() {
        //获取事件列表集合
        List<Date> dateMenus = DateUtil.getDateMenus();
        for(Date dateMenu:dateMenus) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String redisExtName = DateUtil.date2Str(dateMenu);

            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status",1);
            criteria.andGreaterThan("stockCount",0);

            criteria.andGreaterThanOrEqualTo("startTime",simpleDateFormat.format(dateMenu));
            criteria.andLessThan("endTime",simpleDateFormat1.format(DateUtil.addDateHour(dateMenu,2)));

            Set keys = redisTemplate.boundHashOps(SECKILL_GOODS_KEY+redisExtName).keys();
            if(keys!=null && keys.size()>0) {
                criteria.andNotIn("id",keys);
            }
            List<SeckillGoods> seckillGoodsList = seckillMapper.selectByExample(example);

            for(SeckillGoods seckillGoods:seckillGoodsList) {
                redisTemplate.opsForHash().put(SECKILL_GOODS_KEY+redisExtName,seckillGoods.getId(),seckillGoods);
            }
        }
    }

}
