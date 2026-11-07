package es.deusto.sd.ecoembes.entity;

import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;

// Entidad de dominio: empleado de Ecoembes que usa la API.
public class Employee {

	private long id;
	private String name;
	private String email;
	private String password;

	// Constructor sin parámetros
	public Employee() { }

	// Constructor con parámetros
	public Employee(long id, String name, String email, String password) {
		this.id = id;
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
	public long getId() {
		return id;
	}

	public void setId(long id) {
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
		return id == other.id;
	}
}
