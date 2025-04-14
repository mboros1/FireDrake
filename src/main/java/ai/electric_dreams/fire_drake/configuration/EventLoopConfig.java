package ai.electric_dreams.fire_drake.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class EventLoopConfig {
    private static final Logger logger = LoggerFactory.getLogger(EventLoopConfig.class);

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster(TaskExecutor taskExecutor) {
        logger.info("Creating application event multicaster");
        SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();
        logger.info("Registering application event multicaster with [{}] executor", taskExecutor.getClass().getSimpleName());
        multicaster.setTaskExecutor(taskExecutor);
        return multicaster;
    }

}
