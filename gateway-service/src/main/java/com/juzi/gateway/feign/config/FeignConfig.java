package com.juzi.gateway.feign.config;

import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author codejuzi
 */
@Configuration
public class FeignConfig {

    @Bean
    public Decoder feignDecoder() {

        // 完成msg的解析，converter
        ObjectFactory<HttpMessageConverters> objectFactory = () -> {
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
            List<MediaType> mediaTypes = new ArrayList<>();
            mediaTypes.add(MediaType.valueOf(MediaType.TEXT_HTML_VALUE + ";charset=utf-8"));
            converter.setSupportedMediaTypes(mediaTypes);
            return new HttpMessageConverters(converter);
        };

        return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
    }
}
