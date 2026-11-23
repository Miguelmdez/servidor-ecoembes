package es.deusto.sd.ecoembes.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.deusto.sd.ecoembes.entity.RecyclingPlant;
import jakarta.persistence.LockModeType;

// Acceso a datos de las plantas de reciclaje (la clave es su identificador, por ejemplo "PLANT-1").
public interface RecyclingPlantRepository extends JpaRepository<RecyclingPlant, String> {

	// Todas las plantas ordenadas por identificador
	List<RecyclingPlant> findAllByOrderById();

	// Recupera la planta bloqueando su fila para escritura. Se usa al crear una asignación: así dos
	// asignaciones simultáneas a la misma planta se ejecutan una detrás de otra y no pueden superar
	// entre las dos la capacidad ("comprobar capacidad y guardar" deja de ser una carrera).
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from RecyclingPlant p where p.id = :id")
	Optional<RecyclingPlant> findByIdForUpdate(@Param("id") String id);
}
