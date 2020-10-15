package bhhoffmann.experimenting.webflux.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ThreadsApplication {

	private static final Logger logger = LoggerFactory.getLogger(ThreadsApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ThreadsApplication.class, args);
	}

	@PostConstruct
	public void init() {
		logger.info("CPU: {}", Runtime.getRuntime().availableProcessors());
	}

}
