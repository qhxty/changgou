package com.changgou.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.service.ESManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager")
public class ESController {
    @Autowired
    private ESManagerService esManagerService;

    @GetMapping("/create")
    public Result create() {
        esManagerService.createMappingAndIndex();
        return new Result(true, StatusCode.OK,"创建索引库成功");
    }

    @GetMapping("/importAll")
    public Result importAll() {
        esManagerService.importAll();
        return new Result(true,StatusCode.OK,"导入全部数据成功");
    }
}
