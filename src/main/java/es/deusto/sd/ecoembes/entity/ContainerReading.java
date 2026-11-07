package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.Objects;

// Entidad de dominio: lectura diaria de un sensor (nivel de llenado y número estimado de envases).
public class ContainerReading {
	private long id;
	private Container container;
	private LocalDate date;
	private FillLevel fillLevel;
	private int packages;

	// Constructor sin parámetros
	public ContainerReading() { }

	// Constructor con parámetros
	public ContainerReading(long id, Container container, LocalDate date, FillLevel fillLevel, int packages) {
		this.id = id;
		this.container = container;
		this.date = date;
		this.fillLevel = fillLevel;
		this.packages = packages;
	}

	// Getters y setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
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
		ContainerReading other = (ContainerReading) obj;
		return id == other.id;
	}
}
