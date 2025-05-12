package com.example.controller;

import com.example.entity.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试
 */
@RestController
@RequestMapping("/example")
public class TestController {

    @GetMapping("/test")
    public R test() {
        return R.ok("交易小程序");
    }
}
