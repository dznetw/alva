package com.example.alva.storage;

import org.springframework.stereotype.Component;

@Component
public class InMemoryResultStorage extends AbstractCacheBasedGenericStorage<String, VisitorResult>
    implements ResultStorage {

    @Override
    protected String getIdentifierFromValue(final VisitorResult value) {
        return value.getId();
    }
}
