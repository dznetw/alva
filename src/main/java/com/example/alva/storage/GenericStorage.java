package com.example.alva.storage;

import java.util.Optional;

public interface GenericStorage<K, V> {

    void save(V value);

    Optional<V> get(K key);
}
