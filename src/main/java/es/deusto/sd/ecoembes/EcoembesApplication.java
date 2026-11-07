package es.deusto.sd.ecoembes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Punto de entrada. @EnableScheduling activa las tareas programadas (el simulador de sensores a las 3:00).
@SpringBootApplication
@EnableScheduling
public class EcoembesApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcoembesApplication.class, args);
	}
}
