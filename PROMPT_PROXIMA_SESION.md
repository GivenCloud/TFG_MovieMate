# Prompt para la próxima sesión con Claude

Copia y pega esto al inicio de la siguiente conversación:

---

Continuamos el desarrollo del TFG MovieMate. Lee MEMORY.md para el contexto completo.

**Resumen rápido del estado actual:**

PROYECTO: Plataforma social de cine/series (IMDB + Letterboxd + Serializd).
Stack: Spring Boot 3.3.4 + PostgreSQL 16 (backend) / React 19 + TypeScript + TanStack Query 5 + Tailwind CSS 4 (frontend).
Rama activa: `feat/frontend-pages`

**TODAS las tareas MEDIA completadas. Solo quedan BAJA/MUY BAJA.**

**HECHO en las últimas sesiones:**
- #3: PersonPage + cast en DetailPage
- #4: Estadísticas avanzadas (StatsTab en ProfilePage — distribución notas, géneros, actividad mensual)
- #5: Filtros avanzados en DiscoverPage
- #6: "¿Dónde ver?" en DetailPage
- #8: Seguimiento por temporada y episodio (SeasonAccordion)
- #16: Películas favoritas fijadas en perfil
- #19: Etiquetas de spoiler

**Últimos commits (rama feat/frontend-pages):**
1. `@feat: estadísticas personales avanzadas (#4)` ← ÚLTIMO
2. `@fix: corregir import apiClient y tipos en api/episodes.ts`
3. `@feat: seguimiento por temporada y episodio (#8)`
4. `@feat: favoritas en perfil y etiquetas de spoiler en reseñas`
5. `@feat: filtros avanzados en DiscoverPage (#5)`

**Nuevos ficheros clave (#4):**
- `moviemate-backend/.../dto/FullStatsDto.java`
- `moviemate-frontend/src/components/profile/StatsTab.tsx`

**Pendiente BAJA (solo si hay tiempo):**
- #7: Insignias/gamificación
- #9: Recomendaciones personalizadas
- #14: Subida real de avatar (multipart)
- #17: Activity RATING_UPDATED / LIST_UPDATED
- #20: Comentarios en listas
- #21: Usuarios sugeridos en HomePage

**Estado del proyecto:**
Toda la funcionalidad ALTA y MEDIA está implementada. El TFG está prácticamente completo funcionalmente. El siguiente paso natural sería documentación de la memoria o pulir detalles de UX.

Pregunta al usuario qué quiere hacer a continuación.
