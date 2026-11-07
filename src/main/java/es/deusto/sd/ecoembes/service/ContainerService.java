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

// Patrón AppService: lógica de negocio de los contenedores y de sus lecturas, junto con el ESTADO DEL
// SERVIDOR (en el Prototipo 1 los datos viven en memoria, sin base de datos). Las estructuras son
// concurrentes porque el bean es único y lo comparten todos los hilos de peticiones, además del
// simulador de sensores.
@Service
public class ContainerService {

	// Mensajes de las excepciones. El controller los usa en un switch para traducirlos a códigos HTTP,
	// por eso son constantes compartidas y no textos repetidos.
	public static final String INVALID_DATA = "Datos no válidos";
	public static final String INVALID_DATES = "Fechas o periodo no válidos";
	public static final String CONTAINER_ALREADY_EXISTS = "Ya existe un contenedor con ese identificador";
	public static final String CONTAINER_NOT_FOUND = "Contenedor no encontrado";
	public static final String NO_READINGS_FOUND = "No se han encontrado lecturas";

	// Contenedores indexados por su identificador
	private final Map<String, Container> containerRepository = new ConcurrentHashMap<>();

	// Lecturas por contenedor, ordenadas por fecha. Al usar la fecha como clave solo puede haber una
	// lectura por contenedor y día: una lectura nueva del mismo día reemplaza a la anterior.
	private final Map<String, NavigableMap<LocalDate, ContainerReading>> readingStore = new ConcurrentHashMap<>();

	// Generador de identificadores internos de las lecturas
	private final AtomicLong readingSequence = new AtomicLong(1);

	// Crea un contenedor nuevo. Datos incorrectos -> INVALID_DATA; identificador repetido -> CONTAINER_ALREADY_EXISTS.
	public Container createContainer(String containerId, String location, String postalCode, Float initialCapacity) {
		if (isBlank(containerId) || isBlank(location) || isBlank(postalCode)
				|| initialCapacity == null || initialCapacity <= 0) {
			throw new RuntimeException(INVALID_DATA);
		}

		Container container = new Container(containerId, location, postalCode, initialCapacity);

		// putIfAbsent es atómico: si dos peticiones crean el mismo identificador a la vez, solo una gana
		if (containerRepository.putIfAbsent(containerId, container) != null) {
			throw new RuntimeException(CONTAINER_ALREADY_EXISTS);
		}

		readingStore.put(containerId, new ConcurrentSkipListMap<>());

		return container;
	}

	// Guarda una lectura del sensor; si ya había una de ese día, la reemplaza.
	// Datos incorrectos -> INVALID_DATA; contenedor desconocido -> CONTAINER_NOT_FOUND.
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

	// Lecturas de un contenedor entre dos fechas (ambas incluidas), ordenadas por fecha.
	// Fechas ausentes o con inicio posterior al fin -> INVALID_DATES; contenedor desconocido -> CONTAINER_NOT_FOUND.
	// Un contenedor sin lecturas en el periodo devuelve una lista vacía (no es un error).
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

	// Lecturas de todos los contenedores de un código postal en un día, ordenadas por identificador.
	// Parámetros ausentes -> INVALID_DATA; ninguna lectura para esa zona y fecha -> NO_READINGS_FOUND.
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

	// Contenedor por identificador, o null si no existe (lo usan otros servicios)
	public Container getContainer(String containerId) {
		return containerId == null ? null : containerRepository.get(containerId);
	}

	// Última lectura de un contenedor en la fecha indicada o antes, o null si no hay ninguna.
	// Sirve para calcular los envases de un contenedor al asignarlo a una planta.
	public ContainerReading getLatestReading(String containerId, LocalDate date) {
		NavigableMap<LocalDate, ContainerReading> readings = containerId == null ? null : readingStore.get(containerId);

		if (readings == null || date == null) {
			return null;
		}

		Map.Entry<LocalDate, ContainerReading> entry = readings.floorEntry(date);

		return entry == null ? null : entry.getValue();
	}

	// Todos los contenedores (lo usa el simulador de sensores)
	public Collection<Container> getContainers() {
		return containerRepository.values();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
