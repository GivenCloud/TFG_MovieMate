# Prompt para la próxima sesión con Claude

Copia y pega esto al inicio de la siguiente conversación:

---

Continuamos el desarrollo del TFG MovieMate. Lee MEMORY.md para el contexto completo.

**Resumen rápido del estado actual:**

PROYECTO: Plataforma social de cine/series (IMDB + Letterboxd + Serializd).
Stack: Spring Boot 3.3.4 + PostgreSQL 16 (backend) / React 19 + TypeScript + TanStack Query 5 + Tailwind CSS 4 (frontend).
Rama activa: `feat/frontend-pages`

**EL PROYECTO ESTÁ FUNCIONALMENTE COMPLETO.**
Todas las prioridades ALTA, MEDIA y BAJA están implementadas (M1–M31, B1–B12).

---

**HECHO en las últimas sesiones (BAJA):**
- #17: RATING_UPDATED / LIST_UPDATED en ActivityService (detecta edición por `updatedAt > createdAt + 1min`)
- #21: Usuarios sugeridos en HomePage ("Cinéfilos que quizás conozcas")
- #14: Subida real de avatar (multipart → filesystem → servido como estático)
- #9: Recomendaciones personalizadas ("Para ti ✨" en HomePage, top género → TMDB discover)
- #7: Insignias/gamificación (10 badges, BadgeService, UserBadge entity, chips en ProfilePage)
- #20: Comentarios en listas (ListComment entity, ListCommentController, sección en ListDetailPage)

**Último commit:**
`@feat: subida de avatar, recomendaciones, insignias y comentarios en listas`

---

**Archivos nuevos en última sesión:**

Backend:
- `entity/UserBadge.java` — tabla user_badges
- `entity/ListComment.java` — tabla list_comments
- `dto/BadgeDto.java`, `dto/ListCommentResponse.java`
- `repository/UserBadgeRepository.java`, `repository/ListCommentRepository.java`
- `service/BadgeService.java` — 10 insignias con evaluación idempotente
- `service/ListCommentService.java`
- `controller/ListCommentController.java` — GET/POST/DELETE /api/lists/{id}/comments
- `config/WebMvcConfig.java` — sirve /uploads/** desde filesystem

Frontend:
- `types/index.ts` — añadidos `BadgeDto` y `ListCommentResponse`
- `api/users.ts` — `getMyBadges()`, `getBadgesByUserId()`, `getMyRecommendations()`, `uploadAvatar()`
- `api/comments.ts` — `getByList()`, `createForList()`, `deleteFromList()`
- `lib/queryKeys.ts` — `users.badges`, `users.badgesByUser`, `comments.byList`

---

**Posibles tareas para esta sesión:**

1. **Preparar la memoria del TFG** — documentar arquitectura, decisiones técnicas, capturas de pantalla
2. **Pulir detalles de UX** — revisar el diseño de alguna página concreta, ajustar colores/espaciados
3. **Tests** — añadir tests de integración en el backend o tests de componentes en el frontend
4. **Hacer el commit y push** del último batch si aún no se ha hecho
5. **Preparar la demo** — revisar el DataSeeder, asegurarse de que docker-compose up levanta todo correctamente

Pregunta al usuario qué quiere hacer a continuación.
