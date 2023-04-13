package com.changgou.service.impl;

import com.SkuInfo;
import com.alibaba.fastjson.JSON;
import com.changgou.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    //数据源模板
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public Map search(Map<String, String> searchMap) throws Exception {
        //数据格式<手机，值>定义容器为map
        Map<String, Object> resultMap = new HashMap<>();
        //有条件才可以条件查询
        if (null != searchMap) {
            //组合条件对象
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            //例name=手机 and brand=华为，拼接后keyword=手机华为
            if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("name", searchMap.get("keywords")).operator(Operator.AND));
            }
            //品牌 brand
            if (StringUtils.isNotEmpty(searchMap.get("brand"))) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }
            //规格spec
            for (String key : searchMap.keySet()) {
                if (key.startsWith("spec_")) {
                    String value = searchMap.get(key).replace("%2B", "+");
                    boolQueryBuilder.filter(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", value));
                }
            }
            //价格区间
            if (StringUtils.isNotEmpty(searchMap.get("price"))) {
                String[] p = searchMap.get("price").split("-");
                if (p.length == 2) {
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(p[1]));
                }
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(p[0]));
            }
            //实现搜索的实现类
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
            //排序
            if(!StringUtils.isEmpty(searchMap.get("sortField"))) {
                if("ASC".equals(searchMap.get("sortRule"))) {
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.ASC));
                }else {
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.DESC));
                }
            }
            //品牌查询
            String skuBrand = "skuBrand";
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));

            String skuSpec = "skuSpec";
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword"));

            String pageSize = searchMap.get("pageSize");
            String pageNum = searchMap.get("pageNum");
            if(null == pageNum) {
                pageNum = "1";
            }
            if(StringUtils.isEmpty(pageSize)) {
                pageSize = "30";
            }
            HighlightBuilder.Field field = new HighlightBuilder.Field("name")
                    .preTags("<span style='color:red'>")
                    .postTags("</span>");

            nativeSearchQueryBuilder.withHighlightFields(field);

            nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageNum)-1,Integer.parseInt(pageSize)));

            //查询
            AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {

                    List<T> list = new ArrayList<>();
                    //封装结果集
                    SearchHits hits = searchResponse.getHits();
                    if (hits != null) {
                        for (SearchHit hit : hits) {
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                            //高亮
                            Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
                            if(null != highlightFieldMap && highlightFieldMap.size() > 0) {
                                //替换数据
                                skuInfo.setName(highlightFieldMap.get("name").getFragments()[0].toString());
                            }
                            list.add((T) skuInfo);
                        }
                    }
                    return new AggregatedPageImpl<>(list, pageable, hits.getTotalHits(), searchResponse.getAggregations());
                }
            });
            //总条数
            resultMap.put("total", aggregatedPage.getTotalElements());
            //总页数
            resultMap.put("totalPages", aggregatedPage.getTotalPages());
            //结果集
            resultMap.put("rows", aggregatedPage.getContent());
            //获取品牌结果集
            StringTerms brandTerms = (StringTerms) aggregatedPage.getAggregation(skuBrand);
            List<String> brandList = brandTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("brandList", brandList);

            StringTerms specTerms = (StringTerms) aggregatedPage.getAggregation(skuSpec);
            List<String> specList = specTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("specList", this.formartSpec(specList));

            resultMap.put("pageNum",pageNum);
//            resultMap.put("pageSize",pageSize);

            return resultMap;
        }
        return null;
    }
    //遍历所有spec数据，将json转为map，value替换成set
    public Map<String,Set<String>> formartSpec(List<String> specList) {
        Map<String,Set<String>> resultMap = new HashMap<String,Set<String>>();
        if(specList != null && specList.size() > 0) {
            //将数据转换
            for(String specJsonString : specList) {
                Map<String,String> specMap = JSON.parseObject(specJsonString,Map.class);
                for(String specKey : specMap.keySet()) {
                    Set<String> specSet = resultMap.get(specKey);
                    if(specSet == null) {
                        specSet = new HashSet<String>();
                    }
                    specSet.add(specMap.get(specKey));
                    resultMap.put(specKey,specSet);
                }
            }
        }
        return resultMap;
    }
}

