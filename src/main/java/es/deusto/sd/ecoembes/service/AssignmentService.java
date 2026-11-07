package es.deusto.sd.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.entity.Assignment;
import es.deusto.sd.ecoembes.entity.Container;
import es.deusto.sd.ecoembes.entity.ContainerReading;
import es.deusto.sd.ecoembes.entity.Employee;
import es.deusto.sd.ecoembes.entity.PlantCapacity;
import es.deusto.sd.ecoembes.entity.RecyclingPlant;

// Patrón AppService: orquesta la asignación de contenedores a plantas. Colabora con AuthService
// (quién es el empleado), ContainerService (envases de cada contenedor) y PlantService (capacidad de la
// planta), y guarda el ESTADO DEL SERVIDOR de las asignaciones en memoria (Prototipo 1).
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

	// Asignaciones indexadas por su identificador interno
	private final Map<Long, Assignment> assignmentStore = new ConcurrentHashMap<>();

	// Generador de identificadores de asignación
	private final AtomicLong assignmentSequence = new AtomicLong(1);

	public AssignmentService(AuthService authService, ContainerService containerService, PlantService plantService) {
		this.authService = authService;
		this.containerService = containerService;
		this.plantService = plantService;
	}

	// Asigna contenedores a una planta si esta tiene capacidad disponible ese día.
	// Orden de las comprobaciones: token (UNAUTHORIZED), datos (INVALID_DATA), planta y contenedores
	// (PLANT_NOT_FOUND / CONTAINER_NOT_FOUND), contenedor ya asignado ese día y capacidad (ambos
	// producen un conflicto). Es synchronized para que comprobar la capacidad y guardar la asignación
	// sea una operación atómica: dos asignaciones simultáneas no pueden superar la capacidad entre las dos.
	public synchronized Assignment createAssignment(String userToken, String plantId, List<String> containerIds,
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

		RecyclingPlant plant = plantService.getPlant(plantId);

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
		Assignment assignment = new Assignment(assignmentSequence.getAndIncrement(), plant, date, containers,
				totalPackages, tons, employee.getEmail());
		assignmentStore.put(assignment.getId(), assignment);

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

		List<Assignment> result = new ArrayList<>();

		for (Assignment assignment : assignmentStore.values()) {
			if (assignment.getDate().equals(date) && assignment.getPlant().getId().equals(plantId)) {
				result.add(assignment);
			}
		}

		if (result.isEmpty()) {
			throw new RuntimeException(NO_ASSIGNMENTS_FOUND);
		}

		result.sort((a, b) -> Long.compare(a.getId(), b.getId()));

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
		for (Assignment assignment : assignmentStore.values()) {
			if (assignment.getDate().equals(date)) {
				for (Container container : assignment.getContainers()) {
					if (container.getId().equals(containerId)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	// Toneladas ya asignadas a una planta en una fecha
	private double getAssignedTons(String plantId, LocalDate date) {
		double total = 0;

		for (Assignment assignment : assignmentStore.values()) {
			if (assignment.getDate().equals(date) && assignment.getPlant().getId().equals(plantId)) {
				total += assignment.getTons();
			}
		}

		return total;
	}

	// Conversión de envases a toneladas según el supuesto KG_PER_PACKAGE
	private double toTons(int packages) {
		return packages * KG_PER_PACKAGE / 1000;
	}
}
