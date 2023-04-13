package com.changgou.system.filter;

import com.changgou.system.util.JwtUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    private static final String AUTHORIZE_TOKEN = "token";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求
        ServerHttpRequest request = exchange.getRequest();
        //获取响应
        ServerHttpResponse response = exchange.getResponse();
        //如果是登录请求直接放行
        if(request.getURI().getPath().contains("/admin/login")) {
            //放行
            return chain.filter(exchange);
        }
        //获取请求头信息
        HttpHeaders headers = request.getHeaders();
        //从头部信息获取令牌
        String token = headers.getFirst(AUTHORIZE_TOKEN);
        //判断请求里有没有token
        if(StringUtils.isEmpty(token)) {
            //没有令牌，直接驳回，无需验证
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //如果请求头有token
        try {
            JwtUtil.parseJWT(token);
        }catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
            return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
