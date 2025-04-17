package ai.electric_dreams.fire_drake;

import ai.electric_dreams.fire_drake.command.registry.ArgumentCommandNode;
import ai.electric_dreams.fire_drake.command.registry.CommandNode;
import ai.electric_dreams.fire_drake.command.registry.CommandSource;
import ai.electric_dreams.fire_drake.command.registry.CommandTree;
import ai.electric_dreams.fire_drake.command.registry.LiteralCommandNode;
import ai.electric_dreams.fire_drake.configuration.AsyncConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = AsyncConfig.class)
class CommandRegistryTest {
    
    @Autowired
    private TaskExecutor taskExecutor;

    @BeforeEach
    void setUp() {
        CommandNode.setTaskExecutorForTesting(taskExecutor);
    }

    @Test
    void shouldDispatchLiteralThenArgumentCommand() {
        CommandSource source = mock(CommandSource.class);

        CommandTree registry = new CommandTree();

        LiteralCommandNode say = CommandNode.literal("say");
        ArgumentCommandNode message = CommandNode.argument(input -> input);
        message.setExecutor((s, args) -> s.sendMessage((String) args[0]));

        say.addChild(message);
        registry.register(say);

        registry.dispatch(source, "say hello");

        verify(source).sendMessage("hello");
    }

    @Test
    void shouldHandleUnknownCommand() {
        CommandSource source = mock(CommandSource.class);
        CommandTree registry = new CommandTree();

        registry.dispatch(source, "nothing");

        verify(source).sendMessage("Unknown command: nothing");
    }

    @Test
    void shouldDispatchLiteralThenArgumentCommand2() {
        CommandSource source = mock(CommandSource.class);

        CommandTree registry = new CommandTree();

        LiteralCommandNode say = CommandNode.literal("say");
        ArgumentCommandNode message = CommandNode.argument(input -> input);

        // TODO: setting the task executor to be single threaded to simplify the testing
        var singleThreadTaskExecutor = new SyncTaskExecutor();
        CommandNode.setTaskExecutorForTesting(singleThreadTaskExecutor);

        message.setExecutor((s, args) -> s.sendMessage((String) args[0]));

        say.addChild(message);
        registry.register(say);

        registry.dispatch(source, "say hello hello");

        verify(source).sendMessage("hello hello");
    }
}

