package es.deusto.sd.ecoembes.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.deusto.sd.ecoembes.entity.PlantCapacity;

// Acceso a datos de la capacidad diaria que comunica cada planta. Se añade a los repositorios
// pedidos porque PlantCapacity es una entidad con su propia tabla y los services la consultan.
public interface PlantCapacityRepository extends JpaRepository<PlantCapacity, Long> {

	// Capacidad de todas las plantas en una fecha, ordenada por identificador de planta
	List<PlantCapacity> findByDateOrderByPlantId(LocalDate date);

	// Capacidad de una planta en una fecha
	Optional<PlantCapacity> findByPlantIdAndDate(String plantId, LocalDate date);
}
