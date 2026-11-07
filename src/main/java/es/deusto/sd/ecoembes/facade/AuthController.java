package es.deusto.sd.ecoembes.facade;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.deusto.sd.ecoembes.dto.CredentialsDTO;
import es.deusto.sd.ecoembes.dto.LoginResponseDTO;
import es.deusto.sd.ecoembes.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authorization Controller", description = "Login and logout operations")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	// Login endpoint
	@Operation(
		summary = "Login to the system",
		description = "Allows an Ecoembes employee to login by providing email and password. Returns a token and a timestamp if successful.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: Login successful, returns token and timestamp"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Email or password missing"),
			@ApiResponse(responseCode = "401", description = "Unauthorized: Invalid credentials, login failed"),
		}
	)
	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> login(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Employee credentials", required = true)
			@RequestBody(required = false) CredentialsDTO credentials) {
		if (credentials == null || isBlank(credentials.getEmail()) || isBlank(credentials.getPassword())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		Optional<String> token = authService.login(credentials.getEmail(), credentials.getPassword());

		if (token.isPresent()) {
			return new ResponseEntity<>(new LoginResponseDTO(token.get(), Long.valueOf(token.get())), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	// Logout endpoint
	@Operation(
		summary = "Logout from the system",
		description = "Allows an employee to logout by providing the session token in the request body.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: Logout successful"),
			@ApiResponse(responseCode = "400", description = "Bad Request: Token missing"),
			@ApiResponse(responseCode = "401", description = "Unauthorized: Invalid token, logout failed"),
		}
	)
	@PostMapping(value = "/logout", consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Void> logout(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Session token in plain text", required = true,
					content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "1794045820666")))
			@RequestBody(required = false) String token) {
		// Remove surrounding quotes (e.g. token sent as a JSON string)
		String cleanToken = token == null ? null : token.trim().replaceAll("^\"+|\"+$", "");

		if (isBlank(cleanToken)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		Optional<Boolean> result = authService.logout(cleanToken);

		if (result.isPresent() && result.get()) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
