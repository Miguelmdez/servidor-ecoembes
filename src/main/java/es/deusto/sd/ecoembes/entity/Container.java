package es.deusto.sd.ecoembes.entity;

import java.util.Objects;

public class Container {
	private long id;
	private String postalCode;
	private String address;
	private int capacity;

	// Constructor without parameters
	public Container() { }

	// Constructor with parameters
	public Container(long id, String postalCode, String address, int capacity) {
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

	// hashCode and equals (by id)
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Container other = (Container) obj;
		return id == other.id;
	}
}
