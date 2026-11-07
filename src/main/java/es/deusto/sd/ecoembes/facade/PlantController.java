package es.deusto.sd.ecoembes.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.deusto.sd.ecoembes.dto.PlantCapacityDTO;
import es.deusto.sd.ecoembes.entity.PlantCapacity;
import es.deusto.sd.ecoembes.service.AssignmentService;
import es.deusto.sd.ecoembes.service.PlantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/plants")
@Tag(name = "Plant Controller", description = "Operations to query the capacity of the recycling plants")
public class PlantController {

	private final PlantService plantService;
	private final AssignmentService assignmentService;

	public PlantController(PlantService plantService, AssignmentService assignmentService) {
		this.plantService = plantService;
		this.assignmentService = assignmentService;
	}

	// Capacity of all the plants for a date
	@Operation(
		summary = "Get the capacity of the plants for a date",
		description = "Returns the available capacity (in tons, daily capacity minus the tons already assigned) of all the recycling plants for a date between today and today + 10 days.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: Capacities returned successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Invalid date or out of range"),
			@ApiResponse(responseCode = "404", description = "Not Found: No capacity information for that date"),
		}
	)
	@GetMapping("/capacity/date/{date}")
	public ResponseEntity<List<PlantCapacityDTO>> getPlantsCapacity(
			@Parameter(name = "date", description = "Date of the query (yyyy-MM-dd)", required = true, example = "2026-11-08")
			@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		try {
			List<PlantCapacity> capacities = plantService.getPlantsCapacity(date);

			return new ResponseEntity<>(capacities.stream().map(this::capacityToDTO).toList(), HttpStatus.OK);
		} catch (RuntimeException e) {
			return switch (String.valueOf(e.getMessage())) {
				case PlantService.INVALID_DATE -> new ResponseEntity<>(HttpStatus.BAD_REQUEST);
				case PlantService.NO_CAPACITY_FOUND -> new ResponseEntity<>(HttpStatus.NOT_FOUND);
				default -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			};
		}
	}

	private PlantCapacityDTO capacityToDTO(PlantCapacity capacity) {
		return new PlantCapacityDTO(capacity.getPlant().getId(), capacity.getPlant().getName(), capacity.getDate(),
				(float) assignmentService.getAvailableTons(capacity.getPlant().getId(), capacity.getDate()));
	}
}
