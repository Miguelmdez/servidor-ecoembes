package es.deusto.sd.ecoembes.external;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

// Patrón Gateway para ContSocket Ltd., que se comunica por sockets TCP: cada mensaje es un JSON en una línea
// terminada en \n y la respuesta es otro JSON en una línea. Abre una conexión por operación, con un tiempo máximo de
// 3 s para conectar y para esperar la respuesta. No es un bean: lo crea RecyclingPlantGatewayFactory.
// Códigos de respuesta de la planta: 000 OK, 001 fecha incorrecta, 002 sin capacidad, 003 error interno,
// 004 datos de la asignación incorrectos, 005 operación desconocida o JSON inválido.
public class ContSocketGateway implements IRecyclingPlantGateway {

	private static final int TIMEOUT_MS = 3000;

	private final String host;
	private final int port;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public ContSocketGateway(String host, int port) {
		this.host = host;
		this.port = port;
	}

	// 000 -> availableCapacity; 001 y 002 (sin datos para esa fecha) -> vacío; cualquier otro código o fallo de
	// comunicación -> PlantUnavailableException.
	@Override
	public Optional<Double> getCapacity(LocalDate date) {
		Map<String, Object> request = new LinkedHashMap<>();
		request.put("operation", "CONSULTAR_CAPACIDAD");
		request.put("date", date.toString());

		JsonNode response = send(request);

		return switch (response.path("responseCode").asString("")) {
			case "000" -> {
				JsonNode availableCapacity = response.path("availableCapacity");

				if (!availableCapacity.isNumber()) {
					throw new PlantUnavailableException("ContSocket devolvió una respuesta sin availableCapacity");
				}

				yield Optional.of(availableCapacity.asDouble());
			}
			case "001", "002" -> Optional.<Double>empty();
			default -> throw new PlantUnavailableException(
					"ContSocket respondió con el código " + response.path("responseCode").asString(""));
		};
	}

	// 000 -> aceptada; 004 -> rechazada por la planta; cualquier otro código o fallo de comunicación ->
	// PlantUnavailableException.
	@Override
	public boolean notifyAssignment(LocalDate date, List<String> containerIds, int estimatedPackages) {
		Map<String, Object> request = new LinkedHashMap<>();
		request.put("operation", "NOTIFICAR_ASIGNACION");
		request.put("date", date.toString());
		request.put("numberOfContainers", containerIds.size());
		request.put("estimatedPackages", estimatedPackages);
		request.put("containerIds", containerIds);

		JsonNode response = send(request);

		return switch (response.path("responseCode").asString("")) {
			case "000" -> true;
			case "004" -> false;
			default -> throw new PlantUnavailableException(
					"ContSocket respondió con el código " + response.path("responseCode").asString(""));
		};
	}

	// Envía una petición (una línea JSON) y devuelve la respuesta (una línea JSON). Cualquier fallo de red, tiempo
	// agotado o respuesta vacía o ilegible se convierte en PlantUnavailableException.
	private JsonNode send(Map<String, Object> request) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), TIMEOUT_MS);
			socket.setSoTimeout(TIMEOUT_MS);

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

			out.write(objectMapper.writeValueAsString(request));
			out.write('\n');
			out.flush();

			String line = in.readLine();

			if (line == null) {
				throw new PlantUnavailableException("ContSocket cerró la conexión sin responder");
			}

			return objectMapper.readTree(line);
		} catch (IOException e) {
			throw new PlantUnavailableException("ContSocket no responde", e);
		} catch (PlantUnavailableException e) {
			throw e;
		} catch (RuntimeException e) {
			// JSON ilegible
			throw new PlantUnavailableException("ContSocket devolvió una respuesta ilegible", e);
		}
	}
}
