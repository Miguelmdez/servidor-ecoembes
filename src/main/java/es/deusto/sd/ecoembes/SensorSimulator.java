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

// Simula la transmisión diaria de los sensores RecycloBin UltraEye. En el prototipo no hay sensores
// reales: este componente genera lecturas aleatorias pero coherentes (los envases concuerdan con el
// nivel de llenado) y las entrega a ContainerService.addReading, igual que haría un sensor real.
@Component
public class SensorSimulator {

	private static final Logger logger = LoggerFactory.getLogger(SensorSimulator.class);

	private final ContainerService containerService;

	public SensorSimulator(ContainerService containerService) {
		this.containerService = containerService;
	}

	// Todos los días a las 3:00 (requiere @EnableScheduling en la aplicación)
	@Scheduled(cron = "0 0 3 * * *")
	public void simulateToday() {
		simulateDay(LocalDate.now());
	}

	// Envía una lectura aleatoria por cada contenedor para el día indicado.
	// También la usa DataInitializer para generar el histórico de los últimos días.
	public void simulateDay(LocalDate date) {
		for (Container container : containerService.getContainers()) {
			FillLevel fillLevel = FillLevel.values()[ThreadLocalRandom.current().nextInt(FillLevel.values().length)];
			containerService.addReading(container.getId(), date, randomPackages(container, fillLevel), fillLevel);
		}

		logger.info("Lecturas de sensores simuladas para el {}", date);
	}

	// Envases coherentes con el nivel de llenado, como % de la capacidad inicial (en envases):
	// GREEN [0, 40), ORANGE [40, 80), RED [80, 100]. Los rangos son contiguos y cubren de 0 a 100 %.
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
