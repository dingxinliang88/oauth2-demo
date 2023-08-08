package com.juzi.user.service;

import com.juzi.common.resp.CommonResponse;

/**
 * @author codejuzi
 */
public interface UserInfoService {

    CommonResponse checkPhoneBindStatus(String personId);

    CommonResponse bindPhone(String personId, String phoneNumber, String code);
}
