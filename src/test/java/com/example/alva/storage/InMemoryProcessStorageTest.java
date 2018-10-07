package com.example.alva.storage;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

public class InMemoryProcessStorageTest extends CacheBasedStorageContractVerifier<String, VisitorProcess> {

    private static final URI BASE_URI = URI.create("http://www.google.com");
    private static final UUID EXISTING_UUID = UUID.randomUUID();

    public InMemoryProcessStorageTest() throws MalformedURLException {

        this(newPairFrom(createProcessSpy(EXISTING_UUID.toString())),
            newPairFrom(createProcessSpy(EXISTING_UUID.toString())),
            newPairFrom(createProcessSpy(UUID.randomUUID().toString())), newPairFrom(createProcessMock()));
    }

    private InMemoryProcessStorageTest(final Pair<String, VisitorProcess> existingEntity,
        final Pair<String, VisitorProcess> overridingEntity, final Pair<String, VisitorProcess> missingEntity,
        final Pair<String, VisitorProcess> nullEntity) {
        super(newPairFrom(existingEntity.getValue()), overridingEntity, missingEntity, nullEntity);
    }

    private static VisitorProcess createProcessSpy(final String id) throws MalformedURLException {
        final VisitorProcess spy = Mockito.spy(new VisitorProcess(getBaseURL()));
        Mockito.when(spy.getId()).thenReturn(id);
        return spy;
    }

    private static VisitorProcess createProcessMock() {
        return Mockito.mock(VisitorProcess.class);
    }

    private static URL getBaseURL() throws MalformedURLException {
        return BASE_URI.toURL();
    }

    private static Pair<String, VisitorProcess> newPairFrom(final VisitorProcess value) {
        return Pair.of(value.getId(), value);
    }

    @Test
    public void getIdentifierFromValue() throws MalformedURLException {
        final VisitorProcess process = new VisitorProcess(getBaseURL());
        final String identifier = new InMemoryProcessStorage().getIdentifierFromValue(process);

        Assertions.assertThat(identifier).isEqualTo(process.getId());
    }

    @Override
    protected AbstractCacheBasedGenericStorage<String, VisitorProcess> newInstance() {
        return new InMemoryProcessStorage();
    }
}
