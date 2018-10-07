package com.example.alva.storage;

import org.springframework.stereotype.Component;

@Component
public class InMemoryProcessStorage extends AbstractCacheBasedGenericStorage<String, VisitorProcess>
    implements ProcessStorage {

    @Override
    protected String getIdentifierFromValue(final VisitorProcess value) {
        return value.getId();
    }
}
