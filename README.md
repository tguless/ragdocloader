# DocLoader - Document Intelligence Platform

<p align="center">
  <img src="frontend/public/docloader-logo.svg" alt="DocLoader Logo" width="180"/>
</p>

<p align="center">
  <b>Unlock the hidden connections in your document repository</b>
</p>

## Transform Your Document Management

DocLoader is an intelligent document processing platform that goes beyond simple storage. Our advanced AI-powered system automatically processes, analyzes, and connects your documents, revealing relationships and insights that would otherwise remain hidden.

### 🚀 Key Features

- **Smart Document Processing**: Automatically extract and index content from multiple document formats
- **AI-Powered Embeddings**: Transform documents into rich vector embeddings for semantic search and analysis
- **Relationship Discovery**: Automatically identify connections between documents through semantic similarity
- **Knowledge Graph Generation**: Visualize document relationships through an intuitive graph interface
- **Multi-Tenant Architecture**: Enterprise-ready with isolated tenant databases for maximum security
- **Cloud Storage Integration**: Connect directly to your existing S3 storage infrastructure

### 💼 Business Benefits

- **Reduce Research Time**: Find relevant documents instantly instead of manual searching
- **Discover Hidden Insights**: Uncover connections between documents that humans might miss
- **Enhance Collaboration**: Share document collections and their relationships across teams
- **Maintain Security**: Keep sensitive documents secure with our multi-tenant architecture
- **Leverage Existing Infrastructure**: Connect to your current cloud storage without migration

---

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
  - MinIO (S3-compatible storage for local development)

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
5. **Tenant-specific Storage**: Each tenant can configure their own S3 storage details:
   - Custom S3 endpoint, region, credentials and bucket
   - Support for various S3-compatible storage services (AWS S3, MinIO, etc.)

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

### Document Relationship Building

The system identifies and persists relationships between documents using the following approach:

1. **Embedding-based Similarity Detection**:
   - Each document is represented by its embedding vector in high-dimensional space
   - Cosine similarity between document vectors measures semantic relatedness
   - Documents with similarity scores above a configurable threshold (default: 0.7) are considered related
   - The implementation uses Weaviate's vector search capabilities to efficiently find similar documents

2. **Relationship Enrichment**:
   - Beyond vector similarity, relationships are enhanced with additional metadata:
     - Similarity score (a numeric value between 0-1)
     - Relationship type (e.g., "SEMANTIC", "CITATION", "SAME_AUTHOR")
     - Relationship creation timestamp
     - Domain-specific relationship properties

3. **Neo4j Relationship Persistence**:
   - Document relationships are stored in Neo4j using the following pattern:
     ```cypher
     (doc1:Document)-[:RELATED_TO {similarity: 0.87, relationship_type: "SEMANTIC", created_at: timestamp}]->(doc2:Document)
     ```
   - This graph structure enables powerful queries for relationship exploration

4. **Advanced Relationship Types**:
   - Citation Analysis: Extract and link documents based on direct references
   - Entity Co-occurrence: Identify documents mentioning the same entities
   - Temporal Proximity: Link documents created in similar timeframes
   - Thematic Analysis: Group documents by detected themes or topics

5. **Relationship API**:
   - The system exposes endpoints to query document relationships
   - Results can be filtered by relationship type and minimum similarity threshold
   - Relationship depth can be specified (1st-degree, 2nd-degree connections, etc.)

This approach creates a rich knowledge graph of document relationships that can be traversed, queried, and visualized through the application, enabling users to discover connections that might otherwise remain hidden.

### Data Models

Core entities in the system:

- **Tenant**: Represents an organization with its own isolated database and storage.
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

This command starts PostgreSQL, Neo4j, Redis, Weaviate and MinIO services.

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
S3_ENDPOINT=your-s3-endpoint  # Required for non-AWS S3 services like MinIO
S3_BUCKET_NAME=your-bucket-name
S3_PATH_STYLE=true  # Required for MinIO compatibility
```

## Development Notes

- Liquibase changesets are numbered sequentially as 00001-description, 00002-description, etc.
- The application uses separate users for liquibase migrations and normal application operations
- Redis is used for session management to ensure scalability
- The multi-tenant architecture supports horizontal scaling for increased load
- Document processing is executed asynchronously to avoid blocking API responses
- A test tenant with MinIO configuration is automatically created for local development
- Document relationships are built incrementally as new documents are processed 

## About DocLoader

DocLoader was built to solve the growing challenge of information overload in document management. In today's knowledge economy, organizations struggle to extract meaningful insights from their document repositories. DocLoader turns your document storage from a passive archive into an active knowledge base that continuously improves as more documents are processed.

For inquiries or custom deployments, contact us at info@docloader.com 