package es.deusto.sd.ecoembes.entity;

import java.util.Objects;

public class RecyclingPlant {
	private String id;
	private String name;

	// Constructor without parameters
	public RecyclingPlant() { }

	// Constructor with parameters
	public RecyclingPlant(String id, String name) {
		this.id = id;
		this.name = name;
	}

	// Getters and setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		RecyclingPlant other = (RecyclingPlant) obj;
		return Objects.equals(id, other.id);
	}
}
