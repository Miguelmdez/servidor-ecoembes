package es.deusto.sd.ecoembes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EcoembesApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcoembesApplication.class, args);
	}
}
