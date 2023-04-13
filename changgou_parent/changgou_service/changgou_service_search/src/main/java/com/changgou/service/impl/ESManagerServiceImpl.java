package com.changgou.service.impl;

import com.SkuInfo;
import com.alibaba.fastjson.JSON;
import com.changgou.dao.ESManagerMapper;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.service.ESManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ESManagerServiceImpl implements ESManagerService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private ESManagerMapper esManagerMapper;

    @Override
    public void createMappingAndIndex() {
        //创建索引
        elasticsearchTemplate.createIndex(Sku.class);
        //创建映射
        elasticsearchTemplate.putMapping(SkuInfo.class);
    }

    @Override
    public void importDataBySpuId(String spuId) {
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        if(skuList == null || skuList.size() <= 0) {
            throw new RuntimeException("没有数据");
        }
        String jsonSkuList = JSON.toJSONString(skuList);
        List<SkuInfo> skuInfoList = JSON.parseArray(jsonSkuList,SkuInfo.class);
        for(SkuInfo skuInfo : skuInfoList) {
            Map specMap = JSON.parseObject(skuInfo.getSpec(),Map.class);
            skuInfo.setSpecMap(specMap);
        }
        esManagerMapper.saveAll(skuInfoList);

    }

    @Override
    public void importAll() {
        List<Sku> skuList = skuFeign.findSkuListBySpuId("all");
        if(skuList == null || skuList.size() <= 0) {
            throw new RuntimeException("没有数据");
        }
        String jsonSkuList = JSON.toJSONString(skuList);
        List<SkuInfo> skuInfoList = JSON.parseArray(jsonSkuList,SkuInfo.class);
        for(SkuInfo skuInfo : skuInfoList) {
            Map specMap = JSON.parseObject(skuInfo.getSpec(),Map.class);
            skuInfo.setSpecMap(specMap);
        }
        esManagerMapper.saveAll(skuInfoList);
    }

    @Override
    public void delDataBySpuId(String spuId) {
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        if(skuList == null || skuList.size() <= 0) {
            throw new RuntimeException("没有数据");
        }
        for(Sku sku : skuList) {
            esManagerMapper.deleteById(Long.parseLong(sku.getId()));
        }
    }
}
