package es.deusto.sd.ecoembes.dto;

public class ContainerDTO {
	private long id;
	private String postalCode;
	private String address;
	private int capacity;

	// Constructor without parameters
	public ContainerDTO() { }

	// Constructor with parameters
	public ContainerDTO(long id, String postalCode, String address, int capacity) {
		this.id = id;
		this.postalCode = postalCode;
		this.address = address;
		this.capacity = capacity;
	}

	// Getters and setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
}
