package es.deusto.sd.ecoembes.dto;

// DTO (Data Transfer Object): credenciales de inicio de sesión. Es el cuerpo de POST /auth/login.
public class CredentialsDTO {
	private String email;
	private String password;

	// Constructor sin parámetros
	public CredentialsDTO() { }

	// Constructor con parámetros
	public CredentialsDTO(String email, String password) {
		this.email = email;
		this.password = password;
	}

	// Getters y setters
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
