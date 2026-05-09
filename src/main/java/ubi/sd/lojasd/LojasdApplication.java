package ubi.sd.lojasd;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LojasdApplication {

	private static final Logger logger = LoggerFactory.getLogger(LojasdApplication.class);

	public static void main(String[] args) {
		logger.info("Loading environment variables from .env file...");
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(entry -> {
			if (System.getProperty(entry.getKey()) == null) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
		});

		SpringApplication.run(LojasdApplication.class, args);
	}

}
