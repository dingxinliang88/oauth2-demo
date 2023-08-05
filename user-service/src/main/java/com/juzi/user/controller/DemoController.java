package com.juzi.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author codejuzi
 */
@RestController
public class DemoController {
    @GetMapping
    public String test() {
        return "Hello!";
    }
}