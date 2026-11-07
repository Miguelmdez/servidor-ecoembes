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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.deusto.sd.ecoembes.dto.ContainerDTO;
import es.deusto.sd.ecoembes.dto.ContainerReadingDTO;
import es.deusto.sd.ecoembes.entity.ContainerReading;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.ContainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/containers")
@Tag(name = "Container Controller", description = "Operations to manage recycling containers and their sensor readings")
public class ContainerController {

	private final ContainerService containerService;
	private final AuthService authService;

	public ContainerController(ContainerService containerService, AuthService authService) {
		this.containerService = containerService;
		this.authService = authService;
	}

	// Create a new container
	@Operation(
		summary = "Create a new container",
		description = "Registers a new recycling container (identifier, location, postal code and initial capacity). Requires the login token in the Authorization header.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Created: Container created successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid input data"),
			@ApiResponse(responseCode = "401", description = "Unauthorized: User not authenticated"),
			@ApiResponse(responseCode = "409", description = "Conflict: A container with that identifier already exists"),
		}
	)
	@SecurityRequirement(name = "tokenAuth")
	@PostMapping
	public ResponseEntity<Void> createContainer(
			@Parameter(hidden = true)
			@RequestHeader(value = "Authorization", required = false) String token,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Container data", required = true)
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

	// Receive a sensor reading (no token: sent by the sensor)
	@Operation(
		summary = "Receive the daily sensor reading",
		description = "Stores the reading sent by the sensor of a container. A new reading for the same day replaces the previous one.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Created: Reading stored successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid reading data"),
			@ApiResponse(responseCode = "404", description = "Not Found: Container not found"),
		}
	)
	@PostMapping("/{containerId}/readings")
	public ResponseEntity<Void> addReading(
			@Parameter(name = "containerId", description = "Container identifier", required = true, example = "CONT-001")
			@PathVariable("containerId") String containerId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Sensor reading", required = true)
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

	// Readings of a container in a period
	@Operation(
		summary = "Get the readings of a container",
		description = "Returns the readings of a container between two dates (both included).",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: Readings returned successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid dates or period"),
			@ApiResponse(responseCode = "404", description = "Not Found: Container not found"),
		}
	)
	@GetMapping("/{containerId}/readings")
	public ResponseEntity<List<ContainerReadingDTO>> getContainerReadings(
			@Parameter(name = "containerId", description = "Container identifier", required = true, example = "CONT-001")
			@PathVariable("containerId") String containerId,
			@Parameter(name = "startDate", description = "Start of the period (yyyy-MM-dd)", required = true, example = "2026-11-01")
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@Parameter(name = "endDate", description = "End of the period (yyyy-MM-dd)", required = true, example = "2026-11-07")
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		try {
			List<ContainerReading> readings = containerService.getContainerReadings(containerId, startDate, endDate);

			return new ResponseEntity<>(readingsToDTO(readings), HttpStatus.OK);
		} catch (RuntimeException e) {
			return errorResponse(e);
		}
	}

	// Readings of the containers of a zone
	@Operation(
		summary = "Get the status of the containers of a zone",
		description = "Returns the readings of all the containers of a postal code for a given date.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: Containers and their status returned successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid parameters"),
			@ApiResponse(responseCode = "404", description = "Not Found: No containers found for that zone and date"),
		}
	)
	@GetMapping("/readings")
	public ResponseEntity<List<ContainerReadingDTO>> getZoneReadings(
			@Parameter(name = "postalCode", description = "Postal code of the zone", required = true, example = "48001")
			@RequestParam("postalCode") String postalCode,
			@Parameter(name = "date", description = "Date of the query (yyyy-MM-dd)", required = true, example = "2026-11-07")
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		try {
			List<ContainerReading> readings = containerService.getZoneReadings(postalCode, date);

			return new ResponseEntity<>(readingsToDTO(readings), HttpStatus.OK);
		} catch (RuntimeException e) {
			return errorResponse(e);
		}
	}

	// Maps the exception message of the service to an HTTP status
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

	private List<ContainerReadingDTO> readingsToDTO(List<ContainerReading> readings) {
		return readings.stream().map(this::readingToDTO).toList();
	}

	private ContainerReadingDTO readingToDTO(ContainerReading reading) {
		return new ContainerReadingDTO(reading.getContainer().getId(), reading.getDate(), reading.getPackages(),
				reading.getFillLevel());
	}
}
