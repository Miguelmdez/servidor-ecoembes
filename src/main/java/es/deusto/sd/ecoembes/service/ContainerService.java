package es.deusto.sd.ecoembes.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.deusto.sd.ecoembes.dao.ContainerReadingRepository;
import es.deusto.sd.ecoembes.dao.ContainerRepository;
import es.deusto.sd.ecoembes.entity.Container;
import es.deusto.sd.ecoembes.entity.ContainerReading;
import es.deusto.sd.ecoembes.entity.FillLevel;

// Patrón AppService: lógica de negocio de los contenedores y de sus lecturas. Los datos se guardan en la
// base de datos H2 a través de los repositorios (paquete dao). Los métodos que escriben son @Transactional:
// cada uno se ejecuta como una unidad (o se guarda todo o nada).
@Service
public class ContainerService {

	// Mensajes de las excepciones. El controller los usa en un switch para traducirlos a códigos HTTP,
	// por eso son constantes compartidas y no textos repetidos.
	public static final String INVALID_DATA = "Datos no válidos";
	public static final String INVALID_DATES = "Fechas o periodo no válidos";
	public static final String CONTAINER_ALREADY_EXISTS = "Ya existe un contenedor con ese identificador";
	public static final String CONTAINER_NOT_FOUND = "Contenedor no encontrado";
	public static final String NO_READINGS_FOUND = "No se han encontrado lecturas";

	private final ContainerRepository containerRepository;
	private final ContainerReadingRepository readingRepository;

	public ContainerService(ContainerRepository containerRepository, ContainerReadingRepository readingRepository) {
		this.containerRepository = containerRepository;
		this.readingRepository = readingRepository;
	}

	// Crea un contenedor nuevo. Datos incorrectos -> INVALID_DATA; identificador repetido -> CONTAINER_ALREADY_EXISTS.
	@Transactional
	public Container createContainer(String containerId, String location, String postalCode, Float initialCapacity) {
		if (isBlank(containerId) || isBlank(location) || isBlank(postalCode)
				|| initialCapacity == null || initialCapacity <= 0) {
			throw new RuntimeException(INVALID_DATA);
		}

		// Hay que comprobarlo explícitamente: como la clave la asigna el personal, save() sobre un
		// identificador existente lo actualizaría en silencio en vez de fallar
		if (containerRepository.existsById(containerId)) {
			throw new RuntimeException(CONTAINER_ALREADY_EXISTS);
		}

		return containerRepository.save(new Container(containerId, location, postalCode, initialCapacity));
	}

	// Actualiza ubicación, código postal y capacidad inicial de un contenedor existente (el identificador no cambia).
	// Datos incorrectos -> INVALID_DATA; contenedor desconocido -> CONTAINER_NOT_FOUND.
	@Transactional
	public Container updateContainer(String containerId, String location, String postalCode, Float initialCapacity) {
		if (isBlank(containerId) || isBlank(location) || isBlank(postalCode)
				|| initialCapacity == null || initialCapacity <= 0) {
			throw new RuntimeException(INVALID_DATA);
		}

		Container container = containerRepository.findById(containerId)
				.orElseThrow(() -> new RuntimeException(CONTAINER_NOT_FOUND));

		container.setLocation(location);
		container.setPostalCode(postalCode);
		container.setInitialCapacity(initialCapacity);

		return containerRepository.save(container);
	}

	// Guarda una lectura del sensor; si ya había una de ese día, la reemplaza (misma clave compuesta).
	// Datos incorrectos -> INVALID_DATA; contenedor desconocido -> CONTAINER_NOT_FOUND.
	@Transactional
	public ContainerReading addReading(String containerId, LocalDate date, Integer estimatedPackages, FillLevel fillLevel) {
		if (isBlank(containerId) || date == null || estimatedPackages == null || estimatedPackages < 0
				|| fillLevel == null) {
			throw new RuntimeException(INVALID_DATA);
		}

		Container container = containerRepository.findById(containerId)
				.orElseThrow(() -> new RuntimeException(CONTAINER_NOT_FOUND));

		return readingRepository.save(new ContainerReading(container, date, fillLevel, estimatedPackages));
	}

	// Lecturas de un contenedor entre dos fechas (ambas incluidas), ordenadas por fecha.
	// Fechas ausentes o con inicio posterior al fin -> INVALID_DATES; contenedor desconocido -> CONTAINER_NOT_FOUND.
	// Un contenedor sin lecturas en el periodo devuelve una lista vacía (no es un error).
	public List<ContainerReading> getContainerReadings(String containerId, LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
			throw new RuntimeException(INVALID_DATES);
		}

		if (containerId == null || !containerRepository.existsById(containerId)) {
			throw new RuntimeException(CONTAINER_NOT_FOUND);
		}

		return readingRepository.findByContainerIdAndIdDateBetweenOrderByIdDate(containerId, startDate, endDate);
	}

	// Lecturas de todos los contenedores de un código postal en un día, ordenadas por identificador.
	// Parámetros ausentes -> INVALID_DATA; ninguna lectura para esa zona y fecha -> NO_READINGS_FOUND.
	public List<ContainerReading> getZoneReadings(String postalCode, LocalDate date) {
		if (isBlank(postalCode) || date == null) {
			throw new RuntimeException(INVALID_DATA);
		}

		List<ContainerReading> result = readingRepository.findByContainerPostalCodeAndIdDateOrderByContainerId(postalCode, date);

		if (result.isEmpty()) {
			throw new RuntimeException(NO_READINGS_FOUND);
		}

		return result;
	}

	// Contenedor por identificador, o null si no existe (lo usan otros servicios)
	public Container getContainer(String containerId) {
		return containerId == null ? null : containerRepository.findById(containerId).orElse(null);
	}

	// Última lectura de un contenedor en la fecha indicada o antes, o null si no hay ninguna.
	// Sirve para calcular los envases de un contenedor al asignarlo a una planta.
	public ContainerReading getLatestReading(String containerId, LocalDate date) {
		if (containerId == null || date == null) {
			return null;
		}

		return readingRepository.findFirstByContainerIdAndIdDateLessThanEqualOrderByIdDateDesc(containerId, date)
				.orElse(null);
	}

	// Todos los contenedores (lo usa el simulador de sensores)
	public List<Container> getContainers() {
		return containerRepository.findAll();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
