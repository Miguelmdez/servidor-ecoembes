package es.deusto.sd.ecoembes.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.deusto.sd.ecoembes.entity.Container;

// Acceso a datos de los contenedores (la clave es su identificador, por ejemplo "CONT-001").
public interface ContainerRepository extends JpaRepository<Container, String> {
}
