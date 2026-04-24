# Smart Campus Sensor & Room Management API
### 5COSC022W Client-Server Architectures — Coursework 2025/26
**University of Westminster**
- **Name: Chameen Wimalasena**
- **ID: W2121297 / 20241093**
---

## Overview

This project is a fully-featured RESTful API built with **JAX-RS (Jersey 2.32)** and deployed on **Apache Tomcat 9**, implementing a Smart Campus infrastructure management system. The API exposes endpoints for managing physical **Rooms** and the **Sensors** deployed within them, including a full historical **Sensor Readings** log. All data is stored in-memory using thread-safe `ConcurrentHashMap` and `ArrayList` data structures — no database is used.

The implementation covers:
- Versioned API entry point with a HATEOAS-style discovery endpoint
- Full CRUD for Rooms and Sensors with referential integrity enforcement
- Sub-resource locator pattern for nested Sensor Readings
- Four custom JAX-RS `ExceptionMapper` implementations (409, 422, 403, 500)
- A global catch-all safety net mapper to prevent stack trace exposure
- A `ContainerRequestFilter` / `ContainerResponseFilter` logging implementation

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17 (LTS) |
| Framework | JAX-RS via Jersey 2.32 |
| Server | Apache Tomcat 9.0.100 |
| Build Tool | Apache Maven |
| JSON | Jackson (jersey-media-json-jackson) |
| IDE | Apache NetBeans 24 |

---

## Project Structure

```
SmartCampusAPI/
├── pom.xml
├── README.md
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           ├── dao/
│       │           │   └── MockDatabase.java
│       │           ├── exception/
│       │           │   ├── GlobalExceptionMapper.java
│       │           │   ├── LinkedResourceNotFoundException.java
│       │           │   ├── LinkedResourceNotFoundExceptionMapper.java
│       │           │   ├── RoomNotEmptyException.java
│       │           │   ├── RoomNotEmptyExceptionMapper.java
│       │           │   ├── SensorUnavailableException.java
│       │           │   └── SensorUnavailableExceptionMapper.java
│       │           ├── filter/
│       │           │   └── LoggingFilter.java
│       │           ├── model/
│       │           │   ├── ErrorMessage.java
│       │           │   ├── Room.java
│       │           │   ├── Sensor.java
│       │           │   └── SensorReading.java
│       │           └── resource/
│       │               ├── DiscoveryResource.java
│       │               ├── RoomResource.java
│       │               ├── SensorReadingResource.java
│       │               └── SensorResource.java
│       ├── resources/
│       │   └── META-INF/
│       │       └── persistence.xml
│       └── webapp/
│           ├── META-INF/
│           │   └── context.xml
│           ├── WEB-INF/
│           │   ├── beans.xml
│           │   └── web.xml
│           └── index.html
└── nb-configuration.xml                      
```

---

## Build & Run Instructions

### Prerequisites
- Apache NetBeans 24 (with JDK 17)
- Apache Tomcat Apache Tomcat 9.0.100 added to NetBeans Services tab
- Internet connection (for Maven to download dependencies on first build)

### Step 1 — Clone the repository
```bash
git clone https://github.com/chameenamanjana/SmartCampusAPI.git
```

### Step 2 — Open in NetBeans
1. Open **Apache NetBeans**
2. Go to **File → Open Project**
3. Navigate to the cloned folder and select it
4. Click **Open Project**

### Step 3 — Add Apache Tomcat (if not already added)
1. Go to the **Services** tab
2. Right-click **Servers → Add Server**
3. Select **Apache Tomcat or TomEE**, click **Next**
4. Browse to your extracted Tomcat 9 folder, enter credentials, click **Finish**

### Step 4 — Build
Right-click the project root → **Clean and Build**. Wait for `BUILD SUCCESS` in the output window.

### Step 5 — Run
Right-click the project root → **Run**. The server starts and deploys the WAR automatically.

### Step 6 — Verify
Open a browser or Postman and navigate to:
```
http://localhost:8080/SmartCampusAPI/api/v1/
```
You should receive a JSON discovery response confirming the API is live.

---

## API Base URL

```
http://localhost:8080/SmartCampusAPI/api/v1
```

---

## API Endpoints Reference

### Part 1 — Discovery
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/` | Returns API metadata, version, and resource map |

### Part 2 — Rooms
| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/rooms` | List all rooms | 200 |
| POST | `/api/v1/rooms` | Create a new room | 201 Created + Location header |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room by ID | 200 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors present) | 204 |

### Part 3 — Sensors
| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/sensors` | List all sensors (optional `?type=` filter) | 200 |
| POST | `/api/v1/sensors` | Register a new sensor (validates roomId) | 201 Created |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor | 200 |

### Part 4 — Sensor Readings (Sub-Resource)
| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get full reading history | 200 |
| POST | `/api/v1/sensors/{sensorId}/readings` | Post a new reading (updates parent sensor) | 201 |

### Part 5 — Error Responses
| Scenario | HTTP Status | Exception Class |
|---|---|---|
| Delete room with active sensors | 409 Conflict | `RoomNotEmptyException` |
| POST sensor with non-existent roomId | 422 Unprocessable Entity | `LinkedResourceNotFoundException` |
| POST reading to MAINTENANCE sensor | 403 Forbidden | `SensorUnavailableException` |
| Any unexpected runtime error | 500 Internal Server Error | `GlobalExceptionMapper<Throwable>` |
| GET non-existent resource | 404 Not Found | JAX-RS `NotFoundException` (via GlobalMapper) |

---

## Seed Data (Pre-loaded on startup)

**Rooms:**
- `LIB-301` — Library Quiet Study (capacity: 50)
- `LAB-101` — Computer Lab (capacity: 30)
- `HALL-A` — Main Hall (capacity: 200)

**Sensors:**
- `TEMP-001` — Temperature, ACTIVE, in LIB-301
- `CO2-001` — CO2, ACTIVE, in LAB-101
- `OCC-001` — Occupancy, **MAINTENANCE**, in HALL-A

---

## Sample curl Commands

```bash
# 1. Discovery endpoint
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/

# 2. List all rooms
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms

# 3. Create a new room (POST)
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CS-101","name":"CS Lab","capacity":40}'

# 4. Filter sensors by type
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"

# 5. Post a sensor reading (updates parent sensor's currentValue)
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":26.3}'

# 6. Trigger 409 Conflict — attempt to delete a room that has sensors
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301

# 7. Trigger 422 Unprocessable Entity — sensor referencing non-existent room
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"BAD-001","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE-999"}'

# 8. Trigger 403 Forbidden — post reading to a MAINTENANCE sensor
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":15.0}'

# 9. Trigger 500 Internal Server Error — global safety net demonstration
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/?triggerError=true"

# 10. Delete a room with no sensors
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/CS-101
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/CS-101
```
---
## Report

- Answers to the conceptual questions posed in each section of the coursework specification.

---

### Part 1.1 — JAX-RS Resource Lifecycle & Thread Safety

By default, JAX-RS creates a **new instance of each resource class for every incoming HTTP request**. This is the request-scoped lifecycle. It means that any instance fields declared on a resource class (such as a counter or a list) would be reset to their default values on every request and could never be shared between callers.

This architectural decision has a direct and critical impact on how in-memory data must be managed. In this implementation, all persistent state lives in static fields inside `MockDatabase`, which is initialised once via a `static {}` block when the class is first loaded by the JVM. The data exists completely independently of the resource class lifecycle — it is never lost when JAX-RS destroys and recreates resource instances between requests.

However, because multiple requests can arrive simultaneously, two threads could attempt to write to the same data structure at the same moment. A standard `HashMap` is not thread-safe; concurrent writes can corrupt its internal structure and cause silent data loss or a `ConcurrentModificationException`. To prevent this, `ConcurrentHashMap` is used for all three data stores (`ROOMS`, `SENSORS`, `SENSOR_READINGS`). `ConcurrentHashMap` uses fine-grained segment locking internally, meaning two threads writing to different keys can proceed simultaneously without blocking each other, while writes to the same key are serialised safely. This gives the API both correctness and high throughput under concurrent load.

---

### Part 1.2 — HATEOAS and the Discovery Endpoint

HATEOAS (Hypermedia as the Engine of Application State) is the principle that API responses should include navigational links to related resources, making the API self-describing rather than requiring clients to have prior knowledge of every URL structure.

The `GET /api/v1/` discovery endpoint returns a JSON object that includes not just metadata (version, contact, status), but also a `resources` map pointing clients directly to the primary collection URLs (`/api/v1/rooms`, `/api/v1/sensors`). This is a practical implementation of HATEOAS.

The benefit to client developers is significant. Without hypermedia, a client must be given out-of-band documentation telling it that rooms live at `/api/v1/rooms`. If that URL ever changes (for example, in a future API version), every client that hardcoded the path must be updated. With HATEOAS, a client only needs to know a single stable entry point — the root URL. From there, it discovers all available resources dynamically from the response itself. This reduces tight coupling between the client and server, makes versioning and URL refactoring far less disruptive, and produces an API that is genuinely self-documenting even without external Swagger or Javadoc pages.

---

### Part 2.1 — Returning IDs vs Full Objects in List Responses

Returning only IDs in a list response is extremely bandwidth-efficient — the payload is minimal regardless of how many rooms exist. However, this forces the client to make one additional GET request per room to retrieve its details. This is known as the **N+1 problem**: if there are 100 rooms, the client makes 1 list call plus 100 detail calls — 101 requests total. Each round trip adds latency, especially on mobile networks or across regions.

Returning full room objects in the list eliminates all those round trips and delivers everything the client needs in a single response. The trade-off is payload size: if each room object is large, the list response can become heavy, increasing parsing time and memory consumption on the client side.

The correct choice depends on the use case. A facilities manager dashboard that must display names, capacities, and sensor counts for all rooms simultaneously benefits from full objects — it avoids 100 extra calls. A mobile dropdown that only needs to display room IDs for selection should use a lightweight ID-only list to conserve bandwidth. In this implementation, full objects are returned, which is the better default for a management API where clients typically need to display complete metadata.

---

### Part 2.2 — DELETE Idempotency

Yes, the DELETE operation is idempotent in this implementation, and this is by deliberate design in accordance with REST principles.

When a client sends `DELETE /api/v1/rooms/CS-101`:

- **First call (room exists, no sensors):** The room is found, removed from `MockDatabase.ROOMS`, and the server returns `204 No Content`. The server state changes.
- **Second call (room already deleted):** `MockDatabase.ROOMS.get("CS-101")` returns `null`. The code checks for this case explicitly and returns `204 No Content` immediately without throwing an error. The server state does not change further.
- **Third, fourth, Nth call:** Identical to the second — always `204 No Content`.

Idempotency means that the observable side effects of making the same request multiple times are identical to making it once. Since the end result is always "room CS-101 does not exist" and the HTTP status is always 204, the operation is fully idempotent. This is important for reliability: if a client sends a DELETE request and the network drops before receiving the response, it can safely retry without worrying about accidentally deleting something twice or receiving an unexpected error.

The only non-idempotent case is attempting to delete a room with sensors (`LIB-301`), which always returns `409 Conflict` with a JSON error body regardless of how many times it is called.

---

### Part 3.1 — Behaviour on @Consumes Mismatch

The `POST /api/v1/sensors` endpoint is annotated with `@Consumes(MediaType.APPLICATION_JSON)`. This tells JAX-RS that this method only accepts requests whose `Content-Type` header is `application/json`.

If a client sends a POST request with `Content-Type: text/plain` or `Content-Type: application/xml`, the JAX-RS runtime (Jersey) intercepts the request **before** the method body is ever reached. Jersey examines the incoming `Content-Type` header, searches all registered resource methods for one that declares `@Consumes` compatibility with that media type, finds none, and automatically returns **HTTP 415 Unsupported Media Type**. No application code runs.

This is a powerful mechanism because it provides a declarative content-type contract. The developer does not need to write `if (!request.getContentType().equals("application/json")) return 415;` inside every method. JAX-RS enforces it at the framework level, ensuring that the method body only ever receives data in the expected format. It also makes the API self-documenting: API consumers can infer from the 415 response exactly what format is required.

---

### Part 3.2 — @QueryParam vs Path Parameter for Filtering

The filtering endpoint is designed as `GET /api/v1/sensors?type=CO2` using `@QueryParam("type")` rather than `GET /api/v1/sensors/type/CO2` using a path segment.

This distinction is architecturally important. In REST, a URL path identifies a **specific resource or resource collection**. The path `/api/v1/sensors/CO2` implies that `CO2` is an identifier for a discrete resource — similar to how `/api/v1/sensors/TEMP-001` uniquely identifies one sensor. Using the type name in a path segment therefore misrepresents the operation: it suggests a resource with the ID `CO2` exists, rather than a filtered view of the sensors collection.

Query parameters, by contrast, represent **optional modifiers applied to a resource retrieval operation**. They are semantically correct for search and filter operations because they say "give me the sensors collection, but filtered by this criterion." The base resource (`/sensors`) remains the same; the query parameter just narrows the result set.

Query parameters are also far more composable. Multiple filters can be added without changing the URL structure: `?type=CO2&status=ACTIVE&minValue=300`. Achieving the same with path parameters would require a deeply nested URL like `/sensors/type/CO2/status/ACTIVE/minValue/300`, which is unmaintainable and breaks the resource-oriented model. Query parameters are therefore universally preferred for collection filtering and searching.

---

### Part 4.1 — Sub-Resource Locator Pattern

The sub-resource locator pattern is implemented in `SensorResource` via a method annotated only with `@Path("{sensorId}/readings")` — crucially, with **no HTTP verb annotation** (`@GET`, `@POST`, etc.). This absence is what distinguishes a locator from a regular endpoint. When JAX-RS receives a request to `/sensors/TEMP-001/readings`, it finds this method, calls it to obtain a `SensorReadingResource` instance, and then delegates all further routing for that URL sub-tree to that returned object.

The architectural benefit is a clean **separation of concerns**. `SensorResource` is responsible for managing the sensors collection. `SensorReadingResource` is responsible for managing the reading history of one specific sensor. Each class can be understood, tested, and modified independently.

In a large API without this pattern, a single "god controller" class would accumulate dozens of methods handling `/sensors`, `/sensors/{id}`, `/sensors/{id}/readings`, `/sensors/{id}/readings/{rid}`, and potentially deeper nesting. This class becomes difficult to navigate, violates the Single Responsibility Principle, and forces all developers to edit the same file for unrelated changes. The locator pattern imposes a structure where complexity grows horizontally (more resource classes) rather than vertically (one enormous class), which is far more maintainable at enterprise scale.

---

### Part 5.2 — Why 422 Is More Semantically Accurate Than 404

HTTP 404 Not Found communicates that the **requested URL resource does not exist** on the server. If a client sends `POST /api/v1/sensors` and receives a 404, the natural interpretation is that the `/sensors` endpoint itself cannot be found — which is false and misleading, because the endpoint exists and successfully returns a 200 for GET requests.

HTTP 422 Unprocessable Entity communicates a fundamentally different situation: the request was received correctly, the URL was valid, and the JSON body was syntactically well-formed, but the server is **unable to process the semantic content** because a field references something that does not exist. In this case, the JSON payload contains a `roomId` field that points to a room not present in the system. The request itself is not malformed — it is logically invalid because of a broken internal reference.

Using 422 here gives the client precise, actionable information: "your request arrived and was understood, but the data inside it refers to something that doesn't exist." This directly guides the client to fix the `roomId` field rather than wondering whether they have the wrong URL. 422 is therefore the semantically honest and most useful status code for payload reference validation failures.

---

### Part 5.4 — Cybersecurity Risks of Exposing Stack Traces

Exposing a raw Java stack trace in an API response is a serious information disclosure vulnerability. An attacker who receives a stack trace can extract:

**Internal file paths and package structure** — the full class names and file paths reveal how the application is organised (`com.example.dao.MockDatabase`, `com.example.resource.SensorResource`), giving the attacker a map of the codebase to target.

**Library names and exact versions** — lines such as `org.glassfish.jersey.server.ServerRuntime` with a version number allow the attacker to look up known CVEs (Common Vulnerabilities and Exposures) for that exact library version on databases like the National Vulnerability Database, potentially finding exploits that can be directly applied.

**Internal method names and line numbers** — specific method names and line numbers allow an attacker to understand business logic, identify where validation is performed, and potentially craft inputs that bypass checks.

**Database driver or ORM class names** — in a production application, stack traces often reveal database technology (e.g., `com.mysql.jdbc.Driver`), which helps the attacker tailor SQL injection or connection-based attacks.

The `GlobalExceptionMapper<Throwable>` implementation eliminates all of these risks by intercepting every unexpected runtime error and replacing the response with a generic JSON object containing only a safe, human-readable message and an error code. No internal technical detail whatsoever is visible in the response body.

---

### Part 5.5 — Filters vs Manual Logging

Using a JAX-RS `ContainerRequestFilter` / `ContainerResponseFilter` for logging is architecturally superior to inserting `Logger.info()` calls into every resource method for several reasons.

**Single implementation point:** The logging logic is written exactly once in `LoggingFilter.java` and automatically applies to every single request and response in the entire API. If the log format needs to change (for example, to add a timestamp or correlation ID), there is exactly one file to edit. Manual logging would require editing every resource method — dozens of changes, each a potential source of inconsistency or omission.

**Captures pre-route failures:** Filters run before JAX-RS even selects a resource method. This means they capture requests that fail at the framework level — for example, a 415 Unsupported Media Type rejection or a 404 for an unknown path. Manual `Logger.info()` calls inside a method would never fire for these cases, leaving a blind spot in the server logs.

**Separation of concerns:** Logging is a cross-cutting concern — it is relevant to every endpoint but is not part of any endpoint's business logic. Mixing it into resource methods violates the Single Responsibility Principle. Filters keep business logic clean and focused.

**Foundation for enterprise observability:** This same filter architecture can be extended to add authentication enforcement, CORS headers, rate limiting, and request tracing without modifying any resource class. In production, the log output from this filter would be directed to a centralised log aggregation system such as ELK (Elasticsearch, Logstash, Kibana) or AWS CloudWatch.
