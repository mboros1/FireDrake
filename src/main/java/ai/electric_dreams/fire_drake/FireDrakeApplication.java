package ai.electric_dreams.fire_drake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;

@SpringBootApplication
public class FireDrakeApplication implements ApplicationRunner {
	private static final Logger logger = LoggerFactory.getLogger(FireDrakeApplication.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(FireDrakeApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);
		app.run(args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("Source Arguments: {}", List.of(args.getSourceArgs()));
		logger.info("Command Line Options: {}", args.getOptionNames());
		for (String name : args.getOptionNames()) {
			logger.info("  --{} = {}", name, args.getOptionValues(name));
		}

		logger.info("Positional Arguments: {}", args.getNonOptionArgs());
		Thread.currentThread().join();
	}
}

