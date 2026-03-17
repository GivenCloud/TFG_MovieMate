# Prompt para la próxima sesión con Claude

Copia y pega esto al inicio de la siguiente conversación:

---

Continuamos el desarrollo del TFG MovieMate. Lee MEMORY.md para el contexto completo.

**Resumen rápido del estado actual:**

PROYECTO: Plataforma social de cine/series (IMDB + Letterboxd + Serializd).
Stack: Spring Boot 3.3.4 + PostgreSQL 16 (backend) / React 19 + TypeScript + TanStack Query 5 + Tailwind CSS 4 (frontend).
Rama activa: `feat/frontend-pages`

**HECHO en las últimas sesiones (resumen):**
- Todas las páginas implementadas y responsive
- Admin, comentarios, perfil público, WebSocket, paginación ReviewList
- DevOps completo (CI/CD + Kubernetes + self-hosted runner)
- #3: PersonPage (/person/:id/:slug) + cast en DetailPage aside con links
- #5: Filtros avanzados en DiscoverPage (género, año, nota mínima, ordenar + URL sync)
- #6: "¿Dónde ver?" en DetailPage (logos streaming/alquiler/compra + link JustWatch)
- #16: Películas favoritas fijadas en ProfilePage (4 posters sobre los tabs)
- #19: Etiquetas de spoiler (checkbox en RatingWidget, overlay blur en ReviewCard)
- #8: Seguimiento por temporada y episodio (SeasonAccordion en DetailPage para TV)

**Últimos commits (rama feat/frontend-pages):**
1. `@feat: seguimiento por temporada y episodio (#8)` ← ÚLTIMO
2. `@feat: favoritas en perfil y etiquetas de spoiler en reseñas`
3. `@feat: filtros avanzados en DiscoverPage (#5)`
4. `@feat: sección ¿Dónde ver? en DetailPage (#6)`
5. `@feat: perfiles de actor/director y cast en DetailPage (#3)`

**Nuevos ficheros clave (#8):**
- `moviemate-backend/src/main/java/com/moviemate/entity/EpisodeWatch.java`
- `moviemate-backend/src/main/java/com/moviemate/repository/EpisodeWatchRepository.java`
- `moviemate-backend/src/main/java/com/moviemate/service/EpisodeWatchService.java`
- `moviemate-backend/src/main/java/com/moviemate/controller/EpisodeWatchController.java`
- `moviemate-backend/src/main/java/com/moviemate/dto/SeasonSummaryDto.java`
- `moviemate-backend/src/main/java/com/moviemate/dto/EpisodeDto.java`
- `moviemate-backend/src/main/java/com/moviemate/dto/SeasonDto.java`
- `moviemate-frontend/src/api/episodes.ts`
- `moviemate-frontend/src/hooks/useEpisodes.ts`
- `moviemate-frontend/src/components/Detail/SeasonAccordion.tsx`

**Pendiente MEDIA restante:**
- #4: Estadísticas personales avanzadas (backend `/api/users/me/stats/full` + frontend StatsPage/tab)

**Pendiente BAJA:**
- #7: Insignias/gamificación
- #9: Recomendaciones personalizadas
- #14: Subida real de avatar (multipart)
- #17: Activity RATING_UPDATED / LIST_UPDATED
- #20: Comentarios en listas
- #21: Usuarios sugeridos en HomePage

**Próximo paso recomendado:**
#4 (Estadísticas avanzadas) — es el único MEDIA que queda y tiene alto valor para el TFG.

Pregunta al usuario qué quiere hacer a continuación.
