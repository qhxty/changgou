package com.changgou.seckill.controller;

import com.changgou.entity.Result;
import com.changgou.seckill.feign.SeckillFeign;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.stylesheets.LinkStyle;

import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/wseckillgoods")
public class SeckillGoodsController {

    @Autowired
    private SeckillFeign seckillFeign;

    @RequestMapping("/toIndex")
    public String toIndex() {
        return "seckill-index";
    }

    @RequestMapping("/timeMenus")
    @ResponseBody
    public List<String> dateMenus() {
        List<Date> dateMenus = DateUtil.getDateMenus();
        List<String> result = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(Date dateMenu: dateMenus) {
            String format = simpleDateFormat.format(dateMenu);
            result.add(format);
        }
        return result;
    }

    @RequestMapping("/list")
    @ResponseBody
    public Result<List<SeckillGoods>> list(String time) {
        Result<List<SeckillGoods>> listResult = seckillFeign.list(DateUtil.formatStr(time));
        return listResult;
    }
}
