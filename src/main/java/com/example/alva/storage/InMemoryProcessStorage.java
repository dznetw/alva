package com.example.alva.storage;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.example.alva.datamodel.VisitorProcess;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class InMemoryProcessStorage implements ProcessStorage {

    private final Cache<String, VisitorProcess> cache;

    private InMemoryProcessStorage() {
        this.cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
    }

    @Override
    public void save(final VisitorProcess visitorProcess) {
        this.cache.put(visitorProcess.getId(), visitorProcess);
    }

    @Override
    public Optional<VisitorProcess> get(final String key) {
        return Optional.ofNullable(this.cache.getIfPresent(key));
    }
}
