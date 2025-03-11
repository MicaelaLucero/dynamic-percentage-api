# Dynamic Percentage API

## Descripción

**Dynamic Percentage API** es un servicio REST que permite realizar cálculos con un porcentaje dinámico configurable. La API incluye funcionalidades como:

- Cálculo de sumas con un porcentaje variable.
- Almacenamiento en caché con Redis.
- Lógica de reintentos en llamadas a APIs externas.
- Registro de llamadas a la API con historial paginado.
- Rate limiting con Bucket4j.
- Simulación de API externa con WireMock.
- Uso de Nginx como balanceador de carga.

---

## Funcionalidades Implementadas

### 1. Cálculo con porcentaje dinámico
- Implementa un endpoint REST que recibe dos números (`firstNumber` y `secondNumber`).
- Suma ambos números y aplica un porcentaje adicional obtenido de un servicio externo.
- El servicio externo es un mock (simulado con WireMock) que retorna un valor de porcentaje.
- Si el servicio externo retorna un 10% y los números son `firstNumber=5` y `secondNumber=5`, el cálculo se realiza de la siguiente manera:

  ```
  (5 + 5) + (10% de 10) = 11
  ```
- Se registran logs en cada paso del cálculo para facilitar la depuración y monitoreo.

### 2. Caché del porcentaje
- El porcentaje obtenido del servicio externo se almacena en caché usando **Redis** y tiene una validez de **30 minutos**.
- Si el servicio externo falla, la API intenta obtener el último valor almacenado en caché.
- Se implementa manejo de errores con `CacheException` si ocurre algún problema al recuperar o guardar el porcentaje en Redis.

### 3. Reintentos ante fallos del servicio externo
- Si la llamada al servicio externo falla, se utiliza **Spring Retry** para reintentar la solicitud hasta **3 veces** antes de devolver un error.
- Cada intento fallido se registra en los logs con detalles del error recibido.
- Si el servicio externo sigue fallando después de los 3 intentos:
    - Se intenta usar el valor en caché.
    - Si tampoco hay un valor en caché, se lanza una excepción `PercentageUnavailableException` con un mensaje claro para el cliente.

### 4. Historial de llamadas
- Se implementa un endpoint para consultar un historial de todas las llamadas realizadas a la API.
- Cada llamada se almacena con los siguientes datos:
    - **Fecha y hora de la llamada.**
    - **Endpoint invocado.**
    - **Parámetros recibidos.**
    - **Código de estado HTTP retornado.**
    - **Cuerpo de la solicitud y respuesta.**
- El historial soporta **paginación** para facilitar la consulta de registros.
- El almacenamiento de estos registros se realiza de manera **asíncrona** para no afectar el rendimiento del servicio principal.
- Si el registro de una llamada falla, esto **no impacta la ejecución del endpoint** invocado, garantizando disponibilidad del servicio.

### 5. Control de tasas (Rate Limiting)
- Se implementa **rate limiting** con **Bucket4j** para restringir el número de peticiones.
- La API permite un máximo de **3 solicitudes por minuto (3 RPM)** por usuario.
- Si se excede este umbral, la API devuelve un error `429 Too Many Requests` junto con un encabezado `Retry-After` indicando cuántos segundos deben esperar antes de realizar una nueva solicitud.
- Se implementa una excepción personalizada `RateLimitException` para manejar este caso.

### 6. Manejo de errores HTTP
- Se implementa un **Global Exception Handler** para manejar errores de manera centralizada.
- Se manejan los siguientes errores con respuestas adecuadas:
    - `ExternalApiException` → `503 Service Unavailable` cuando el servicio externo no responde correctamente.
    - `InvalidResponseException` → `500 Internal Server Error` si la respuesta del servicio externo tiene un formato inesperado.
    - `InvalidPercentageException` → `400 Bad Request` si se intenta actualizar un porcentaje inválido (por ejemplo, negativo).
    - `CacheException` → `500 Internal Server Error` si ocurre un fallo en Redis.
    - `DatabaseException` → `500 Internal Server Error` si hay problemas en la base de datos.
    - `PercentageUnavailableException` → `503 Service Unavailable` si no se puede obtener un porcentaje ni del servicio externo ni de la caché.
    - `RateLimitException` → `429 Too Many Requests` si se excede el límite de peticiones.
    - `MethodArgumentNotValidException` → `400 Bad Request` cuando hay errores de validación en los parámetros enviados por el cliente.
    - `Exception` genérica → `500 Internal Server Error` para manejar errores no previstos.

---

## Requisitos Previos

Para ejecutar la API localmente, necesitas:

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Postman](https://www.postman.com/) (opcional para probar la API)

---

## Instalación y Ejecución

### 1. Clonar el Repositorio
```bash
git clone https://github.com/tu-usuario/dynamic-percentage-api.git
cd dynamic-percentage-api
```

### 2. Construir y Levantar los Servicios con Docker Compose

 La API utiliza la imagen publicada en Docker Hub, por lo que no es necesario construir la imagen localmente, solo ejecutar:
```bash
docker-compose up
```
Esto descargará la imagen de Docker Hub y levantará los servicios necesarios (PostgreSQL, Redis, WireMock, Nginx y la API).

Para detener los contenedores, usa:
```bash
docker-compose down
```

### Construir y Levantar los Servicios Localmente

Para ejecutar la API sin Docker Compose, sigue estos pasos:

1. **Conectarse a PostgreSQL**
  - Asegúrate de que PostgreSQL esté corriendo en el puerto **5432** con las credenciales adecuadas.
  - Crea la base de datos tenpo_db. Puedes hacerlo con los siguientes comandos:
    ```bash
    psql -h localhost -U {tu_user} -d {tu_password}
    ```
    ```bash
    CREATE DATABASE tenpo_db;
    ```
    ```bash
    \q
    ```

2. **Ejecutar Redis**
  - Inicia Redis en el puerto **6379**. Puedes usar el siguiente comando:
    ```bash
    redis-server
    ```

3. **Levantar WireMock**
  - Para simular la API externa con WireMock, ejecuta el siguiente comando en la raíz del proyecto:
    ```bash
    docker run --rm -p 8081:8080 -v $(pwd)/wiremock-config:/home/wiremock wiremock/wiremock:latest \
      --global-response-templating --verbose --root-dir /home/wiremock
    ```
  - Esto iniciará WireMock en el puerto **8081**, utilizando el directorio `wiremock-config` para almacenar las configuraciones de respuestas simuladas.

4. **Ejecutar la API**
  - Una vez que PostgreSQL, Redis y WireMock estén en funcionamiento, ejecuta la aplicación Spring Boot:
    ```bash
    mvn spring-boot:run
    ```

Con estos pasos, la API estará en ejecución y lista para recibir solicitudes. 🚀

### 3. Probar la API
Puedes usar la colección de Postman para probar los endpoints. Descarga el archivo desde la raíz del proyecto e importa en Postman: `dynamic_api.postman_collection.json`

También puedes probar manualmente usando `curl`:

#### Realizar un cálculo
```bash
curl -X POST http://localhost/api/v1/calculations \
     -H "Content-Type: application/json" \
     -d '{"firstNumber": 100.00, "secondNumber": 50.00}'
```
**Respuesta esperada:**
```json
{
  "firstNumber": 100.0,
  "secondNumber": 50.0,
  "percentage": 10.0,
  "result": 165.0
}
```

#### Consultar el historial de llamadas
```bash
curl -X GET http://localhost/api/v1/history
```
**Respuesta esperada:**
```json
{
  "currentPage": 0,
  "totalPages": 1,
  "totalElements": 1,
  "data": [
    {
      "method": "PUT",
      "endpoint": "/api/v1/percentage?newPercentage=30",
      "statusCode": 200,
      "requestBody": "",
      "responseBody": "{\"newPercentage\":30.0}",
      "timestamp": "2025-03-11T04:04:29.901309"
    }
  ]
}
```
---

#### Obtener el porcentaje actual
```bash
curl -X GET http://localhost/api/v1/percentage
```
**Respuesta esperada:**
```json
{
  "percentage": 10.0,
  "source": "EXTERNAL_API"
}
```

#### Actualizar el porcentaje
```bash
curl -X PUT "http://localhost/api/v1/percentage?newPercentage=20"
```
**Respuesta esperada:**
```json
{
  "newPercentage": 20.0
}
```

## Endpoints Disponibles

| Método | Endpoint             | Descripción                                               |
|--------|----------------------|-----------------------------------------------------------|
| POST   | `/api/v1/calculations` | Realiza la suma de dos números y un porcentaje adicional. |
| GET    | `/api/v1/percentage`   | Obtiene el porcentaje actual.                             |
| PUT    | `/api/v1/percentage`   | Actualiza el porcentaje.                                  |
| GET    | `/api/v1/history`      | Consulta el historial de llamadas.                        |

---

## Configuración

Las variables de entorno están definidas en `application.yml`. Se pueden modificar según sea necesario.

### Configuración de la Base de Datos
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/tenpo_db
    username: tenpo_user
    password: tenpo_password
```

### Configuración de Redis
```yaml
spring:
  redis:
    host: redis
    port: 6379
```

### Configuración de WireMock (API simulada)
```yaml
external:
  api:
    wiremockUrl: http://nginx/external-percentage
    wiremockAdminUrl: http://nginx/__admin
```

---

## Arquitectura

El proyecto sigue una arquitectura basada en:

- **Spring Boot** para la lógica de negocio.
- **PostgreSQL** para persistencia de datos.
- **Redis** como caché distribuida.
- **WireMock** para simular API externas.
- **Nginx** como balanceador de carga.
- **Docker** para contenerización y despliegue.

---

## Calidad del Código

El proyecto incluye herramientas para garantizar la calidad del código:

- **JaCoCo**: Generación de reportes de cobertura de código.
- **Checkstyle**: Validación de estilo de código.
- **Maven Surefire Plugin**: Ejecución de pruebas unitarias.

### Ejecutar Pruebas y Reportes
```bash
mvn clean test
mvn jacoco:report
mvn checkstyle:check
```
---

## 📌 Análisis Técnico y Justificación de Decisiones

Durante el desarrollo de este proyecto, tomé decisiones técnicas enfocadas en mantenibilidad, escalabilidad y buenas prácticas de desarrollo. A continuación, detallo los aspectos más relevantes y el razonamiento detrás de cada uno.

### 1️⃣ Arquitectura y Organización del Código
Opté por una arquitectura modular y limpia inspirada en **Hexagonal Architecture**, lo que facilita la separación de responsabilidades y mejora la mantenibilidad.

📂 **Estructura de paquetes:**
- `controller/`: Expone los endpoints de la API.
- `service/`: Contiene la lógica de negocio.
- `repository/`: Interactúa con la base de datos y caché.
- `model/`: Contiene DTOs, entidades y mappers para transformación de datos.
- `config/`: Configuración de Redis, seguridad, etc.
- `client/`: Contiene la integración con la API externa.
- `exception/`: Manejo centralizado de errores y excepciones personalizadas.
- `utils/`: Contiene constantes y utilidades comunes para la aplicación.

✔️ **Decisión clave:** Organizar el código en capas bien definidas para mejorar la modularidad y facilitar futuras ampliaciones.

### 2️⃣ Uso de Redis para Caché Distribuida
Decidí utilizar **Redis** para manejar la caché de los valores de porcentaje en lugar de almacenar los datos en memoria con `@Cacheable`.

- Implementé un repositorio específico (`PercentageCacheRepositoryImpl`) para manejar manualmente la persistencia en Redis con `RedisTemplate`.
- Se configuró Redis con un tiempo de expiración configurable para evitar datos obsoletos.

✔️ **Decisión clave:** Redis permite que múltiples réplicas de la aplicación compartan la misma caché, evitando cálculos innecesarios y mejorando la eficiencia.

### 3️⃣ Implementación de un Filter para Guardar el Historial de Llamadas
Para registrar cada request realizado a la API, implementé un **filtro de Spring** (`ApiCallLoggingFilter`) que intercepta las solicitudes y almacena la información en la base de datos.

- Captura el método HTTP, endpoint, request body, response body y status code de cada solicitud.
- Permite futuras auditorías y análisis del uso de la API.

✔️ **Decisión clave:** Utilizar un Filter en lugar de manejar esto en cada controller asegura separación de preocupaciones y evita código repetitivo.

### 4️⃣ Uso de Interfaces para Mayor Abstracción
Para mejorar la extensibilidad y testabilidad, definí **interfaces** en los servicios y repositorios:

- `PercentageCacheRepository` → Implementación concreta: `PercentageCacheRepositoryImpl`
- `ExternalApiClient` → Implementación concreta: `ExternalApiClientImpl`
- `ApiCallHistoryService` → Implementación concreta: `ApiCallHistoryServiceImpl`

✔️ **Decisión clave:** El uso de interfaces permite código desacoplado, facilita pruebas unitarias y permite cambios en la implementación sin modificar otras capas.

### 5️⃣ Uso de DTOs, Entities y Mappers
Para evitar exponer directamente las entidades de la base de datos en los endpoints, implementé:

- **DTOs (Data Transfer Objects):** `CalculationRequest`, `CalculationResponse`, `ApiCallHistoryResponse`, etc.
- **Entities:** `ApiCallHistoryEntity`, que representa la tabla en la base de datos.
- **Mappers:** `ApiCallHistoryMapper`, `CalculationMapper`, que convierten entre entidades y DTOs.

✔️ **Decisión clave:** Mantener las entidades encapsuladas evita problemas de seguridad y optimiza el modelo de datos.
✔️ Separar DTOs y Entities mejora la compatibilidad con futuras versiones sin afectar la base de datos.

### 6️⃣ Uso de Lombok
Para evitar código repetitivo en DTOs, Entities y Services, utilicé **Lombok** con anotaciones como:

- `@Getter / @Setter` → Evita escribir getters y setters manualmente.
- `@AllArgsConstructor / @NoArgsConstructor` → Evita definir constructores manualmente.
- `@Builder` → Facilita la creación de objetos con una sintaxis más clara.

✔️ **Decisión clave:** Lombok reduce significativamente la cantidad de código repetitivo, haciendo el código más limpio y fácil de mantener.

### 7️⃣ Implementación de Rate Limiting con Bucket4j
Para proteger la API de un uso excesivo, decidí utilizar **Bucket4j** ya que tiene buena integración con Spring.

- Configuré un interceptor global en lugar de aplicar la lógica en cada endpoint.
- Se devuelve un HTTP `429 Too Many Requests` con el tiempo de espera antes de un nuevo intento.

✔️ **Decisión clave:** Prevenir abusos y mejorar la disponibilidad de la API sin necesidad de infraestructura compleja como API Gateways.

### 8️⃣ Implementación de Endpoints Adicionales
El challenge solo pedía la suma con porcentaje dinámico, pero decidí agregar los siguientes endpoints:

- `GET /percentage` → Obtiene el porcentaje actual desde la API externa o desde la caché en Redis.
    - Incluye un campo `source` que indica de dónde proviene el dato (`CACHE` o `EXTERNAL_API`).
- `PUT /percentage` → Permite actualizar manualmente el porcentaje almacenado en la caché.

✔️ **Decisión clave:** Facilita la gestión del porcentaje actual, proporcionando información clara sobre su origen y permitiendo ajustes.

### 9️⃣ Manejo de Constantes en una Clase Separada
Para evitar valores **hardcodeados** en el código, definí una clase `Constants` en el paquete `utils`, donde centralicé valores como:

- `CACHE_KEY`
- `CACHE_EXPIRATION_MINUTES`

✔️ **Decisión clave:** Mejor organización y fácil modificación de valores sin tocar múltiples archivos.

### 🔟 Uso de WireMock para Mockear APIs Externas
En lugar de depender de una API real para las pruebas, utilicé **WireMock** como mock server.

- Permite simular respuestas controladas de la API externa.
- Se usó tanto en tests de integración como en Docker Compose.
- Se configuró para responder con diferentes códigos de estado y tiempos de respuesta.

✔️ **Decisión clave:** WireMock agiliza el desarrollo y evita fallos en tests debido a problemas con servicios externos.

### 🔢 Verificación de Calidad con JaCoCo y Checkstyle
Para garantizar la calidad del código y la cobertura de pruebas:

- **JaCoCo:** Mide la cobertura de pruebas, asegurando que la lógica crítica esté testeada.
- **Checkstyle:** Analiza la calidad del código, detectando errores y malas prácticas.

✔️ **Decisión clave:** Estas herramientas garantizan un código más robusto y facilitan la detección de problemas antes de la entrega.

### 🏁 Conclusión
A lo largo del desarrollo tomé decisiones con el objetivo de construir una API bien estructurada, modular y fácil de mantener.

- ✔️ **Arquitectura clara y organizada** → Uso de interfaces, DTOs, entidades y mappers.
- ✔️ **Eficiencia y rendimiento** → Redis para caché distribuida, rate limiting con Bucket4j.
- ✔️ **Pruebas robustas** → WireMock, JaCoCo y análisis estático de código.
- ✔️ **Flexibilidad y escalabilidad** → Endpoints adicionales y separación de responsabilidades.


## Autor

Desarrollado por **Micaela Lucero**.
Cualquier sugerencia o mejora es bienvenida. 🚀
