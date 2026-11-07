package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Entidad de dominio: asignación de contenedores a una planta; guarda quién la hizo (auditoría).
public class Assignment {
	private long id;
	private RecyclingPlant plant;
	private LocalDate date;
	private List<Container> containers = new ArrayList<>();
	private int totalPackages;
	private double tons;
	private String assignedBy;

	// Constructor sin parámetros
	public Assignment() { }

	// Constructor con parámetros
	public Assignment(long id, RecyclingPlant plant, LocalDate date, List<Container> containers, int totalPackages, double tons, String assignedBy) {
		this.id = id;
		this.plant = plant;
		this.date = date;
		this.containers = containers;
		this.totalPackages = totalPackages;
		this.tons = tons;
		this.assignedBy = assignedBy;
	}

	// Getters y setters
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

	public List<Container> getContainers() {
		return containers;
	}

	public void setContainers(List<Container> containers) {
		this.containers = containers;
	}

	public int getTotalPackages() {
		return totalPackages;
	}

	public void setTotalPackages(int totalPackages) {
		this.totalPackages = totalPackages;
	}

	public double getTons() {
		return tons;
	}

	public void setTons(double tons) {
		this.tons = tons;
	}

	public String getAssignedBy() {
		return assignedBy;
	}

	public void setAssignedBy(String assignedBy) {
		this.assignedBy = assignedBy;
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
		Assignment other = (Assignment) obj;
		return id == other.id;
	}
}
