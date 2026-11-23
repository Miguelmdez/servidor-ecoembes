package es.deusto.sd.ecoembes.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.deusto.sd.ecoembes.dao.PlantCapacityRepository;
import es.deusto.sd.ecoembes.dao.RecyclingPlantRepository;
import es.deusto.sd.ecoembes.entity.PlantCapacity;
import es.deusto.sd.ecoembes.entity.RecyclingPlant;

// Patrón AppService: lógica de las plantas de reciclaje y de la capacidad diaria que comunican, guardadas en
// la base de datos. Las plantas y sus capacidades son datos: añadir una planta nueva solo requiere llamar a
// addPlant y setCapacity, sin modificar la API ni el código.
@Service
public class PlantService {

	// Mensajes de las excepciones; el controller los traduce a códigos HTTP en un switch
	public static final String INVALID_DATE = "Fecha no válida o fuera de rango";
	public static final String NO_CAPACITY_FOUND = "No hay información de capacidad";
	public static final String PLANT_NOT_FOUND = "Planta no encontrada";

	// Las plantas comunican su capacidad con una antelación máxima de este número de días
	public static final int MAX_DAYS_AHEAD = 10;

	private final RecyclingPlantRepository plantRepository;
	private final PlantCapacityRepository capacityRepository;

	public PlantService(RecyclingPlantRepository plantRepository, PlantCapacityRepository capacityRepository) {
		this.plantRepository = plantRepository;
		this.capacityRepository = capacityRepository;
	}

	// Añadir una planta nueva solo requiere datos: no hay que cambiar código. Si ya existe, no hace nada.
	@Transactional
	public void addPlant(RecyclingPlant plant) {
		if (plant != null && !plantRepository.existsById(plant.getId())) {
			plantRepository.save(plant);
		}
	}

	// Una planta comunica su capacidad (en toneladas) para un día; reemplaza la anterior de ese día
	@Transactional
	public void setCapacity(String plantId, LocalDate date, double tons) {
		RecyclingPlant plant = getPlant(plantId);

		if (plant == null) {
			throw new RuntimeException(PLANT_NOT_FOUND);
		}

		if (date == null || tons < 0) {
			throw new RuntimeException(INVALID_DATE);
		}

		PlantCapacity capacity = capacityRepository.findByPlantIdAndDate(plantId, date)
				.orElseGet(() -> new PlantCapacity(plant, date, tons));
		capacity.setTons(tons);
		capacityRepository.save(capacity);
	}

	// Capacidad comunicada por todas las plantas para una fecha, ordenada por identificador de planta.
	// Fecha fuera de la ventana (hoy .. hoy+10) -> INVALID_DATE; ninguna planta con datos -> NO_CAPACITY_FOUND.
	// Es la capacidad diaria comunicada; el controller la convierte en capacidad DISPONIBLE descontando
	// las asignaciones (ese cálculo vive en AssignmentService para no duplicarlo).
	public List<PlantCapacity> getPlantsCapacity(LocalDate date) {
		if (!isInWindow(date)) {
			throw new RuntimeException(INVALID_DATE);
		}

		List<PlantCapacity> result = capacityRepository.findByDateOrderByPlantId(date);

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

	// Capacidad (en toneladas) de una planta para una fecha, o null si no la ha comunicado
	public PlantCapacity getPlantCapacity(String plantId, LocalDate date) {
		return plantId == null || date == null ? null : capacityRepository.findByPlantIdAndDate(plantId, date).orElse(null);
	}

	// Verdadero si la fecha está entre hoy y hoy+10 días (ambos incluidos)
	public boolean isInWindow(LocalDate date) {
		LocalDate today = LocalDate.now();

		return date != null && !date.isBefore(today) && !date.isAfter(today.plusDays(MAX_DAYS_AHEAD));
	}
}
