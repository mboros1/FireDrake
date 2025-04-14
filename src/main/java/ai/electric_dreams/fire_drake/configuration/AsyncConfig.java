package ai.electric_dreams.fire_drake.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean
    @Primary
    public TaskExecutor taskExecutor() {
        var executor = new VirtualThreadTaskExecutor();
        logger.info("Configuring task executor: [{}]", executor.getClass().getSimpleName());

        return executor;
    }
}
