package ai.electric_dreams.fire_drake.command.registry;

import org.springframework.stereotype.Component;

@Component
public class StringArgumentParser implements ArgumentParser {
    @Override
    public Object parse(String input) {
        return input;
    }
}

