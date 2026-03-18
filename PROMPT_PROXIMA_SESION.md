# Prompt para la próxima sesión con Claude

Copia y pega esto al inicio de la siguiente conversación:

---

Continuamos el TFG MovieMate. Lee MEMORY.md para el contexto completo.

**Resumen rápido del estado actual:**

PROYECTO: Plataforma social de cine/series (IMDB + Letterboxd + Serializd).
Stack: Spring Boot 3.3.4 + PostgreSQL 16 (backend) / React 19 + TypeScript + TanStack Query 5 + Tailwind CSS 4 (frontend).
Rama activa: `feat/frontend-pages` — **pipeline CI/CD verde (205/205 tests)**.

**EL PROYECTO ESTÁ FUNCIONALMENTE COMPLETO.**
Todos los bugs (B1–B19) y mejoras (M1–M34) están implementados y resueltos.

---

**HECHO en la última sesión (2026-03-26):**

Memoria TFG:
- Generado **`Memoria TFG_rev260320.docx`** (825 párrafos):
  - §II Abstract extendido en inglés insertado (~1894 palabras, 7 secciones con headings en negrita).
    Secciones: Context and Motivation, Objectives, Methodology, Back-End Architecture, Front-End Architecture, Testing and Deployment, Results and Conclusions.
- Generado **`Memoria TFG_rev260321.docx`** (831 párrafos) ← **VERSIÓN MÁS RECIENTE**:
  - Tabla de endpoints: 41 → 64 filas (+23 endpoints faltantes: recommendations, badges, avatar, trending, comments, listComments, episodeWatch, reports, admin).
  - §4.2.1 ampliada con 3 párrafos sobre 7 entidades faltantes: Comment, ReviewLike, EpisodeWatch, ListComment, ContentReport, UserBadge.
  - §4.3.1 ampliada con 3 párrafos sobre: WebSocket/STOMP, BadgeService/UserStatsService/CacheCleaner, SwaggerConfig/@RequirePublicProfile.

**Scripts usados:**
- `TFG_MovieMate/insert_abstract.py` → generó rev260320
- `TFG_MovieMate/update_backend_docs.py` → generó rev260321

**Últimos commits (rama `feat/frontend-pages`):**
1. `@fix: eliminar doble save en ContentService y añadir logging a GlobalExceptionHandler`
2. `@fix: actualizar ContentServiceTest tras eliminar doble save en fetchFromTmdb` ← ÚLTIMO

---

**Archivos clave de la memoria:**
- `TFG_MovieMate/Memoria TFG_rev260321.docx` ← versión más reciente
- `TFG_MovieMate/update_backend_docs.py` ← último script python-docx ejecutado

---

**Tareas manuales pendientes en Word** (no automatizables fácilmente):
- Términos en inglés en cursiva (React, Spring Boot, JWT, endpoint, etc.)
- Nombres de entidades/clases en Courier New
- Colores en filas de la tabla de endpoints
- Insertar capturas de pantalla reales en las posiciones `[Figura 4.X: ...]`
- Actualizar tabla de contenidos (F9 en Word)

---

**Posibles tareas para esta sesión:**

1. **Revisar o ampliar secciones de la memoria** — si el tutor pide más detalle en algún punto
2. **Capturas de pantalla** — planificar qué capturas hacer y dónde insertarlas
3. **Preparar la defensa oral** — esquema de presentación, posibles preguntas del tribunal, slides
4. **Correcciones de formato** — automatizar cursivas, Courier New u otros ajustes en el .docx
5. **Ajustes de código de última hora** — si surge algún bug antes de la entrega

Pregunta al usuario qué quiere hacer a continuación.
