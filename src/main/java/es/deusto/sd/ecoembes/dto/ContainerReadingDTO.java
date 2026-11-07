package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;

import es.deusto.sd.ecoembes.entity.FillLevel;

// DTO: lectura diaria de un sensor. Sirve de cuerpo al recibir lecturas y de respuesta en las consultas.
public class ContainerReadingDTO {
	private String containerId;
	private LocalDate date;
	private Integer estimatedPackages;
	private FillLevel fillLevel;

	// Constructor sin parámetros
	public ContainerReadingDTO() { }

	// Constructor con parámetros
	public ContainerReadingDTO(String containerId, LocalDate date, Integer estimatedPackages, FillLevel fillLevel) {
		this.containerId = containerId;
		this.date = date;
		this.estimatedPackages = estimatedPackages;
		this.fillLevel = fillLevel;
	}

	// Getters y setters
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
