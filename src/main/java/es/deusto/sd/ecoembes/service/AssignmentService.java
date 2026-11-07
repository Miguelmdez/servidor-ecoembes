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

@Service
public class AssignmentService {

	// Messages used as exception text and mapped to HTTP codes by the controller
	public static final String INVALID_DATA = "Invalid data";
	public static final String UNAUTHORIZED = "User not authenticated";
	public static final String INSUFFICIENT_CAPACITY = "Insufficient plant capacity";
	public static final String CONTAINER_ALREADY_ASSIGNED = "Container already assigned that day";
	public static final String NO_ASSIGNMENTS_FOUND = "No assignments found";

	// Assumption: weight of one package in kg (tons = packages * KG_PER_PACKAGE / 1000)
	public static final double KG_PER_PACKAGE = 0.03;

	private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);

	private final AuthService authService;
	private final ContainerService containerService;
	private final PlantService plantService;

	// Assignments by id (thread-safe: shared across request threads)
	private final Map<Long, Assignment> assignmentStore = new ConcurrentHashMap<>();

	private final AtomicLong assignmentSequence = new AtomicLong(1);

	public AssignmentService(AuthService authService, ContainerService containerService, PlantService plantService) {
		this.authService = authService;
		this.containerService = containerService;
		this.plantService = plantService;
	}

	// Assigns containers to a plant if it has enough remaining capacity that day
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

			// Packages of a container = its latest reading on or before the date (0 if it has none)
			ContainerReading reading = containerService.getLatestReading(containerId, date);

			// A container cannot be assigned more than once the same day (to any plant)
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

		Assignment assignment = new Assignment(assignmentSequence.getAndIncrement(), plant, date, containers,
				totalPackages, tons, employee.getEmail());
		assignmentStore.put(assignment.getId(), assignment);

		// Notification to the plant (simulated with a log)
		logger.info("Plant {} notified: {} containers, {} packages on {}", plant.getName(), containers.size(),
				totalPackages, date);

		return assignment;
	}

	// Assignments of a plant for a date
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

	// Available capacity (tons) = capacity of the day - tons already assigned to the plant that day (min 0)
	public double getAvailableTons(String plantId, LocalDate date) {
		PlantCapacity capacity = plantService.getPlantCapacity(plantId, date);

		return capacity == null ? 0 : Math.max(0, capacity.getTons() - getAssignedTons(plantId, date));
	}

	// True if the container is already in an assignment of that day (any plant)
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

	// Tons already assigned to a plant for a date
	private double getAssignedTons(String plantId, LocalDate date) {
		double total = 0;

		for (Assignment assignment : assignmentStore.values()) {
			if (assignment.getDate().equals(date) && assignment.getPlant().getId().equals(plantId)) {
				total += assignment.getTons();
			}
		}

		return total;
	}

	private double toTons(int packages) {
		return packages * KG_PER_PACKAGE / 1000;
	}
}
