package ai.electric_dreams.fire_drake.command.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LiteralCommandNode implements CommandTreeNode {

    private static final Logger logger = LoggerFactory.getLogger(LiteralCommandNode.class);

    private final String literal;
    private final Map<String, LiteralCommandNode> literalChildren = new HashMap<>();
    private final List<ArgumentCommandNode> argumentChildren = new ArrayList<>();
    private CommandExecutor executor;


    public LiteralCommandNode(String literal) {
        this.literal = literal;
    }


    @Override
    public void addChild(CommandTreeNode child) {
        if (child instanceof LiteralCommandNode literalChild) {
            literalChildren.put(literalChild.getLiteral(), literalChild);
        } else if (child instanceof ArgumentCommandNode argumentChild) {
            argumentChildren.add(argumentChild);
        } else {
            logger.info("Unknown command node: " + child);
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

        var next = literalChildren.get(tokens[index]);
        if (next != null) {
            next.dispatch(source, tokens, index + 1);
            return;
        }

        for (var argChild : argumentChildren) {
            argChild.dispatch(source, tokens, index);
            return;
        }

        if (executor != null) {
            executor.execute(source, Arrays.copyOfRange(tokens, index, tokens.length));
        } else {
            source.sendMessage("Unknown command: " + tokens[index]);
        }
    }
}
