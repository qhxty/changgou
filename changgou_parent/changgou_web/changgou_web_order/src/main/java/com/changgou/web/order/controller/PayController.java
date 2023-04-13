package com.changgou.web.order.controller;

import com.changgou.entity.Result;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.pay.feign.PayFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/wxpay")
public class PayController {
    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private PayFeign payFeign;

    //跳转微信支付二维码
    @GetMapping
    public String wxPay(String orderId, Model model) {
        //根据订单id查询订单
        Result<Order> orderResult = orderFeign.findById(orderId);
        if(orderResult.getData()==null) {//没有此订单
            return "fail";
        }
        //判断订单支付状态
        Order order = orderResult.getData();
        if(!"0".equals(order.getPayStatus())) {//订单不是未支付状态
            return "fail";
        }
        Result payResult = payFeign.nativePay(orderId,order.getPayMoney());
        if(payResult.getData()==null) {//调用统一下单接口出错
            return "fail";
        }
        Map payMap = (Map) payResult.getData();
        payMap.put("payMoney",order.getPayMoney());
        payMap.put("orderId",orderId);
        model.addAllAttributes(payMap);
        return "wxpay";
    }

    @RequestMapping("/toPaySuccess")
    public String toPaySuccess(Integer payMoney,Model model) {
        model.addAttribute("payMoney",payMoney);
        return "paysuccess";
    }
}
