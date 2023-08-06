package com.juzi.gateway.feign.filter;

import com.juzi.gateway.feign.api.OAuth2ServerClient;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author codejuzi
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
    private static final Set<String> WHITE_ACCESS_URLS = new ConcurrentSkipListSet<>() {{
        add("/**/oauth/**");
        add("/user/register");
    }};

    /**
     * 懒加载，否则和Gateway启动过程会产生死锁
     */
    @Lazy
    @Autowired
    private OAuth2ServerClient oAuth2ServerClient;

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String reqUrl = request.getURI().getPath();
        if (checkAccessUrl(reqUrl)) {
            return chain.filter(exchange);
        }

        // 获取token
        String token = request.getHeaders().getFirst("Authorization");
        CompletableFuture<Map<String, Object>> authCheckFuture
                = CompletableFuture.supplyAsync(() -> oAuth2ServerClient.checkToken(token));

        Map<String, Object> authCheckResult = authCheckFuture.get();
        boolean active = (boolean) authCheckResult.get("active");
        if (!active) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 转发到微服务，并携带一些header，比如链路id
        ServerHttpRequest httpRequest = request.mutate().headers(httpHeaders -> {
            httpHeaders.set("personId", request.getHeaders().getFirst("personId"));
//            httpHeaders.set("tracingId", request.getHeaders().getFirst("tracingId"));
        }).build();
        exchange.mutate().request(httpRequest);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private boolean checkAccessUrl(String reqUrl) {
        return WHITE_ACCESS_URLS.stream().anyMatch(url -> ANT_PATH_MATCHER.match(url, reqUrl));
    }
}
