package es.deusto.sd.ecoembes.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.entity.Employee;

@Service
public class AuthService {

	// Simulating an employee repository (thread-safe: shared across request threads)
	private final Map<String, Employee> employeeRepository = new ConcurrentHashMap<>();

	// Storage to keep the session of the employees that are logged in
	private final Map<String, Employee> tokenStore = new ConcurrentHashMap<>();

	// Login method that checks if the employee exists and validates the password
	public Optional<String> login(String email, String password) {
		if (email == null || password == null) {
			return Optional.empty();
		}

		Employee employee = employeeRepository.get(email);

		if (employee != null && employee.checkPassword(password)) {
			String token = generateToken();
			tokenStore.put(token, employee);

			return Optional.of(token);
		} else {
			return Optional.empty();
		}
	}

	// Logout method to remove the token from the session store
	public Optional<Boolean> logout(String token) {
		if (token != null && tokenStore.remove(token) != null) {
			return Optional.of(true);
		} else {
			return Optional.empty();
		}
	}

	// Method to add a new employee to the repository
	public void addEmployee(Employee employee) {
		if (employee != null) {
			employeeRepository.putIfAbsent(employee.getEmail(), employee);
		}
	}

	// Method to get the employee based on the token
	public Employee getEmployeeByToken(String token) {
		return token == null ? null : tokenStore.get(token);
	}

	// Token = current time in millis (required by the case study); retry on collision
	private synchronized String generateToken() {
		String token;

		do {
			token = String.valueOf(System.currentTimeMillis());
		} while (tokenStore.containsKey(token));

		return token;
	}
}
