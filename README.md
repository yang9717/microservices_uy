# Spring Boot Microservices Project

## 📖 Project Overview

This project is an implementation of a microservices architecture using Spring Boot, developed following a Udemy course for beginners in RESTful Web Services and Microservices.

## 🧬 Project Structure

The repository contains the following microservices and components:

- `PhotoAppApiConfigServer`: Centralized configuration server
- `PhotoAppDiscoveryService`: Service discovery with Eureka
- `ApiGateway`: Spring Cloud API Gateway for routing and filtering
- `PhotoAppApiUsers`: User management microservice
- `PhotoAppApiAlbums`: Microservice for handling album-related operations
- `JwtAuthorities`: Jwt claims parser
- `Zipkin`: Distributed tracing service
- `ELK`: Centralized logging stack (Elasticsearch, Logstash, Kibana)

## 🔧 Key Technologies and Features

### Microservices Architecture
- RESTful Web Services
- Eureka Discovery Server
- Spring Cloud API Gateway
- Load Balancing
- Feign Client
- Resilience4J

### Communication and Integration
- Spring Cloud Bus
- RabbitMQ Message Broker
- OpenFeign Declarative REST Clients

### Security
- Spring Security
- User Authentication
- User Authorization
- JWT (JSON Web Token)
- Role-based Access Control (RBAC)

### Data Management
- Spring Data JPA
- H2 In-Memory Database
- MySQL Database

### Observability
- Zipkin Distributed Tracing
- ELK Stack for Centralized Logging
- Spring Boot Actuator

### Deployment
- Dockerization
- AWS EC2 Deployment

## 🕹️ Getting Started

### Prerequisites
- Java 23
- Maven
- RabbitMQ
- MySQL
- Zipkin
- ELK stack
- Docker (for cloud deployment)
- AWS Account (for cloud deployment)

### Local Setup
1. Clone the repository
2. Install dependencies
3. Run Zipkin, Elasticsearch, Logstash, and RabbitMQ
4. Run services in order:
   - PhotoAppApiConfigServer
   - DiscoveryService
   - ApiGateway
   - Individual Microservices

## 💡 Learning Outcomes

This project demonstrates:
- Building microservices with Spring Boot
- Implementing secure authentication and authorization
- Configuring service discovery
- Managing distributed systems
- Implementing centralized logging and tracing

## 📝 Acknowledgements

This project was developed following [a Udemy course on Spring Boot Microservices](https://www.udemy.com/course/spring-boot-microservices-and-spring-cloud).