package com.juzi.user.service.impl;

import cn.hutool.json.JSONObject;
import com.juzi.common.resp.CommonResponse;
import com.juzi.common.resp.RespCodeEnum;
import com.juzi.common.resp.RespUtils;
import com.juzi.user.config.GiteeConfig;
import com.juzi.user.pojo.enums.AuthGrantType;
import com.juzi.user.pojo.enums.RegisterType;
import com.juzi.user.pojo.po.OAuth2Client;
import com.juzi.user.pojo.po.User;
import com.juzi.user.repo.OAuth2ClientRepository;
import com.juzi.user.repo.UserRepository;
import com.juzi.user.service.UserRegisterLoginService;
import com.juzi.user.utils.RedisCommonProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author codejuzi
 */
@Slf4j
@Service
public class UserRegisterLoginServiceImpl implements UserRegisterLoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuth2ClientRepository oAuth2ClientRepository;

    @Autowired
    private RedisCommonProcessor redisCommonProcessor;

    @Autowired
    private RestTemplate innerRestTemplate;

    @Autowired
    private RestTemplate outerRestTemplate;

    @Resource(name = "transactionManager")
    private JpaTransactionManager transactionManager;

    @Autowired
    private GiteeConfig giteeConfig;

    @Override
    public CommonResponse namePasswordRegister(User user) {
        if (!(userRepository.findByUserName(user.getUserName()) == null
                && oAuth2ClientRepository.findByClientId(user.getUserName()) == null)) {
            return RespUtils.fail(RespCodeEnum.BAD_REQUEST.getCode(), null, "User Already Exists! Please Login!");
        }
        // 新用户注册
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String originPwd = user.getPasswd();
        user.setPasswd(bCryptPasswordEncoder.encode(originPwd));

        OAuth2Client oAuth2Client = OAuth2Client.builder()
                .clientId(user.getUserName())
                .clientSecret(user.getPasswd())
                .resourceIds(RegisterType.USER_PASSWORD.name())
                .authorizedGrantTypes(AuthGrantType.refresh_token.name().concat(",")
                        .concat(AuthGrantType.password.name()))
                .scope("web")
                .authorities(RegisterType.USER_PASSWORD.name())
                .build();

        // 保存 user， oauth2 信息，内部开启事务
        Integer uid = this.saveUserAndOAuth2Client(user, oAuth2Client);

        String personId = String.valueOf(uid + 10000000);
        redisCommonProcessor.setExpiredDays(personId, user, 30);
        // 返回 user、token信息
        return RespUtils.success(
                formatUserAndToken(user,
                        generateOAuthToken(AuthGrantType.password, user.getUserName(), originPwd, user.getUserName(), originPwd))
        );
    }

    @Override
    public CommonResponse phoneCodeRegister(String phoneNumber, String code) {
        String cacheCode = String.valueOf(redisCommonProcessor.get(phoneNumber));
        if (StringUtils.isEmpty(cacheCode)) {
            return RespUtils.fail(RespCodeEnum.BAD_REQUEST.getCode(), null, "Phone Code is Expired!");
        }
        if (!cacheCode.equals(code)) {
            return RespUtils.fail(RespCodeEnum.BAD_REQUEST.getCode(), null, "Phone Code is Wrong!");
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        // 验证码加密作为 client_secret，符合 oauth2
        String encodePwd = bCryptPasswordEncoder.encode(code);
        User user = userRepository.findByUserPhone(phoneNumber);
        if (Objects.isNull(user)) {
            // 新用户注册
            String username = getSystemDefineUserName(phoneNumber);
            user = User.builder()
                    .userName(username)
                    // 可不存
                    .passwd("")
                    .userPhone(phoneNumber)
                    .userRole(RegisterType.PHONE_NUMBER.name())
                    .build();
            OAuth2Client oAuth2Client = OAuth2Client.builder()
                    .clientId(phoneNumber)
                    .clientSecret(encodePwd)
                    .resourceIds(RegisterType.PHONE_NUMBER.name())
                    .authorizedGrantTypes(AuthGrantType.refresh_token.name().concat(",")
                            .concat(AuthGrantType.client_credentials.name()))
                    .scope("web")
                    .authorities(RegisterType.PHONE_NUMBER.name())
                    .build();
            Integer uid = saveUserAndOAuth2Client(user, oAuth2Client);
            String personId = String.valueOf(uid + 10000000);
            redisCommonProcessor.setExpiredDays(personId, user, 30);
        } else {
            // secret 过期，更新
            oAuth2ClientRepository.updateSecretByClientId(encodePwd, phoneNumber);
        }
        return RespUtils.success(
                formatUserAndToken(user,
                        generateOAuthToken(AuthGrantType.client_credentials, null, null, phoneNumber, code))
        );
    }

    @Override
    public CommonResponse thirdPlatformGiteeRegister(HttpServletRequest request) {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        if (!giteeConfig.getState().equals(state)) {
            return RespUtils.fail(RespCodeEnum.BAD_REQUEST.getCode(), null, "Invalid State!");
        }
        // https://gitee.com/oauth/token?grant_type=authorization_code&client_id=%s&client_secret=%s&redirect_uri=%s&code=%s
        String tokenUrl = String.format(giteeConfig.getTokenUrl(),
                giteeConfig.getClientId(), giteeConfig.getClientSecret(),
                giteeConfig.getCallBack(), code);

        JSONObject tokenRes = outerRestTemplate.postForObject(tokenUrl, null, JSONObject.class);
        assert tokenRes != null;
        String token = String.valueOf(tokenRes.get("access_token"));

        // https://gitee.com/api/v5/user?access_token=%s
        String userUrl = String.format(giteeConfig.getUserUrl(), token);
        JSONObject giteeUserInfo = outerRestTemplate.getForObject(userUrl, JSONObject.class);
        assert giteeUserInfo != null;

        String username = giteeConfig.getState().concat("-").concat(String.valueOf(giteeUserInfo.get("login")));
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodePwd = bCryptPasswordEncoder.encode(username);

        User user = userRepository.findByUserName(username);
        if (Objects.isNull(user)) {
            user = User.builder()
                    .userName(username)
                    .passwd("")
                    .userRole(RegisterType.THIRD_PARTY.name())
                    .build();
            OAuth2Client oAuth2Client = OAuth2Client.builder()
                    .clientId(username)
                    .clientSecret(encodePwd)
                    .resourceIds(RegisterType.THIRD_PARTY.name())
                    .authorizedGrantTypes(AuthGrantType.refresh_token.name().concat(",")
                            .concat(AuthGrantType.client_credentials.name()))
                    .scope("web")
                    .authorities(RegisterType.THIRD_PARTY.name())
                    .build();
            Integer uid = saveUserAndOAuth2Client(user, oAuth2Client);
            String personId = String.valueOf(uid + 10000000);
            redisCommonProcessor.setExpiredDays(personId, user, 30);
        }
        return RespUtils.success(
                formatUserAndToken(user,
                        generateOAuthToken(AuthGrantType.client_credentials, null, null, username, username))
        );
    }

    @Override
    public CommonResponse login(String username, String password) {
        User user = userRepository.findByUserName(username);
        if (Objects.isNull(user)) {
            return RespUtils.fail(RespCodeEnum.BAD_REQUEST.getCode(), null, "User Not Exists!");
        }

        Map<String, Object> content = formatUserAndToken(user,
                generateOAuthToken(AuthGrantType.password, username, password, username, password));

        // 刷新缓存用户信息过期时间
        String personId = String.valueOf(user.getId() + 10000000);
        redisCommonProcessor.setExpiredDays(personId, user, 30);

        return RespUtils.success(content);
    }

    private String getSystemDefineUserName(String phoneNumber) {
        //前缀 MALL_ + 当前时间戳 + 手机号后4位
        return "MALL_" + System.currentTimeMillis() + phoneNumber.substring(phoneNumber.length() - 4);
    }

    @SuppressWarnings("rawtypes")
    private Map<String, Object> formatUserAndToken(User user, Map map) {
        return new HashMap<>() {{
            put("user", user);
            put("oauth", map);
        }};
    }

    @SuppressWarnings("rawtypes")
    private Map generateOAuthToken(AuthGrantType authGrantType, String username,
                                   String password, String clientId, String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", authGrantType.name());
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        if (authGrantType.equals(AuthGrantType.password)) {
            params.add("username", username);
            params.add("password", password);
        }
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
        return innerRestTemplate.postForObject("http://oauth2-service/oauth/token", requestEntity, Map.class);
    }

    private Integer saveUserAndOAuth2Client(User user, OAuth2Client oAuth2Client) {
        DefaultTransactionDefinition dtx = new DefaultTransactionDefinition();
        dtx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        dtx.setTimeout(30);

        TransactionStatus status = transactionManager.getTransaction(dtx);

        try {
            user = userRepository.save(user);
            oAuth2ClientRepository.save(oAuth2Client);
            transactionManager.commit(status);
        } catch (Exception e) {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
            log.error("DB Save Error", e);
        }
        return user.getId();
    }
}
