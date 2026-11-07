package es.deusto.sd.ecoembes.entity;

import java.util.Objects;

// Entidad de dominio: contenedor de reciclaje sensorizado.
public class Container {
	private String id;
	private String location;
	private String postalCode;
	private float initialCapacity;

	// Constructor sin parámetros
	public Container() { }

	// Constructor con parámetros
	public Container(String id, String location, String postalCode, float initialCapacity) {
		this.id = id;
		this.location = location;
		this.postalCode = postalCode;
		this.initialCapacity = initialCapacity;
	}

	// Getters y setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public float getInitialCapacity() {
		return initialCapacity;
	}

	public void setInitialCapacity(float initialCapacity) {
		this.initialCapacity = initialCapacity;
	}

	// hashCode y equals (por id)
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
		return Objects.equals(id, other.id);
	}
}
