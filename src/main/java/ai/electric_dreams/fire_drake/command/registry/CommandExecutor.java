package ai.electric_dreams.fire_drake.command.registry;

public interface CommandExecutor {
    void execute(CommandSource source, Object[] args);
}
