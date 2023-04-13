package com.changgou.dao;

import com.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

public interface ESManagerMapper extends ElasticsearchCrudRepository<SkuInfo,Long> {
}
