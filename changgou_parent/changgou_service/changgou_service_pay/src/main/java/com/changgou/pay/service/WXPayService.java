package com.changgou.pay.service;

import java.util.Map;

public interface WXPayService {
    Map nativePay(String orderId, Integer money);

    //支付成功之后自动修改支付状态
    //根据订单id查询订单然后修改状态
    Map queryOrder(String orderId);

    Map closeOrder(String orderId);
}
