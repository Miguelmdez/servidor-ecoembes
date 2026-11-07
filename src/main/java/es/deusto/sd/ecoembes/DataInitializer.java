package es.deusto.sd.ecoembes;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.deusto.sd.ecoembes.entity.Employee;
import es.deusto.sd.ecoembes.entity.RecyclingPlant;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.ContainerService;

@Configuration
public class DataInitializer {

	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

	@Bean
	CommandLineRunner initData(AuthService authService, ContainerService containerService,
			SensorSimulator sensorSimulator) {
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
			containerService.createContainer("CONT-001", "Gran Via 10, Bilbao", "48001", 1000f);
			containerService.createContainer("CONT-002", "Calle Colon de Larreategui 5, Bilbao", "48001", 1000f);
			containerService.createContainer("CONT-003", "Plaza Moyua 1, Bilbao", "48001", 1500f);
			containerService.createContainer("CONT-004", "Calle Dato 20, Vitoria-Gasteiz", "01001", 1000f);
			containerService.createContainer("CONT-005", "Paseo de la Senda 8, Vitoria-Gasteiz", "01001", 1200f);
			containerService.createContainer("CONT-006", "Calle Postas 3, Vitoria-Gasteiz", "01001", 1500f);

			// Readings of the last 7 days (today included)
			for (int i = 6; i >= 0; i--) {
				sensorSimulator.simulateDay(LocalDate.now().minusDays(i));
			}

			// There is no plant service yet: plants will be stored there in the next tasks
			logger.info("{} plants created (not stored yet), {} containers with 7 days of readings stored!",
					plants.size(), containerService.getContainers().size());
		};
	}
}
