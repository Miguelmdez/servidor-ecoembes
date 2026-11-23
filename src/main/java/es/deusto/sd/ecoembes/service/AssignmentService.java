package es.deusto.sd.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.deusto.sd.ecoembes.dao.AssignmentRepository;
import es.deusto.sd.ecoembes.entity.Assignment;
import es.deusto.sd.ecoembes.entity.Container;
import es.deusto.sd.ecoembes.entity.ContainerReading;
import es.deusto.sd.ecoembes.entity.Employee;
import es.deusto.sd.ecoembes.entity.RecyclingPlant;
import es.deusto.sd.ecoembes.external.IRecyclingPlantGateway;
import es.deusto.sd.ecoembes.external.PlantUnavailableException;
import es.deusto.sd.ecoembes.external.RecyclingPlantGatewayFactory;

// Patrón AppService: orquesta la asignación de contenedores a plantas. Colabora con AuthService
// (quién es el empleado), ContainerService (envases de cada contenedor), PlantService (planta) y el gateway de la
// planta (capacidad y notificación, obtenido con RecyclingPlantGatewayFactory), y guarda las asignaciones en la
// base de datos mediante AssignmentRepository.
@Service
public class AssignmentService {

	// Mensajes de las excepciones; el controller los traduce a códigos HTTP en un switch
	public static final String INVALID_DATA = "Datos no válidos";
	public static final String UNAUTHORIZED = "Usuario no autenticado";
	public static final String INSUFFICIENT_CAPACITY = "Capacidad de la planta insuficiente";
	public static final String PLANT_REJECTED = "La planta ha rechazado la asignación";
	public static final String CONTAINER_ALREADY_ASSIGNED = "El contenedor ya está asignado ese día";
	public static final String NO_ASSIGNMENTS_FOUND = "No se han encontrado asignaciones";

	// Supuesto documentado: peso de un envase en kg. Toneladas = envases * KG_PER_PACKAGE / 1000
	public static final double KG_PER_PACKAGE = 0.03;

	private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);

	private final AuthService authService;
	private final ContainerService containerService;
	private final PlantService plantService;
	private final AssignmentRepository assignmentRepository;
	private final RecyclingPlantGatewayFactory gatewayFactory;

	public AssignmentService(AuthService authService, ContainerService containerService, PlantService plantService,
			AssignmentRepository assignmentRepository, RecyclingPlantGatewayFactory gatewayFactory) {
		this.authService = authService;
		this.containerService = containerService;
		this.plantService = plantService;
		this.assignmentRepository = assignmentRepository;
		this.gatewayFactory = gatewayFactory;
	}

	// Asigna contenedores a una planta si esta tiene capacidad disponible ese día.
	// Orden de las comprobaciones: token (UNAUTHORIZED), datos (INVALID_DATA), planta y contenedores
	// (PLANT_NOT_FOUND / CONTAINER_NOT_FOUND), contenedor ya asignado ese día, capacidad disponible según la
	// planta (INSUFFICIENT_CAPACITY) y aceptación de la planta (PLANT_REJECTED); estos tres últimos producen un
	// conflicto. Si la planta no responde -> PLANT_UNAVAILABLE. La asignación solo se guarda si la planta la acepta.
	// Es @Transactional y bloquea la fila de la planta (getPlantForUpdate) para que comprobar la capacidad,
	// notificar y guardar sean una operación atómica respecto a otras asignaciones a la misma planta.
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

		// La capacidad la gestiona la planta: se le pregunta a través de su gateway (REST o sockets, según su
		// gatewayType). Una planta sin datos para ese día equivale a capacidad 0.
		IRecyclingPlantGateway gateway = gatewayFactory.getGateway(plant);

		try {
			Optional<Double> available = gateway.getCapacity(date);

			if (available.isEmpty() || tons > available.get()) {
				throw new RuntimeException(INSUFFICIENT_CAPACITY);
			}

			// Si cabe, se notifica la asignación a la planta; si la rechaza, no se guarda nada
			if (!gateway.notifyAssignment(date, containerIds, totalPackages)) {
				throw new RuntimeException(PLANT_REJECTED);
			}
		} catch (PlantUnavailableException e) {
			logger.warn("La planta {} no responde: {}", plant.getName(), e.getMessage());

			throw new RuntimeException(PlantService.PLANT_UNAVAILABLE, e);
		}

		// Solo se guarda la asignación en la base de datos si la planta la ha aceptado.
		// assignedBy guarda el email del empleado del token: registro de auditoría
		Assignment assignment = assignmentRepository.save(
				new Assignment(plant, date, containers, totalPackages, tons, employee.getEmail()));

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

	// Verdadero si el contenedor ya está en alguna asignación de ese día (de cualquier planta)
	private boolean isContainerAssigned(String containerId, LocalDate date) {
		return assignmentRepository.existsByDateAndContainersId(date, containerId);
	}

	// Conversión de envases a toneladas según el supuesto KG_PER_PACKAGE
	private double toTons(int packages) {
		return packages * KG_PER_PACKAGE / 1000;
	}
}
