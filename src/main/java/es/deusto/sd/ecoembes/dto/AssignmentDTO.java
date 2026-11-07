package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;
import java.util.List;

// DTO: asignación de contenedores a una planta. Es la respuesta de POST /assignments y GET /assignments.
public class AssignmentDTO {
	private String assignmentId;
	private String plantId;
	private List<String> containerIds;
	private LocalDate date;
	private Integer totalContainers;
	private Integer estimatedPackages;
	private String assignedBy;

	// Constructor sin parámetros
	public AssignmentDTO() { }

	// Constructor con parámetros
	public AssignmentDTO(String assignmentId, String plantId, List<String> containerIds, LocalDate date, Integer totalContainers, Integer estimatedPackages, String assignedBy) {
		this.assignmentId = assignmentId;
		this.plantId = plantId;
		this.containerIds = containerIds;
		this.date = date;
		this.totalContainers = totalContainers;
		this.estimatedPackages = estimatedPackages;
		this.assignedBy = assignedBy;
	}

	// Getters y setters
	public String getAssignmentId() {
		return assignmentId;
	}

	public void setAssignmentId(String assignmentId) {
		this.assignmentId = assignmentId;
	}

	public String getPlantId() {
		return plantId;
	}

	public void setPlantId(String plantId) {
		this.plantId = plantId;
	}

	public List<String> getContainerIds() {
		return containerIds;
	}

	public void setContainerIds(List<String> containerIds) {
		this.containerIds = containerIds;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Integer getTotalContainers() {
		return totalContainers;
	}

	public void setTotalContainers(Integer totalContainers) {
		this.totalContainers = totalContainers;
	}

	public Integer getEstimatedPackages() {
		return estimatedPackages;
	}

	public void setEstimatedPackages(Integer estimatedPackages) {
		this.estimatedPackages = estimatedPackages;
	}

	public String getAssignedBy() {
		return assignedBy;
	}

	public void setAssignedBy(String assignedBy) {
		this.assignedBy = assignedBy;
	}
}
