package ai.electric_dreams.fire_drake;

import ai.electric_dreams.fire_drake.command.registry.ArgumentCommandNode;
import ai.electric_dreams.fire_drake.command.registry.CommandSource;
import ai.electric_dreams.fire_drake.command.registry.CommandTree;
import ai.electric_dreams.fire_drake.command.registry.LiteralCommandNode;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CommandRegistryTest {

    @Test
    void shouldDispatchLiteralThenArgumentCommand() {
        CommandSource source = mock(CommandSource.class);

        CommandTree registry = new CommandTree();

        LiteralCommandNode say = new LiteralCommandNode("say");
        ArgumentCommandNode message = new ArgumentCommandNode(input -> input);
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

        LiteralCommandNode say = new LiteralCommandNode("say");
        ArgumentCommandNode message = new ArgumentCommandNode(input -> input);
        message.setExecutor((s, args) -> s.sendMessage((String) args[0]));

        say.addChild(message);
        registry.register(say);

        registry.dispatch(source, "say hello hello");

        verify(source).sendMessage("hello hello");
    }
}

