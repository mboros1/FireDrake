package ai.electric_dreams.fire_drake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FireDrakeApplication {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(FireDrakeApplication.class);
		app.setWebApplicationType(WebApplicationType.NONE);
		app.run(args);
	}
}

