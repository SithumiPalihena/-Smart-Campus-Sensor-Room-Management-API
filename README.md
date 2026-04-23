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


