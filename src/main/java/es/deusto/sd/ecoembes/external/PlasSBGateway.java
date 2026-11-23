package es.deusto.sd.ecoembes.external;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

// Patrón Gateway para PlasSB Ltd., que ofrece una API REST (GET /capacidades?fecha= y POST /asignaciones bajo
// /api/plassb). Usa RestClient con un tiempo máximo de conexión y de lectura, para que una planta caída o lenta
// no deje bloqueada la petición. No es un bean: lo crea RecyclingPlantGatewayFactory con el host y el puerto de cada planta.
public class PlasSBGateway implements IRecyclingPlantGateway {

	private static final Duration TIMEOUT = Duration.ofSeconds(3);

	private final RestClient restClient;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public PlasSBGateway(String host, int port) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(TIMEOUT);
		requestFactory.setReadTimeout(TIMEOUT);

		this.restClient = RestClient.builder()
				.baseUrl("http://" + host + ":" + port + "/api/plassb")
				.requestFactory(requestFactory)
				.build();
	}

	// 200 -> availableCapacity; 404 (sin datos) y 422 (fecha fuera de rango para la planta) -> vacío;
	// cualquier otro estado o fallo de comunicación -> PlantUnavailableException.
	@Override
	public Optional<Double> getCapacity(LocalDate date) {
		try {
			return restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/capacidades").queryParam("fecha", date.toString()).build())
					.exchange((request, response) -> {
						HttpStatusCode status = response.getStatusCode();

						if (status.value() == 200) {
							String body = response.bodyTo(String.class);
							JsonNode availableCapacity = objectMapper.readTree(body).path("availableCapacity");

							if (!availableCapacity.isNumber()) {
								throw new PlantUnavailableException("PlasSB devolvió una respuesta sin availableCapacity");
							}

							return Optional.of(availableCapacity.asDouble());
						} else if (status.value() == 404 || status.value() == 422) {
							return Optional.<Double>empty();
						} else {
							throw new PlantUnavailableException("PlasSB respondió con el estado " + status.value());
						}
					});
		} catch (PlantUnavailableException e) {
			throw e;
		} catch (RuntimeException e) {
			// Conexión rechazada, tiempo agotado, JSON ilegible...
			throw new PlantUnavailableException("PlasSB no responde", e);
		}
	}

	// 201 -> aceptada; 400 o 409 -> rechazada por la planta; cualquier otro estado o fallo de comunicación ->
	// PlantUnavailableException.
	@Override
	public boolean notifyAssignment(LocalDate date, List<String> containerIds, int estimatedPackages) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("date", date.toString());
		body.put("numberOfContainers", containerIds.size());
		body.put("estimatedPackages", estimatedPackages);
		body.put("containerIds", containerIds);

		try {
			return restClient.post()
					.uri("/asignaciones")
					.body(body)
					.exchange((request, response) -> {
						int status = response.getStatusCode().value();

						if (status == 201) {
							return true;
						} else if (status == 400 || status == 409) {
							return false;
						} else {
							throw new PlantUnavailableException("PlasSB respondió con el estado " + status);
						}
					});
		} catch (PlantUnavailableException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new PlantUnavailableException("PlasSB no responde", e);
		}
	}
}
