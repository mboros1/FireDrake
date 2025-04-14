package ai.electric_dreams.fire_drake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class SpringRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(SpringRunner.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Source Arguments: {}", List.of(args.getSourceArgs()));
        logger.info("Command Line Options: {}", args.getOptionNames());
        for (String name : args.getOptionNames()) {
            logger.info("  --{} = {}", name, args.getOptionValues(name));
        }

        logger.info("Positional Arguments: {}", args.getNonOptionArgs());
    }
}
