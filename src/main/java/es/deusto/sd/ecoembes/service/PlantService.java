package es.deusto.sd.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.entity.PlantCapacity;
import es.deusto.sd.ecoembes.entity.RecyclingPlant;

// Patrón AppService: lógica de las plantas de reciclaje y de la capacidad diaria que comunican, con su
// ESTADO DEL SERVIDOR en memoria (Prototipo 1). Las plantas y sus capacidades son datos: añadir una
// planta nueva solo requiere llamar a addPlant y setCapacity, sin modificar la API ni el código.
@Service
public class PlantService {

	// Mensajes de las excepciones; el controller los traduce a códigos HTTP en un switch
	public static final String INVALID_DATE = "Fecha no válida o fuera de rango";
	public static final String NO_CAPACITY_FOUND = "No hay información de capacidad";
	public static final String PLANT_NOT_FOUND = "Planta no encontrada";

	// Las plantas comunican su capacidad con una antelación máxima de este número de días
	public static final int MAX_DAYS_AHEAD = 10;

	// Plantas indexadas por su identificador
	private final Map<String, RecyclingPlant> plantRepository = new ConcurrentHashMap<>();

	// Capacidad diaria (en toneladas) comunicada por cada planta: id de planta -> fecha -> capacidad
	private final Map<String, Map<LocalDate, PlantCapacity>> capacityStore = new ConcurrentHashMap<>();

	// Generador de identificadores internos de las capacidades
	private final AtomicLong capacitySequence = new AtomicLong(1);

	// Añadir una planta nueva solo requiere datos: no hay que cambiar código. Si ya existe, no hace nada.
	public void addPlant(RecyclingPlant plant) {
		if (plant != null && plantRepository.putIfAbsent(plant.getId(), plant) == null) {
			capacityStore.put(plant.getId(), new ConcurrentHashMap<>());
		}
	}

	// Una planta comunica su capacidad (en toneladas) para un día; reemplaza la anterior de ese día
	public void setCapacity(String plantId, LocalDate date, double tons) {
		RecyclingPlant plant = plantRepository.get(plantId);

		if (plant == null) {
			throw new RuntimeException(PLANT_NOT_FOUND);
		}

		if (date == null || tons < 0) {
			throw new RuntimeException(INVALID_DATE);
		}

		capacityStore.get(plantId).put(date, new PlantCapacity(capacitySequence.getAndIncrement(), plant, date, tons));
	}

	// Capacidad comunicada por todas las plantas para una fecha, ordenada por identificador de planta.
	// Fecha fuera de la ventana (hoy .. hoy+10) -> INVALID_DATE; ninguna planta con datos -> NO_CAPACITY_FOUND.
	// Es la capacidad diaria comunicada; el controller la convierte en capacidad DISPONIBLE descontando
	// las asignaciones (ese cálculo vive en AssignmentService para no duplicarlo).
	public List<PlantCapacity> getPlantsCapacity(LocalDate date) {
		if (!isInWindow(date)) {
			throw new RuntimeException(INVALID_DATE);
		}

		List<PlantCapacity> result = new ArrayList<>();

		for (Map<LocalDate, PlantCapacity> capacities : capacityStore.values()) {
			PlantCapacity capacity = capacities.get(date);

			if (capacity != null) {
				result.add(capacity);
			}
		}

		if (result.isEmpty()) {
			throw new RuntimeException(NO_CAPACITY_FOUND);
		}

		result.sort((a, b) -> a.getPlant().getId().compareTo(b.getPlant().getId()));

		return result;
	}

	// Planta por identificador, o null si no existe (lo usan otros servicios)
	public RecyclingPlant getPlant(String plantId) {
		return plantId == null ? null : plantRepository.get(plantId);
	}

	// Capacidad (en toneladas) de una planta para una fecha, o null si no la ha comunicado
	public PlantCapacity getPlantCapacity(String plantId, LocalDate date) {
		Map<LocalDate, PlantCapacity> capacities = plantId == null ? null : capacityStore.get(plantId);

		return capacities == null || date == null ? null : capacities.get(date);
	}

	// Verdadero si la fecha está entre hoy y hoy+10 días (ambos incluidos)
	public boolean isInWindow(LocalDate date) {
		LocalDate today = LocalDate.now();

		return date != null && !date.isBefore(today) && !date.isAfter(today.plusDays(MAX_DAYS_AHEAD));
	}
}
