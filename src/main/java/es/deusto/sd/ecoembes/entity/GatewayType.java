package es.deusto.sd.ecoembes.entity;

// Protocolo con el que se habla con una planta de reciclaje; lo usa RecyclingPlantGatewayFactory para elegir el gateway.
public enum GatewayType {
	REST, SOCKET
}
