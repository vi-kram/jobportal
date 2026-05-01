# 🧭 JobCompass — Job Portal

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=vi-kram_jobportal&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=vi-kram_jobportal)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=vi-kram_jobportal&metric=bugs)](https://sonarcloud.io/summary/new_code?id=vi-kram_jobportal)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=vi-kram_jobportal&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=vi-kram_jobportal)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=vi-kram_jobportal&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=vi-kram_jobportal)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=vi-kram_jobportal&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=vi-kram_jobportal)

A full-stack **Job Portal** built with Spring Boot Microservices and Angular. Supports Job Seekers, Recruiters, and Admins with real-time notifications, AI-powered resume analysis, and advanced job search.

---

## 🏗️ Architecture

| Service | Port | Description |
|---|---|---|
| `api-gateway` | 8080 | Entry point, JWT routing |
| `eureka-server` | 8761 | Service discovery |
| `config-server` | 8888 | Centralized configuration |
| `user-service` | 8081 | Auth, registration, JWT |
| `job-service` | 8082 | Job CRUD, Redis caching |
| `application-service` | 8083 | Job applications |
| `resume-service` | 8084 | Resume upload & management |
| `notification-service` | 8085 | Email notifications |
| `search-service` | 8086 | Elasticsearch job search |
| `analytics-service` | 8087 | Metrics & analytics |
| `ai-service` | 8089 | AI resume analysis (Gemini) |
| `frontend` | 4200 | Angular UI |

---

## 🛠️ Tech Stack

**Backend**
- Java 17/21, Spring Boot 3
- Spring Cloud (Eureka, Config Server, Gateway)
- PostgreSQL, Redis, RabbitMQ
- OpenFeign, Resilience4j
- JWT Authentication
- Docker

**Frontend**
- Angular 18
- Tailwind CSS
- Lucide Icons

**DevOps**
- GitHub Actions CI/CD
- Docker Hub
- SonarCloud

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL
- Redis
- RabbitMQ
- Docker (optional)

### 1. Clone the repo
```bash
git clone https://github.com/vi-kram/jobportal.git
cd jobportal
```

### 2. Setup Databases
Run the init script:
```bash
psql -U postgres -f init-db.sql
```

### 3. Start Infrastructure
```bash
# Start RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management

# Start Redis
docker run -d --name redis -p 6379:6379 redis
```

### 4. Start Services (in order)
```bash
# 1. Config Server
cd config-server && ./mvnw spring-boot:run

# 2. Eureka Server
cd eureka-server && ./mvnw spring-boot:run

# 3. All other services
cd user-service && ./mvnw spring-boot:run
cd job-service && ./mvnw spring-boot:run
# ... repeat for each service
```

### 5. Start Frontend
```bash
cd frontend
npm install
ng serve
```

Open `http://localhost:4200`

---

## 👥 Roles

| Role | Capabilities |
|---|---|
| **Job Seeker** | Search jobs, apply, track applications, upload resume, AI analysis |
| **Recruiter** | Post jobs, manage applicants, view analytics, export CSV |
| **Admin** | View platform analytics and metrics |

---

## 🔑 Environment Variables

| Variable | Description |
|---|---|
| `MAIL_USERNAME` | Gmail address for notifications |
| `MAIL_PASSWORD` | Gmail App Password |
| `DOCKER_USERNAME` | Docker Hub username |
| `DOCKER_PASSWORD` | Docker Hub password |
| `SONAR_TOKEN` | SonarCloud token |

---

## 📄 License

MIT License — feel free to use and modify.
