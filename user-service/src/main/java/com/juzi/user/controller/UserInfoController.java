package com.juzi.user.controller;

import com.juzi.common.resp.CommonResponse;
import com.juzi.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author codejuzi
 */
@RestController
@RequestMapping("/user/info")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @PostMapping("check-phone-bind-status")
    public CommonResponse checkPhoneBindStatus(@RequestHeader(value = "personId") String personId) {
        return userInfoService.checkPhoneBindStatus(personId);
    }

    @PostMapping("bind-phone")
    public CommonResponse bindPhone(@RequestHeader(value = "personId") String personId,
                                    @RequestParam(value = "phoneNumber") String phoneNumber,
                                    @RequestParam(value = "code") String code) {
        return userInfoService.bindPhone(personId, phoneNumber, code);
    }
}
