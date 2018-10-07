package com.example.alva.storage;

import org.springframework.stereotype.Component;

import com.example.alva.datamodel.VisitorProcess;

@Component
public class InMemoryProcessStorage extends AbstractCacheBasedGenericStorage<String, VisitorProcess>
    implements ProcessStorage {

    @Override
    protected String getIdentifierFromValue(final VisitorProcess value) {
        return value.getId();
    }
}
