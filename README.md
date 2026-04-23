Smart Campus Sensor & Room Management API

A RESTful API built with Java 11, JAX-RS (Jersey 2.39.1), and Jackson, deployed as a WAR on Apache Tomcat 10. It manages campus rooms and IoT sensor devices (temperature, CO₂, occupancy, etc.) and follows HATEOAS conventions every response includes _links that tell clients where to go next.

API Design Overview

Base URL - http://localhost:8080/api/v1

Data is held in an in-memory DataStore singleton backed by ConcurrentHashMap and CopyOnWriteArrayList, so no database setup is needed.

Resources

Rooms - /api/v1/rooms

| Method | Path | Description | Success |
|--------|------|-------------|---------|
| GET | /api/v1/rooms | List all rooms | 200 |
| POST | /api/v1/rooms | Create a new room | 201 |
| GET | /api/v1/rooms/{roomId} | Get a single room | 200 |
| DELETE | /api/v1/rooms/{roomId} | Delete a room (blocked if sensors are assigned) | 200 |

Sensors - /api/v1/sensors
Represents an IoT sensor device deployed in a room.

| Method | Path | Description | Success |
|--------|------|-------------|---------|
| GET | /api/v1/sensors | List all sensors (supports ?type= filter) | 200 |
| POST | /api/v1/sensors | Register a new sensor | 201 |
| GET | /api/v1/sensors/{sensorId} | Get a single sensor | 200 |

Sensor Readings - /api/v1/sensors/{sensorId}/readings
Timestamped measurements recorded by a sensor. A sensor must be ACTIVE to accept new readings.

| Method | Path | Description | Success |
|--------|------|-------------|---------|
| GET | /api/v1/sensors/{sensorId}/readings | List all readings for a sensor | 200 |
| POST | /api/v1/sensors/{sensorId}/readings | Record a new reading | 201 |
| GET | /api/v1/sensors/{sensorId}/readings/{readingId} | Get a specific reading | 200 |

Error Format

| HTTP Status | Error Code | Trigger |
|-------------|-----------|---------|
| 400 | MISSING_FIELD / INVALID_FIELD | Validation failure |
| 403 | SENSOR_UNAVAILABLE | Posting a reading to a non-ACTIVE sensor |
| 404 | ROOM_NOT_FOUND / SENSOR_NOT_FOUND / READING_NOT_FOUND | Resource does not exist |
| 409 | DUPLICATE_ID / ROOM_NOT_EMPTY | ID clash or room has sensors on delete |
| 422 | LINKED_RESOURCE_NOT_FOUND | roomId on a sensor references a non-existent room |


Build Instructions

step 1 - Clone the repository  git clone https://github.com/SithumiPalihena/-Smart-Campus-Sensor-Room-Management-API

Step 2 - Add Apache Tomcat 9 as a server

Open the Services tab (press Ctrl+5 or go to Window → Services).
Right-click Servers → Add Server…
Select Apache Tomcat or TomEE and click Next.
Click Browse… and select the root folder of your extracted Tomcat 9 download
(the folder that contains bin/, webapps/, etc.).
Enter any username and password you like (these are only for the Tomcat Manager web UI).
Click Finish.

Tomcat will now appear under the Servers node in the Services tab.

Step 3 - Build the project

In the Projects panel, right-click the smart-campus-api project.
Select Clean and Build.

NetBeans runs mvn clean package internally and produces target/ROOT.war. Watch the Output tab at the bottom — a successful build ends with:
BUILD SUCCESS

Step 4 - Run the project

Right-click the smart-campus-api project again.
Select Run.

NetBeans will automatically start Tomcat 9, deploy ROOT.war to it, and open a browser tab. The Output tab shows the Tomcat startup log.


Sample curl Commands

The five commands below walk through the complete workflow: create a room → register a sensor → submit a reading → query it back → clean up.

1 - Discover the API - 
curl -s -X GET http://localhost:8080/api/v1/ \
  -H "Accept: application/json"

2 - Create a room

curl -s -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "id":       "LIB-301",
    "name":     "Library Quiet Study",
    "capacity": 40
  }'

  3 - Register a sensor in that room

  curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "id":           "TEMP-001",
    "type":         "Temperature",
    "status":       "ACTIVE",
    "currentValue": 21.5,
    "roomId":       "LIB-301"
  }'

  4 -  Post a sensor reading

  curl -s -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "timestamp": 1714000000000,
    "value":     23.7
  }'

  5 -  List all sensors filtered by type

  curl -s -X GET "http://localhost:8080/api/v1/sensors?type=Temperature" \
  -H "Accept: application/json"

6 - Attempt to delete a room that still has sensors (demonstrates 409 error)

curl -s -X DELETE http://localhost:8080/api/v1/rooms/LIB-301 \
  -H "Accept: application/json"



Project Structure
```
smart-campus/
├── pom.xml
└── src/
    └── main/
        ├── java/com/smartcampus/
        │   ├── SmartCampusApplication.java
        │   ├── data/
        │   │   └── DataStore.java
        │   ├── model/
        │   │   ├── Room.java
        │   │   ├── Sensor.java
        │   │   └── SensorReading.java
        │   ├── resource/
        │   │   ├── DiscoveryResource.java
        │   │   ├── RoomResource.java
        │   │   ├── SensorResource.java
        │   │   └── SensorReadingResource.java
        │   ├── exception/
        │   │   ├── LinkedResourceNotFoundException.java
        │   │   ├── RoomNotEmptyException.java
        │   │   └── SensorUnavailableException.java
        │   ├── exception/mapper/
        │   │   ├── GlobalExceptionMapper.java
        │   │   ├── LinkedResourceNotFoundExceptionMapper.java
        │   │   ├── RoomNotEmptyExceptionMapper.java
        │   │   └── SensorUnavailableExceptionMapper.java
        │   └── filter/
        │       └── ApiLoggingFilter.java
        └── webapp/
            ├── META-INF/context.xml
            └── WEB-INF/web.xml
```



Report: Answers to Coursework Questions


1. Service Architecture & Setup
1.1Project & Application Configuration 

Question: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a
new instance instantiated for every incoming request, or does the runtime treat it as a
singleton? Elaborate on how this architectural decision impacts the way you manage and
synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

By default, the JAX-RS runtime follows a per-request lifecycle for resource classes, meaning a brand-new instance of each resource class is instantiated for every single incoming HTTP request, and that instance is discarded immediately after the response is sent. This is the default behavior defined in the JAX-RS specification and is the model used in this implementation with Jersey as the runtime.
This architectural decision has a direct and critical consequence for in-memory data management. If mutable state such as rooms or sensors were stored as instance fields on RoomResource or SensorResource, that data would be created fresh with every request and destroyed the moment the response was delivered making it impossible to persist any data across multiple HTTP calls.
To solve this, this implementation introduces a dedicated singleton DataStore class (com.smartcampus.data.DataStore). The singleton is initialized once using the classic static final instance pattern:

Every resource class - RoomResource, SensorResource, and SensorReadingResource obtains a reference to this single shared instance via DataStore.getInstance() at the start of each request. Because the singleton lives for the entire lifetime of the JVM process, all rooms, sensors, and readings are preserved across requests regardless of how many resource class instances are created and destroyed.
The second consequence of per-request instantiation is thread safety. Since JAX-RS containers handle concurrent requests on multiple threads simultaneously, multiple resource instances may be reading and writing to the shared DataStore at the same time. To prevent race conditions and data corruption, this implementation deliberately uses ConcurrentHashMap for both the rooms and sensors maps, and CopyOnWriteArrayList for the per-sensor readings list. ConcurrentHashMap guarantees atomic put, get, and remove operations without requiring explicit synchronized blocks, while CopyOnWriteArrayList allows safe concurrent reads which are the dominant operation for reading history by creating a fresh copy of the array on each write. This combination ensures that no two threads can corrupt shared state even when processing simultaneous requests, making the data store safe without manually managing locks.

1.2. The “Discovery” Endpoint 

Question: Why is the provision of “Hypermedia” (links and navigation within responses)
considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach
benefit client developers compared to static documentation?

HATEOAS, which stands for Hypermedia as the Engine of Application State, is considered a hallmark of mature RESTful API design because it allows clients to navigate an entire API dynamically through links embedded directly within API responses, rather than relying on external documentation to discover what actions are available.
In this implementation, the DiscoveryResource at GET /api/v1 returns a links object that explicitly tells the client where to find the rooms collection and the sensors collection, along with the HTTP method required for each:


This pattern is carried through every resource in the API. For example, when a room is successfully created via POST /api/v1/rooms, the response includes links to self, delete, and all, telling the client exactly where to go next without requiring them to construct URLs manually.
The benefit over static documentation is significant. With static documentation, a client developer must read an external document, memorize or hardcode URL patterns, and update their code every time the API changes. With HATEOAS, the client can start at the root discovery endpoint and programmatically follow links to reach any resource. This means the server can change its internal URL structure without breaking clients, if the links in responses are updated. This is the essence of client-server decoupling, the client has no hardcoded knowledge of the server's URL structure, so the two can evolve independently.
2. Room Management
2.1.Room Resource Implementation 

Question: When returning a list of rooms, what are the implications of returning only
IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

When designing a list endpoint such as GET /api/v1/rooms, the choice between returning only IDs or returning full room objects involves a fundamental trade-off between network efficiency and client-side complexity.
This implementation returns full room objects in the list response, along with a count field and HATEOAS links. The response from RoomResource.getAllRooms() includes every field of every Room.
Returning only IDs would minimise the response payload size, which is beneficial in environments with limited bandwidth or when the list is very large. However, it forces the client to make one additional GET request per room to retrieve its details a pattern known as the N+1 problem. If there are 50 rooms, the client would need to make 51 HTTP requests in total (1 for the list + 50 individual GETs), dramatically increasing latency and server load.
Returning full objects, as this API does, means the client gets everything it needs in a single request. This is especially appropriate for a campus management dashboard where a client might render a table of all rooms with their names and capacities immediately. The trade-off is a larger payload, but since room objects are relatively small (a few string and integer fields), this is far outweighed by the reduction in round-trip network calls. The inclusion of sensorIds in each room object also allows clients to understand which sensors are linked without making further requests, making the API more efficient for read-heavy cases.


2.2 Room Deletion & Safety Logic 

Question: Is the DELETE operation idempotent in your implementation? Provide detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple time.

In REST, an operation is considered idempotent if making the same request multiple times produces the same server state as making it once. The DELETE operation in this implementation is partially idempotent in terms of server state, but not fully idempotent in terms of HTTP response code, which is the practically important distinction.
In RoomResource.deleteRoom(), the first thing the method does is look up the room by ID:


If a client sends a DELETE request for a room that exists, the room is removed from the ConcurrentHashMap and a 200 OK is returned. If the same client mistakenly sends the exact same DELETE request a second time, the room no longer exists in the store, so the method returns 404 Not Found with the message "It may have already been deleted."
From a server state perspective, the implementation is idempotent after the first successful delete, no further state change occurs no matter how many times the same request is repeated. The data store remains in the same condition: that room simply does not exist. However, from a response perspective, the first call returns 200 and subsequent calls return 404, meaning the response code differs. Some API designers argue that a truly idempotent DELETE should return 204 No Content even on repeated calls to the same resource. The trade-off in this implementation is that returning 404 on subsequent calls is arguably more informative to the client it signals clearly that the resource does not exist rather than silently succeeding even though this makes the status code behavior technically non-uniform across repeated requests.


3.Sensor Operations & Linking

3.1Sensor Resource & Integrity 

Question: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on
the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

The @Consumes(MediaType.APPLICATION_JSON) annotation on the
 POST /api/v1/sensors method in SensorResource is a declaration to the JAX-RS runtime that this endpoint will only accept request bodies with a Content-Type of application/json. This annotation acts as a contract enforced entirely by the framework before any application code is executed.
                    If a client sends a request with Content-Type: text/plain or Content-Type: application/xml, the JAX-RS runtime in this case Jersey inspects the Content-Type header of the incoming request and compares it against the media types declared in @Consumes. Finding no match, Jersey immediately rejects the request and returns an HTTP 415 Unsupported Media Type response, without ever invoking the createSensor() method. This means validation happens at the framework level, not the application level, no sensor validation logic, no DataStore interaction, nothing runs.
                     This is extremely beneficial because it prevents malformed or unexpected data formats from even reaching the business logic layer. A text/plain body, for example, cannot be desterilized by Jackson into a Sensor object, and attempting to do so would cause a parsing exception. By intercepting at the @Consumes level, Jersey provides a clean, standardized error response without requiring the developer to write defensive parsing code. It also clearly communicates to API consumers what format is expected, which pairs well with the HATEOAS links in responses that implicitly describe the API's contract.


3.2Filtered Retrieval & Search

Question: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

In SensorResource.getAllSensors(), the optional type of filter is implemented using @QueryParam("type"), allowing requests like GET /api/v1/sensors?type=CO2. An alternative design would embed the filter value in the URL path itself, such as GET /api/v1/sensors/type/CO2.
The query parameter approach is considered superior for filtering and searching for several well-established reasons. First, URL path segments represent resources, not filter criteria. The path /api/v1/sensors identifies the sensors collection as a resource. Appending /type/CO2 to the path implies that type/CO2 is a sub-resource of sensors, which is semantically incorrect, it is a filtered view of the same collection, not a separate resource. Query parameters, by contrast, are universally understood as modifiers that refine or filter the representation of the resource identified by the path.
Second, query parameters are composable and extensible. If in the future the API needs to support filtering by both type and status simultaneously, a query parameter approach allows GET /api/v1/sensors?type=CO2&status=ACTIVE naturally. A path-based approach would require inventing awkward URL patterns like /sensors/type/CO2/status/ACTIVE, which are difficult to read, hard to route, and brittle to extend.
Third, query parameters are optional by design. In this implementation, if no type parameter is provided, getAllSensors() simply returns all sensors. This falls naturally from the @QueryParam approach since the parameter resolves to null when absent. A path-based design would require defining two entirely separate route methods, one for filtered and one for unfiltered to achieve the same behavior.
Finally, query parameters align with how HTTP caching infrastructure proxies, CDNs treats URLs. The base URL /api/v1/sensors can be cached as the canonical representation of the collection, while filtered variants are understood as query-modified views.



 4. Deep Nesting with Sub – Resources
4.1The Sub-Resource Locator Pattern

Question: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class? 



The sub-resource locator pattern is implemented in SensorResource via the following method:

Notice that this method has no HTTP verb annotation (@GET, @POST, etc.). This signals to the JAX-RS runtime that it should not invoke this method directly in response to a request instead, it should call this method to obtain a delegate object, and then dispatch the actual HTTP request to a method on that returned object. In this case, the sensorId is extracted from the path and passed to SensorReadingResource's constructor, giving the sub-resource full context about which sensor it is operating on.

The architectural benefit of this pattern over defining all nested paths in a single monolithic controller is primarily one of separation of concerns and maintainability. If every endpoint including GET /sensors/{id}, POST /sensors/{id}/readings, GET /sensors/{id}/readings, and GET /sensors/{id}/readings/{rid} were defined in one SensorResource class, that class would rapidly grow to hundreds of lines, mixing the logic for managing sensors with the logic for managing readings. These are distinct domain concepts with distinct validation rules, distinct error conditions, and distinct data access patterns.

By delegating to SensorReadingResource, reading-related logic is entirely self-contained in one class. SensorReadingResource handles the 403-state constraint (checking if the sensor is in ACTIVE status before accepting a reading), manages the currentValue side effect, and handles 404 errors for missing sensors all without SensorResource needing to know anything about it. This also makes the codebase far easier to test, extend, and reason about independently. In a large real-world campus API that might eventually support dozens of nested resource types (maintenance logs, calibration records, alerts), the sub-resource locator pattern is the only scalable approach to keeping controllers focused and manageable.




 5. Advanced Error Handling, Exception Mapping & Logging 
5.1Dependency Validation (422 Processable Entity)
Question: Why is HTTP 422 often considered more semantically accurate than a                                                                     standard 404 when the issue is a missing reference inside a valid JSON payload?

When a client sends a POST /api/v1/sensors request with a roomId that does not exist in the system, this implementation throws a LinkedResourceNotFoundException which is mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundExceptionMapper.
The reason 422 is more semantically accurate than 404 in this scenario comes down to what exactly was "not found." HTTP 404 Not Found is defined in the HTTP specification to mean that the requested URL could not be resolved to any resource on the server. In other words, 404 means the endpoint itself does not exist. In this case, the endpoint POST /api/v1/sensors exists perfectly well the server found it, routed the request to it, and began processing the payload.
The problem is not with the URL. The problem is with the semantic content of the JSON body. The roomId field within the payload references an entity that does not exist in the data store, making the request logically unprocessable despite being syntactically valid JSON delivered to a valid endpoint. HTTP 422 is explicitly defined to cover this scenario: the server understood the request, the request was well-formed, but it could not be processed due to semantic errors in the payload content. A missing foreign-key reference is precisely such a semantic error.
Using 404 here would mislead client developers into thinking the endpoint URL was wrong, causing them to check their routing when the actual fix is to create the referenced room first. The LinkedResourceNotFoundExceptionMapper in this implementation also includes a hint field in the response body reinforcing this, telling the client to ensure the referenced resource exists before retrying guidance that is only meaningful because the 422 correctly frames the problem as a data integrity issue rather than a routing issue.





5.2The Global Safety Net (500)

Question: From a cybersecurity standpoint, explain the risks associated with exposing
internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

The GlobalExceptionMapper in this implementation catches all unhandled Throwable instances and returns a generic, sanitised HTTP 500 response to the client, while logging the full stack trace server-side only. This design is a deliberate security measure.
Exposing raw Java stack traces to external API consumers presents several serious risks. First, stack traces reveal internal package and class names, such as com.smartcampus.data.DataStore or com.smartcampus.resource.SensorResource. This tells an attacker the exact internal architecture of the application, making it easier to craft targeted payloads that exploit known weaknesses in specific classes or patterns.
Second, stack traces often include third-party library names and version numbers, such as org.glassfish.jersey.server.ServerRuntime or com.fasterxml.jackson.databind.ObjectMapper. Knowing the exact versions of libraries in use allows an attacker to cross-reference public CVE databases and identify known, unpatched vulnerabilities in those specific versions, dramatically lowering the effort required to mount an exploit.
Third, stack traces can reveal file system paths, such as the absolute path to the compiled .class files or configuration files on the server. This exposes server directory structure information that aids in further reconnaissance.
Fourth, the line numbers included in stack traces tell an attacker exactly where in the source code an exception was thrown, which combined with the class name can reveal business logic flows, conditional branches, and data access patterns that would otherwise be opaque.
By returning only a generic message "An unexpected error occurred on the server. The issue has been logged." , the GlobalExceptionMapper ensures that none of this information reaches an external consumer, while the full details are preserved server-side in the Java logging system for legitimate debugging by engineers. This is a fundamental principle of secure API design: fail safely and silently to the outside world, verbosely to internal systems.





5.3API Request & Response Logging Filters 

Question: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

The ApiLoggingFilter in this implementation implements both ContainerRequestFilter and ContainerResponseFilter in a single @Provider-annotated class. This approach is far superior to manually inserting Logger.info() calls inside every individual resource method for several reasons.
The most fundamental reason is the DRY principle (Don't Repeat Yourself). The Smart Campus API has endpoints across DiscoveryResource, RoomResource, SensorResource, and SensorReadingResource. Manually logging in every method would require dozens of identical log statements scattered throughout the codebase. Any change to the log format for example, adding the client's IP address to every log line would require finding and updating every single one of those statements. With a filter, the change is made in exactly one place.
Second, filters operate as genuine cross-cutting concerns in the AOP (Aspect-Oriented Programming) sense. Logging is not part of the business logic of creating a sensor or fetching a room's details. Mixing logging code into business logic methods violates the single-responsibility principle and makes resource methods harder to read and test. A filter separates these concerns cleanly: resource methods focus purely on their domain logic, while the filter handles observability transparently.
Third, the filter approach guarantees completeness. If a developer adds a new resource class in the future but forgets to add logging statements to it, its requests will go unlogged. With ApiLoggingFilter, every request that passes through the JAX-RS pipeline is automatically logged regardless of which resource handles it, no new code required.
Finally, this implementation adds an intelligent enhancement on top of basic logging: the response filter uses different log levels (LOGGER.warning() for 4xx/5xx, LOGGER.info() for 2xx/3xx), which allows operations teams to filter server logs by severity and quickly identify error patterns in production without wading through successful requests a capability that would be cumbersome and inconsistent to implement manually across every resource method.


