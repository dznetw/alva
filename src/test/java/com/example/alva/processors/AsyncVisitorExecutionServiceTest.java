package com.example.alva.processors;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.alva.TestConstants;
import com.example.alva.storage.VisitorProcess;
import com.example.alva.storage.VisitorResult;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = AsyncVisitorExecutionServiceTest.TestConfig.class)
public class AsyncVisitorExecutionServiceTest {

    @Autowired
    private AsyncVisitorProcessor processorBean;
    @Autowired
    private AsyncContentTypeChecker checkerBean;
    private AsyncVisitorExecutionService executionService;

    @Before
    public void setUp() {
        this.executionService = new AsyncVisitorExecutionService(this.processorBean, this.checkerBean);
    }

    @Test
    public void execute_recursionOfZero() throws IOException, InterruptedException {
        final VisitorProcess origin = new VisitorProcess(TestConstants.DEFAULT_URL);
        final VisitorResult result = new VisitorResult(origin);

        this.executionService.execute(origin, result, 0);

        assertThat(result.getNumberOfUniqueURIs()).isEqualTo(TestConstants.getExpectedChildURLs());
    }

    @Configuration
    @EnableAsync
    public static class TestConfig {

        @Bean
        @Primary
        public AsyncVisitorProcessor asyncLinkVisitorProcessor() {
            return new AsyncVisitorProcessor();
        }

        @Bean
        @Primary
        public AsyncContentTypeChecker asyncContentTypeChecker() {
            return new AsyncContentTypeChecker();
        }

        @Bean
        @Primary
        public TaskExecutor taskExecutor() {
            final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(250);
            executor.setRejectedExecutionHandler((runnable, executor1) -> {
                try {
                    executor1.getQueue().put(runnable);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            });
            return executor;
        }
    }
}
