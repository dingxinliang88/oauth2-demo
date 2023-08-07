package com.juzi.user.controller;

import com.juzi.common.resp.CommonResponse;
import com.juzi.user.pojo.po.User;
import com.juzi.user.service.UserRegisterLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author codejuzi
 */
@RestController
@RequestMapping("/user")
public class UserRegisterLoginController {

    @Autowired
    private UserRegisterLoginService userRegisterLoginService;

    // region register
    @PostMapping("/register/name-password")
    public CommonResponse namePasswordRegister(@RequestBody User user) {
        return userRegisterLoginService.namePasswordRegister(user);
    }

    @PostMapping("/register/phone-code")
    public CommonResponse phoneCodeRegister(@RequestParam String phoneNumber, @RequestParam String code) {
        return userRegisterLoginService.phoneCodeRegister(phoneNumber, code);
    }

    /**
     * 此接口提供给 gitee 平台使用，用于第三方登录
     */
    @RequestMapping("/register/gitee")
    public CommonResponse thirdPlatformGiteeRegister(HttpServletRequest request) {
        return userRegisterLoginService.thirdPlatformGiteeRegister(request);
    }

    // endregion

    // region login

    @PostMapping("/login")
    public CommonResponse login(@RequestParam String username,
                                @RequestParam String password) {
        return userRegisterLoginService.login(username, password);
    }


    // endregion
}
