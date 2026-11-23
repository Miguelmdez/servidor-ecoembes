package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

// Entidad de dominio y de persistencia: asignación de contenedores a una planta; guarda quién la hizo (auditoría).
// Una asignación tiene una planta (@ManyToOne) y varios contenedores (@ManyToMany, tabla intermedia
// assignment_container). Los contenedores se cargan con EAGER porque el controller convierte la asignación
// a DTO fuera de la transacción (spring.jpa.open-in-view=false).
@Entity
public class Assignment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "plant_id", nullable = false)
	private RecyclingPlant plant;

	@Column(nullable = false)
	private LocalDate date;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "assignment_container",
			joinColumns = @JoinColumn(name = "assignment_id"),
			inverseJoinColumns = @JoinColumn(name = "container_id"))
	private List<Container> containers = new ArrayList<>();

	@Column(nullable = false)
	private int totalPackages;

	@Column(nullable = false)
	private double tons;

	// Email del empleado que hizo la asignación (auditoría)
	@Column(nullable = false)
	private String assignedBy;

	// Constructor sin parámetros
	public Assignment() { }

	// Constructor con parámetros (el id lo genera la base de datos)
	public Assignment(RecyclingPlant plant, LocalDate date, List<Container> containers, int totalPackages, double tons, String assignedBy) {
		this.plant = plant;
		this.date = date;
		this.containers = containers;
		this.totalPackages = totalPackages;
		this.tons = tons;
		this.assignedBy = assignedBy;
	}

	// Getters y setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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
		return id != null && id.equals(other.id);
	}
}
