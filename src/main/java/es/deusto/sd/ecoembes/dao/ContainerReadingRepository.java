package es.deusto.sd.ecoembes.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.deusto.sd.ecoembes.entity.ContainerReading;
import es.deusto.sd.ecoembes.entity.ContainerReadingId;

// Acceso a datos de las lecturas. La clave es compuesta (contenedor + fecha), por eso en los nombres
// de los métodos la fecha se escribe IdDate (atributo date de la clave embebida "id").
public interface ContainerReadingRepository extends JpaRepository<ContainerReading, ContainerReadingId> {

	// Lecturas de un contenedor entre dos fechas (ambas incluidas), ordenadas por fecha
	List<ContainerReading> findByContainerIdAndIdDateBetweenOrderByIdDate(String containerId, LocalDate startDate,
			LocalDate endDate);

	// Lecturas de los contenedores de un código postal en una fecha, ordenadas por identificador de contenedor
	List<ContainerReading> findByContainerPostalCodeAndIdDateOrderByContainerId(String postalCode, LocalDate date);

	// Última lectura de un contenedor en la fecha indicada o antes
	Optional<ContainerReading> findFirstByContainerIdAndIdDateLessThanEqualOrderByIdDateDesc(String containerId,
			LocalDate date);
}
