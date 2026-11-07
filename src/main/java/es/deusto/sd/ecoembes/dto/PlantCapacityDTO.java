package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;

public class PlantCapacityDTO {
	private String plantId;
	private String plantName;
	private LocalDate date;
	private Float capacity;

	// Constructor without parameters
	public PlantCapacityDTO() { }

	// Constructor with parameters
	public PlantCapacityDTO(String plantId, String plantName, LocalDate date, Float capacity) {
		this.plantId = plantId;
		this.plantName = plantName;
		this.date = date;
		this.capacity = capacity;
	}

	// Getters and setters
	public String getPlantId() {
		return plantId;
	}

	public void setPlantId(String plantId) {
		this.plantId = plantId;
	}

	public String getPlantName() {
		return plantName;
	}

	public void setPlantName(String plantName) {
		this.plantName = plantName;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Float getCapacity() {
		return capacity;
	}

	public void setCapacity(Float capacity) {
		this.capacity = capacity;
	}
}
