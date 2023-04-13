package com.changgou.web.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //判断cookie是否有jti
    public String getJtiFromCookie(ServerHttpRequest request) {
        HttpCookie httpCookie = request.getCookies().getFirst("uid");
        if(httpCookie != null) {
            return httpCookie.getValue();
        }
        return null;
    }
    //判断redis令牌是否过期
    public String getTokenFromRedis(String jti) {
        String token = stringRedisTemplate.boundValueOps(jti).get();
        return token;
    }
}
