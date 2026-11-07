package es.deusto.sd.ecoembes;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;

// Swagger UI reserves the "Authorization" header for its Authorize button
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
