package es.deusto.sd.ecoembes.dto;

// DTO: respuesta de un inicio de sesión correcto (token de sesión y su marca de tiempo).
public class LoginResponseDTO {
	private String token;
	private Long timestamp;

	// Constructor sin parámetros
	public LoginResponseDTO() { }

	// Constructor con parámetros
	public LoginResponseDTO(String token, Long timestamp) {
		this.token = token;
		this.timestamp = timestamp;
	}

	// Getters y setters
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}
