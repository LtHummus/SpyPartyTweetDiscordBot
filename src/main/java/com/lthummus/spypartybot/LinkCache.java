package com.lthummus.spypartybot;

import net.jodah.expiringmap.ExpiringMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LinkCache {

    private final Map<String, Integer> cache;

    public LinkCache() {
        cache = ExpiringMap
                .builder()
                .expiration(30, TimeUnit.MINUTES)
                .build();
    }

    public boolean isInCache(String x) {
        return cache.containsKey(x);
    }

    public void insert(String x) {
        cache.put(x, 1);
    }
}
