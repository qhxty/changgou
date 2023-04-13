package com.changgou.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.page.service.PageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class PageServiceImpl implements PageService {
    @Autowired
    private SpuFeign spuFeign;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private CategoryFeign categoryFeign;
    @Value("${pagepath}")
    private String pagepath;
    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void generateItemPage(String spuId) {
        //获取对象，存放商品数据
        Context context = new Context();
        //获取静态化页面需要用到的数据
        Map<String,Object> itemData = this.getItemData(spuId);
        context.setVariables(itemData);

        //获取模板文件位置
        File dir = new File(pagepath);
        //判断有没有文件夹，没有创建，有添加
        if(!dir.exists()) {
            dir.mkdirs();
        }
        //定义输出流传递数据
        File file = new File(dir + "/" + spuId + ".html");
        Writer out = null;
        try {
            out = new PrintWriter(file);
            templateEngine.process("item",context,out);
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                out.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public Map<String,Object> getItemData(String spuId) {
        Map<String,Object> resultMap = new HashMap<>();
        Spu spu = spuFeign.findSpuById(spuId).getData();
        resultMap.put("spu",spu);
        //获取图片信息
        if(spu != null) {
            if(StringUtils.isNotEmpty(spu.getImages())) {
                resultMap.put("imageList",spu.getImages().split(","));
            }
        }
        //获取商品分类信息
        Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
        resultMap.put("category1",category1);
        Category category2 = categoryFeign.findById(spu.getCategory1Id()).getData();
        resultMap.put("category2",category2);
        Category category3 = categoryFeign.findById(spu.getCategory1Id()).getData();
        resultMap.put("category3",category3);
        //获取sku
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        resultMap.put("skuList",skuList);
        //商品数据信息
        resultMap.put("specificationList", JSON.parseObject(spu.getSpecItems(),Map.class));
        return resultMap;
    }
}
