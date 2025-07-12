# Human Resource Management System

A comprehensive HRM solution built with modern technologies and enterprise-grade features.

## 🏗️ Architecture

### Backend
- **Framework**: Spring Boot 3.2
- **Database**: PostgreSQL
- **Authentication**: Keycloak OAuth 2.0
- **Workflow Engine**: Apache Airflow
- **Telemetry**: Micrometer + Prometheus
- **API Documentation**: OpenAPI 3.0

### Frontend
- **Framework**: React 18 + TypeScript
- **UI Library**: Tailwind CSS
- **State Management**: Redux Toolkit
- **Build Tool**: Vite
- **Testing**: Jest + React Testing Library

### DevOps
- **Containerization**: Docker & Docker Compose
- **Monitoring**: Prometheus + Grafana
- **CI/CD**: GitHub Actions

## 🚀 Features

### Core Modules
- **Employee Management**: Complete profiles, departments, organizational structure
- **Leave Management**: Requests, approvals, workflow automation
- **Attendance Tracking**: Daily check-ins, timesheet management
- **Performance Reviews**: Goal setting, evaluations, feedback cycles
- **Recruitment**: Job postings, candidate tracking, interview scheduling
- **Payroll Overview**: Salary information, payment tracking
- **HR Analytics**: Dashboard with key metrics and insights

### Technical Features
- Role-based access control (HR Admin vs Employee)
- Real-time notifications
- Audit logging
- Data export capabilities
- Mobile-responsive design
- Multi-tenant support

## 🛠️ Setup Instructions

### Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 14+

### Quick Start
```bash
# Clone the repository
git clone <repository-url>
cd hrm-system

# Start infrastructure services
docker-compose up -d postgres keycloak airflow

# Start backend
cd backend
./mvnw spring-boot:run

# Start frontend
cd ../frontend
npm install
npm start
```

### Environment Configuration
Copy `.env.example` to `.env` and configure:
- Database connection strings
- Keycloak server details
- Airflow connection
- JWT secrets

## 📁 Project Structure

```
hrm-system/
├── backend/                 # Spring Boot application
│   ├── src/main/java/
│   │   ├── config/         # Configuration classes
│   │   ├── controller/     # REST controllers
│   │   ├── entity/         # JPA entities
│   │   ├── repository/     # Data repositories
│   │   ├── service/        # Business logic
│   │   └── dto/           # Data transfer objects
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/   # Flyway migrations
├── frontend/               # React application
│   ├── src/
│   │   ├── components/    # Reusable UI components
│   │   ├── pages/         # Page components
│   │   ├── services/      # API services
│   │   ├── store/         # Redux store
│   │   └── utils/         # Utility functions
│   └── public/
├── airflow/               # Workflow definitions
│   ├── dags/             # Airflow DAGs
│   └── plugins/          # Custom plugins
├── docker/               # Docker configurations
└── docs/                 # Documentation
```

## 🔐 Security

- OAuth 2.0 with Keycloak
- JWT token-based authentication
- Role-based authorization
- API rate limiting
- Input validation & sanitization

## 📊 Monitoring

- Application metrics with Micrometer
- Performance monitoring with Prometheus
- Centralized logging
- Health checks and alerts

## 🧪 Testing

```bash
# Backend tests
cd backend
./mvnw test

# Frontend tests
cd frontend
npm test
```

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.