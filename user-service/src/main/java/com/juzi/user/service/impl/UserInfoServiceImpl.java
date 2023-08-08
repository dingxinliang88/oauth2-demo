package com.juzi.user.service.impl;

import com.juzi.common.resp.CommonResponse;
import com.juzi.common.resp.RespCodeEnum;
import com.juzi.common.resp.RespUtils;
import com.juzi.user.pojo.po.User;
import com.juzi.user.repo.UserRepository;
import com.juzi.user.service.UserInfoService;
import com.juzi.user.utils.RedisCommonProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author codejuzi
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisCommonProcessor redisCommonProcessor;

    @Override
    public CommonResponse checkPhoneBindStatus(String personId) {
        User user = (User) redisCommonProcessor.get(personId);
        boolean isBind;
        if (user != null) {
            isBind = user.getUserPhone() != null;
            return RespUtils.success(isBind);
        }
        // 查询db
        Integer userId = Integer.parseInt(personId) - 10000000;
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            isBind = userOptional.get().getUserPhone() != null;
            redisCommonProcessor.setExpiredDays(personId, userOptional.get(), 30);
            return RespUtils.success(isBind);
        }

        return RespUtils.fail(RespCodeEnum.BAD_REQUEST.getCode(), null, "Invalid User");
    }

    @Override
    public CommonResponse bindPhone(String personId, String phoneNumber, String code) {
        String cacheCode = String.valueOf(redisCommonProcessor.get(phoneNumber));
        if (StringUtils.isEmpty(cacheCode)) {
            return RespUtils.fail(RespCodeEnum.BAD_REQUEST.getCode(), null, "Phone Code is Expired!");
        }
        if (!cacheCode.equals(code)) {
            return RespUtils.fail(RespCodeEnum.BAD_REQUEST.getCode(), null, "Phone Code is Wrong!");
        }
        Integer userId = Integer.parseInt(personId) - 10000000;
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            userRepository.updateUserPhoneById(phoneNumber, userId);
            // 删除缓存
            redisCommonProcessor.remove(personId);
            return RespUtils.success(Boolean.TRUE);
        }
        return RespUtils.fail(RespCodeEnum.BAD_REQUEST.getCode(), null, "Invalid User");
    }
}
