package es.deusto.sd.ecoembes.dto;

public class ContainerStatusDTO {
	private long containerId;
	private String fillLevel;
	private int packages;

	// Constructor without parameters
	public ContainerStatusDTO() { }

	// Constructor with parameters
	public ContainerStatusDTO(long containerId, String fillLevel, int packages) {
		this.containerId = containerId;
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
