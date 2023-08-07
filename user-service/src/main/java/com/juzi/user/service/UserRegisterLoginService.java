package com.juzi.user.service;

import com.juzi.common.resp.CommonResponse;
import com.juzi.user.pojo.po.User;

/**
 * @author codejuzi
 */
public interface UserRegisterLoginService {

    // region register
    CommonResponse namePasswordRegister(User user);

    CommonResponse phoneCodeRegister(String phoneNumber, String code);

    // endregion
}

