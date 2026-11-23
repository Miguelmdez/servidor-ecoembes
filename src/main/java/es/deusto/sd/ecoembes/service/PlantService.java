package es.deusto.sd.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.deusto.sd.ecoembes.dao.RecyclingPlantRepository;
import es.deusto.sd.ecoembes.entity.RecyclingPlant;
import es.deusto.sd.ecoembes.external.PlantUnavailableException;
import es.deusto.sd.ecoembes.external.RecyclingPlantGatewayFactory;

// Patrón AppService: lógica de las plantas de reciclaje. Las plantas (con su protocolo, host y puerto) están en la
// base de datos; la capacidad NO se guarda aquí: se pregunta a cada planta a través de su gateway (patrón Gateway,
// elegido por RecyclingPlantGatewayFactory). Añadir una planta nueva solo requiere añadir sus datos.
@Service
public class PlantService {

	// Mensajes de las excepciones; el controller los traduce a códigos HTTP en un switch
	public static final String INVALID_DATE = "Fecha no válida o fuera de rango";
	public static final String NO_CAPACITY_FOUND = "No hay información de capacidad";
	public static final String PLANT_NOT_FOUND = "Planta no encontrada";
	public static final String PLANT_UNAVAILABLE = "La planta no responde";

	// Las plantas comunican su capacidad con una antelación máxima de este número de días
	public static final int MAX_DAYS_AHEAD = 10;

	// Capacidad disponible (en toneladas) que ha comunicado una planta para una fecha
	public record PlantAvailability(RecyclingPlant plant, LocalDate date, double availableTons) { }

	private final RecyclingPlantRepository plantRepository;
	private final RecyclingPlantGatewayFactory gatewayFactory;

	public PlantService(RecyclingPlantRepository plantRepository, RecyclingPlantGatewayFactory gatewayFactory) {
		this.plantRepository = plantRepository;
		this.gatewayFactory = gatewayFactory;
	}

	// Añadir una planta nueva solo requiere datos: no hay que cambiar código. Si ya existe, no hace nada.
	@Transactional
	public void addPlant(RecyclingPlant plant) {
		if (plant != null && !plantRepository.existsById(plant.getId())) {
			plantRepository.save(plant);
		}
	}

	// Capacidad disponible de todas las plantas en una fecha, ordenada por identificador de planta, preguntándosela a
	// cada planta. Fecha fuera de la ventana (hoy .. hoy+10) -> INVALID_DATE; las plantas sin datos para esa fecha se
	// omiten; ninguna planta con datos -> NO_CAPACITY_FOUND; si alguna planta no responde -> PLANT_UNAVAILABLE.
	public List<PlantAvailability> getPlantsCapacity(LocalDate date) {
		if (!isInWindow(date)) {
			throw new RuntimeException(INVALID_DATE);
		}

		List<PlantAvailability> result = new ArrayList<>();

		for (RecyclingPlant plant : plantRepository.findAllByOrderById()) {
			Optional<Double> available;

			try {
				available = gatewayFactory.getGateway(plant).getCapacity(date);
			} catch (PlantUnavailableException e) {
				throw new RuntimeException(PLANT_UNAVAILABLE, e);
			}

			available.ifPresent(tons -> result.add(new PlantAvailability(plant, date, tons)));
		}

		if (result.isEmpty()) {
			throw new RuntimeException(NO_CAPACITY_FOUND);
		}

		return result;
	}

	// Planta por identificador, o null si no existe (lo usan otros servicios)
	public RecyclingPlant getPlant(String plantId) {
		return plantId == null ? null : plantRepository.findById(plantId).orElse(null);
	}

	// Planta por identificador bloqueando su fila hasta el final de la transacción en curso, o null si no
	// existe. Lo usa AssignmentService para serializar las asignaciones simultáneas a una misma planta.
	// Hay que llamarlo dentro de una transacción (la del método que crea la asignación).
	public RecyclingPlant getPlantForUpdate(String plantId) {
		return plantId == null ? null : plantRepository.findByIdForUpdate(plantId).orElse(null);
	}

	// Verdadero si la fecha está entre hoy y hoy+10 días (ambos incluidos)
	public boolean isInWindow(LocalDate date) {
		LocalDate today = LocalDate.now();

		return date != null && !date.isBefore(today) && !date.isAfter(today.plusDays(MAX_DAYS_AHEAD));
	}
}
