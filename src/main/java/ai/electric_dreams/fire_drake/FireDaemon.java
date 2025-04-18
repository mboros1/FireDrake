package ai.electric_dreams.fire_drake;

import ai.electric_dreams.fire_drake.command.registry.CommandSource;
import ai.electric_dreams.fire_drake.command.registry.CommandTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class FireDaemon implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(FireDaemon.class);

    private final CommandTree registry;

    public FireDaemon(CommandTree registry) {
        logger.info("Initializing command tree registrar");
        this.registry = registry;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Source Arguments: {}", List.of(args.getSourceArgs()));
        logger.info("Command Line Options: {}", args.getOptionNames());
        for (String name : args.getOptionNames()) {
            logger.info("  --{} = {}", name, args.getOptionValues(name));
        }

        logger.info("Positional Arguments: {}", args.getNonOptionArgs());

        CommandSource source = msg -> System.out.println("[GAME] " + msg);
        registry.dispatch(source, "say hey dude");

    }
}
