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

// Patrón Façade: este @RestController es la única puerta de entrada HTTP de las asignaciones de contenedores a plantas. Solo se ocupa de la capa
// web (rutas, códigos HTTP y conversión entidad <-> DTO); la lógica de negocio está en AssignmentService.
// El service lanza RuntimeException con un mensaje constante y el controller lo traduce a un código HTTP.
@RestController
@RequestMapping("/assignments")
@Tag(name = "Controlador de asignaciones", description = "Operaciones para asignar contenedores a plantas de reciclaje")
public class AssignmentController {

	private final AssignmentService assignmentService;

	public AssignmentController(AssignmentService assignmentService) {
		this.assignmentService = assignmentService;
	}

	// Asignar contenedores a una planta
	@Operation(
		summary = "Asignar contenedores a una planta",
		description = "Asigna uno o varios contenedores a una planta de reciclaje para una fecha, si la planta tiene capacidad disponible. El token del empleado va en el campo userToken del cuerpo.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Creado: asignación realizada correctamente"),
			@ApiResponse(responseCode = "400", description = "Solicitud incorrecta: datos de entrada no válidos"),
			@ApiResponse(responseCode = "401", description = "No autorizado: usuario no autenticado"),
			@ApiResponse(responseCode = "404", description = "No encontrado: no existe la planta o alguno de los contenedores"),
			@ApiResponse(responseCode = "409", description = "Conflicto: la capacidad disponible de la planta no basta, o un contenedor ya está asignado ese día"),
		}
	)
	@PostMapping
	public ResponseEntity<AssignmentDTO> createAssignment(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la asignación", required = true)
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

	// Asignaciones de una planta en una fecha
	@Operation(
		summary = "Consultar las asignaciones de una planta",
		description = "Devuelve las asignaciones realizadas a una planta en una fecha.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: asignaciones devueltas correctamente"),
			@ApiResponse(responseCode = "400", description = "Solicitud incorrecta: parámetros no válidos"),
			@ApiResponse(responseCode = "404", description = "No encontrado: no hay asignaciones con esos criterios"),
		}
	)
	@GetMapping
	public ResponseEntity<List<AssignmentDTO>> getAssignments(
			@Parameter(name = "date", description = "Fecha de las asignaciones (yyyy-MM-dd)", required = true, example = "2026-11-08")
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@Parameter(name = "plantId", description = "Identificador de la planta", required = true, example = "PLANT-1")
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
