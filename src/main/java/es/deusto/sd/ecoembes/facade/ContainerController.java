package es.deusto.sd.ecoembes.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.deusto.sd.ecoembes.dto.ContainerDTO;
import es.deusto.sd.ecoembes.dto.ContainerReadingDTO;
import es.deusto.sd.ecoembes.entity.Container;
import es.deusto.sd.ecoembes.entity.ContainerReading;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.ContainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

// Patrón Façade: este @RestController es la única puerta de entrada HTTP de los contenedores y sus lecturas. Solo se ocupa de la capa
// web (rutas, códigos HTTP y conversión entidad <-> DTO); la lógica de negocio está en ContainerService.
// El service lanza RuntimeException con un mensaje constante y el controller lo traduce a un código HTTP.
// Los endpoints del personal reciben el token como parámetro "token" de la propia petición y lo validan con
// AuthService.getEmployeeByToken (ausente o inválido -> 401). El del sensor no lleva token: lo envía el propio sensor.
@RestController
@RequestMapping("/containers")
@Tag(name = "Controlador de contenedores", description = "Operaciones para gestionar los contenedores de reciclaje y sus lecturas de sensor")
public class ContainerController {

	private static final String TOKEN_DESCRIPTION = "Token de sesión devuelto por /auth/login";
	private static final String TOKEN_EXAMPLE = "pega-aqui-el-token-del-login";

	private final ContainerService containerService;
	private final AuthService authService;

	public ContainerController(ContainerService containerService, AuthService authService) {
		this.containerService = containerService;
		this.authService = authService;
	}

	// Crear un contenedor nuevo
	@Operation(
		summary = "Crear un contenedor",
		description = "Registra un nuevo contenedor de reciclaje (identificador, ubicación, código postal y capacidad inicial en envases). Requiere el token del login en el parámetro token.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Creado: contenedor creado correctamente"),
			@ApiResponse(responseCode = "400", description = "Solicitud incorrecta: datos de entrada no válidos"),
			@ApiResponse(responseCode = "401", description = "No autorizado: token ausente o no válido"),
			@ApiResponse(responseCode = "409", description = "Conflicto: ya existe un contenedor con ese identificador"),
		}
	)
	@PostMapping
	public ResponseEntity<Void> createContainer(
			@Parameter(name = "token", description = TOKEN_DESCRIPTION, required = true, example = TOKEN_EXAMPLE)
			@RequestParam(value = "token", required = false) String token,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del contenedor", required = true)
			@RequestBody(required = false) ContainerDTO containerDTO) {
		if (authService.getEmployeeByToken(token) == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		if (containerDTO == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			containerService.createContainer(containerDTO.getContainerId(), containerDTO.getLocation(),
					containerDTO.getPostalCode(), containerDTO.getInitialCapacity());

			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return errorResponse(e);
		}
	}

	// Actualizar un contenedor existente
	@Operation(
		summary = "Actualizar un contenedor",
		description = "Actualiza la ubicación, el código postal y la capacidad inicial de un contenedor existente. Si el containerId del cuerpo no coincide con el de la ruta, devuelve 400. Requiere el token del login en el parámetro token.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: contenedor actualizado correctamente"),
			@ApiResponse(responseCode = "400", description = "Solicitud incorrecta: datos no válidos o identificador del cuerpo distinto al de la ruta"),
			@ApiResponse(responseCode = "401", description = "No autorizado: token ausente o no válido"),
			@ApiResponse(responseCode = "404", description = "No encontrado: el contenedor no existe"),
		}
	)
	@PutMapping("/{containerId}")
	public ResponseEntity<ContainerDTO> updateContainer(
			@Parameter(name = "token", description = TOKEN_DESCRIPTION, required = true, example = TOKEN_EXAMPLE)
			@RequestParam(value = "token", required = false) String token,
			@Parameter(name = "containerId", description = "Identificador del contenedor", required = true, example = "CONT-001")
			@PathVariable("containerId") String containerId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Nuevos datos del contenedor", required = true)
			@RequestBody(required = false) ContainerDTO containerDTO) {
		if (authService.getEmployeeByToken(token) == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		if (containerDTO == null
				|| (containerDTO.getContainerId() != null && !containerDTO.getContainerId().equals(containerId))) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			Container container = containerService.updateContainer(containerId, containerDTO.getLocation(),
					containerDTO.getPostalCode(), containerDTO.getInitialCapacity());

			return new ResponseEntity<>(containerToDTO(container), HttpStatus.OK);
		} catch (RuntimeException e) {
			return errorResponse(e);
		}
	}

	// Recibir una lectura del sensor (sin token: la envía el propio sensor, no una persona)
	@Operation(
		summary = "Recibir la lectura diaria del sensor",
		description = "Guarda la lectura enviada por el sensor de un contenedor. No lleva token: la envía el sensor, no una persona. Una lectura nueva del mismo día reemplaza a la anterior. Si el containerId del cuerpo no coincide con el de la ruta, devuelve 400.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Creado: lectura almacenada correctamente"),
			@ApiResponse(responseCode = "400", description = "Solicitud incorrecta: datos de la lectura no válidos"),
			@ApiResponse(responseCode = "404", description = "No encontrado: el contenedor no existe"),
		}
	)
	@PostMapping("/{containerId}/readings")
	public ResponseEntity<Void> addReading(
			@Parameter(name = "containerId", description = "Identificador del contenedor", required = true, example = "CONT-001")
			@PathVariable("containerId") String containerId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Lectura del sensor", required = true)
			@RequestBody(required = false) ContainerReadingDTO readingDTO) {
		if (readingDTO == null
				|| (readingDTO.getContainerId() != null && !readingDTO.getContainerId().equals(containerId))) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		try {
			containerService.addReading(containerId, readingDTO.getDate(), readingDTO.getEstimatedPackages(),
					readingDTO.getFillLevel());

			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return errorResponse(e);
		}
	}

	// Lecturas de un contenedor en un periodo
	@Operation(
		summary = "Consultar las lecturas de un contenedor",
		description = "Devuelve las lecturas de un contenedor entre dos fechas (ambas incluidas). Requiere el token del login en el parámetro token.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: lecturas devueltas correctamente"),
			@ApiResponse(responseCode = "400", description = "Solicitud incorrecta: fechas o periodo no válidos"),
			@ApiResponse(responseCode = "401", description = "No autorizado: token ausente o no válido"),
			@ApiResponse(responseCode = "404", description = "No encontrado: el contenedor no existe"),
		}
	)
	@GetMapping("/{containerId}/readings")
	public ResponseEntity<List<ContainerReadingDTO>> getContainerReadings(
			@Parameter(name = "token", description = TOKEN_DESCRIPTION, required = true, example = TOKEN_EXAMPLE)
			@RequestParam(value = "token", required = false) String token,
			@Parameter(name = "containerId", description = "Identificador del contenedor", required = true, example = "CONT-001")
			@PathVariable("containerId") String containerId,
			@Parameter(name = "startDate", description = "Inicio del periodo (yyyy-MM-dd)", required = true, example = "2026-11-01")
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@Parameter(name = "endDate", description = "Fin del periodo (yyyy-MM-dd)", required = true, example = "2026-11-07")
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		if (authService.getEmployeeByToken(token) == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<ContainerReading> readings = containerService.getContainerReadings(containerId, startDate, endDate);

			return new ResponseEntity<>(readingsToDTO(readings), HttpStatus.OK);
		} catch (RuntimeException e) {
			return errorResponse(e);
		}
	}

	// Lecturas de los contenedores de una zona
	@Operation(
		summary = "Consultar el estado de los contenedores de una zona",
		description = "Devuelve las lecturas de todos los contenedores de un código postal en una fecha. Requiere el token del login en el parámetro token.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: contenedores y su estado devueltos correctamente"),
			@ApiResponse(responseCode = "400", description = "Solicitud incorrecta: parámetros no válidos"),
			@ApiResponse(responseCode = "401", description = "No autorizado: token ausente o no válido"),
			@ApiResponse(responseCode = "404", description = "No encontrado: no hay contenedores con lecturas para esa zona y fecha"),
		}
	)
	@GetMapping("/readings")
	public ResponseEntity<List<ContainerReadingDTO>> getZoneReadings(
			@Parameter(name = "token", description = TOKEN_DESCRIPTION, required = true, example = TOKEN_EXAMPLE)
			@RequestParam(value = "token", required = false) String token,
			@Parameter(name = "postalCode", description = "Código postal de la zona", required = true, example = "48001")
			@RequestParam("postalCode") String postalCode,
			@Parameter(name = "date", description = "Fecha de la consulta (yyyy-MM-dd)", required = true, example = "2026-11-07")
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		if (authService.getEmployeeByToken(token) == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		try {
			List<ContainerReading> readings = containerService.getZoneReadings(postalCode, date);

			return new ResponseEntity<>(readingsToDTO(readings), HttpStatus.OK);
		} catch (RuntimeException e) {
			return errorResponse(e);
		}
	}

	// Traduce el mensaje de la excepción del service a un código HTTP (mismas constantes en ambos lados)
	private <T> ResponseEntity<T> errorResponse(RuntimeException e) {
		return switch (String.valueOf(e.getMessage())) {
			case ContainerService.INVALID_DATA, ContainerService.INVALID_DATES ->
				new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			case ContainerService.CONTAINER_NOT_FOUND, ContainerService.NO_READINGS_FOUND ->
				new ResponseEntity<>(HttpStatus.NOT_FOUND);
			case ContainerService.CONTAINER_ALREADY_EXISTS ->
				new ResponseEntity<>(HttpStatus.CONFLICT);
			default -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		};
	}

	private ContainerDTO containerToDTO(Container container) {
		return new ContainerDTO(container.getId(), container.getLocation(), container.getPostalCode(),
				container.getInitialCapacity());
	}

	private List<ContainerReadingDTO> readingsToDTO(List<ContainerReading> readings) {
		return readings.stream().map(this::readingToDTO).toList();
	}

	private ContainerReadingDTO readingToDTO(ContainerReading reading) {
		return new ContainerReadingDTO(reading.getContainer().getId(), reading.getDate(), reading.getPackages(),
				reading.getFillLevel());
	}
}
