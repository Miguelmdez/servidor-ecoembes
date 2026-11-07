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

// Patrón Façade: este @RestController es la única puerta de entrada HTTP de las plantas de reciclaje. Solo se ocupa de la capa
// web (rutas, códigos HTTP y conversión entidad <-> DTO); la lógica de negocio está en PlantService (y AssignmentService para la capacidad disponible).
// El service lanza RuntimeException con un mensaje constante y el controller lo traduce a un código HTTP.
@RestController
@RequestMapping("/plants")
@Tag(name = "Controlador de plantas", description = "Operaciones para consultar la capacidad de las plantas de reciclaje")
public class PlantController {

	private final PlantService plantService;
	private final AssignmentService assignmentService;

	public PlantController(PlantService plantService, AssignmentService assignmentService) {
		this.plantService = plantService;
		this.assignmentService = assignmentService;
	}

	// Capacidad disponible de todas las plantas en una fecha
	@Operation(
		summary = "Consultar la capacidad disponible de las plantas en una fecha",
		description = "Devuelve la capacidad disponible (en toneladas: capacidad diaria comunicada menos lo ya asignado) de todas las plantas de reciclaje para una fecha entre hoy y hoy+10 días.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: capacidades devueltas correctamente"),
			@ApiResponse(responseCode = "400", description = "Solicitud incorrecta: fecha no válida o fuera de rango"),
			@ApiResponse(responseCode = "404", description = "No encontrado: no hay información de capacidad para esa fecha"),
		}
	)
	@GetMapping("/capacity/date/{date}")
	public ResponseEntity<List<PlantCapacityDTO>> getPlantsCapacity(
			@Parameter(name = "date", description = "Fecha de la consulta (yyyy-MM-dd)", required = true, example = "2026-11-08")
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
