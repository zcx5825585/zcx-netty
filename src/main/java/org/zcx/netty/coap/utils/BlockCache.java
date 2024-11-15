package org.zcx.netty.coap.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zcx.netty.coap.entity.CoapBlock;

import java.util.concurrent.TimeUnit;

@Configuration
public class BlockCache {

    @Bean("blockCache")
    public Cache<String, CoapBlock> blockCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.MINUTES)
                // 初始的缓存空间大小
                .initialCapacity(10)
                // 缓存的最大条数
                .maximumSize(500)
                .build();
    }
}