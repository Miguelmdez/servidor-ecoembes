package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.Objects;

public class PlantCapacity {
	private long id;
	private RecyclingPlant plant;
	private LocalDate date;
	private double tons;

	// Constructor without parameters
	public PlantCapacity() { }

	// Constructor with parameters
	public PlantCapacity(long id, RecyclingPlant plant, LocalDate date, double tons) {
		this.id = id;
		this.plant = plant;
		this.date = date;
		this.tons = tons;
	}

	// Getters and setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public RecyclingPlant getPlant() {
		return plant;
	}

	public void setPlant(RecyclingPlant plant) {
		this.plant = plant;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public double getTons() {
		return tons;
	}

	public void setTons(double tons) {
		this.tons = tons;
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
		PlantCapacity other = (PlantCapacity) obj;
		return id == other.id;
	}
}
