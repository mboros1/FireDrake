package ai.electric_dreams.fire_drake.command.registry;

@FunctionalInterface
public interface CommandExecutor {
    void execute(CommandSource source, Object[] args);
}
