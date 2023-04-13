package com.changgou.service;

public interface ESManagerService {
    //创建索引结构
    void createMappingAndIndex();
    //根据spuId导入数据到ES索引库
    void importDataBySpuId(String spuId);

    void importAll();

    void delDataBySpuId(String spuId);
}
