package es.deusto.sd.ecoembes.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Clave primaria compuesta de ContainerReading: contenedor + fecha. Así una lectura se identifica por
// su contenedor y su día (solo hay una por contenedor y día) sin necesidad de un id propio.
// Una clave compuesta debe ser Serializable y definir equals/hashCode con todos sus campos.
@Embeddable
public class ContainerReadingId implements Serializable {

	private static final long serialVersionUID = 1L;

	// Se rellena a partir de la relación con Container gracias a @MapsId("containerId")
	@Column(name = "container_id")
	private String containerId;

	@Column(name = "date")
	private LocalDate date;

	// Constructor sin parámetros (lo exige JPA)
	public ContainerReadingId() { }

	// Constructor con parámetros
	public ContainerReadingId(String containerId, LocalDate date) {
		this.containerId = containerId;
		this.date = date;
	}

	// Getters y setters
	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	// hashCode y equals (por contenedor y fecha)
	@Override
	public int hashCode() {
		return Objects.hash(containerId, date);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContainerReadingId other = (ContainerReadingId) obj;
		return Objects.equals(containerId, other.containerId) && Objects.equals(date, other.date);
	}
}
