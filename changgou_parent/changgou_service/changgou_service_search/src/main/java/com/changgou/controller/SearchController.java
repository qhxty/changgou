package com.changgou.controller;

import com.SkuInfo;
import com.changgou.entity.Page;
import com.changgou.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private SearchService searchService;

    @GetMapping("/list")
    public String search(@RequestParam Map<String,String> searchMap, Model model) throws Exception{
        this.handleSearchMap(searchMap);
        Map<Object,Object> resultMap = searchService.search(searchMap);
        model.addAttribute("searchMap",searchMap);
        model.addAttribute("result",resultMap);

        //总记录数、第几页、一页几条
        Page<SkuInfo> page = new Page<SkuInfo>(
                Long.parseLong(String.valueOf(resultMap.get("total"))),
                Integer.parseInt(String.valueOf(resultMap.get("pageNum"))),
                Page.pageSize
        );
        model.addAttribute("page",page);

        //拼接url
        StringBuilder url = new StringBuilder("/search/list");
        if(searchMap != null &&searchMap.size() > 0) {
            //？
            url.append("?");
            for(String paramKey : searchMap.keySet()) {
                if(!"sortRule".equals(paramKey) && !"sortField".equals(paramKey) && !"pageNum".equals(paramKey)) {
                    url.append(paramKey).append("=").append(searchMap.get(paramKey)).append("&");

                }
            }
            String urlString = url.toString();
            //去掉路径上最后一个&
            urlString = urlString.substring(0,urlString.length()-1);
            model.addAttribute("url",urlString);
        }else {
            model.addAttribute("url",url);
        }
        return "search";
    }

    @GetMapping
    public Map search(@RequestParam Map<String,String> searchMap) throws Exception{
        this.handleSearchMap(searchMap);
        Map searchResult = searchService.search(searchMap);
        return searchResult;
    }

    //处理特殊符号
    private void handleSearchMap(Map<String,String> searchMap) {
        Set<Map.Entry<String,String>> entries = searchMap.entrySet();
        for(Map.Entry<String,String> entry : entries) {
            if(entry.getKey().startsWith("spec_")) {
                searchMap.put(entry.getKey(),entry.getValue().replace("+","%2B"));
            }
        }
    }
}
