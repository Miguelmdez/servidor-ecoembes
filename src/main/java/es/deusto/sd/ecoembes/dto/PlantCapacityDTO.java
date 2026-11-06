package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;

public class PlantCapacityDTO {
	private long plantId;
	private String plantName;
	private LocalDate date;
	private double tons;

	// Constructor without parameters
	public PlantCapacityDTO() { }

	// Constructor with parameters
	public PlantCapacityDTO(long plantId, String plantName, LocalDate date, double tons) {
		this.plantId = plantId;
		this.plantName = plantName;
		this.date = date;
		this.tons = tons;
	}

	// Getters and setters
	public long getPlantId() {
		return plantId;
	}

	public void setPlantId(long plantId) {
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

	public double getTons() {
		return tons;
	}

	public void setTons(double tons) {
		this.tons = tons;
	}
}
