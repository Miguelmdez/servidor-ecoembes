package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;

import es.deusto.sd.ecoembes.entity.FillLevel;

public class ContainerReadingDTO {
	private String containerId;
	private LocalDate date;
	private Integer estimatedPackages;
	private FillLevel fillLevel;

	// Constructor without parameters
	public ContainerReadingDTO() { }

	// Constructor with parameters
	public ContainerReadingDTO(String containerId, LocalDate date, Integer estimatedPackages, FillLevel fillLevel) {
		this.containerId = containerId;
		this.date = date;
		this.estimatedPackages = estimatedPackages;
		this.fillLevel = fillLevel;
	}

	// Getters and setters
	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Integer getEstimatedPackages() {
		return estimatedPackages;
	}

	public void setEstimatedPackages(Integer estimatedPackages) {
		this.estimatedPackages = estimatedPackages;
	}

	public FillLevel getFillLevel() {
		return fillLevel;
	}

	public void setFillLevel(FillLevel fillLevel) {
		this.fillLevel = fillLevel;
	}
}
