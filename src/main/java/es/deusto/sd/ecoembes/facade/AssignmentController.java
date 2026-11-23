package es.deusto.sd.ecoembes.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.deusto.sd.ecoembes.dto.AssignmentDTO;
import es.deusto.sd.ecoembes.dto.AssignmentRequestDTO;
import es.deusto.sd.ecoembes.dto.PlantCapacityDTO;
import es.deusto.sd.ecoembes.entity.Assignment;
import es.deusto.sd.ecoembes.entity.Container;
import es.deusto.sd.ecoembes.service.AssignmentService;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.ContainerService;
import es.deusto.sd.ecoembes.service.PlantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

// Patrón Façade: este @RestController es la única puerta de entrada HTTP de la consulta de capacidad de las plantas y de las
// asignaciones de contenedores a plantas. Se fusionan en un solo controller porque ambas operaciones forman un mismo flujo de
// trabajo del personal (consultar la capacidad y asignar contenedores). Solo se ocupa de la capa web (rutas, códigos HTTP y
// conversión entidad <-> DTO); la lógica de negocio está en PlantService y AssignmentService.
// Como las rutas cuelgan de dos prefijos (/plants y /assignments), no hay @RequestMapping de clase: cada método declara su ruta completa.
// El service lanza RuntimeException con un mensaje constante y el controller lo traduce a un código HTTP.
// Los endpoints del personal reciben el token como parámetro "userToken" y lo validan con AuthService.getEmployeeByToken
// (ausente o inválido -> 401). POST /assignments sigue llevándolo en el campo userToken del cuerpo, como indica la especificación.
@RestController
@Tag(name = "Controlador de asignaciones", description = "Operaciones para consultar la capacidad de las plantas de reciclaje y asignarles contenedores")
public class AssignmentController {

	private static final String TOKEN_DESCRIPTION = "Token de sesión devuelto por /auth/login";
	private static final String TOKEN_EXAMPLE = "pega-aqui-el-token-del-login";

	private final AssignmentService assignmentService;
	private final PlantService plantService;
	private final AuthService authService;

	public AssignmentController(AssignmentService assignmentService, PlantService plantService, AuthService authService) {
		this.assignmentService = assignmentService;
		this.plantService = plantService;
		this.authService = authService;
	}

	// Capacidad disponible de todas las plantas en una fecha
	@Operation(
		summary = "Consultar la capacidad disponible de las plantas en una fecha",
		description = "Pregunta a cada planta de reciclaje (por su gateway REST o de sockets) su capacidad disponible en toneladas para una fecha entre hoy y hoy+10 días. Las plantas sin datos para esa fecha se omiten. Requiere el token del login en el parámetro userToken.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: capacidades devueltas correctamente"),
			@ApiResponse(responseCode = "400", description = "Solicitud incorrecta: fecha no válida o fuera de rango"),
			@ApiResponse(responseCode = "401", description = "No autorizado: token ausente o no válido"),
			@ApiResponse(responseCode = "404", description = "No encontrado: ninguna planta tiene información de capacidad para esa fecha"),
			@ApiResponse(responseCode = "503", description = "Servicio no disponible: alguna planta no responde"),
		}
	)
	@GetMapping("/plants/capacity/date/{date}")
	public ResponseEntity<List<PlantCapacityDTO>> getPlantsCapacity(
			@Parameter(name = "userToken", description = TOKEN_DESCRIPTION, required = true, example = TOKEN_EXAMPLE)
			@RequestParam(value = "userToken", required = false) String userToken,
			@Parameter(name = "date", description = "Fecha de la consulta (yyyy-MM-dd)", required = true, example = "2026-11-08")
			@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		if (authService.getEmployeeByToken(userToken) == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<PlantService.PlantAvailability> capacities = plantService.getPlantsCapacity(date);

			return new ResponseEntity<>(capacities.stream().map(this::capacityToDTO).toList(), HttpStatus.OK);
		} catch (RuntimeException e) {
			return switch (String.valueOf(e.getMessage())) {
				case PlantService.INVALID_DATE -> new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				case PlantService.NO_CAPACITY_FOUND -> new ResponseEntity<>(HttpStatus.NOT_FOUND);
				case PlantService.PLANT_UNAVAILABLE -> new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
				default -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			};
		}
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
			@ApiResponse(responseCode = "409", description = "Conflicto: la capacidad disponible de la planta no basta, la planta rechaza la asignación o un contenedor ya está asignado ese día"),
			@ApiResponse(responseCode = "503", description = "Servicio no disponible: la planta no responde"),
		}
	)
	@PostMapping("/assignments")
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
				case AssignmentService.INSUFFICIENT_CAPACITY, AssignmentService.CONTAINER_ALREADY_ASSIGNED,
						AssignmentService.PLANT_REJECTED -> new ResponseEntity<>(HttpStatus.CONFLICT);
				case PlantService.PLANT_UNAVAILABLE -> new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
				default -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			};
		}
	}

	// Asignaciones de una planta en una fecha
	@Operation(
		summary = "Consultar las asignaciones de una planta",
		description = "Devuelve las asignaciones realizadas a una planta en una fecha. Requiere el token del login en el parámetro userToken.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: asignaciones devueltas correctamente"),
			@ApiResponse(responseCode = "400", description = "Solicitud incorrecta: parámetros no válidos"),
			@ApiResponse(responseCode = "401", description = "No autorizado: token ausente o no válido"),
			@ApiResponse(responseCode = "404", description = "No encontrado: no hay asignaciones con esos criterios"),
		}
	)
	@GetMapping("/assignments")
	public ResponseEntity<List<AssignmentDTO>> getAssignments(
			@Parameter(name = "userToken", description = TOKEN_DESCRIPTION, required = true, example = TOKEN_EXAMPLE)
			@RequestParam(value = "userToken", required = false) String userToken,
			@Parameter(name = "date", description = "Fecha de las asignaciones (yyyy-MM-dd)", required = true, example = "2026-11-08")
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@Parameter(name = "plantId", description = "Identificador de la planta", required = true, example = "PLANT-1")
			@RequestParam("plantId") String plantId) {
		if (authService.getEmployeeByToken(userToken) == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

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

	// capacity = capacidad disponible que informa la propia planta
	private PlantCapacityDTO capacityToDTO(PlantService.PlantAvailability availability) {
		return new PlantCapacityDTO(availability.plant().getId(), availability.plant().getName(), availability.date(),
				(float) availability.availableTons());
	}

	private AssignmentDTO assignmentToDTO(Assignment assignment) {
		List<String> containerIds = assignment.getContainers().stream().map(Container::getId).toList();

		return new AssignmentDTO("ASG-" + assignment.getId(), assignment.getPlant().getId(), containerIds,
				assignment.getDate(), containerIds.size(), assignment.getTotalPackages(), assignment.getAssignedBy());
	}
}
