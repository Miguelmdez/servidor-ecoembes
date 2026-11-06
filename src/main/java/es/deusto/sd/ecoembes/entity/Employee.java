package es.deusto.sd.ecoembes.entity;

import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;

public class Employee {

	private long id;
	private String name;
	private String email;
	private String password;

	// Constructor without parameters
	public Employee() { }

	// Constructor with parameters
	public Employee(long id, String name, String email, String password) {
		this.id = id;
		this.name = name;
		this.email = email;
		// Passwords are never stored in plain text: only the SHA-1 hash is kept
		this.password = DigestUtils.sha1Hex(password);
	}

	// Compare the hash of the provided password against the stored hash
	public boolean checkPassword(String password) {
		return this.password.equals(DigestUtils.sha1Hex(password));
	}

	// Getters and setters
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

	// Note: there is no getPassword() on purpose, the hash is never exposed
	public void setPassword(String password) {
		this.password = DigestUtils.sha1Hex(password);
	}

	// hashCode and equals (by id)
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
