# DocLoader - Document Embedding & Relationship Platform

DocLoader is a Spring Boot and React-based SaaS platform for tracking embedding operations of documents hosted on tenant's cloud storage. The application provides multi-tenancy support, job scheduling, and document relationship management.

## Architecture

- **Backend**: Spring Boot 3.x with multi-tenancy support
- **Frontend**: React 18 with Material UI
- **Databases**: 
  - PostgreSQL (primary data store with multi-tenant database design)
  - Neo4j (document relationship connections)
  - Weaviate (vector database for document embeddings)
- **Additional Services**:
  - Redis (user session management)
  - Quartz (job scheduling)

## Technical Implementation Details

### Multi-Tenancy Architecture

The platform implements a database-per-tenant approach for strong data isolation:

1. **Tenant Database Creation**: Each tenant gets their own PostgreSQL database which is created from a template database.
2. **Dynamic Connection Resolution**: The `TenantConnectionProvider` dynamically routes database connections to the appropriate tenant database.
3. **Tenant Context Management**: `TenantContext` uses a ThreadLocal to store the current tenant identifier throughout request processing.
4. **Tenant Resolution Strategies**: Supports multiple tenant identification strategies:
   - Subdomain-based resolution (e.g., tenant1.docloader.com)
   - Header-based resolution (using X-TenantID header)
   - Path-based resolution

### Security Implementation

- **JWT-based Authentication**: Stateless authentication using JWT tokens.
- **Role-based Access Control**: Default ADMIN and USER roles with permission management.
- **Password Encryption**: BCrypt password encoding for secure storage.
- **CORS Configuration**: Configured for cross-origin requests.
- **API Security**: Endpoint protection using Spring Security.

### Document Processing Workflow

The document processing system follows these steps:

1. **Job Creation**: Users create document processing jobs specifying S3 bucket source locations.
2. **Scheduling**: Jobs can be executed immediately or scheduled for future execution.
3. **Processing**: The system:
   - Extracts document content using Apache Tika
   - Generates embeddings via Spring AI
   - Stores embeddings in Weaviate vector database
   - Builds document relationships in Neo4j
4. **Duplicate Detection**: Uses MD5 hashing to avoid reprocessing the same documents.

### Data Models

Core entities in the system:

- **Tenant**: Represents an organization with its own isolated database.
- **User**: Users belonging to tenants with role-based permissions.
- **DocumentJob**: Represents a document processing job with status tracking.
- **Document**: Represents a processed document with embedding information.

### API Endpoints

The platform exposes these key API endpoints:

- **/api/auth**: Authentication and user registration.
- **/api/tenants**: Tenant management operations.
- **/api/jobs**: Document job management.
- **/api/health**: System health check.

## Prerequisites

- Docker and Docker Compose
- JDK 17+
- Node.js 16+
- Maven 3.6+

## Quick Start

### 1. Start the Infrastructure Services

```bash
docker-compose up -d
```

This command starts PostgreSQL, Neo4j, Redis, and Weaviate services.

### 2. Build and Run the Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The Spring Boot application will start on port 8081 with the API accessible at http://localhost:8081/api

### 3. Start the Frontend Development Server

```bash
cd frontend
npm install
npm start
```

The React application will start on port 3000, accessible at http://localhost:3000

## Environment Configuration

The following environment variables can be configured:

```
# JWT Configuration
JWT_SECRET=your-secret-key

# OpenAI Configuration
OPENAI_API_KEY=your-openai-key
OPENAI_BASE_URL=https://api.openai.com

# AWS Configuration
AWS_ACCESS_KEY=your-aws-access-key
AWS_SECRET_KEY=your-aws-secret-key
AWS_REGION=us-east-1
```

## Development Notes

- Liquibase changesets are numbered sequentially as 00001-description, 00002-description, etc.
- The application uses separate users for liquibase migrations and normal application operations
- Redis is used for session management to ensure scalability
- The multi-tenant architecture supports horizontal scaling for increased load
- Document processing is executed asynchronously to avoid blocking API responses 