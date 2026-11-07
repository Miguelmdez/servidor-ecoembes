package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;
import java.util.List;

// DTO: petición de asignación. El token del empleado viaja en userToken, dentro del cuerpo.
public class AssignmentRequestDTO {
	private String plantId;
	private List<String> containerIds;
	private LocalDate date;
	private String userToken;

	// Constructor sin parámetros
	public AssignmentRequestDTO() { }

	// Constructor con parámetros
	public AssignmentRequestDTO(String plantId, List<String> containerIds, LocalDate date, String userToken) {
		this.plantId = plantId;
		this.containerIds = containerIds;
		this.date = date;
		this.userToken = userToken;
	}

	// Getters y setters
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

	public String getUserToken() {
		return userToken;
	}

	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}
}
