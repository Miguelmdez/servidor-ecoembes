package es.deusto.sd.ecoembes.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.deusto.sd.ecoembes.entity.Assignment;

// Acceso a datos de las asignaciones de contenedores a plantas.
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

	// Asignaciones de una planta en una fecha, ordenadas por identificador
	List<Assignment> findByDateAndPlantIdOrderById(LocalDate date, String plantId);

	// Verdadero si el contenedor ya está en alguna asignación de ese día (de cualquier planta)
	boolean existsByDateAndContainersId(LocalDate date, String containerId);
}
