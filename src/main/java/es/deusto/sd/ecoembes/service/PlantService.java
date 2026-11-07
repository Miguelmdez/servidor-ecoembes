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

@Service
public class PlantService {

	// Messages used as exception text and mapped to HTTP codes by the controller
	public static final String INVALID_DATE = "Invalid date or out of range";
	public static final String NO_CAPACITY_FOUND = "No capacity information found";
	public static final String PLANT_NOT_FOUND = "Plant not found";

	// Plants communicate their capacity up to this number of days in advance
	public static final int MAX_DAYS_AHEAD = 10;

	// Plants by id (thread-safe: shared across request threads)
	private final Map<String, RecyclingPlant> plantRepository = new ConcurrentHashMap<>();

	// Daily capacity (tons) by plant id and date
	private final Map<String, Map<LocalDate, PlantCapacity>> capacityStore = new ConcurrentHashMap<>();

	private final AtomicLong capacitySequence = new AtomicLong(1);

	// Adding a new plant only requires data: no code changes
	public void addPlant(RecyclingPlant plant) {
		if (plant != null && plantRepository.putIfAbsent(plant.getId(), plant) == null) {
			capacityStore.put(plant.getId(), new ConcurrentHashMap<>());
		}
	}

	// A plant communicates its capacity (tons) for a day
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

	// Capacity of all the plants for a date (from today up to today + 10 days)
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

	// Plant by id, or null if it does not exist
	public RecyclingPlant getPlant(String plantId) {
		return plantId == null ? null : plantRepository.get(plantId);
	}

	// Capacity (tons) of a plant for a date, or null if the plant has not communicated it
	public PlantCapacity getPlantCapacity(String plantId, LocalDate date) {
		Map<LocalDate, PlantCapacity> capacities = plantId == null ? null : capacityStore.get(plantId);

		return capacities == null || date == null ? null : capacities.get(date);
	}

	// True if the date is between today and today + 10 days (both included)
	public boolean isInWindow(LocalDate date) {
		LocalDate today = LocalDate.now();

		return date != null && !date.isBefore(today) && !date.isAfter(today.plusDays(MAX_DAYS_AHEAD));
	}
}
