package es.deusto.sd.ecoembes.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.deusto.sd.ecoembes.entity.Employee;

// Acceso a datos de los empleados. Spring Data genera la implementación a partir del nombre del método.
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	// Empleado por correo electrónico (se usa en el login)
	Optional<Employee> findByEmail(String email);
}
