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

// Patrón Façade: este @RestController es la única puerta de entrada HTTP de la autenticación. Solo se ocupa de la capa
// web (rutas, códigos HTTP y conversión entidad <-> DTO); la lógica de negocio está en AuthService.
// El service lanza RuntimeException con un mensaje constante y el controller lo traduce a un código HTTP.
@RestController
@RequestMapping("/auth")
@Tag(name = "Controlador de autenticación", description = "Operaciones de inicio y cierre de sesión")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	// Inicio de sesión
	@Operation(
		summary = "Iniciar sesión",
		description = "Permite a un empleado de Ecoembes iniciar sesión con su email y contraseña. Si son correctos, devuelve el token de sesión y su marca de tiempo.",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: inicio de sesión correcto; devuelve token y marca de tiempo"),
			@ApiResponse(responseCode = "400", description = "Solicitud incorrecta: falta el email o la contraseña"),
			@ApiResponse(responseCode = "401", description = "No autorizado: credenciales incorrectas"),
		}
	)
	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> login(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credenciales del empleado", required = true)
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

	// Cierre de sesión
	@Operation(
		summary = "Cerrar sesión",
		description = "Permite a un empleado cerrar su sesión enviando el token de sesión en el cuerpo de la petición (texto plano o JSON).",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK: sesión cerrada correctamente"),
			@ApiResponse(responseCode = "400", description = "Solicitud incorrecta: falta el token"),
			@ApiResponse(responseCode = "401", description = "No autorizado: token no válido o sesión no activa"),
		}
	)
	@PostMapping(value = "/logout", consumes = { MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Void> logout(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Token de sesión en texto plano", required = true,
					content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "1794045820666")))
			@RequestBody(required = false) String token) {
		// Quita las comillas que rodean al token (por ejemplo, si llega como cadena JSON desde Swagger)
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
