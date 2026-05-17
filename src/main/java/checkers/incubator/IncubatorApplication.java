package checkers.incubator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"checkers.incubator", "com.checkers"})
@EnableJpaRepositories(basePackages = "com.checkers.repository")
@EntityScan(basePackages = "com.checkers.entity")
public class IncubatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(IncubatorApplication.class, args);
	}

}
