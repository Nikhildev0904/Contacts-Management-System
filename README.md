# Contact Management System

A multi-tenant contact management system built with Spring Boot and MongoDB. This application provides isolated data storage for different tenants with comprehensive REST APIs for managing contacts and categories.

## Features

- **Multi-Tenant Architecture**: Complete data isolation with database-per-tenant approach
- **Authentication & Authorization**: Role-based access control with Basic Authentication
- **Contact Management**: Full CRUD operations for contacts with pagination and search
- **Category Management**: Organize contacts with custom categories
- **Admin Panel**: Tenant management capabilities for administrators
- **RESTful APIs**: Well-structured APIs with proper HTTP semantics
- **Security**: Tenant isolation and user authentication

## Architecture

This application follows a multi-tenant architecture where:
- Each tenant has their own isolated MongoDB database
- Tenant selection is done through authenticated user context
- Admin users can manage all tenants
- Regular users access only their tenant's data

## Technology Stack

- **Backend**: Spring Boot 3.x
- **Database**: MongoDB
- **Security**: Spring Security with Basic Authentication
- **Build Tool**: Maven
- **Java Version**: 17+

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- MongoDB 4.4+ (running on localhost:27017)
- Postman or similar API testing tool (optional)

## Project Structure

```
contacts-management-system/
├── src/main/java/com/nikhildev/projects/cms/
│   ├── config/          # Configuration classes (Security, MongoDB, Tenant)
│   ├── controllers/     # REST Controllers
│   ├── models/          # Domain models
│   ├── repositories/    # MongoDB repositories
│   ├── services/        # Business logic
│   └── exceptions/      # Custom exceptions
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

## Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Nikhildev0904/Contacts-Management-System.git
   cd Contacts-Management-System
   ```

2. **Ensure MongoDB is running**
   ```bash
   # Default connection: mongodb://localhost:27017
   mongod
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start at `http://localhost:8080`

## Configuration

Key configuration properties in `application.properties`:

```properties
# Server Configuration
server.port=8080

# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017

# Default admin credentials (created on startup)
admin.username=admin
admin.password=admin
```

## Authentication & Authorization

The system uses Basic Authentication with two roles:

- **ADMIN**: Can manage tenants and access all tenant data
- **USER**: Can only access their own tenant's contacts and categories

When the application starts, it automatically creates an admin account:
- Username: `admin`
- Password: `admin`

## API Documentation

### Authentication

All API endpoints require Basic Authentication. Include the following header in all requests:

```
Authorization: Basic <base64-encoded-credentials>
```

### Tenant Management (Admin Only)

#### Create Tenant
```http
POST /admin/tenants
Authorization: Basic YWRtaW46YWRtaW4=
Content-Type: application/json

{
    "name": "Company ABC",
    "description": "Primary tenant for Company ABC",
    "username": "companyabc",
    "password": "password123",
    "role": "USER"
}
```

#### List All Tenants
```http
GET /admin/tenants?page=0&pageSize=20&sortBy=name&sortOrder=ASC
Authorization: Basic YWRtaW46YWRtaW4=
```

#### Get Tenant by ID
```http
GET /admin/tenants/{tenantId}
Authorization: Basic YWRtaW46YWRtaW4=
```

#### Update Tenant
```http
PUT /admin/tenants/{tenantId}
Authorization: Basic YWRtaW46YWRtaW4=
Content-Type: application/json

{
    "name": "Updated Company Name",
    "description": "Updated description"
}
```

#### Delete Tenant
```http
DELETE /admin/tenants/{tenantId}
Authorization: Basic YWRtaW46YWRtaW4=
```

### Contact Management

#### Create Contact
```http
POST /api/contacts
Authorization: Basic <tenant-credentials>
Content-Type: application/json

{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "category": "Professional",
    "address": "123 Main St, City, Country"
}
```

#### List Contacts
```http
GET /api/contacts?page=0&pageSize=20&sortBy=name&sortOrder=ASC
Authorization: Basic <tenant-credentials>
```

#### Search Contacts
```http
GET /api/contacts/search?name=john&email=&phone=&category=Professional
Authorization: Basic <tenant-credentials>
```

#### Get Contact by ID
```http
GET /api/contacts/{contactId}
Authorization: Basic <tenant-credentials>
```

#### Update Contact
```http
PUT /api/contacts/{contactId}
Authorization: Basic <tenant-credentials>
Content-Type: application/json

{
    "name": "John Smith",
    "email": "john.smith@example.com",
    "phone": "+1234567890",
    "category": "Personal",
    "address": "456 Oak St, City, Country"
}
```

#### Delete Contact
```http
DELETE /api/contacts/{contactId}
Authorization: Basic <tenant-credentials>
```

### Category Management

#### Create Category
```http
POST /api/categories
Authorization: Basic <tenant-credentials>
Content-Type: application/json

{
    "name": "VIP Clients",
    "description": "High-value client contacts"
}
```

#### List Categories
```http
GET /api/categories?page=0&pageSize=20&sortBy=name&sortOrder=ASC
Authorization: Basic <tenant-credentials>
```

#### Update Category
```http
PUT /api/categories/{categoryId}
Authorization: Basic <tenant-credentials>
Content-Type: application/json

{
    "name": "Premium Clients",
    "description": "Updated description"
}
```

#### Delete Category
```http
DELETE /api/categories/{categoryId}
Authorization: Basic <tenant-credentials>
```

## Multi-Tenancy Implementation

The application implements multi-tenancy using:

1. **Database Isolation**: Each tenant has a separate MongoDB database (tenant_{id})
2. **Authentication-Based Tenant Resolution**: Tenant is determined from the authenticated user
3. **Interceptor Pattern**: TenantInterceptor sets the current tenant context
4. **Dynamic Database Selection**: MongoDBFactory selects database based on tenant context

## Security Features

- Basic Authentication for all endpoints
- Role-based access control (ADMIN/USER)
- Tenant isolation through authentication
- Password encryption using BCrypt
- Stateless session management

## Error Handling

The application provides comprehensive error handling with appropriate HTTP status codes:

- `400 Bad Request`: Invalid input data
- `401 Unauthorized`: Missing or invalid authentication
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource already exists

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ContactServiceTest

# Skip tests during build
mvn clean install -DskipTests
```

## Deployment

### Local Deployment

1. Build the JAR file:
   ```bash
   mvn clean package
   ```

2. Run the JAR:
   ```bash
   java -jar target/cms-0.0.1-SNAPSHOT.jar
   ```

### Docker Deployment (Optional)

Create a `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/cms-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
docker build -t contacts-management-system .
docker run -p 8080:8080 contacts-management-system
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Create a Pull Request

## Troubleshooting

### Common Issues

1. **MongoDB Connection Issues**
    - Ensure MongoDB is running on the correct port
    - Check MongoDB connection string in application.properties

2. **Authentication Errors**
    - Verify Basic Auth header is correctly encoded
    - Ensure username/password are correct

3. **Tenant Context Issues**
    - Check that the user is authenticated before accessing endpoints
    - Verify tenant exists in the database

## Future Enhancements

- JWT-based authentication
- API rate limiting
- Audit logging
- Export/Import functionality
- Advanced search with Elasticsearch
- Email notifications
- File attachments for contacts

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---
