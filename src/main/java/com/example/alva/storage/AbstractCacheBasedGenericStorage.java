package com.example.alva.storage;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

abstract class AbstractCacheBasedGenericStorage<K, V> implements GenericStorage<K, V> {

    private final Cache<K, V> cache;

    protected AbstractCacheBasedGenericStorage() {
        this.cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
    }

    protected abstract K getIdentifierFromValue(final V value);

    @Override
    public void save(final V value) {
        this.cache.put(Objects.requireNonNull(this.getIdentifierFromValue(value)), value);
    }

    @Override
    public Optional<V> get(final K key) {
        return Optional.ofNullable(this.cache.getIfPresent(key));
    }
}
