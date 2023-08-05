package com.juzi.gateway.oauth;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * 认证管理
 *
 * @author codejuzi
 */
public class ReactiveJdbcAuthenticationManager implements ReactiveAuthenticationManager {

    private final TokenStore tokenStore;
    public ReactiveJdbcAuthenticationManager(DataSource dataSource) {
        this.tokenStore = new JdbcTokenStore(dataSource);
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .filter(a -> a instanceof BearerTokenAuthenticationToken)
                .cast(BearerTokenAuthenticationToken.class)
                .map(BearerTokenAuthenticationToken::getToken)
                .flatMap(accessToken -> {
                    OAuth2AccessToken oAuth2AccessToken = this.tokenStore.readAccessToken(accessToken);
                    if (Objects.isNull(oAuth2AccessToken)) {
                        return Mono.error(new InvalidTokenException("Invalid token"));
                    } else if (oAuth2AccessToken.isExpired()) {
                        // token 过期
                        return Mono.error(new InvalidTokenException("Token expired"));
                    }
                    // 校验token
                    OAuth2Authentication oAuth2Authentication = this.tokenStore.readAuthentication(accessToken);
                    if (Objects.isNull(oAuth2Authentication)) {
                        return Mono.error(new InvalidTokenException("Fake Token"));
                    }
                    return Mono.justOrEmpty(oAuth2Authentication);
                }).cast(Authentication.class);
    }
}
