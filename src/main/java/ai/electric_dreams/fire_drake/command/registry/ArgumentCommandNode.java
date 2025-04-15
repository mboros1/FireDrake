package ai.electric_dreams.fire_drake.command.registry;

import java.util.Arrays;

public class ArgumentCommandNode implements CommandTreeNode {

    private final ArgumentParser parser;
    private CommandExecutor executor;

    public ArgumentCommandNode(ArgumentParser parser) {
        this.parser = parser;
    }

    @Override
    public void addChild(CommandTreeNode child) {
        throw new UnsupportedOperationException("Arguments can't have children in this simple model.");
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void dispatch(CommandSource source, String[] tokens, int index) {
        if (index >= tokens.length) {
            source.sendMessage("Expected argument.");
            return;
        }

        String[] remaining = Arrays.copyOfRange(tokens, index, tokens.length);
        Object arg = parser.parse(String.join(" ", remaining));

        if (executor != null) {
            executor.execute(source, new Object[]{arg});
        } else {
            source.sendMessage("Missing execution target.");
        }
    }

}

