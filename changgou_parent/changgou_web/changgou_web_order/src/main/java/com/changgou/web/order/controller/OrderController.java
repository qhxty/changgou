package com.changgou.web.order.controller;

import com.changgou.entity.Result;
import com.changgou.order.feign.CartFeign;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.user.feign.AddressFeign;
import com.changgou.user.pojo.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/worder")
public class OrderController {
    @Autowired
    private AddressFeign addressFeign;
    @Autowired
    private CartFeign cartFeign;
    @Autowired
    private OrderFeign orderFeign;

    @RequestMapping("/ready/order")
    public String readyOrder(Model model) {
        List<Address> addressList = addressFeign.list().getData();
        model.addAttribute("address",addressList);

        Map map = cartFeign.list();
        List<OrderItem> orderItemList = (List<OrderItem>) map.get("orderItemList");
        Integer totalMoney = (Integer) map.get("totalPrice");
        Integer totalNum = (Integer) map.get("totalNum");
        for(Address addr : addressList) {
            if("1".equals(addr.getIsDefault())) {
                model.addAttribute("deAddr",addr);
                break;
            }
        }

        model.addAttribute("carts",orderItemList);
        model.addAttribute("totalMoney",totalMoney);
        model.addAttribute("totalNum",totalNum);
        return "order";
    }

    @PostMapping("/add")
    @ResponseBody
    public Result add(@RequestBody Order order) {
        Result result = orderFeign.add(order);
        return result;
    }

    @GetMapping("/toPayPage")
    public String toPayPage(String orderId,Model model) {
        Order order = orderFeign.findById(orderId).getData();
        model.addAttribute("orderId",orderId);
        model.addAttribute("payMoney",order.getPayMoney());
        return "pay";
    }
}
