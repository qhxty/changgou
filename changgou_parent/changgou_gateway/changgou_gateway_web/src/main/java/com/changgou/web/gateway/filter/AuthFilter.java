package com.changgou.web.gateway.filter;

import com.changgou.web.gateway.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    public static final String Authorization = "Authorization";

    private static final String LOGIN_URL="http://localhost:9200/oauth/toLogin";

    @Autowired
    private AuthService authService;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取当前对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();
        if("/api/oauth/login".equals(path)) {
            //放行登录接口
            return chain.filter(exchange);
        }
        //判断cookie有没有jti
        String jti = authService.getJtiFromCookie(request);
        if(StringUtils.isEmpty(jti)) {
            //拒绝访问
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            return response.setComplete();
            return this.toLoginPage(LOGIN_URL+"?FROM="+request.getURI(),exchange);
        }
        //判断redis是否存在token
        String redisToken = authService.getTokenFromRedis(jti);
        if(StringUtils.isEmpty(redisToken)) {
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            return response.setComplete();
            return this.toLoginPage(LOGIN_URL,exchange);
        }
        //校验通过
        request.mutate().header(Authorization,"Bearer " + redisToken);
        return chain.filter(exchange);
    }
    //跳转登录页面方法
    private Mono<Void> toLoginPage(String loginurl,ServerWebExchange exchange) {
        //获取登录结果
        ServerHttpResponse response = exchange.getResponse();
        //结果为未登录
        response.setStatusCode(HttpStatus.SEE_OTHER);
        //获取头信息，跳转到loginurl
        response.getHeaders().set("Location",loginurl);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
