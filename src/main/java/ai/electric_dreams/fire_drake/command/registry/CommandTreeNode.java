package ai.electric_dreams.fire_drake.command.registry;

public interface CommandTreeNode {
    void addChild(CommandTreeNode child);
    void dispatch(CommandSource source, String[] tokens, int index);
}
