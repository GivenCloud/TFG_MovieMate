# MovieMate — Capacidades del rol Admin

## Credenciales de prueba

| Campo    | Valor                  |
|----------|------------------------|
| Username | `admin`                |
| Password | `Admin1234!`           |
| Email    | `admin@moviemate.com`  |

El usuario admin se crea automáticamente al arrancar el backend (`DataSeeder`) si no existe.

---

## Acceso

- **Ruta frontend**: `/admin` (redirige a `/` si el usuario no es admin)
- **Sidebar**: aparece el enlace "🛡️ Administración" solo para admins
- **Seguridad backend**: todos los endpoints `/api/admin/**` requieren `hasRole('ADMIN')` (verificado en `SecurityConfig` + `@PreAuthorize` en el controlador)

---

## Capacidades

### 1. Gestión de usuarios

| Acción | Endpoint | Descripción |
|--------|----------|-------------|
| Listar usuarios | `GET /api/admin/users` | Lista todos los usuarios. Admite búsqueda por `?q=` (filtra por username o email) |
| Cambiar rol | `PUT /api/admin/users/{id}/role` | Promueve o degrada a un usuario (`{ "role": "ADMIN" }` o `{ "role": "USER" }`) |
| Banear / desbanear | `PUT /api/admin/users/{id}/ban` | Bloquea o desbloquea una cuenta (`{ "banned": true/false }`). Un usuario baneado no puede iniciar sesión |

**UI**: Tab "Usuarios" en `/admin` — tabla con buscador, botones "Hacer Admin / Quitar Admin" y "Banear / Desbanear".

---

### 2. Moderación de contenido

| Acción | Endpoint | Descripción |
|--------|----------|-------------|
| Borrar cualquier valoración | `DELETE /api/admin/ratings/{id}` | Elimina permanentemente una valoración sin importar el autor |
| Borrar cualquier comentario | `DELETE /api/admin/comments/{id}` | Soft-delete de un comentario sin importar el autor |

**Nota**: un usuario normal solo puede borrar sus propios comentarios. El admin puede borrar cualquiera.

---

### 3. Sistema de reportes

Cualquier usuario autenticado puede reportar una valoración o comentario (`POST /api/reports`). Los reportes llegan al panel del admin.

| Acción | Endpoint | Descripción |
|--------|----------|-------------|
| Ver reportes | `GET /api/admin/reports` | Lista reportes. Filtrable por estado: `?status=PENDING`, `RESOLVED` o `DISMISSED` |
| Resolver reporte | `PUT /api/admin/reports/{id}/resolve` | Marca el reporte como resuelto (el admin debe borrar el contenido manualmente si procede) |
| Desestimar reporte | `PUT /api/admin/reports/{id}/dismiss` | Marca el reporte como desestimado (sin acción) |

**Motivos de reporte disponibles**: `SPAM`, `INAPPROPRIATE`, `SPOILER`, `OTHER`
**Estados del reporte**: `PENDING` → `RESOLVED` o `DISMISSED`

**UI**: Tab "Reportes" en `/admin` — lista con filtro por estado, botones "Resolver" y "Desestimar".

---

## Flujo típico de moderación

1. Un usuario ve una valoración o comentario inapropiado y hace clic en 🚩
2. Selecciona el motivo en el `ReportDialog` y envía
3. El admin accede a `/admin` → tab "Reportes"
4. Ve el reporte con `status = PENDING`, el target type, ID y motivo
5. Si procede, va a `DELETE /api/admin/ratings/{id}` o `DELETE /api/admin/comments/{id}` para borrar el contenido
6. Marca el reporte como `RESOLVED` o `DISMISSED`

---

## Lo que el admin NO puede hacer (aún pendiente)

- No hay notificación automática al admin cuando llega un nuevo reporte (el admin debe revisar manualmente)
- No hay acción "borrar + resolver en un solo click" (hay que hacer las dos operaciones por separado)
- No hay log de acciones de moderación (quién borró qué y cuándo)
- No hay distinción entre "super admin" y "moderador" (un solo nivel de admin)
- No hay sección de comentarios/valoraciones directa en el panel (solo se acceden via reportes)

---

## Archivos clave

| Tipo | Archivo |
|------|---------|
| Backend — Controlador | `controller/AdminController.java` |
| Backend — Reportes | `controller/ReportController.java` |
| Backend — Servicio reportes | `service/ContentReportService.java` |
| Backend — Entidad reporte | `entity/ContentReport.java` |
| Backend — Enum rol | `entity/Role.java` |
| Backend — Campo en User | `entity/User.java` (campos `role` y `banned`) |
| Backend — Seguridad | `config/SecurityConfig.java` (regla `/api/admin/**`) |
| Backend — Seeder | `config/DataSeeder.java` (crea usuario `admin`) |
| Frontend — Página | `features/admin/AdminPage.tsx` |
| Frontend — Ruta protegida | `components/AdminRoute.tsx` |
| Frontend — Dialog reporte | `features/moderation/ReportDialog.tsx` |
| Frontend — API admin | `api/admin.ts` |
| Frontend — Hooks admin | `hooks/useAdmin.ts` |
