package com.changgou.seckill.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.seckill.aspect.AccessLimit;
import com.changgou.seckill.feign.SeckillOrderFeign;
import com.changgou.util.CookieUtil;
import com.changgou.util.RandomUtil;
import org.aspectj.lang.annotation.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/wseckillorder")
public class SeckillOrderController {
    @Autowired
    private SeckillOrderFeign seckillOrderFeign;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/add")
    @ResponseBody
    @AccessLimit//接口进行令牌桶限流
    public Result add(String time, Long id, String code) {
        String cookieValue = this.readCookie();
        String redisCode = (String) redisTemplate.boundValueOps("randomcode_"+cookieValue).get();
        if(!redisCode.equals(code)) {
            return new Result(false, StatusCode.ERROR,"下单失败");
        }

        Result result = seckillOrderFeign.add(time,id);
        return result;
    }

    @GetMapping("/getToken")
    @ResponseBody
    public String getToken() {
        String randomString = RandomUtil.getRandomString();

        String cookieValue = this.readCookie();
        redisTemplate.opsForValue().set("randomcode_"+cookieValue,randomString,5, TimeUnit.SECONDS);
        return randomString;
    }

    private String readCookie() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String cookieValue = CookieUtil.readCookie(request,"uid").get("uid");
        return cookieValue;
    }

}
