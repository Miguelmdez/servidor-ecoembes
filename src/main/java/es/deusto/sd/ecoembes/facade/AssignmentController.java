package es.deusto.sd.ecoembes.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.deusto.sd.ecoembes.dto.AssignmentDTO;
import es.deusto.sd.ecoembes.dto.AssignmentRequestDTO;
import es.deusto.sd.ecoembes.entity.Assignment;
import es.deusto.sd.ecoembes.entity.Container;
import es.deusto.sd.ecoembes.service.AssignmentService;
import es.deusto.sd.ecoembes.service.ContainerService;
import es.deusto.sd.ecoembes.service.PlantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/assignments")
@Tag(name = "Assignment Controller", description = "Operations to assign containers to recycling plants")
public class AssignmentController {

	private final AssignmentService assignmentService;

	public AssignmentController(AssignmentService assignmentService) {
		this.assignmentService = assignmentService;
	}

	// Assign containers to a plant
	@Operation(
		summary = "Assign containers to a plant",
		description = "Assigns one or more containers to a recycling plant for a date, if the plant has enough remaining capacity. The token of the employee goes in the userToken field of the body.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Created: Assignment created successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data"),
			@ApiResponse(responseCode = "401", description = "Unauthorized: User not authenticated"),
			@ApiResponse(responseCode = "404", description = "Not Found: Plant or any of the containers not found"),
			@ApiResponse(responseCode = "409", description = "Conflict: The remaining capacity of the plant is not enough, or a container is already assigned that day"),
		}
	)
	@PostMapping
	public ResponseEntity<AssignmentDTO> createAssignment(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Assignment data", required = true)
			@RequestBody(required = false) AssignmentRequestDTO request) {
		if (request == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			Assignment assignment = assignmentService.createAssignment(request.getUserToken(), request.getPlantId(),
					request.getContainerIds(), request.getDate());

			return new ResponseEntity<>(assignmentToDTO(assignment), HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return switch (String.valueOf(e.getMessage())) {
				case AssignmentService.INVALID_DATA -> new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				case AssignmentService.UNAUTHORIZED -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
				case PlantService.PLANT_NOT_FOUND, ContainerService.CONTAINER_NOT_FOUND ->
					new ResponseEntity<>(HttpStatus.NOT_FOUND);
				case AssignmentService.INSUFFICIENT_CAPACITY, AssignmentService.CONTAINER_ALREADY_ASSIGNED ->
					new ResponseEntity<>(HttpStatus.CONFLICT);
				default -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			};
		}
	}

	// Assignments of a plant for a date
	@Operation(
		summary = "Get the assignments of a plant",
		description = "Returns the assignments made to a plant for a date.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: Assignments returned successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid parameters"),
			@ApiResponse(responseCode = "404", description = "Not Found: No assignments for those criteria"),
		}
	)
	@GetMapping
	public ResponseEntity<List<AssignmentDTO>> getAssignments(
			@Parameter(name = "date", description = "Date of the assignments (yyyy-MM-dd)", required = true, example = "2026-11-08")
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@Parameter(name = "plantId", description = "Plant identifier", required = true, example = "PLANT-1")
			@RequestParam("plantId") String plantId) {
		try {
			List<Assignment> assignments = assignmentService.getAssignments(date, plantId);

			return new ResponseEntity<>(assignments.stream().map(this::assignmentToDTO).toList(), HttpStatus.OK);
		} catch (RuntimeException e) {
			return switch (String.valueOf(e.getMessage())) {
				case AssignmentService.INVALID_DATA -> new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				case AssignmentService.NO_ASSIGNMENTS_FOUND -> new ResponseEntity<>(HttpStatus.NOT_FOUND);
				default -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			};
		}
	}

	private AssignmentDTO assignmentToDTO(Assignment assignment) {
		List<String> containerIds = assignment.getContainers().stream().map(Container::getId).toList();

		return new AssignmentDTO("ASG-" + assignment.getId(), assignment.getPlant().getId(), containerIds,
				assignment.getDate(), containerIds.size(), assignment.getTotalPackages(), assignment.getAssignedBy());
	}
}
