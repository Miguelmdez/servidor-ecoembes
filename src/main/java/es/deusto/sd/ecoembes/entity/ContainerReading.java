package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

// Entidad de dominio y de persistencia: lectura diaria de un sensor (nivel de llenado y número estimado de envases).
// No tiene atributo id propio: la clave primaria es compuesta (@EmbeddedId ContainerReadingId = contenedor + fecha).
// @MapsId("containerId") hace que la parte "containerId" de la clave sea la propia clave foránea hacia Container,
// de modo que no se guarda dos veces. Guardar otra lectura del mismo contenedor y día sustituye a la anterior.
@Entity
public class ContainerReading {

	@EmbeddedId
	private ContainerReadingId id;

	@MapsId("containerId")
	@ManyToOne
	@JoinColumn(name = "container_id", nullable = false)
	private Container container;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private FillLevel fillLevel;

	@Column(nullable = false)
	private int packages;

	// Constructor sin parámetros
	public ContainerReading() { }

	// Constructor con parámetros: construye la clave compuesta a partir del contenedor y la fecha
	public ContainerReading(Container container, LocalDate date, FillLevel fillLevel, int packages) {
		this.id = new ContainerReadingId(container.getId(), date);
		this.container = container;
		this.fillLevel = fillLevel;
		this.packages = packages;
	}

	// Getters y setters
	public Container getContainer() {
		return container;
	}

	// La fecha vive dentro de la clave compuesta
	public LocalDate getDate() {
		return id == null ? null : id.getDate();
	}

	public FillLevel getFillLevel() {
		return fillLevel;
	}

	public void setFillLevel(FillLevel fillLevel) {
		this.fillLevel = fillLevel;
	}

	public int getPackages() {
		return packages;
	}

	public void setPackages(int packages) {
		this.packages = packages;
	}

	// hashCode y equals (por clave compuesta: contenedor y fecha)
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
		ContainerReading other = (ContainerReading) obj;
		return Objects.equals(id, other.id);
	}
}
