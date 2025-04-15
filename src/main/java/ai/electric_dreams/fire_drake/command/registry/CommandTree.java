package ai.electric_dreams.fire_drake.command.registry;

import org.springframework.stereotype.Component;

@Component
public class CommandTree {
    private final CommandTreeNode root = new LiteralCommandNode("");

    public void register(CommandTreeNode node) {
        root.addChild(node);
    }

    public void dispatch(CommandSource source, String line) {
        String[] tokens = line.trim().split("\\s+");
        root.dispatch(source, tokens, 0);
    }
}
