package es.deusto.sd.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.entity.Container;
import es.deusto.sd.ecoembes.entity.ContainerReading;
import es.deusto.sd.ecoembes.entity.FillLevel;

@Service
public class ContainerService {

	// Messages used as exception text and mapped to HTTP codes by the controller
	public static final String INVALID_DATA = "Invalid data";
	public static final String INVALID_DATES = "Invalid dates or period";
	public static final String CONTAINER_ALREADY_EXISTS = "Container already exists";
	public static final String CONTAINER_NOT_FOUND = "Container not found";
	public static final String NO_READINGS_FOUND = "No readings found";

	// Containers by id (thread-safe: shared across request threads)
	private final Map<String, Container> containerRepository = new ConcurrentHashMap<>();

	// Readings by container id; one reading per container and day (sorted by date)
	private final Map<String, NavigableMap<LocalDate, ContainerReading>> readingStore = new ConcurrentHashMap<>();

	private final AtomicLong readingSequence = new AtomicLong(1);

	// Creates a new container
	public Container createContainer(String containerId, String location, String postalCode, Float initialCapacity) {
		if (isBlank(containerId) || isBlank(location) || isBlank(postalCode)
				|| initialCapacity == null || initialCapacity <= 0) {
			throw new RuntimeException(INVALID_DATA);
		}

		Container container = new Container(containerId, location, postalCode, initialCapacity);

		if (containerRepository.putIfAbsent(containerId, container) != null) {
			throw new RuntimeException(CONTAINER_ALREADY_EXISTS);
		}

		readingStore.put(containerId, new ConcurrentSkipListMap<>());

		return container;
	}

	// Stores a sensor reading; a new reading for the same day replaces the previous one
	public ContainerReading addReading(String containerId, LocalDate date, Integer estimatedPackages, FillLevel fillLevel) {
		if (isBlank(containerId) || date == null || estimatedPackages == null || estimatedPackages < 0
				|| fillLevel == null) {
			throw new RuntimeException(INVALID_DATA);
		}

		Container container = containerRepository.get(containerId);
		NavigableMap<LocalDate, ContainerReading> readings = readingStore.get(containerId);

		if (container == null || readings == null) {
			throw new RuntimeException(CONTAINER_NOT_FOUND);
		}

		ContainerReading reading = new ContainerReading(readingSequence.getAndIncrement(), container, date, fillLevel,
				estimatedPackages);
		readings.put(date, reading);

		return reading;
	}

	// Readings of a container between two dates (both included)
	public List<ContainerReading> getContainerReadings(String containerId, LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
			throw new RuntimeException(INVALID_DATES);
		}

		NavigableMap<LocalDate, ContainerReading> readings = containerId == null ? null : readingStore.get(containerId);

		if (readings == null) {
			throw new RuntimeException(CONTAINER_NOT_FOUND);
		}

		return new ArrayList<>(readings.subMap(startDate, true, endDate, true).values());
	}

	// Readings of all the containers of a postal code for a given day
	public List<ContainerReading> getZoneReadings(String postalCode, LocalDate date) {
		if (isBlank(postalCode) || date == null) {
			throw new RuntimeException(INVALID_DATA);
		}

		List<ContainerReading> result = new ArrayList<>();

		for (Container container : containerRepository.values()) {
			if (postalCode.equals(container.getPostalCode())) {
				ContainerReading reading = readingStore.get(container.getId()).get(date);

				if (reading != null) {
					result.add(reading);
				}
			}
		}

		if (result.isEmpty()) {
			throw new RuntimeException(NO_READINGS_FOUND);
		}

		result.sort((a, b) -> a.getContainer().getId().compareTo(b.getContainer().getId()));

		return result;
	}

	// Container by id, or null if it does not exist
	public Container getContainer(String containerId) {
		return containerId == null ? null : containerRepository.get(containerId);
	}

	// Latest reading of a container on or before a date, or null if there is none
	public ContainerReading getLatestReading(String containerId, LocalDate date) {
		NavigableMap<LocalDate, ContainerReading> readings = containerId == null ? null : readingStore.get(containerId);

		if (readings == null || date == null) {
			return null;
		}

		Map.Entry<LocalDate, ContainerReading> entry = readings.floorEntry(date);

		return entry == null ? null : entry.getValue();
	}

	// All the containers (used by the sensor simulator)
	public Collection<Container> getContainers() {
		return containerRepository.values();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
