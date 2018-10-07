package com.example.alva.storage;

import org.apache.commons.lang3.tuple.Pair;

public class AbstractCacheBasedGenericStorageTest
    extends CacheBasedStorageContractVerifier<Integer, Pair<Integer, String>> {

    private static final Pair<Integer, String> EXISTING_ENTITY = Pair.of(0, "existing");
    private static final Pair<Integer, String> OVERRIDING_ENTITY = Pair.of(EXISTING_ENTITY.getKey(), "new");
    private static final Pair<Integer, String> MISSING_ENTITY = Pair.of(100, "missing");
    private static final Pair<Integer, String> NULL_ENTITY = Pair.of(null, "existing");

    public AbstractCacheBasedGenericStorageTest() {
        super(newTupleFrom(EXISTING_ENTITY), newTupleFrom(OVERRIDING_ENTITY), newTupleFrom(MISSING_ENTITY),
            newTupleFrom(NULL_ENTITY));
    }

    private static Pair<Integer, Pair<Integer, String>> newTupleFrom(final Pair<Integer, String> entity) {
        return Pair.of(entity.getKey(), entity);
    }

    @Override
    protected AbstractCacheBasedGenericStorage<Integer, Pair<Integer, String>> newInstance() {
        return new TestStorage();
    }

    private static final class TestStorage extends AbstractCacheBasedGenericStorage<Integer, Pair<Integer, String>> {

        @Override
        protected Integer getIdentifierFromValue(final Pair<Integer, String> value) {
            return value.getLeft();
        }
    }
}
