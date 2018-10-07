package com.example.alva.storage;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class CacheBasedStorageContractVerifier<K, V> {

    private final Pair<K, V> existingEntity;
    private final Pair<K, V> overridingEntity;
    private final Pair<K, V> missingEntity;
    private final Pair<K, V> nullEntity;
    private AbstractCacheBasedGenericStorage<K, V> storage;

    public CacheBasedStorageContractVerifier(final Pair<K, V> existingEntity, final Pair<K, V> overridingEntity,
        final Pair<K, V> missingEntity, final Pair<K, V> nullEntity) {
        this.existingEntity = existingEntity;
        this.overridingEntity = overridingEntity;
        this.missingEntity = missingEntity;
        this.nullEntity = nullEntity;
    }

    protected abstract AbstractCacheBasedGenericStorage<K, V> newInstance();

    @Before
    public void setUp() {
        this.storage = this.newInstance();
        this.storage.save(this.existingEntity.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void save_nullIdentifier() {
        this.storage.save(this.nullEntity.getValue());
    }

    @Test
    public void save_overridingExistingEntity() {
        this.storage.save(this.overridingEntity.getValue());
        final Optional<V> get = this.storage.get(this.existingEntity.getKey());

        assertThat(get.isPresent()).isTrue();
        assertThat(get.get()).isEqualTo(this.overridingEntity.getValue());
    }

    @Test
    public void get_existingEntity() {
        final Optional<V> get = this.storage.get(this.existingEntity.getKey());
        assertThat(get.isPresent()).isTrue();
        assertThat(get.get()).isEqualTo(this.existingEntity.getValue());
    }

    @Test
    public void get_missingEntity() {
        final Optional<V> get = this.storage.get(this.missingEntity.getKey());
        assertThat(get.isPresent()).isFalse();
    }
}
