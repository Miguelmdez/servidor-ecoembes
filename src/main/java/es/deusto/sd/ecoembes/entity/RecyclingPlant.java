package es.deusto.sd.ecoembes.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

// Entidad de dominio y de persistencia: planta de reciclaje. Añadir una planta nueva solo requiere datos, no código.
// Además de su identidad guarda cómo comunicarse con ella: protocolo (gatewayType), host y puerto.
@Entity
public class RecyclingPlant {
	@Id
	private String id;

	@Column(nullable = false)
	private String name;

	// Protocolo de comunicación con la planta (REST o SOCKET)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GatewayType gatewayType;

	@Column(nullable = false)
	private String host;

	@Column(nullable = false)
	private int port;

	// Constructor sin parámetros
	public RecyclingPlant() { }

	// Constructor con parámetros
	public RecyclingPlant(String id, String name, GatewayType gatewayType, String host, int port) {
		this.id = id;
		this.name = name;
		this.gatewayType = gatewayType;
		this.host = host;
		this.port = port;
	}

	// Getters y setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GatewayType getGatewayType() {
		return gatewayType;
	}

	public void setGatewayType(GatewayType gatewayType) {
		this.gatewayType = gatewayType;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	// hashCode y equals (por id)
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecyclingPlant other = (RecyclingPlant) obj;
		return Objects.equals(id, other.id);
	}
}
