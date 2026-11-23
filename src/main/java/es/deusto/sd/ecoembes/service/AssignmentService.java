package es.deusto.sd.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.deusto.sd.ecoembes.dao.AssignmentRepository;
import es.deusto.sd.ecoembes.entity.Assignment;
import es.deusto.sd.ecoembes.entity.Container;
import es.deusto.sd.ecoembes.entity.ContainerReading;
import es.deusto.sd.ecoembes.entity.Employee;
import es.deusto.sd.ecoembes.entity.PlantCapacity;
import es.deusto.sd.ecoembes.entity.RecyclingPlant;

// Patrón AppService: orquesta la asignación de contenedores a plantas. Colabora con AuthService
// (quién es el empleado), ContainerService (envases de cada contenedor) y PlantService (capacidad de la
// planta), y guarda las asignaciones en la base de datos mediante AssignmentRepository.
@Service
public class AssignmentService {

	// Mensajes de las excepciones; el controller los traduce a códigos HTTP en un switch
	public static final String INVALID_DATA = "Datos no válidos";
	public static final String UNAUTHORIZED = "Usuario no autenticado";
	public static final String INSUFFICIENT_CAPACITY = "Capacidad de la planta insuficiente";
	public static final String CONTAINER_ALREADY_ASSIGNED = "El contenedor ya está asignado ese día";
	public static final String NO_ASSIGNMENTS_FOUND = "No se han encontrado asignaciones";

	// Supuesto documentado: peso de un envase en kg. Toneladas = envases * KG_PER_PACKAGE / 1000
	public static final double KG_PER_PACKAGE = 0.03;

	private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);

	private final AuthService authService;
	private final ContainerService containerService;
	private final PlantService plantService;
	private final AssignmentRepository assignmentRepository;

	public AssignmentService(AuthService authService, ContainerService containerService, PlantService plantService,
			AssignmentRepository assignmentRepository) {
		this.authService = authService;
		this.containerService = containerService;
		this.plantService = plantService;
		this.assignmentRepository = assignmentRepository;
	}

	// Asigna contenedores a una planta si esta tiene capacidad disponible ese día.
	// Orden de las comprobaciones: token (UNAUTHORIZED), datos (INVALID_DATA), planta y contenedores
	// (PLANT_NOT_FOUND / CONTAINER_NOT_FOUND), contenedor ya asignado ese día y capacidad (ambos
	// producen un conflicto). Es @Transactional y bloquea la fila de la planta (getPlantForUpdate) para que
	// comprobar la capacidad y guardar la asignación sea una operación atómica: dos asignaciones simultáneas
	// a la misma planta se ejecutan una detrás de otra y no pueden superar la capacidad entre las dos.
	@Transactional
	public Assignment createAssignment(String userToken, String plantId, List<String> containerIds,
			LocalDate date) {
		Employee employee = authService.getEmployeeByToken(userToken);

		if (employee == null) {
			throw new RuntimeException(UNAUTHORIZED);
		}

		if (plantId == null || plantId.isBlank() || containerIds == null || containerIds.isEmpty()
				|| containerIds.contains(null) || new HashSet<>(containerIds).size() != containerIds.size()
				|| !plantService.isInWindow(date)) {
			throw new RuntimeException(INVALID_DATA);
		}

		RecyclingPlant plant = plantService.getPlantForUpdate(plantId);

		if (plant == null) {
			throw new RuntimeException(PlantService.PLANT_NOT_FOUND);
		}

		List<Container> containers = new ArrayList<>();
		int totalPackages = 0;

		for (String containerId : containerIds) {
			Container container = containerService.getContainer(containerId);

			if (container == null) {
				throw new RuntimeException(ContainerService.CONTAINER_NOT_FOUND);
			}

			// Envases de un contenedor = los de su última lectura en la fecha o antes (0 si no tiene ninguna)
			ContainerReading reading = containerService.getLatestReading(containerId, date);

			// Un contenedor no puede asignarse más de una vez el mismo día (a ninguna planta)
			if (isContainerAssigned(containerId, date)) {
				throw new RuntimeException(CONTAINER_ALREADY_ASSIGNED);
			}

			containers.add(container);
			totalPackages += reading == null ? 0 : reading.getPackages();
		}

		double tons = toTons(totalPackages);

		if (tons > getAvailableTons(plantId, date)) {
			throw new RuntimeException(INSUFFICIENT_CAPACITY);
		}

		// assignedBy guarda el email del empleado del token: registro de auditoría
		Assignment assignment = assignmentRepository.save(
				new Assignment(plant, date, containers, totalPackages, tons, employee.getEmail()));

		// Notificación a la planta (en el Prototipo 1 se simula con un mensaje de log)
		logger.info("Planta {} notificada: {} contenedores, {} envases para el {}", plant.getName(), containers.size(),
				totalPackages, date);

		return assignment;
	}

	// Asignaciones de una planta en una fecha, ordenadas por identificador.
	// Parámetros ausentes -> INVALID_DATA; sin asignaciones (o planta sin ellas) -> NO_ASSIGNMENTS_FOUND.
	public List<Assignment> getAssignments(LocalDate date, String plantId) {
		if (date == null || plantId == null || plantId.isBlank()) {
			throw new RuntimeException(INVALID_DATA);
		}

		List<Assignment> result = assignmentRepository.findByDateAndPlantIdOrderById(date, plantId);

		if (result.isEmpty()) {
			throw new RuntimeException(NO_ASSIGNMENTS_FOUND);
		}

		return result;
	}

	// Capacidad disponible (en toneladas) = capacidad del día - toneladas ya asignadas a la planta ese día
	// (mínimo 0). Es el único sitio donde se calcula: lo usan tanto el 409 de createAssignment como la
	// consulta de capacidad de las plantas, para que ambas den siempre el mismo resultado.
	public double getAvailableTons(String plantId, LocalDate date) {
		PlantCapacity capacity = plantService.getPlantCapacity(plantId, date);

		return capacity == null ? 0 : Math.max(0, capacity.getTons() - getAssignedTons(plantId, date));
	}

	// Verdadero si el contenedor ya está en alguna asignación de ese día (de cualquier planta)
	private boolean isContainerAssigned(String containerId, LocalDate date) {
		return assignmentRepository.existsByDateAndContainersId(date, containerId);
	}

	// Toneladas ya asignadas a una planta en una fecha
	private double getAssignedTons(String plantId, LocalDate date) {
		return assignmentRepository.findByDateAndPlantIdOrderById(date, plantId).stream()
				.mapToDouble(Assignment::getTons).sum();
	}

	// Conversión de envases a toneladas según el supuesto KG_PER_PACKAGE
	private double toTons(int packages) {
		return packages * KG_PER_PACKAGE / 1000;
	}
}
