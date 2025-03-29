# OpenAPI Documentation Generator

A Spring Boot application that generates documentation from OpenAPI specifications.

## Features

- Generate documentation from OpenAPI specifications in various formats:
  - From a URL pointing to an OpenAPI JSON
  - From an OpenAPI JSON string
  - From an uploaded OpenAPI JSON file
- Customizable document templates
- Swagger UI integration for API documentation

## API Documentation

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

This provides interactive documentation of all the available endpoints.

## API Endpoints

### Generate Document from URL

```
POST /api/openapi-doc/generate-from-url
```

Parameters:
- `url` (required): URL to the OpenAPI specification
- `templateName` (optional): Name of the template to use
- `template` (optional): Custom template file upload

### Generate Document from JSON String

```
POST /api/openapi-doc/generate-from-json
```

Parameters:
- Request body (required): OpenAPI specification as JSON string
- `templateName` (optional): Name of the template to use
- `template` (optional): Custom template file upload

### Generate Document from File

```
POST /api/openapi-doc/generate-from-file
```

Parameters:
- `file` (required): OpenAPI specification as JSON file
- `templateName` (optional): Name of the template to use
- `template` (optional): Custom template file upload

## Development

### Prerequisites

- Java 8 or later
- Maven

### Building the Project

```bash
mvn clean install
```

### Running the Application

```bash
mvn spring-boot:run
```

### Running Tests

```bash
mvn test
```

## Technologies Used

- Spring Boot 2.2.6
- Swagger/SpringFox 2.9.2 for API documentation
- poi-tl for Word document generation
- JUnit 5 for testing 