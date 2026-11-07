# Especificación del API REST del Servidor Ecoembes

## 1. URL base
https://servidor-ecoembes/api/v1

## 2. Descripción general
La API de Ecoembes permite gestionar la información de los contenedores de reciclaje sensorizados RecycloBin UltraEye y consultar la capacidad disponible de las plantas de reciclaje.

Los sensores transmiten diariamente información al Servidor Ecoembes, incluyendo el identificador del contenedor, el número estimado de envases y el nivel de llenado.

El personal de Ecoembes puede utilizar la API para gestionar los contenedores, consultar su estado, comprobar la capacidad disponible de las plantas y realizar asignaciones de contenedores a dichas plantas.

La API requiere autenticación mediante correo electrónico y contraseña. Cuando una persona inicia sesión correctamente, el servidor genera un token que permite identificarla durante su sesión.

## 3. Operaciones de la API

### 3.1. Autenticación

#### 3.1.1. Iniciar sesión
- Método: POST
- Endpoint: /auth/login
- Descripción: Permite al personal de Ecoembes iniciar sesión en el sistema mediante su correo electrónico y contraseña. Si las credenciales son correctas, el servidor genera un token utilizando la marca de tiempo del servidor y registra la sesión como activa.
- Parámetros de entrada:
  - email: correo electrónico de la persona.
  - password: contraseña de acceso.
- Respuestas HTTP:
  - 200 OK: Inicio de sesión correcto. Devuelve el token generado.
  - 401 Unauthorized: Correo electrónico o contraseña incorrectos.
  - 400 Bad Request: Faltan datos obligatorios o tienen un formato incorrecto.

#### 3.1.2. Cerrar sesión
- Método: POST
- Endpoint: /auth/logout
- Descripción: Permite al personal de Ecoembes cerrar su sesión. El servidor elimina el token de la lista de tokens activos y borra los datos asociados a la persona autenticada.
- Parámetros de entrada:
  - token: token de la sesión activa.
- Respuestas HTTP:
  - 200 OK: Sesión cerrada correctamente.
  - 401 Unauthorized: Token no válido o sesión no activa.
  - 400 Bad Request: Falta el token.

### 3.2. Gestión de contenedores de reciclaje

#### 3.2.1. Crear un nuevo contenedor
- Método: POST
- Endpoint: /containers
- Descripción: Permite al personal de Ecoembes registrar un nuevo contenedor de reciclaje en el Servidor Ecoembes. Se almacena su identificador, ubicación y capacidad inicial.
- Parámetros de entrada:
  - containerId: identificador del contenedor.
  - location: ubicación del contenedor.
  - postalCode: código postal donde está situado el contenedor.
  - initialCapacity: capacidad inicial del contenedor.
- Respuestas HTTP:
  - 201 Created: Contenedor creado correctamente.
  - 400 Bad Request: Datos de entrada incorrectos.
  - 401 Unauthorized: Usuario no autenticado.
  - 409 Conflict: Ya existe un contenedor con ese identificador.

#### 3.2.2. Recibir información diaria del sensor
- Método: POST
- Endpoint: /containers/{containerId}/readings
- Descripción: Permite recibir y almacenar la información transmitida diariamente por el sensor RecycloBin UltraEye. En el prototipo, esta transmisión se simula a las 3:00. La información recibida incluye el número estimado de envases y el nivel de llenado del contenedor.
- Parámetros de entrada:
  - containerId: identificador del contenedor.
  - date: fecha de la lectura.
  - estimatedPackages: número estimado de envases.
  - fillLevel: nivel de llenado del contenedor.
- El nivel de llenado se clasifica en tres estados:
  - GREEN: hay espacio disponible.
  - ORANGE: espacio limitado.
  - RED: contenedor lleno.
- Respuestas HTTP:
  - 201 Created: Lectura recibida y almacenada correctamente.
  - 400 Bad Request: Datos de la lectura incorrectos.
  - 404 Not Found: No existe un contenedor con ese identificador.

#### 3.2.3. Consultar el uso y estado de un contenedor por fecha
- Método: GET
- Endpoint: /containers/{containerId}/readings?startDate={startDate}&endDate={endDate}
- Descripción: Permite consultar el uso y el estado de un contenedor durante un periodo de tiempo determinado, utilizando las lecturas almacenadas en el Servidor Ecoembes.
- Parámetros de entrada:
  - containerId: identificador del contenedor.
  - startDate: fecha de inicio del periodo de consulta.
  - endDate: fecha de finalización del periodo de consulta.
- Respuestas HTTP:
  - 200 OK: Lecturas del contenedor devueltas correctamente.
  - 400 Bad Request: Fechas incorrectas o periodo no válido.
  - 404 Not Found: No existe el contenedor solicitado.

#### 3.2.4. Consultar el estado de los contenedores de una zona
- Método: GET
- Endpoint: /containers/readings?postalCode={postalCode}&date={date}
- Descripción: Permite consultar el estado de los contenedores de reciclaje situados en una zona determinada, utilizando su código postal y una fecha concreta.
- Parámetros de entrada:
  - postalCode: código postal de la zona.
  - date: fecha para la que se desea consultar el estado.
- Respuestas HTTP:
  - 200 OK: Lista de contenedores y sus estados devuelta correctamente.
  - 400 Bad Request: Parámetros incorrectos.
  - 404 Not Found: No se encuentran contenedores para esa zona y fecha.

### 3.3. Consulta de plantas de reciclaje

#### 3.3.1. Consultar la capacidad disponible de las plantas
- Método: GET
- Endpoint: /plants/capacity/date/{date}
- Descripción: Permite consultar la capacidad disponible de las plantas de reciclaje para una fecha determinada. Las plantas comunican su capacidad diaria en toneladas con una antelación de hasta 10 días. Inicialmente, el sistema incluye las plantas PlasSB Ltd. y ContSocket Ltd., pero debe permitir incorporar nuevas plantas dinámicamente.
- Parámetros de entrada:
  - date: fecha para la que se desea consultar la capacidad disponible.
- Respuestas HTTP:
  - 200 OK: Capacidades disponibles devueltas correctamente.
  - 400 Bad Request: Fecha incorrecta o no válida.
  - 404 Not Found: No hay información de capacidad disponible para esa fecha.

### 3.4. Asignación de contenedores a plantas de reciclaje

#### 3.4.1. Asignar contenedores a una planta
- Método: POST
- Endpoint: /assignments
- Descripción: Permite al personal de Ecoembes asignar uno o varios contenedores a una planta de reciclaje, teniendo en cuenta el número estimado de envases y la capacidad disponible de la planta. Una vez realizada la asignación, la planta recibe una notificación con el número total de contenedores asignados y el número estimado de envases que recibirá. Además, el sistema registra la identidad de la persona que realiza la asignación para mantener un registro de auditoría.
- Parámetros de entrada:
  - plantId: identificador de la planta de reciclaje.
  - containerIds: lista de identificadores de los contenedores asignados.
  - date: fecha prevista para la asignación.
  - userToken: token de la persona que realiza la asignación.
- Respuestas HTTP:
  - 201 Created: Asignación realizada correctamente.
  - 400 Bad Request: Datos de entrada incorrectos.
  - 401 Unauthorized: Usuario no autenticado.
  - 404 Not Found: No existe la planta o alguno de los contenedores indicados.
  - 409 Conflict: La capacidad disponible de la planta no permite realizar la asignación.

#### 3.4.2. Consultar asignaciones realizadas
- Método: GET
- Endpoint: /assignments
- Descripción: Permite consultar las asignaciones de contenedores a plantas de reciclaje que se han realizado en el sistema.
- Parámetros de entrada:
  - date: fecha de las asignaciones que se desean consultar.
  - plantId: identificador de la planta.
- Respuestas HTTP:
  - 200 OK: Asignaciones devueltas correctamente.
  - 400 Bad Request: Parámetros incorrectos.
  - 404 Not Found: No se encuentran asignaciones para los criterios indicados.

## 4. Esquemas de datos (Data Schemas)
Los siguientes esquemas representan la estructura de los datos intercambiados entre el cliente y el Servidor Ecoembes.

### 4.1. CredentialsDTO
Representa las credenciales necesarias para iniciar sesión.
- email: String
- password: String

### 4.2. LoginResponseDTO
Representa la respuesta del servidor tras un inicio de sesión correcto.
- token: String
- timestamp: Long

### 4.3. ContainerDTO
Representa la información básica de un contenedor de reciclaje.
- containerId: String
- location: String
- postalCode: String
- initialCapacity: Float

### 4.4. ContainerReadingDTO
Representa la información diaria transmitida por el sensor RecycloBin UltraEye.
- containerId: String
- date: Date
- estimatedPackages: Integer
- fillLevel: String (GREEN, ORANGE, RED)

### 4.5. PlantCapacityDTO
Representa la capacidad disponible de una planta de reciclaje para una fecha determinada.
- plantId: String
- plantName: String
- date: Date
- capacity: Float (toneladas)

### 4.6. AssignmentDTO
Representa una asignación de contenedores a una planta de reciclaje.
- assignmentId: String
- plantId: String
- containerIds: List<String>
- date: Date
- totalContainers: Integer
- estimatedPackages: Integer
- assignedBy: String

## 5. Reglas básicas del sistema
1. Los sensores RecycloBin UltraEye transmiten información diariamente. En el prototipo, la transmisión se simula a las 3:00.
2. Cada lectura incluye el identificador del contenedor, el número estimado de envases y el nivel de llenado.
3. El nivel de llenado puede ser verde (hay espacio disponible), naranja (espacio limitado) o rojo (contenedor lleno).
4. Todas las lecturas recibidas se almacenan en la base de datos del Servidor Ecoembes.
5. Las plantas de reciclaje comunican su capacidad diaria en toneladas con hasta 10 días de antelación.
6. El sistema debe permitir incorporar nuevas plantas de reciclaje sin modificar la estructura de la API.
7. Antes de realizar una asignación, el sistema debe comprobar que la capacidad disponible de la planta es suficiente.
8. Cada asignación debe registrar la identidad de la persona del personal de Ecoembes que la realiza.
9. Tras una asignación, la planta correspondiente debe recibir una notificación con el número total de contenedores y el número estimado de envases.

## 6. Decisiones de diseño
a) **Base URL**: `/api/v1`, configurada con `server.servlet.context-path=/api/v1` (puerto 8081).
b) **POST /containers**: el token de sesión se envía en la cabecera `Authorization`.
c) **POST /assignments**: el body es `AssignmentRequestDTO {plantId, containerIds, date, userToken}`.
d) Los endpoints cuya especificación no incluye 401 no piden token.
e) **Toneladas** = sumaEnvases * `KG_PER_PACKAGE` (0.03) / 1000.
f) **Fechas** como `LocalDate` ISO (`yyyy-MM-dd`); `fillLevel` como enum `FillLevel` (`"GREEN"`/`"ORANGE"`/`"RED"`).
g) `initialCapacity` se expresa en número de envases.
h) `POST /containers/{containerId}/readings`: si el `containerId` del body no coincide con el de la ruta, se devuelve 400.
i) **Rangos del simulador** (envases generados como % de `initialCapacity`):

| fillLevel | Rango |
|---|---|
| GREEN | [0 %, 40 %) |
| ORANGE | [40 %, 80 %) |
| RED | [80 %, 100 %] |

Los tres rangos son contiguos (cada uno empieza donde acaba el anterior) y cubren del 0 al 100 % sin huecos. El número de envases se obtiene truncando a entero.

j) En Swagger UI el token se introduce con el botón **Authorize** (esquema `tokenAuth`, cabecera `Authorization`), porque Swagger UI no envía parámetros de cabecera llamados `Authorization`. Los endpoints que leen esa cabecera (`POST /containers`) llevan `@SecurityRequirement(name = "tokenAuth")`.
