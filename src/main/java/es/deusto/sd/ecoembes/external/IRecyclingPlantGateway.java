package es.deusto.sd.ecoembes.external;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Patrón Gateway: oculta al resto de la aplicación CÓMO se habla con una planta de reciclaje (REST, sockets...).
// Los services solo conocen esta interfaz; cada implementación traduce las dos operaciones al protocolo de su planta.
// Las plantas son servicios externos que pueden fallar: si no responden (conexión rechazada, tiempo agotado,
// error interno...) las implementaciones lanzan PlantUnavailableException.
public interface IRecyclingPlantGateway {

	// Capacidad DISPONIBLE (en toneladas) de la planta en una fecha, ya descontadas las asignaciones que la propia
	// planta tiene reservadas. Vacío si la planta no tiene datos para esa fecha.
	Optional<Double> getCapacity(LocalDate date);

	// Notifica a la planta una asignación (fecha, contenedores y envases estimados). Devuelve true si la planta la
	// acepta y false si la rechaza (datos incorrectos o contenedor ya asignado en la planta ese día).
	boolean notifyAssignment(LocalDate date, List<String> containerIds, int estimatedPackages);
}
