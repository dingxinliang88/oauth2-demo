package com.juzi.gateway.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

import javax.sql.DataSource;

/**
 * 融合 datasource 和 权限校验管理
 *
 * @author codejuzi
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AccessManager accessManager;

    @Bean
    public SecurityWebFilterChain webFluxSecurityFilterChain(ServerHttpSecurity serverHttpSecurity) {

        // 配置过滤器，进行db交互
        ReactiveAuthenticationManager reactiveAuthenticationManager
                = new ReactiveJdbcAuthenticationManager(dataSource);
        AuthenticationWebFilter authenticationWebFilter
                = new AuthenticationWebFilter(reactiveAuthenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(new ServerBearerTokenAuthenticationConverter());

        serverHttpSecurity
                .httpBasic().disable()
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .anyExchange().access(accessManager)
                .and().addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return serverHttpSecurity.build();
    }
}
