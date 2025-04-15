package ai.electric_dreams.fire_drake.command.registry;

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

        Object arg = parser.parse(tokens[index]);
        executor.execute(source, new Object[]{arg});
    }
}

