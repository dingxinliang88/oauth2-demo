package com.juzi.gateway.feign.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


/**
 * @author codejuzi
 */
@FeignClient(name = "oauth2-service")
public interface OAuth2ServerClient {

    /**
     * oauth2 校验token 接口
     *
     * @param token token
     * @return resp
     */
    @GetMapping("/oauth/check_token")
    Map<String, Object> checkToken(@RequestParam(value = "token") String token);
}
