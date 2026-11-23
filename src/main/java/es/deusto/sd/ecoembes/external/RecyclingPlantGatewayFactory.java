package es.deusto.sd.ecoembes.external;

import org.springframework.stereotype.Component;

import es.deusto.sd.ecoembes.entity.RecyclingPlant;

// Patrón Factory: decide QUÉ gateway usar para cada planta según su gatewayType (dato guardado en la base de datos).
// Así los services no tienen ningún "if" por planta y el protocolo de cada planta queda aislado en su gateway.
// Añadir una planta que use un protocolo ya existente es solo añadir datos; un protocolo nuevo requiere un valor
// en GatewayType, un gateway nuevo y una línea en este switch. Los gateways no guardan estado: se crean al pedirlos.
@Component
public class RecyclingPlantGatewayFactory {

	public IRecyclingPlantGateway getGateway(RecyclingPlant plant) {
		return switch (plant.getGatewayType()) {
			case REST -> new PlasSBGateway(plant.getHost(), plant.getPort());
			case SOCKET -> new ContSocketGateway(plant.getHost(), plant.getPort());
		};
	}
}
