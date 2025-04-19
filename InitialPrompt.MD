I want to create a ReactJS Spring Boot based cloud native application that hosts a SaaS platform for tracking embedding operation of a large number of documents that are hosted on a shared cloud location like a tenant’s own S3 bucket (they would provide access via keys?) - it tracks state and job progress in Postgres DB, we want to use JWT based session management to be able to scale indefinitely, user sessions can be persisted to Redis if we must. For tracking user created and scheduled job (load operations), let’s use the off the shelf Quartz solution.  We do not want to reprocess already processed documents.  To keep the LLM interchangeable use Spring AI abstraction.  We prefer Spring Data for persistence.

Once the data is loaded into the vector database, we also want to build relationship connections between them - to do this we want to explore using Neo4j, unless you don’t think this is a good idea. 

All the service dependencies (Postgres, Neo4j, Redis, Vector Database) should be stood up via a docker-compose file so we can develop this platform on our laptops.  

Each tenant gets their own user database, with 2 permission levels for now. We will likely allow them to create new roles with custom permissions sets in the future. 

To make and track the Postgres database schema changes we want to use liquibase, but make sure the privileged liquibase user is not the same as the application user. We like to number the liquibase changeset filenames in the format 0000x  e.g. 00001-descriptionx, 00002-otherdescription, etc. 

Make sure we always have multi-tenancy in mind. 

Let's use a modern GUI framework like Material UI. 

Let us make sure we are using an up-to-date version of Spring Boot, ReactJS, Material UI, Postgres etc. 
