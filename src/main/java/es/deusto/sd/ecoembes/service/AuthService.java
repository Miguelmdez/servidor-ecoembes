package es.deusto.sd.ecoembes.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.dao.EmployeeRepository;
import es.deusto.sd.ecoembes.entity.Employee;

// Patrón AppService: contiene la lógica de autenticación y el ESTADO DEL SERVIDOR de las sesiones activas.
// Los empleados se leen de la base de datos (EmployeeRepository), pero el mapa token -> empleado se mantiene
// EN MEMORIA a propósito: así no se consulta la base de datos en cada petición para saber quién es el
// usuario, como pide el enunciado. Consecuencia: al reiniciar el servidor las sesiones se pierden y hay que
// volver a iniciar sesión. El mapa es ConcurrentHashMap porque este bean es único (singleton) y lo comparten
// todos los hilos que atienden peticiones.
@Service
public class AuthService {

	private final EmployeeRepository employeeRepository;

	// Sesiones activas: token -> empleado que ha iniciado sesión
	private final Map<String, Employee> tokenStore = new ConcurrentHashMap<>();

	public AuthService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	// Inicio de sesión: comprueba que el empleado existe y que la contraseña es correcta.
	// Devuelve el token generado, o vacío si las credenciales no son válidas.
	public Optional<String> login(String email, String password) {
		if (email == null || password == null) {
			return Optional.empty();
		}

		Optional<Employee> employee = employeeRepository.findByEmail(email);

		if (employee.isPresent() && employee.get().checkPassword(password)) {
			String token = generateToken();
			tokenStore.put(token, employee.get());

			return Optional.of(token);
		} else {
			return Optional.empty();
		}
	}

	// Cierre de sesión: elimina el token de las sesiones activas. Vacío si el token no estaba activo.
	public Optional<Boolean> logout(String token) {
		if (token != null && tokenStore.remove(token) != null) {
			return Optional.of(true);
		} else {
			return Optional.empty();
		}
	}

	// Devuelve el empleado de una sesión activa, o null si el token no es válido.
	// Lo usan otros servicios y controllers para autenticar y para auditar (quién hace cada operación).
	public Employee getEmployeeByToken(String token) {
		return token == null ? null : tokenStore.get(token);
	}

	// El token es la hora actual en milisegundos (lo exige el enunciado). Si dos inicios de sesión
	// coinciden en el mismo milisegundo, se reintenta para no pisar la sesión anterior.
	private synchronized String generateToken() {
		String token;

		do {
			token = String.valueOf(System.currentTimeMillis());
		} while (tokenStore.containsKey(token));

		return token;
	}
}
