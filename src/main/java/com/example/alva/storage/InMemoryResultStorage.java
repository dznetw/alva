package com.example.alva.storage;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.example.alva.datamodel.VisitorResult;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class InMemoryResultStorage implements ResultStorage {

    private final Cache<String, VisitorResult> cache;

    private InMemoryResultStorage() {
        this.cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
    }

    @Override
    public void save(final VisitorResult result) {
        this.cache.put(result.getId(), result);
    }

    @Override
    public Optional<VisitorResult> get(final String key) {
        return Optional.ofNullable(this.cache.getIfPresent(key));
    }
}
