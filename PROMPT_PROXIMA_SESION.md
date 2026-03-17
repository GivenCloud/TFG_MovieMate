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

**Últimos commits (rama feat/frontend-pages):**
1. `@feat: favoritas en perfil y etiquetas de spoiler en reseñas`
2. `@feat: filtros avanzados en DiscoverPage (#5)`
3. `@feat: sección ¿Dónde ver? en DetailPage (#6)`
4. `@feat: perfiles de actor/director y cast en DetailPage (#3)` ← ÚLTIMO

**Nuevos ficheros clave:**
- `moviemate-backend/src/main/java/com/moviemate/dto/PersonDto.java`
- `moviemate-backend/src/main/java/com/moviemate/dto/CastMemberDto.java`
- `moviemate-backend/src/main/java/com/moviemate/dto/GenreDto.java`
- `moviemate-backend/src/main/java/com/moviemate/dto/WatchProvidersDto.java`
- `moviemate-frontend/src/features/person/PersonPage.tsx`

**Pendiente MEDIA restante:**
- #4: Estadísticas personales avanzadas (backend `/api/users/me/stats/full` + frontend StatsPage/tab)
- #8: Seguimiento por temporada y episodio (Series → acordeón de temporadas + checkboxes)

**Pendiente BAJA:**
- #7: Insignias/gamificación
- #9: Recomendaciones personalizadas
- #14: Subida real de avatar (multipart)
- #17: Activity RATING_UPDATED / LIST_UPDATED
- #20: Comentarios en listas
- #21: Usuarios sugeridos en HomePage

**Próximo paso recomendado:**
Continuar con #4 (Estadísticas avanzadas) o #8 (Temporadas/episodios). Ambos tienen alto valor para el TFG.

Pregunta al usuario qué quiere hacer a continuación.
