package com.changgou.seckill.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.seckill.config.TokenDecode;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckillorder")
public class SecKillOrderController {
    @Autowired
    private TokenDecode tokenDecode;

    @Autowired
    private SeckillOrderService seckillOrderService;

    //秒杀下单
    @RequestMapping("/add")
    public Result add(@RequestParam("time")String time,@RequestParam("id") Long id) {
        //获取登录人
        String username = tokenDecode.getUserInfo().get("username");
        boolean result = seckillOrderService.add(id,time,username);
        if(result) {
            return new Result(true,StatusCode.OK,"下单成功");
        }else {
            return new Result(false, StatusCode.ERROR,"下单失败");
        }
    }
}
