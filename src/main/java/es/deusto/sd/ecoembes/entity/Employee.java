package es.deusto.sd.ecoembes.entity;

import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Entidad de dominio y de persistencia: empleado de Ecoembes que usa la API.
@Entity
public class Employee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	// Solo se guarda el hash de la contraseña, nunca la contraseña en claro
	@Column(nullable = false)
	private String password;

	// Constructor sin parámetros
	public Employee() { }

	// Constructor con parámetros (el id lo genera la base de datos)
	public Employee(String name, String email, String password) {
		this.name = name;
		this.email = email;
		// Las contraseñas nunca se guardan en claro: solo se conserva su hash SHA-1
		this.password = DigestUtils.sha1Hex(password);
	}

	// Compara el hash de la contraseña recibida con el hash almacenado
	public boolean checkPassword(String password) {
		return this.password.equals(DigestUtils.sha1Hex(password));
	}

	// Getters y setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	// Nota: no hay getPassword() a propósito, el hash nunca se expone
	public void setPassword(String password) {
		this.password = DigestUtils.sha1Hex(password);
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
		Employee other = (Employee) obj;
		return id != null && id.equals(other.id);
	}
}
