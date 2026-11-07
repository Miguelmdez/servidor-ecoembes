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
import es.deusto.sd.ecoembes.service.PlantService;

@Configuration
public class DataInitializer {

	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

	@Bean
	CommandLineRunner initData(AuthService authService, ContainerService containerService,
			PlantService plantService, SensorSimulator sensorSimulator) {
		return args -> {
			// Create some Ecoembes employees
			Employee ana = new Employee(1, "Ana Garcia", "ana.garcia@ecoembes.com", "Ana#2026");
			Employee luis = new Employee(2, "Luis Martinez", "luis.martinez@ecoembes.com", "Luis#2026");
			Employee marta = new Employee(3, "Marta Lopez", "marta.lopez@ecoembes.com", "Marta#2026");

			authService.addEmployee(ana);
			authService.addEmployee(luis);
			authService.addEmployee(marta);
			logger.info("Employees saved!");

			// Create the recycling plants (a new plant only requires adding data here)
			List<RecyclingPlant> plants = List.of(
				new RecyclingPlant("PLANT-1", "PlasSB Ltd."),
				new RecyclingPlant("PLANT-2", "ContSocket Ltd."));

			for (RecyclingPlant plant : plants) {
				plantService.addPlant(plant);
			}

			// Daily capacity in tons from today to today + 10 days (day 3 is very low to provoke a 409)
			for (int i = 0; i <= PlantService.MAX_DAYS_AHEAD; i++) {
				LocalDate day = LocalDate.now().plusDays(i);

				plantService.setCapacity("PLANT-1", day, i == 3 ? 0.02 : 6.0 + (i % 3) * 0.5);
				plantService.setCapacity("PLANT-2", day, i == 3 ? 0.03 : 4.0 + (i % 4) * 0.5);
			}

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

			logger.info("{} plants with capacity for the next {} days and {} containers with 7 days of readings stored!",
					plants.size(), PlantService.MAX_DAYS_AHEAD, containerService.getContainers().size());
		};
	}
}
