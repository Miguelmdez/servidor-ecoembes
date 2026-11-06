package es.deusto.sd.ecoembes.dto;

public class CredentialsDTO {
	private String email;
	private String password;

	// Constructor without parameters
	public CredentialsDTO() { }

	// Constructor with parameters
	public CredentialsDTO(String email, String password) {
		this.email = email;
		this.password = password;
	}

	// Getters and setters
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
