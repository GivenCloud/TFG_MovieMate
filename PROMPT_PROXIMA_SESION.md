# Prompt para la próxima sesión con Claude

Copia y pega esto al inicio de la siguiente conversación:

---

Continuamos el TFG MovieMate. Lee MEMORY.md para el contexto completo.

**Resumen rápido del estado actual:**

PROYECTO: Plataforma social de cine/series (IMDB + Letterboxd + Serializd).
Stack: Spring Boot 3.3.4 + PostgreSQL 16 (backend) / React 19 + TypeScript + TanStack Query 5 + Tailwind CSS 4 (frontend).
Rama activa: `feat/frontend-pages`

**EL PROYECTO ESTÁ FUNCIONALMENTE COMPLETO.**
Todos los bugs (B1–B19) y mejoras (M1–M34) están implementados y resueltos.

---

**HECHO en la última sesión (2026-03-18):**

Backend:
- **B19** — eliminada llamada redundante a `contentRepository.save(content)` en `ContentService.fetchFromTmdb` (TmdbService ya persistía la entidad; la segunda llamada causaba error de Hibernate al re-insertar). Añadido `@Slf4j` + `log.error(...)` en `GlobalExceptionHandler.handleRuntimeException`.
- Archivos modificados: `service/ContentService.java`, `exception/GlobalExceptionHandler.java`

Memoria TFG:
- Generado **`Memoria TFG_rev260319.docx`** (802 párrafos) a partir de `rev260318.docx` con tres bloques de cambios:
  - **§4.3.2 Arquitectura del front-end** — completamente reescrita con 10 subsecciones (Heading 4): estructura del proyecto (árbol de directorios feature-based), navegación y autenticación, gestión del estado (Zustand + TanStack Query), y una subsección por página (HomePage, DiscoverPage, DetailPage, ProfilePage, ListsPage, Actividad/WebSocket, Sistema de diseño). Cada subsección incluye una pista `[Figura 4.X: ...]` para insertar capturas de pantalla.
  - **§4.5.3 CI/CD** — añadidos 3 párrafos sobre la estrategia de ramificación: `develop` para el desarrollo activo, merge a `main` solo cuando el pipeline pasa, `main` dispara la construcción de la imagen Docker.
  - **§4.5.4 Despliegue en Kubernetes con Minikube** — sección nueva (Heading 3) con: introducción a Kubernetes y Minikube, descripción de los 8 manifiestos (Namespace, ConfigMap, Secret, PVC, Deployments, Services), proceso de despliegue local en 5 pasos con comandos exactos, integración con el pipeline de CI/CD.
- Script utilizado: `TFG_MovieMate/expand_frontend_k8s.py`

**Último commit de código:**
`@fix: eliminar doble save en ContentService y añadir logging a GlobalExceptionHandler`

---

**Archivos clave de la memoria:**
- `TFG_MovieMate/Memoria TFG_rev260319.docx` ← versión más reciente
- `TFG_MovieMate/Memoria TFG_rev260318.docx` ← revisión anterior
- `TFG_MovieMate/expand_frontend_k8s.py` ← script python-docx para modificar el .docx

---

**Tareas manuales pendientes en Word** (no automatizables fácilmente con python-docx):
- Términos en inglés en cursiva (React, Spring Boot, JWT, endpoint, etc.)
- Nombres de entidades/clases en Courier New
- Colores en las filas de la tabla de endpoints
- **§II Resumen extendido en inglés** (~2000 palabras) — abstract obligatorio del TFG
- Insertar capturas de pantalla reales en las posiciones `[Figura 4.X: ...]`
- Actualizar tabla de contenidos (F9 en Word)

---

**Posibles tareas para esta sesión:**

1. **§II Abstract en inglés** — redactar el resumen extendido (~2000 palabras) obligatorio del TFG
2. **Revisar o ampliar cualquier sección de la memoria** — si el tutor pide más detalle o hay secciones incompletas
3. **Capturas de pantalla** — decidir qué capturas hacer y en qué orden insertarlas en las posiciones `[Figura X.X: ...]`
4. **Preparar la defensa oral** — esquema de presentación, posibles preguntas del tribunal, estructura de slides
5. **Correcciones de formato** — automatizar cursivas, Courier New u otros ajustes de estilo en el .docx
6. **Ajustes de código de última hora** — si surge algún bug o mejora menor antes de entregar

Pregunta al usuario qué quiere hacer a continuación.
