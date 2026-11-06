package es.deusto.sd.ecoembes;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.deusto.sd.ecoembes.entity.Container;
import es.deusto.sd.ecoembes.entity.Employee;
import es.deusto.sd.ecoembes.entity.RecyclingPlant;
import es.deusto.sd.ecoembes.service.AuthService;

@Configuration
public class DataInitializer {

	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

	@Bean
	CommandLineRunner initData(AuthService authService) {
		return args -> {
			// Create some Ecoembes employees
			Employee ana = new Employee(1, "Ana Garcia", "ana.garcia@ecoembes.com", "Ana#2026");
			Employee luis = new Employee(2, "Luis Martinez", "luis.martinez@ecoembes.com", "Luis#2026");
			Employee marta = new Employee(3, "Marta Lopez", "marta.lopez@ecoembes.com", "Marta#2026");

			authService.addEmployee(ana);
			authService.addEmployee(luis);
			authService.addEmployee(marta);
			logger.info("Employees saved!");

			// Create the recycling plants
			List<RecyclingPlant> plants = List.of(
				new RecyclingPlant(1, "PlasSB Ltd."),
				new RecyclingPlant(2, "ContSocket Ltd."));

			// Create 6 containers in 2 postal codes
			List<Container> containers = List.of(
				new Container(1, "48001", "Gran Via 10, Bilbao", 1000),
				new Container(2, "48001", "Calle Colon de Larreategui 5, Bilbao", 1000),
				new Container(3, "48001", "Plaza Moyua 1, Bilbao", 1500),
				new Container(4, "01001", "Calle Dato 20, Vitoria-Gasteiz", 1000),
				new Container(5, "01001", "Paseo de la Senda 8, Vitoria-Gasteiz", 1200),
				new Container(6, "01001", "Calle Postas 3, Vitoria-Gasteiz", 1500));

			// There is no plant/container service yet: they will be stored there in the next tasks
			logger.info("{} plants and {} containers created (not stored yet)!", plants.size(), containers.size());
		};
	}
}
