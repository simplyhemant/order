package com.simply.order;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class Test {

    @GetMapping("/test")
    public String hello() {
        return "This is customer-service!";
    }

}