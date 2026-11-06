package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.Objects;

public class ContainerReading {
	private long id;
	private Container container;
	private LocalDate date;
	private FillLevel fillLevel;
	private int packages;

	// Constructor without parameters
	public ContainerReading() { }

	// Constructor with parameters
	public ContainerReading(long id, Container container, LocalDate date, FillLevel fillLevel, int packages) {
		this.id = id;
		this.container = container;
		this.date = date;
		this.fillLevel = fillLevel;
		this.packages = packages;
	}

	// Getters and setters
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
		ContainerReading other = (ContainerReading) obj;
		return id == other.id;
	}
}
