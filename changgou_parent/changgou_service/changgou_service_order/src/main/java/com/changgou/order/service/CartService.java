package com.changgou.order.service;

import java.util.Map;

public interface CartService {
    void add(String skuId, Integer num, String username);

    Map list(String username);
}
