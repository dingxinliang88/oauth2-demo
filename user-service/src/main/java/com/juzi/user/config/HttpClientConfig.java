package com.juzi.user.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author codejuzi
 */
@Slf4j
@Configuration
public class HttpClientConfig {

    /**
     * 连接超时时间（ms）
     */
    private static final int CONNECTION_TIMEOUT = 30000;

    /**
     * 请求超时时间（ms）
     */
    private static final int REQUEST_TIMEOUT = 30000;

    /**
     * socket 超时时间（ms）
     */
    private static final int SOCKET_TIMEOUT = 60000;

    /**
     * 最大连接数
     */
    private static final int MAX_CONNECTIONS = 50;

    /**
     * 默认 keep-alive 时间（ms）
     */
    private static final int DEFAULT_KEEP_ALIVE_TIME = 20000;

    /**
     * 清理空闲线程的任务定时时间（ms）
     */
    private static final int CLOSE_IDLE_CONN_TIME = 30000;


    @Bean
    public CloseableHttpClient httpClient() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(config)
                // pooling 线程池设置
                .setConnectionManager(poolingHttpClientConnectionManager())
                // keep-alive 设置
                .setKeepAliveStrategy(connectionKeepAliveStrategy())
                .build();
    }

    /**
     * 进行 http、 https 的注册， 设置最大连接数
     */
    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        try {
            // 内部通信不需要 key-store(trust-store)
            sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            log.error("loadTrustMaterial error: ", e);
        }
        SSLConnectionSocketFactory sslConnectionSocketFactory = null;

        try {
            sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("SSLConnectionSocketFactory create error: ", e);
        }

        @SuppressWarnings("DataFlowIssue") Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build();

        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager
                = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingHttpClientConnectionManager.setMaxTotal(MAX_CONNECTIONS);
        return poolingHttpClientConnectionManager;
    }

    @Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (response, context) -> {
            BasicHeaderElementIterator iterator = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (iterator.hasNext()) {
                HeaderElement headerElement = iterator.nextElement();
                String name = headerElement.getName();
                String value = headerElement.getValue();
                if (StringUtils.isNotEmpty(value) && "timeout".equalsIgnoreCase(name)) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return DEFAULT_KEEP_ALIVE_TIME;
        };
    }

    /**
     * 定时清理空闲线程（10s）
     */
    @Bean
    public Runnable idleConnCleaner(final PoolingHttpClientConnectionManager connectionManager) {
        return new Runnable() {
            @Override
            @Scheduled(fixedDelay = 10000L)
            public void run() {
                try {
                    if (!Objects.isNull(connectionManager)) {
                        connectionManager.closeExpiredConnections();
                        connectionManager.closeIdleConnections(CLOSE_IDLE_CONN_TIME, TimeUnit.MILLISECONDS);
                    } else {
                        log.warn("PoolingHttpClientConnectionManager is not initialized");
                    }
                } catch (Exception e) {
                    log.error("idleConnCleaner error: ", e);
                }
            }
        };
    }

}
