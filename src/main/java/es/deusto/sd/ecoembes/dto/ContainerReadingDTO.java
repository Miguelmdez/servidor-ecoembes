package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;

public class ContainerReadingDTO {
	private long containerId;
	private LocalDate date;
	private String fillLevel;
	private int packages;

	// Constructor without parameters
	public ContainerReadingDTO() { }

	// Constructor with parameters
	public ContainerReadingDTO(long containerId, LocalDate date, String fillLevel, int packages) {
		this.containerId = containerId;
		this.date = date;
		this.fillLevel = fillLevel;
		this.packages = packages;
	}

	// Getters and setters
	public long getContainerId() {
		return containerId;
	}

	public void setContainerId(long containerId) {
		this.containerId = containerId;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getFillLevel() {
		return fillLevel;
	}

	public void setFillLevel(String fillLevel) {
		this.fillLevel = fillLevel;
	}

	public int getPackages() {
		return packages;
	}

	public void setPackages(int packages) {
		this.packages = packages;
	}
}
