package tt.haschat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    public static final String ASYNC_EXECUTOR_BEAN_NAME = "threadPoolTaskExecutor";

    @Bean(name = ASYNC_EXECUTOR_BEAN_NAME)
    public Executor executor(@Value("${hashcat.tt.executors.basic.pool.size}") int poolSize) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("processor-executor-");
        threadPoolTaskExecutor.setCorePoolSize(poolSize);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
