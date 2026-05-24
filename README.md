# 🎬 MovieMate Backend

![CI/CD](https://github.com/GivenCloud/TFG_MovieMate/actions/workflows/backend-ci.yml/badge.svg)
![Coverage](https://img.shields.io/badge/tests-340-success)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen)
![License](https://img.shields.io/badge/license-Academic-blue)

> API REST y plataforma social para la gestión, valoración y descubrimiento de películas y series.  
> Trabajo Fin de Grado (TFG) – Universidad de Murcia.

---

## 📋 Descripción

MovieMate es una plataforma web social orientada a la gestión, valoración y descubrimiento de contenido audiovisual. El proyecto nace con el objetivo de unificar en una única aplicación funcionalidades inspiradas en plataformas como Letterboxd, IMDb y Serializd, evitando la fragmentación entre herramientas especializadas.

La aplicación permite a los usuarios:

- 🎥 Descubrir películas y series
- ⭐ Valorar contenido y escribir reseñas
- 📝 Crear listas personalizadas
- 👥 Seguir a otros usuarios
- 🔔 Recibir notificaciones en tiempo real
- 📊 Consultar estadísticas y progreso de visionado
- 🛡️ Reportar contenido y moderar la plataforma
- 🏅 Obtener insignias mediante gamificación

MovieMate se desarrolla como una aplicación full-stack basada en arquitectura cliente-servidor, utilizando Spring Boot en el back-end y React + TypeScript en el front-end.

---

## ✨ Características principales

### 🔐 Seguridad y autenticación
- Autenticación stateless mediante JWT
- Spring Security
- Protección de rutas y control de acceso por roles
- Gestión de usuarios y perfiles privados

### 🎥 Gestión de contenido audiovisual
- Integración con TMDB API
- Sincronización bajo demanda
- Caché local para reducir llamadas externas
- Soporte completo para películas y series

### ⭐ Sistema social
- Valoraciones y reseñas
- Likes y comentarios en reseñas
- Comentarios en listas
- Sistema de seguidores y solicitudes de seguimiento
- Feed de actividad personalizado

### 📺 Funcionalidades avanzadas
- Seguimiento de episodios vistos
- Sistema de recomendaciones personalizadas
- Sistema de insignias y gamificación
- Notificaciones en tiempo real mediante WebSockets
- Sistema de reportes y moderación de contenido

### 🛠️ DevOps y calidad
- Docker y Docker Compose
- CI/CD con GitHub Actions
- Tests automatizados con JUnit y Mockito
- Cobertura de pruebas mediante JaCoCo
- Arquitectura modular y mantenible

---

## 🏗️ Arquitectura

MovieMate sigue una arquitectura cliente-servidor basada en un monolito modular y organizada en capas:

- **Capa de presentación:** Controladores REST
- **Capa de aplicación:** Servicios y lógica de negocio
- **Capa de persistencia:** Repositorios JPA
- **Capa de seguridad:** JWT + Spring Security

### Tecnologías principales

#### Back-end
- Java 21
- Spring Boot 3.3.4
- Spring Security
- Spring Data JPA
- PostgreSQL
- WebSockets (STOMP + SockJS)
- JWT (JJWT)
- SpringDoc OpenAPI / Swagger

#### Front-end
- React
- TypeScript
- Tailwind CSS
- Zustand
- TanStack Query
- Axios
- React Router v6
- Shadcn/ui + Radix UI

#### DevOps
- Docker
- Docker Compose
- GitHub Actions
- Kubernetes (Minikube)

---

## 🧩 Funcionalidades implementadas

### 👤 Usuarios
- Registro e inicio de sesión
- Gestión de perfil
- Perfil público/privado
- Avatar y biografía

### 🎞️ Contenido
- Descubrimiento de películas y series
- Tendencias y populares
- Fichas detalladas
- Reparto y metadatos

### 📝 Listas
- Crear listas personalizadas
- Editar y eliminar listas
- Añadir y eliminar contenido
- Listas públicas y privadas

### ⭐ Valoraciones y reseñas
- Puntuar películas y series
- Crear reseñas
- Editar y eliminar valoraciones
- Likes y comentarios

### 👥 Social
- Seguir usuarios
- Solicitudes de seguimiento
- Feed de actividad
- Notificaciones en tiempo real

### 🛡️ Administración
- Panel de administración
- Gestión de usuarios
- Moderación de contenido
- Resolución de reportes

---

## 🚀 Inicio rápido

### Prerrequisitos

- Docker Desktop
- Java 21 (desarrollo local)
- Maven (desarrollo local)
- Node.js 20+ (front-end)

---

## ⚙️ Instalación

### 1️⃣ Clonar el repositorio

```bash
git clone https://github.com/GivenCloud/TFG_MovieMate.git
cd TFG_MovieMate
```

### 2️⃣ Configurar variables de entorno

```bash
cp .env.example .env
```

Configura las siguientes variables:

```env
TMDB_API_KEY=your_tmdb_api_key
JWT_SECRET=your_jwt_secret
POSTGRES_DB=moviemate
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

---

### 3️⃣ Ejecutar con Docker Compose

```bash
docker compose up --build
```

---

### 4️⃣ Acceder a los servicios

| Servicio | URL |
|---|---|
| Front-end | http://localhost:3000 |
| Back-end | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5434 |

---

## 📚 Documentación API

### Swagger/OpenAPI
- Swagger UI:  
  `http://localhost:8080/swagger-ui.html`

- OpenAPI JSON:  
  `http://localhost:8080/v3/api-docs`

La API REST sigue principios RESTful y se divide en:

- 🟢 Endpoints públicos
- 🔴 Endpoints autenticados
- 🔵 Endpoints de administrador

---

## 🧪 Testing

### Ejecutar tests

```bash
mvn test
```

### Cobertura

- ✅ 340 tests automatizados
- ✅ JUnit 5
- ✅ Mockito
- ✅ JaCoCo

Los tests validan:
- Servicios
- Repositorios
- Seguridad
- Casos de uso principales
- Integración de componentes

---

## 🐳 Docker

### Construcción manual

```bash
docker build -t moviemate-backend .
```

### Ejecutar contenedor

```bash
docker run -p 8080:8080 moviemate-backend
```

---

## 🔄 CI/CD

El proyecto incorpora pipelines automatizados mediante GitHub Actions para:

- Ejecución automática de tests
- Verificación de builds
- Generación de imágenes Docker
- Validación continua del código

---

## 📈 Características técnicas destacadas

- Arquitectura en capas
- API RESTful
- Autenticación JWT
- Persistencia relacional con PostgreSQL
- Sincronización inteligente con TMDB
- WebSockets en tiempo real
- Caché bajo demanda
- Diseño modular
- Sistema de moderación
- Gamificación mediante insignias

---

## 🌐 Integración con TMDB

MovieMate utiliza la API de TMDB para:

- Obtener metadatos reales
- Recuperar posters y backdrops
- Consultar tendencias
- Obtener reparto y detalles
- Generar recomendaciones

> Este producto utiliza la API de TMDB pero no está respaldado ni certificado por TMDB.

---

## 🔮 Trabajo futuro

Líneas futuras contempladas en el proyecto:

- Recomendaciones mediante Machine Learning
- Filtrado colaborativo
- Aplicaciones móviles nativas
- Arquitectura basada en microservicios
- Integración con plataformas de streaming
- Escalado horizontal con Kubernetes

---

## 👨‍💻 Autor

**Christian Matas Conesa**

- 🎓 Grado en Ingeniería Informática
- 🏫 Universidad de Murcia
- 📅 Curso 2025–2026

Tutor:
- Francisco García Sánchez

---

## 📄 Licencia

Proyecto desarrollado con fines académicos como Trabajo Fin de Grado.

---

⭐ Si este proyecto te ha resultado interesante, considera darle una estrella al repositorio.