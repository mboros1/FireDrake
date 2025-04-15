package ai.electric_dreams.fire_drake.command.registry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LiteralCommandNode implements CommandTreeNode {
    private final String literal;
    private final Map<String, CommandTreeNode> children = new HashMap<>();
    private CommandExecutor executor;


    public LiteralCommandNode(String literal) {
        this.literal = literal;
    }


    @Override
    public void addChild(CommandTreeNode child) {
        if (child instanceof LiteralCommandNode literalChild) {
            children.put(literalChild.getLiteral(), child);
        }
    }

    public String getLiteral() {
        return literal;
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void dispatch(CommandSource source, String[] tokens, int index) {
        if (index >= tokens.length) {
            if (executor != null) {
                executor.execute(source, new String[0]);
            }
            return;
        }

        var next = children.get(tokens[index]);
        if (next != null) {
            next.dispatch(source, tokens, index + 1);
        } else if (executor != null) {
            executor.execute(source, Arrays.copyOfRange(tokens, index, tokens.length));
        } else {
            source.sendMessage("Unknown command.");
        }
    }
}
