# Complaint Service

A RESTful service for managing product complaints. It allows adding new complaints, editing existing complaint content, and retrieving saved complaints.

## Features

- Add new complaints
- Edit the content of existing complaints
- Retrieve saved complaints
- Automatically detect the user's country based on their IP address
- Automatically increment a counter for duplicate complaints (based on product ID and reporter)

## Technical Requirements

- Java 17
- Maven 3.8+
- Internet access (for IP geolocation service)

## Technologies Used

- Java 17
- Spring Boot 3.4.3
- Spring Data JPA
- H2 Database (in-memory database)
- Lombok
- JUnit 5
- Mockito
- JaCoCo (code coverage)
- Jakarta Validation
- Jackson (JSON handling)

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── pl/empik/complaintservice/
│   │       ├── constants/           # Application constants
│   │       ├── controller/          # REST controllers
│   │       ├── exception/           # Exception handling
│   │       ├── model/               # Data models and DTOs
│   │       ├── repository/          # JPA repositories
│   │       └── service/             # Service layer
│   └── resources/                   # Configuration files
└── test/
    └── java/
        └── pl/empik/complaintservice/
            └── service/             # Unit tests
```

## How to Build

```bash
mvn clean install
```

## How to Run

### Using Maven

```bash
mvn spring-boot:run
```

### As a JAR File

```bash
java -jar target/complaint-service-0.0.1-SNAPSHOT.jar
```

After launching, the application will be available at: http://localhost:8080

## How to Debug

### In IntelliJ IDEA

1. Open the project in IntelliJ IDEA
2. Right-click on the `ComplaintServiceApplication` class
3. Select "Debug 'ComplaintServiceApplication'"

### Using Maven

```bash
mvnDebug spring-boot:run
```

Then connect your IDE to port 8000.

## Testing

### Running Unit Tests

```bash
mvn test
```

### Generating Code Coverage Report (JaCoCo)

```bash
mvn clean test jacoco:report
```

The report will be available in the `target/site/jacoco/index.html` directory

### Verifying Code Coverage

```bash
mvn clean verify
```

This command runs tests and checks if code coverage meets minimum requirements (80% instruction coverage, 70% branch coverage).

## API Testing

### Postman Collection

The project includes a Postman collection (`Complaints-API.postman_collection.json`) that can be imported into the Postman application to test the API.

To import the collection:
1. Open Postman
2. Click "Import"
3. Select the `Complaints-API.postman_collection.json` file

### Available Endpoints

- `GET /api/complaints` - Retrieves all complaints
- `GET /api/complaints/{id}` - Retrieves a complaint by its ID
- `POST /api/complaints` - Creates a new complaint
- `PUT /api/complaints/{id}` - Updates the content of a complaint by its ID

### Example Request for Creating a Complaint (POST)

```json
{
  "productId": "PROD-123",
  "content": "Product does not work as described",
  "reportedBy": "john.doe@example.com"
}
```

### Testing IP Geolocation

To test the IP geolocation feature during local development, you can use the `X-Forwarded-For` header in Postman:
1. In the "Headers" tab, add a header
2. Enter `X-Forwarded-For` as the key
3. Enter a chosen IP address as the value (e.g., `8.8.8.8` for Google DNS)

## H2 Database Console

The H2 database console is available at: http://localhost:8080/h2-console

Login credentials:
- JDBC URL: `jdbc:h2:mem:complaintsdb`
- Username: `sa`
- Password: `password`

## Known Limitations

- The geolocation service uses a free API which may have request limits
- Data is stored in memory (H2 Database) and is lost after application restart