package es.deusto.sd.ecoembes;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;

// Configuración de Swagger UI. Swagger UI no envía los parámetros de cabecera llamados "Authorization"
// (los reserva para su botón Authorize), así que el token se declara como esquema de seguridad: se
// introduce una vez con Authorize y Swagger lo manda en la cabecera Authorization de los endpoints
// marcados con @SecurityRequirement(name = "tokenAuth").
@Configuration
@SecurityScheme(
	name = "tokenAuth",
	type = SecuritySchemeType.APIKEY,
	in = SecuritySchemeIn.HEADER,
	paramName = "Authorization",
	description = "Pega aquí el token devuelto por /auth/login"
)
public class OpenApiConfig {
}
