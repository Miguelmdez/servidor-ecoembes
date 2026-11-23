package es.deusto.sd.ecoembes.external;

// Se lanza cuando una planta externa no responde o responde con un error del servidor. Los services la convierten en el
// mensaje constante PLANT_UNAVAILABLE y el controller en un 503 (Service Unavailable).
public class PlantUnavailableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PlantUnavailableException(String message) {
		super(message);
	}

	public PlantUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
