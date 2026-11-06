package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDTO {
	private long id;
	private long plantId;
	private LocalDate date;
	private List<Long> containerIds;
	private int totalPackages;
	private double tons;
	private String assignedBy;

	// Constructor without parameters
	public AssignmentDTO() { }

	// Constructor with parameters
	public AssignmentDTO(long id, long plantId, LocalDate date, List<Long> containerIds, int totalPackages, double tons, String assignedBy) {
		this.id = id;
		this.plantId = plantId;
		this.date = date;
		this.containerIds = containerIds;
		this.totalPackages = totalPackages;
		this.tons = tons;
		this.assignedBy = assignedBy;
	}

	// Getters and setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getPlantId() {
		return plantId;
	}

	public void setPlantId(long plantId) {
		this.plantId = plantId;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public List<Long> getContainerIds() {
		return containerIds;
	}

	public void setContainerIds(List<Long> containerIds) {
		this.containerIds = containerIds;
	}

	public int getTotalPackages() {
		return totalPackages;
	}

	public void setTotalPackages(int totalPackages) {
		this.totalPackages = totalPackages;
	}

	public double getTons() {
		return tons;
	}

	public void setTons(double tons) {
		this.tons = tons;
	}

	public String getAssignedBy() {
		return assignedBy;
	}

	public void setAssignedBy(String assignedBy) {
		this.assignedBy = assignedBy;
	}
}
