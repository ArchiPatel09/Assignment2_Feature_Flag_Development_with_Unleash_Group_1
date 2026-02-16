Microservices, Docker Compose and CI/CD Pipelines
Group: 01 
This project consists of two Spring Boot microservices, namley, product-service and order-service. This services use an H2 in-memory database, and are containerized with Docker and are built and tested using GitHub Actions CI/CD.

Setup Instructions:
The following needs to be installed for this project: 
- Java JDK21 
- Docker Desktop 
- Git

How to run the application: -Build & start using docker compose: docker compose up --build

CI/CD:
This project is using GitHub Actions to automatically build, test and run Docker containers on each and every push and pull request to main branch.