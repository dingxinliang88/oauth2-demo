package com.juzi.user.service;

import com.juzi.common.resp.CommonResponse;
import com.juzi.user.pojo.po.User;

import javax.servlet.http.HttpServletRequest;

/**
 * @author codejuzi
 */
public interface UserRegisterLoginService {

    // region register
    CommonResponse namePasswordRegister(User user);

    CommonResponse phoneCodeRegister(String phoneNumber, String code);

    CommonResponse thirdPlatformGiteeRegister(HttpServletRequest request);

    // endregion

    // region login
    CommonResponse login(String username, String password);

    // endregion
}

