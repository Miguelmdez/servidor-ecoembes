package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ZoneStatusDTO {
	private String postalCode;
	private LocalDate date;
	private List<ContainerStatusDTO> containers;

	// Constructor without parameters
	public ZoneStatusDTO() { }

	// Constructor with parameters
	public ZoneStatusDTO(String postalCode, LocalDate date, List<ContainerStatusDTO> containers) {
		this.postalCode = postalCode;
		this.date = date;
		this.containers = containers;
	}

	// Getters and setters
	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public List<ContainerStatusDTO> getContainers() {
		return containers;
	}

	public void setContainers(List<ContainerStatusDTO> containers) {
		this.containers = containers;
	}
}
