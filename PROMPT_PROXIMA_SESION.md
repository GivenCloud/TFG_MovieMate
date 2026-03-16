# Prompt para la próxima sesión con Claude

Copia y pega esto al inicio de la siguiente conversación:

---

Continuamos el desarrollo del TFG MovieMate. Lee MEMORY.md para el contexto completo.

**Resumen rápido del estado actual:**

PROYECTO: Plataforma social de cine/series (IMDB + Letterboxd + Serializd).
Stack: Spring Boot 3.3.4 + PostgreSQL 16 (backend) / React 19 + TypeScript + TanStack Query 5 + Tailwind CSS 4 (frontend).
Rama activa: `feat/frontend-pages`

**HECHO en las últimas sesiones (resumen):**
- Todas las páginas implementadas y responsive (HomePage, DiscoverPage, DetailPage, ProfilePage, ListsPage, ListDetailPage, SpecialListPage, NotificationsPage, SettingsPage, ActivityPage)
- #11: Perfil público — tabs Valoraciones y Listas muestran datos reales de perfiles ajenos
- #1: Comentarios planos en valoraciones — CommentSection en ReviewList con toggle, form inline, delete propio
- #2: Rol Admin + panel de moderación — AdminPage, AdminRoute, ReportDialog, botón 🚩, link Administración en Sidebar
- #10: Búsqueda de usuarios en DiscoverPage — modo Contenido / Usuarios con UserCard y sugerencias
- #12: Lista "Ya vistas" (WATCHED) — SpecialListPage, ruta /watched, Sidebar
- #13: Cambio de contraseña — SettingsPage con sección Seguridad, endpoint PUT /api/users/me/password
- WebSocket STOMP, paginación ReviewList, mejoras responsive M1-M19

**DevOps — estado actual (ver `DEVOPS.md` para documentación completa):**
- ✅ Paso 1: Dockerfile frontend multi-stage (node:22-alpine → nginx:1.27-alpine)
- ✅ Paso 2: nginx.conf.template con envsubst para BACKEND_HOST en runtime
- ✅ Paso 3: docker-compose.yml completo (postgres + backend + frontend)
- ✅ Paso 4: GitHub Actions — ci.yml (tests en feat/*) + cd.yml (build+push GHCR en main)
- ✅ Paso 4b: Helm chart completo en k8s/moviemate/ con subdirectorios backend/frontend/postgres
- ✅ Paso 5: Minikube — desplegado y verificado en local con namespace moviemate
- ✅ Decisión final: NO se despliega en cloud real — todos los proveedores gratuitos requieren método de pago
- ✅ Pipeline CD completo con self-hosted runner:
  - Self-hosted runner registrado en WSL2 (`~/actions-runner-moviemate/`)
  - imagePullSecrets en Helm chart para GHCR privado (`ghcr-secret` en namespace moviemate)
  - Job `deploy` en cd.yml activo: push a main → tests → build+push GHCR → helm upgrade Minikube automático
  - Fix aplicado: imagen tag lowercase (`givencloud`) en cd.yml
  - CORS configurado para `http://moviemate.local:8080`

**Bugs/fixes aplicados en esta sesión que están en el código:**
- `SecurityConfig.java`: CORS lee de env var `CORS_ALLOWED_ORIGINS` con `@Value`, soporta múltiples orígenes separados por coma
- `k8s/moviemate/templates/postgres/postgres-statefulset.yaml`: probe pg_isready con args separados (no sh -c)
- `k8s/moviemate/templates/ingress.yaml`: certManagerIssuer como campo limpio (antes usaba sintaxis inválida con '-')
- 5 tests de servicios corregidos (constructores desincronizados): UserServiceTest, ActivityServiceTest, ListServiceTest, NotificationServiceTest, RatingServiceTest

**Ficheros clave:**
- `TFG_MovieMate/DEVOPS.md` — documentación completa del despliegue
- `TFG_MovieMate/PENDIENTE.md` — funcionalidades pendientes con estado
- `TFG_MovieMate/k8s/moviemate/` — Helm chart completo
- `.github/workflows/ci.yml` y `cd.yml` — pipelines CI/CD
- Backend: `MovieMate/moviemate-backend/src/main/java/com/moviemate/`
- Frontend: `MovieMate/moviemate-frontend/src/`

**Próximo paso recomendado:**
DevOps completado para el TFG. Continuar con features pendientes de PENDIENTE.md (prioridades MEDIAS: #3 perfiles actor/director, #4 stats avanzadas, #5 filtros Discover, etc.)

Pregunta al usuario qué quiere hacer a continuación.
