package es.deusto.sd.ecoembes.entity;

import java.util.Objects;

// Entidad de dominio: planta de reciclaje. Añadir una planta nueva solo requiere datos, no código.
public class RecyclingPlant {
	private String id;
	private String name;

	// Constructor sin parámetros
	public RecyclingPlant() { }

	// Constructor con parámetros
	public RecyclingPlant(String id, String name) {
		this.id = id;
		this.name = name;
	}

	// Getters y setters
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
		RecyclingPlant other = (RecyclingPlant) obj;
		return Objects.equals(id, other.id);
	}
}
