package com.changgou.pay.service.impl;

import com.changgou.pay.service.WXPayService;
import com.github.wxpay.sdk.WXPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class WXPayServiceImpl implements WXPayService {

    @Autowired
    private WXPay wxPay;

    @Value("${wxpay.notify_url}")
    private String notifyUrl;

    @Override
    public Map nativePay(String orderId, Integer money) {
        try {
            Map<String,String> map = new HashMap<String,String>();
            map.put("body","畅购");
            map.put("out_trade_no",orderId);

            BigDecimal payMoney = new BigDecimal("0.01");
            BigDecimal fen = payMoney.multiply(new BigDecimal("100"));//1.00
            fen = fen.setScale(0,BigDecimal.ROUND_UP);//1
            map.put("total_fee",String.valueOf(fen));
            map.put("spbill_create_ip","127.0.0.1");
            map.put("notify_url",notifyUrl);
            map.put("trade_type","NATIVE");
            Map<String,String> result = wxPay.unifiedOrder(map);
            return result;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //out_trade_no是传的订单id，封装到map中，当参数查询
    @Override
    public Map queryOrder(String orderId) {
        Map map = new HashMap();
        map.put("out_trade_no",orderId);
        try {
            return wxPay.orderQuery(map);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map closeOrder(String orderId) {
        Map map = new HashMap();
        map.put("out_trade_no",orderId);
        try {
            return wxPay.closeOrder(map);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
