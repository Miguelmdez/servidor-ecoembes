package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Entidad de dominio y de persistencia: capacidad diaria (en toneladas) que comunica una planta para una fecha.
// Una planta solo puede comunicar una capacidad por día (restricción única planta + fecha).
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "plant_id", "date" }))
public class PlantCapacity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "plant_id", nullable = false)
	private RecyclingPlant plant;

	@Column(nullable = false)
	private LocalDate date;

	@Column(nullable = false)
	private double tons;

	// Constructor sin parámetros
	public PlantCapacity() { }

	// Constructor con parámetros
	public PlantCapacity(RecyclingPlant plant, LocalDate date, double tons) {
		this.plant = plant;
		this.date = date;
		this.tons = tons;
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

	public double getTons() {
		return tons;
	}

	public void setTons(double tons) {
		this.tons = tons;
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
		PlantCapacity other = (PlantCapacity) obj;
		return id != null && id.equals(other.id);
	}
}
