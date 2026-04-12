# TO-DO — Memoria TFG MovieMate
> Comparativa con `trabajoFin_JuanPablo.pdf` (mismo tutor, misma asignatura)
> Última actualización: 12/04/2026 — mejoras aplicadas en `rev260323.docx`

---

## 📊 COMPARATIVA (tras rev260323)

| Elemento | TFG referencia (Travily) | MovieMate (rev260323) |
|---|---|---|
| Páginas | 66 | ~70 |
| Figuras declaradas | 55 | 17 reales + 11 placeholders = 28 |
| Tablas | 4 | 4 |
| Referencias | 51 | **43** ✅ (subido desde 31) |
| Screenshots app real | 10+ | 0 reales (**9 placeholders** añadidos) |
| Fragmentos de código | 30+ | 0 reales (**3 placeholders** añadidos) |
| Diagrama de navegación | Sí | Placeholder añadido |
| Pruebas unitarias | 0 | 205 / 16 clases ✅ |
| CI/CD | No | GitHub Actions completo ✅ |
| Kubernetes | No | Sí ✅ |
| IA en conclusiones | Sí | Añadido en rev260323 ✅ |

---

## 🔴 PENDIENTE CRÍTICO — Requiere acción manual (no automatizable)

### 1. Insertar capturas de pantalla reales de la aplicación
Son los `[INSERTAR FIGURA X.X — ...]` que aparecen en el documento. Abrir la app con Docker y tomar cada captura.

| Placeholder en el doc | Qué capturar |
|---|---|
| `[INSERTAR FIGURA 4.3]` (sección 4.2.3) | Diagrama de navegación del front-end (draw.io / Figma) |
| `[INSERTAR FIGURA 4.4]` (sección 4.3.3 — HomePage) | Página de inicio con carruseles |
| `[INSERTAR FIGURA 4.5]` (sección 4.3.3 — DiscoverPage) | Búsqueda con resultados mixtos películas + series |
| `[INSERTAR FIGURA 4.6]` (sección 4.3.3 — ContentDetailPage) | Ficha de película con RatingWidget y reparto |
| `[INSERTAR FIGURA 4.7]` (sección 4.3.3 — ProfilePage) | Perfil con pestaña de estadísticas |
| `[INSERTAR FIGURA 4.8]` (sección 4.3.3 — ListDetailPage) | Lista personalizada con comentarios |
| `[INSERTAR FIGURA 4.9]` (sección 4.3.3 — NotificationsPage) | Bandeja de notificaciones con varios tipos |
| `[INSERTAR FIGURA 4.10]` (sección 4.3.3 — diseño) | Comparativa escritorio vs. móvil |
| `[INSERTAR FIGURA 4.11]` (sección 4.5.4 — CI/CD) | Diagrama arquitectura despliegue |
| `[INSERTAR FIGURA 4.15]` (sección 4.4 — pruebas) | Terminal con `mvn test` en verde (205 tests, BUILD SUCCESS) |
| `[INSERTAR FIGURA 4.16]` (sección 4.4 — JaCoCo) | Informe JaCoCo de cobertura de código |
| `[INSERTAR FIGURA 4.17]` (sección 4.5.3 — CI/CD) | GitHub Actions pipeline completo en verde |

---

### 2. Insertar capturas de fragmentos de código
| Placeholder en el doc | Qué mostrar |
|---|---|
| `[INSERTAR FIGURA 4.12]` (sección 4.3.1) | Código de `JwtAuthFilter.java` (método doFilterInternal) |
| `[INSERTAR FIGURA 4.13]` (sección 4.3.1) | Código de `WebSocketAuthInterceptor.java` |
| `[INSERTAR FIGURA 4.14]` (sección 4.3.1) | Código de `RatingService.createOrUpdate` |

> **Nota**: Usar capturas de VS Code / IntelliJ con tema oscuro para consistencia visual.

---

### 3. Figuras 1–17 ya declaradas en el índice — verificar que están insertadas

| Figura | Descripción |
|--------|-------------|
| Figura 1 | Captura de Letterboxd |
| Figura 2 | Captura de IMDb |
| Figura 3 | Captura de Serializd |
| Figura 4 | Diagrama UML — Casos de uso usuarios no registrados |
| Figura 5 | Diagrama UML — Interacción social |
| Figura 6 | Diagrama UML — Listas personalizadas |
| Figura 7 | Diagrama UML — Valoraciones y reseñas |
| Figura 8 | Diagrama Entidad-Relación (15 entidades) |
| Figura 9 | Arquitectura general cliente-servidor |
| Figura 10 | Estructura de carpetas del back-end |
| Figura 11 | Código FollowerController |
| Figura 12 | Código FollowerService |
| Figura 13 | Código FollowerRepository |
| Figura 14 | Diagrama capa de seguridad |
| Figura 15 | Endpoints de seguidores (Swagger UI) |
| Figura 16 | ContentDto (código) |
| Figura 17 | (verificar en el documento original) |

---

### 4. Actualizar el índice de figuras
Cuando se inserten las figuras 4.3–4.17 y las de código (4.12–4.14), añadirlas al índice de figuras con sus números y títulos definitivos.

---

### 5. Portada — Fecha de entrega
Cambiar "Murcia, a **XX de XXXXXXX de 20XX**" por la fecha real.

---

### 6. Portada — Verificar nombre del tutor
Confirmar que "Francisco García Sánchez" es el nombre oficial correcto.

---

### 7. Tabla 2 — Planificación mensual
Verificar que está como tabla real en el documento.

---

### 8. Tabla 3 — Estimación de horas
Verificar que está como tabla real (305h total).

---

### 9. Tabla 4 — Endpoints (>70)
Verificar que la tabla completa de endpoints cubre todos los grupos de la API.

---

## ✅ COMPLETADO EN rev260322 y rev260323

### Errores técnicos (rev260322)
- **4.2.1 Notification**: 7 tipos + campo de texto contextual ✅
- **4.3.1 NotificationService**: añadido a servicios ✅
- **4.3.3 NotificationsPage**: descripción expandida ✅
- **Extended Abstract**: ya tenía "a comment on a rating" ✅
- **4.1.2.3**: añadidos "Comentar reseñas" y "Comentar en listas" ✅
- **Bibliografía [18]**: Mantine → TanStack Query ✅

### Mejoras estructurales (rev260323)
- **4.2.3**: Placeholder diagrama de navegación del front-end ✅
- **4.3.1**: Placeholders JwtAuthFilter + WebSocketAuthInterceptor + RatingService ✅
- **4.4**: Lista de las 16 clases de test con 205 métodos ✅
- **4.4**: Placeholders captura ejecución tests + informe JaCoCo ✅
- **4.5.3**: Placeholder captura GitHub Actions en verde ✅
- **5.1 Conclusiones**: Párrafo sobre uso de herramientas IA ✅
- **Bibliografía**: Referencias [32]–[43] añadidas (Anthropic, Zustand, Vite, SockJS, STOMP.js, JJWT, Spring WebSocket, Radix UI, shadcn/ui, React Router, Kubernetes, Minikube) ✅

### Ventajas de MovieMate vs. TFG referencia (destacar al tutor)
- 205 tests automatizados en 16 clases de test (Travily: **cero**)
- CI/CD completo con GitHub Actions (Travily: ninguno)
- Kubernetes + Minikube (Travily: Vercel/Render básico)
- 70+ endpoints / 15 entidades (Travily: 28 endpoints / ~8 entidades)
- Gamificación, recomendaciones, moderación, episodios
- WebSocket con autenticación JWT (Travily: Socket.IO sin auth)
- Annex A con especificación formal de casos de uso
