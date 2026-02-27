# 🎬 MovieMate Backend

![CI/CD](https://github.com/TU_USUARIO/TFG_MovieMate/workflows/CI%2FCD%20Pipeline%20-%20MovieMate%20Backend/badge.svg)
![Coverage](https://img.shields.io/badge/coverage-99%25-brightgreen)
![Version](https://img.shields.io/badge/version-1.0.0-blue)

> API REST para la gestión y recomendación de películas y series. Trabajo de Fin de Grado (TFG) - Universidad de Murcia

## 📋 Descripción

MovieMate es una plataforma social para amantes del cine que permite...

## ✨ Características

- 🔐 Autenticación JWT
- 🎥 Integración con TMDB API
- ⭐ Sistema de valoraciones
- 📝 Listas personalizadas
- 👥 Sistema de seguimiento de usuarios
- 🐳 Dockerizado
- 🧪 163 tests unitarios (99% cobertura)
- 🔄 CI/CD con GitHub Actions

## 🛠️ Tecnologías

- **Backend:** Spring Boot 3.3.4, Java 21
- **Base de datos:** PostgreSQL 16
- **Seguridad:** Spring Security + JWT
- **Testing:** JUnit 5, Mockito, AssertJ
- **Containerización:** Docker & Docker Compose
- **CI/CD:** GitHub Actions
- **API Externa:** TMDB API

## 🚀 Inicio Rápido

### Prerrequisitos

- Docker Desktop
- Java 21 (solo para desarrollo local)
- Maven (solo para desarrollo local)

### Instalación

1. Clonar el repositorio
\`\`\`bash
git clone https://github.com/TU_USUARIO/TFG_MovieMate.git
cd TFG_MovieMate/MovieMate/moviemate-backend
\`\`\`

2. Configurar variables de entorno
\`\`\`bash
cp .env.example .env
# Editar .env y añadir TMDB_API_KEY
\`\`\`

3. Ejecutar con Docker
\`\`\`bash
docker-compose up -d
\`\`\`

4. Verificar
\`\`\`bash
curl http://localhost:8080/actuator/health
\`\`\`

## 📚 Documentación API

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI Spec:** http://localhost:8080/v3/api-docs

## 🧪 Tests

\`\`\`bash
mvn test
\`\`\`

**Cobertura:** 99% (163 tests)

## 📊 Arquitectura

[Diagrama de arquitectura]

## 👨‍💻 Autor

**Christian Matas**
- Universidad: [Universidad de Murcia]
- Grado: Ingeniería Informática
- Año: 2025-2026

## 🙏 Agradecimientos

- TMDB por su API
- [Otros créditos]

---

⭐ Si este proyecto te ha sido útil, considera darle una estrella
\`\`\`

---