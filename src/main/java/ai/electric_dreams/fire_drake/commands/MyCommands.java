package ai.electric_dreams.fire_drake.commands;

import ai.electric_dreams.fire_drake.command.registry.ArgumentCommandNode;
import ai.electric_dreams.fire_drake.command.registry.CommandTree;
import ai.electric_dreams.fire_drake.command.registry.LiteralCommandNode;
import ai.electric_dreams.fire_drake.command.registry.StringArgumentParser;
import org.springframework.stereotype.Component;

@Component
public class MyCommands {
    public MyCommands(CommandTree registry) {
        LiteralCommandNode say = new LiteralCommandNode("say");
        ArgumentCommandNode message = new ArgumentCommandNode(new StringArgumentParser());

        message.setExecutor((source, args) -> {
            String msg = (String) args[0];
            source.sendMessage(msg);
        });

        say.addChild(message);
        registry.register(say);
    }
}
