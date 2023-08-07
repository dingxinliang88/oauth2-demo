package com.juzi.user.controller;

import com.juzi.common.resp.CommonResponse;
import com.juzi.user.pojo.po.User;
import com.juzi.user.service.UserRegisterLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author codejuzi
 */
@RestController
@RequestMapping("/user")
public class UserRegisterLoginController {

    @Autowired
    private UserRegisterLoginService userRegisterLoginService;


    /**
     * 用户名 - 密码注册
     *
     * @param user 用户注册信息
     * @return 用户信息 + OAuth2 信息
     */
    @PostMapping("/register/name-password")
    public CommonResponse namePasswordRegister(@RequestBody User user) {
        return userRegisterLoginService.namePasswordRegister(user);
    }

    @PostMapping("/register/phone-code")
    public CommonResponse phoneCodeRegister(@RequestParam String phoneNumber, @RequestParam String code) {
        return userRegisterLoginService.phoneCodeRegister(phoneNumber, code);
    }

}
