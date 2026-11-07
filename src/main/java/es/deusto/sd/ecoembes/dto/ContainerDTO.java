package es.deusto.sd.ecoembes.dto;

// DTO: datos básicos de un contenedor. Es el cuerpo de POST /containers. initialCapacity se expresa en número de envases.
public class ContainerDTO {
	private String containerId;
	private String location;
	private String postalCode;
	private Float initialCapacity;

	// Constructor sin parámetros
	public ContainerDTO() { }

	// Constructor con parámetros
	public ContainerDTO(String containerId, String location, String postalCode, Float initialCapacity) {
		this.containerId = containerId;
		this.location = location;
		this.postalCode = postalCode;
		this.initialCapacity = initialCapacity;
	}

	// Getters y setters
	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public Float getInitialCapacity() {
		return initialCapacity;
	}

	public void setInitialCapacity(Float initialCapacity) {
		this.initialCapacity = initialCapacity;
	}
}
