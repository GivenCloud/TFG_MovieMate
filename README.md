# MovieMate Backend

![CI/CD Pipeline](https://github.com/GivenCloud/moviemate-backend/actions/workflows/ci-cd.yml/badge.svg)

API Backend para la aplicación MovieMate - Sistema de gestión y recomendación de películas.

## Estado del Proyecto

- ✅ Tests pasando
- ✅ Docker configurado
- ✅ CI/CD activo con GitHub Actions
- ✅ PostgreSQL 16
- ✅ Spring Boot 3.3.4
- ✅ Java 21

## Tecnologías

- Spring Boot 3.3.4
- PostgreSQL 16
- Docker & Docker Compose
- JWT Authentication
- TMDB API Integration
- GitHub Actions CI/CD

## Ejecutar con Docker

### Pasos

\```bash
# Configurar variables de entorno
cp .env.example .env
# Editar .env y añadir TMDB_API_KEY

# Iniciar
docker-compose up -d

# Verificar
curl http://localhost:8080/actuator/health
\```

## Documentación API

Swagger UI: http://localhost:8080/swagger-ui.html