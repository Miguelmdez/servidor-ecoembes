package es.deusto.sd.ecoembes.dto;

public class ContainerDTO {
	private String containerId;
	private String location;
	private String postalCode;
	private Float initialCapacity;

	// Constructor without parameters
	public ContainerDTO() { }

	// Constructor with parameters
	public ContainerDTO(String containerId, String location, String postalCode, Float initialCapacity) {
		this.containerId = containerId;
		this.location = location;
		this.postalCode = postalCode;
		this.initialCapacity = initialCapacity;
	}

	// Getters and setters
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
