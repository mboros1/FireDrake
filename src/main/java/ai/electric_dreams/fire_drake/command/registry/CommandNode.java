package ai.electric_dreams.fire_drake.command.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CommandNode {

    private static final Logger logger = LoggerFactory.getLogger(CommandNode.class);
    private static volatile CommandNode instance;
    private static volatile TaskExecutor taskExecutor;
    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        logger.info("Assigning executor to CommandNode Factory..");
        applicationContext = context;
        taskExecutor = context.getBean(TaskExecutor.class);
    }

    private CommandNode() {
        logger.info("initializing CommandNode Factory..");
        // Private constructor to prevent instantiation
    }

    public static void setTaskExecutorForTesting(TaskExecutor executor) {
        taskExecutor = executor;
    }

    private static TaskExecutor getTaskExecutor() {
        return Optional.ofNullable(taskExecutor)
                .orElseThrow(() -> new IllegalStateException("TaskExecutor not initialized. In tests, call setTaskExecutorForTesting first."));
    }

    public static CommandNode getInstance() {
        if (instance == null) {
            synchronized (CommandNode.class) {
                if (instance == null) {
                    instance = new CommandNode();
                }
            }
        }
        return instance;
    }

    public static LiteralCommandNode literal(String name) {
        return new LiteralCommandNode(name);
    }

    public static ArgumentCommandNode argument(ArgumentParser parser) {
        return new ArgumentCommandNode(parser, getTaskExecutor());
    }
} 