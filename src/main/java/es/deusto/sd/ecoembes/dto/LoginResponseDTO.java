package es.deusto.sd.ecoembes.dto;

public class LoginResponseDTO {
	private String token;
	private Long timestamp;

	// Constructor without parameters
	public LoginResponseDTO() { }

	// Constructor with parameters
	public LoginResponseDTO(String token, Long timestamp) {
		this.token = token;
		this.timestamp = timestamp;
	}

	// Getters and setters
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
