package es.deusto.sd.ecoembes;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.deusto.sd.ecoembes.entity.Container;
import es.deusto.sd.ecoembes.entity.FillLevel;
import es.deusto.sd.ecoembes.service.ContainerService;

// Simulates the daily transmission of the RecycloBin UltraEye sensors
@Component
public class SensorSimulator {

	private static final Logger logger = LoggerFactory.getLogger(SensorSimulator.class);

	private final ContainerService containerService;

	public SensorSimulator(ContainerService containerService) {
		this.containerService = containerService;
	}

	// Every day at 3:00
	@Scheduled(cron = "0 0 3 * * *")
	public void simulateToday() {
		simulateDay(LocalDate.now());
	}

	// Sends one random reading per container for the given day
	public void simulateDay(LocalDate date) {
		for (Container container : containerService.getContainers()) {
			FillLevel fillLevel = FillLevel.values()[ThreadLocalRandom.current().nextInt(FillLevel.values().length)];
			containerService.addReading(container.getId(), date, randomPackages(container, fillLevel), fillLevel);
		}

		logger.info("Sensor readings simulated for {}", date);
	}

	// Packages consistent with the fill level, as % of the capacity: GREEN [0, 40), ORANGE [40, 80), RED [80, 100]
	private int randomPackages(Container container, FillLevel fillLevel) {
		double capacity = container.getInitialCapacity();
		double min = switch (fillLevel) {
			case GREEN -> 0.00;
			case ORANGE -> 0.40;
			case RED -> 0.80;
		};
		double max = switch (fillLevel) {
			case GREEN -> 0.40;
			case ORANGE -> 0.80;
			case RED -> Math.nextUp(1.00);
		};

		return (int) (capacity * ThreadLocalRandom.current().nextDouble(min, max));
	}
}
