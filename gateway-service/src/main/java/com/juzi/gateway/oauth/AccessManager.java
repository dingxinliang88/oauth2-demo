package com.juzi.gateway.oauth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 权限校验
 *
 * @author codejuzi
 */
@Component
public class AccessManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    /**
     * 放行白名单（正则形式）
     */
    private static final Set<String> ACCESS_WHITELIST = new ConcurrentSkipListSet<>();

    /**
     * 正则校验器
     */
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    public AccessManager() {
        ACCESS_WHITELIST.add("/**/oauth/**");
    }

    /**
     * 决定是否放行
     */
    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext ctx) {
        // 获取exchange，内含有请求信息
        ServerWebExchange exchange = ctx.getExchange();

        return authentication.map(auth -> {
            // 校验请求路径
            String reqUrl = exchange.getRequest().getURI().getPath();
            if (checkAccess(reqUrl)) {
                return new AuthorizationDecision(true);
            }

            if (auth instanceof OAuth2Authentication) {
                OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) auth;
                // 获取client_id
                String clientId = oAuth2Authentication.getOAuth2Request().getClientId();
                if (StringUtils.isNotEmpty(clientId)) {
                    return new AuthorizationDecision(true);
                }
            }
            return new AuthorizationDecision(false);
        });
    }

    private boolean checkAccess(String reqUrl) {
        return ACCESS_WHITELIST.stream()
                .anyMatch(validUrlPattern -> ANT_PATH_MATCHER.match(validUrlPattern, reqUrl));
    }
}
