package com.example.alva.storage;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import com.example.alva.datamodel.VisitorProcess;
import com.example.alva.datamodel.VisitorResult;

public class InMemoryResultStorageTest extends CacheBasedStorageContractVerifier<String, VisitorResult> {

    private static final URI BASE_URI = URI.create("http://www.google.com");
    private static final UUID EXISTING_UUID = UUID.randomUUID();

    public InMemoryResultStorageTest() throws MalformedURLException {

        this(newPairFrom(createNewResult(EXISTING_UUID.toString())),
            newPairFrom(createNewResult(EXISTING_UUID.toString())),
            newPairFrom(createNewResult(UUID.randomUUID().toString())), newPairFrom(createNewResult(null)));
    }

    private InMemoryResultStorageTest(final Pair<String, VisitorResult> existingEntity,
        final Pair<String, VisitorResult> overridingEntity, final Pair<String, VisitorResult> missingEntity,
        final Pair<String, VisitorResult> nullEntity) {
        super(newPairFrom(existingEntity.getValue()), overridingEntity, missingEntity, nullEntity);
    }

    private static VisitorResult createNewResult(final String id) throws MalformedURLException {
        final VisitorProcess spy = Mockito.spy(new VisitorProcess(getBaseURL()));
        Mockito.when(spy.getId()).thenReturn(id);
        return new VisitorResult(spy);
    }

    private static URL getBaseURL() throws MalformedURLException {
        return BASE_URI.toURL();
    }

    private static Pair<String, VisitorResult> newPairFrom(final VisitorResult value) {
        return Pair.of(value.getId(), value);
    }

    @Test
    public void getIdentifierFromValue() throws MalformedURLException {
        final VisitorResult result = new VisitorResult(new VisitorProcess(getBaseURL()));
        final String identifier = new InMemoryResultStorage().getIdentifierFromValue(result);

        Assertions.assertThat(identifier).isEqualTo(result.getId());
    }

    @Override
    protected AbstractCacheBasedGenericStorage<String, VisitorResult> newInstance() {
        return new InMemoryResultStorage();
    }
}
