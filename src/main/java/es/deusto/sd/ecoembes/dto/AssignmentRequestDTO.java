package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AssignmentRequestDTO {
	private long plantId;
	private LocalDate date;
	private List<Long> containerIds;

	// Constructor without parameters
	public AssignmentRequestDTO() { }

	// Constructor with parameters
	public AssignmentRequestDTO(long plantId, LocalDate date, List<Long> containerIds) {
		this.plantId = plantId;
		this.date = date;
		this.containerIds = containerIds;
	}

	// Getters and setters
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
}
