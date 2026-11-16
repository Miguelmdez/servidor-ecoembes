package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.Objects;

// Entidad de dominio: lectura diaria de un sensor (nivel de llenado y número estimado de envases).
// No tiene identificador propio: una lectura se identifica por su contenedor y su fecha (solo hay una por contenedor y día).
public class ContainerReading {
	private Container container;
	private LocalDate date;
	private FillLevel fillLevel;
	private int packages;

	// Constructor sin parámetros
	public ContainerReading() { }

	// Constructor con parámetros
	public ContainerReading(Container container, LocalDate date, FillLevel fillLevel, int packages) {
		this.container = container;
		this.date = date;
		this.fillLevel = fillLevel;
		this.packages = packages;
	}

	// Getters y setters
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

	// hashCode y equals (por contenedor y fecha)
	@Override
	public int hashCode() {
		return Objects.hash(container, date);
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
		return Objects.equals(container, other.container) && Objects.equals(date, other.date);
	}
}
