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

## Multi-Tenancy

The platform uses a database-per-tenant approach for strong data isolation:

1. Each tenant has its own PostgreSQL database
2. Tenant databases are created from a template database
3. The subdomain is used for tenant identification

## Document Processing

The document processing workflow includes:

1. Tenant provides S3 bucket credentials and document location
2. Documents are processed and embedded using Spring AI
3. Document relationships are stored in Neo4j
4. Quartz jobs manage scheduled processing tasks

## Security

- JWT-based authentication
- Role-based access control with two default roles (ADMIN, USER)
- User sessions persisted in Redis
- Separate application and database users
- Liquibase for controlled schema changes

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