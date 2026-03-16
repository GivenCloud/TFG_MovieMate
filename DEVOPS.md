# MovieMate — DevOps: Contenerización, CI/CD y Kubernetes

## Índice

1. [Visión general](#1-visión-general)
2. [Contenerización](#2-contenerización)
   - [Backend — Dockerfile](#21-backend--dockerfile)
   - [Frontend — Dockerfile](#22-frontend--dockerfile)
   - [nginx.conf.template](#23-nginxconftemplate)
3. [Docker Compose — desarrollo local](#3-docker-compose--desarrollo-local)
4. [GitHub Actions — CI/CD](#4-github-actions--cicd)
   - [ci.yml — Integración continua](#41-ciyml--integración-continua)
   - [cd.yml — Entrega continua](#42-cdyml--entrega-continua)
5. [Helm Chart](#5-helm-chart)
   - [Estructura](#51-estructura)
   - [values.yaml y values-prod.yaml](#52-valuesyaml-y-values-prodyaml)
   - [Secrets](#53-secrets)
   - [Templates](#54-templates)
6. [Despliegue en Minikube](#6-despliegue-en-minikube)
   - [Requisitos](#61-requisitos)
   - [Paso a paso](#62-paso-a-paso)
   - [Acceso desde Windows con WSL2](#63-acceso-desde-windows-con-wsl2)
   - [Problemas encontrados y soluciones](#64-problemas-encontrados-y-soluciones)
7. [Producción — Oracle Cloud + k3s](#7-producción--oracle-cloud--k3s)
8. [Referencia de comandos útiles](#8-referencia-de-comandos-útiles)

---

## 1. Visión general

El stack de MovieMate se compone de tres servicios:

| Servicio | Tecnología | Puerto interno |
|---|---|---|
| **Backend** | Spring Boot 3 + JPA | 8080 |
| **Frontend** | React + Vite (servido por nginx) | 80 |
| **Base de datos** | PostgreSQL 16 | 5432 |

### Flujo de despliegue

```
Desarrollador
    │
    ├─► push a feat/* ──► ci.yml ──► tests + build (sin push de imagen)
    │
    └─► merge a main ───► cd.yml ──► tests → build → push GHCR → helm deploy (k3s)
```

### Registro de imágenes

Se usa **GitHub Container Registry (GHCR)** — gratuito en repositorios públicos, integrado con el `GITHUB_TOKEN` de GitHub Actions sin secretos adicionales.

- `ghcr.io/GivenCloud/moviemate-backend:{git-sha}`
- `ghcr.io/GivenCloud/moviemate-frontend:{git-sha}`

---

## 2. Contenerización

### 2.1 Backend — Dockerfile

Ubicación: `MovieMate/moviemate-backend/Dockerfile`

```
Etapa 1 (build): maven:3.9-eclipse-temurin-21
    └─► mvn package -DskipTests → target/moviemate-backend.jar

Etapa 2 (runtime): eclipse-temurin:21-jre-alpine
    └─► copia el .jar y lo ejecuta con java -jar
```

El multi-stage build evita que las herramientas de compilación (Maven, JDK completo) entren en la imagen final. La imagen de runtime solo tiene el JRE, reduciendo el tamaño considerablemente (~268 MB vs ~600 MB).

### 2.2 Frontend — Dockerfile

Ubicación: `MovieMate/moviemate-frontend/Dockerfile`

```
Etapa 1 (build): node:22-alpine
    ├─► npm ci (instala dependencias exactas del package-lock.json)
    └─► vite build → dist/

Etapa 2 (runtime): nginx:1.27-alpine
    ├─► copia dist/ al directorio de nginx
    └─► copia nginx.conf.template a /etc/nginx/templates/
```

**ARGs de build** (se pueden sobreescribir sin reconstruir en producción):
- `VITE_API_BASE_URL=/api` — URL relativa, funciona tanto con nginx proxy como con k8s Ingress
- `VITE_TMDB_IMAGE_BASE=https://image.tmdb.org/t/p` — base para imágenes de TMDB

### 2.3 nginx.conf.template

Ubicación: `MovieMate/moviemate-frontend/nginx.conf.template`

nginx tiene un mecanismo integrado: al arrancar el contenedor, procesa automáticamente todos los ficheros en `/etc/nginx/templates/*.template` sustituyendo variables de entorno con `envsubst`, y deja el resultado en `/etc/nginx/conf.d/`.

Esto permite inyectar la dirección del backend en **tiempo de ejecución** sin reconstruir la imagen:

```nginx
# Proxy de API hacia el backend
location /api/ {
    proxy_pass http://${BACKEND_HOST}:8080/api/;
}

# WebSocket (STOMP/SockJS)
location /ws {
    proxy_pass http://${BACKEND_HOST}:8080/ws;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}

# SPA — todas las rutas van a index.html (React Router)
location / {
    try_files $uri $uri/ /index.html;
}
```

La variable `${BACKEND_HOST}` se inyecta como env var al contenedor:
- En Docker Compose: `BACKEND_HOST=moviemate-backend`
- En Kubernetes: `BACKEND_HOST=moviemate-backend` (nombre del Service)

---

## 3. Docker Compose — desarrollo local

Ubicación: `MovieMate/docker-compose.yml`

Levanta los tres servicios en red local:

```bash
docker compose up -d
```

| Servicio | Imagen | Puerto expuesto |
|---|---|---|
| postgres | postgres:16-alpine | 5432 |
| backend | build desde ./moviemate-backend | 8080 |
| frontend | build desde ./moviemate-frontend | 80 |

El frontend hace proxy de `/api/` y `/ws` hacia el backend automáticamente gracias a nginx. Acceso: `http://localhost`.

---

## 4. GitHub Actions — CI/CD

### 4.1 ci.yml — Integración continua

Ubicación: `.github/workflows/ci.yml`

**Se activa en:** push a cualquier rama excepto `main`, y pull requests a `main`.

```
Jobs:
├── backend-test     → mvn test (JUnit)
├── backend-build    → mvn package (verifica que compila)
├── frontend-build   → npm ci + vite build (verifica que compila)
└── docker-lint      → docker build --load (sin push, valida Dockerfiles)
```

Objetivo: fallar rápido antes de llegar a `main`.

### 4.2 cd.yml — Entrega continua

Ubicación: `.github/workflows/cd.yml`

**Se activa en:** push a `main` únicamente.

```
Jobs:
├── test             → mvn test + vite build (pre-deploy safety net)
└── build-and-push   → docker buildx (multi-arch: amd64 + arm64)
                        └─► ghcr.io/GivenCloud/moviemate-backend:{sha} + :latest
                        └─► ghcr.io/GivenCloud/moviemate-frontend:{sha} + :latest
```

> El job `deploy` está escrito pero comentado hasta que esté configurado el cluster de Oracle Cloud (paso 8).

**Secretos necesarios en GitHub** (Settings → Secrets → Actions):

| Secret | Descripción |
|---|---|
| `DB_POSTGRE_PASSWORD` | Contraseña de PostgreSQL |
| `JWT_SECRET` | Clave JWT (mínimo 32 caracteres) |
| `TMDB_API_KEY` | API key de TMDB |
| `KUBECONFIG` | kubeconfig del cluster k3s (pendiente, paso 8) |

El `GITHUB_TOKEN` para push a GHCR es automático — no hace falta configurarlo.

---

## 5. Helm Chart

Se elige **Helm** sobre Kustomize porque es el estándar de facto (~70% de adopción en empresas) y permite templating real con Go templates, a diferencia de los overlays de Kustomize.

### 5.1 Estructura

```
k8s/moviemate/
├── Chart.yaml               ← metadatos del chart (nombre, versión, appVersion)
├── values.yaml              ← valores por defecto (minikube / dev)
├── values-prod.yaml         ← overrides para Oracle Cloud (local-path StorageClass, etc.)
└── templates/
    ├── _helpers.tpl         ← macros reutilizables (fullname, labels, selectorLabels)
    ├── secrets.yaml         ← Secret de Kubernetes con credenciales
    ├── ingress.yaml         ← Ingress con rutas /api, /ws y /
    ├── backend/
    │   ├── backend-configmap.yaml   ← variables de entorno no sensibles
    │   ├── backend-deployment.yaml  ← Deployment con initContainer + probes
    │   └── backend-service.yaml     ← ClusterIP :8080
    ├── frontend/
    │   ├── frontend-deployment.yaml ← Deployment con BACKEND_HOST env var
    │   └── frontend-service.yaml    ← ClusterIP :80
    └── postgres/
        ├── postgres-statefulset.yaml ← StatefulSet con PVC para persistencia
        └── postgres-service.yaml     ← ClusterIP :5432
```

### 5.2 values.yaml y values-prod.yaml

`values.yaml` contiene los valores por defecto para entorno local/minikube. **Nunca debe contener secretos reales** — los valores `"changeme"` son intencionales.

`values-prod.yaml` sobreescribe únicamente lo que cambia en producción (Oracle Cloud):
- `storageClass: "local-path"` (StorageClass de k3s)
- recursos ajustados para la VM ARM

Para desplegar en producción:
```bash
helm upgrade --install moviemate ./k8s/moviemate \
  -f k8s/moviemate/values-prod.yaml \
  --set secrets.tmdbApiKey=$TMDB_API_KEY \
  --set secrets.jwtSecret=$JWT_SECRET \
  --set secrets.dbPassword=$DB_PASSWORD \
  -n moviemate
```

### 5.3 Secrets

Los secretos se gestionan con un `Secret` de Kubernetes generado por Helm. Tiene la anotación `helm.sh/resource-policy: keep` para que no se borre aunque se haga `helm uninstall`.

Los valores reales se pasan **siempre por fuera** del repositorio, mediante `--set` o un fichero `values-local.yaml` que no se commitea (añadir al `.gitignore`):

```yaml
# values-local.yaml — NO commitear
secrets:
  tmdbApiKey: "tu-clave-real"
  jwtSecret:  "clave-segura-32-chars"
  dbPassword: "contraseña-segura"
```

```bash
helm upgrade moviemate ./k8s/moviemate -f k8s/moviemate/values-local.yaml -n moviemate
```

### 5.4 Templates

**`_helpers.tpl`** — Define macros Go template reutilizables en todos los recursos:
- `moviemate.fullname` — nombre completo del release (evita duplicar el nombre si el release ya lo contiene)
- `moviemate.labels` — etiquetas estándar de Helm (`helm.sh/chart`, `app.kubernetes.io/*`)
- `moviemate.backend.selectorLabels` / `frontend` / `postgres` — selectores para Services y Deployments
- `moviemate.secretName` / `moviemate.backend.configMapName` — nombres de recursos referenciados desde múltiples templates

**`backend-deployment.yaml`** — Puntos clave:
- **initContainer** `wait-for-postgres`: usa `busybox` con `nc` para esperar a que el puerto 5432 de PostgreSQL esté disponible antes de arrancar Spring Boot. Evita errores de conexión al inicio.
- **readinessProbe**: llama a `/actuator/health` con `initialDelaySeconds: 60` (la JVM tarda en arrancar)
- **livenessProbe**: misma ruta, con `initialDelaySeconds: 90`
- **RollingUpdate**: garantiza cero downtime en actualizaciones

**`postgres-statefulset.yaml`** — Puntos clave:
- **StatefulSet** en lugar de Deployment: garantiza identidad de red estable (`moviemate-postgres-0`) y que el PVC se mantenga aunque el pod se reinicie
- **volumeClaimTemplates**: Kubernetes crea y gestiona automáticamente el PVC. Al hacer `helm uninstall`, el PVC NO se borra (protección de datos)
- **PGDATA** apunta a un subdirectorio dentro del volumen (`/var/lib/postgresql/data/pgdata`) para evitar conflictos con los ficheros de inicialización de la imagen
- **updateStrategy: OnDelete**: el pod NO se reinicia automáticamente al hacer `helm upgrade`. Hay que borrarlo manualmente: `kubectl delete pod moviemate-postgres-0 -n moviemate`
- **probes**: usan `pg_isready` con argumentos como items separados del array (no dentro de `sh -c`) para evitar problemas con la sustitución de variables

**`ingress.yaml`** — Puntos clave:
- Rutas en orden de especificidad: `/api` → backend, `/ws` → backend, `/` → frontend
- Anotaciones para WebSocket: `proxy-read-timeout: 3600` y `proxy-send-timeout: 3600` (necesario para mantener conexiones STOMP/SockJS abiertas)
- TLS condicional: solo se añade la sección `tls:` y la anotación de cert-manager si `ingress.tls.enabled=true`

---

## 6. Despliegue en Minikube

### 6.1 Requisitos

- Docker Desktop (o Docker Engine en Linux)
- minikube `>= v1.30`
- kubectl
- helm `>= v3.12`

### 6.2 Paso a paso

```bash
# 1. Arrancar minikube
minikube start

# 2. Habilitar el addon de ingress-nginx
minikube addons enable ingress

# 3. Crear el namespace
kubectl create namespace moviemate

# 4. Apuntar el Docker CLI al daemon interno de minikube
#    (temporal, solo afecta a la terminal actual)
eval $(minikube docker-env)

# 5. Construir las imágenes dentro del Docker de minikube
docker build -t moviemate-backend:local ./MovieMate/moviemate-backend
docker build -t moviemate-frontend:local ./MovieMate/moviemate-frontend

# 6. Instalar el Helm chart con imágenes locales
helm install moviemate k8s/moviemate \
  --namespace moviemate \
  --set backend.image.repository=moviemate-backend \
  --set backend.image.tag=local \
  --set backend.image.pullPolicy=Never \
  --set frontend.image.repository=moviemate-frontend \
  --set frontend.image.tag=local \
  --set frontend.image.pullPolicy=Never \
  --set secrets.tmdbApiKey="TU_TMDB_KEY" \
  --set secrets.jwtSecret="minikube-dev-secret-32chars-ok!!" \
  --set secrets.dbPassword="devpassword123"

# 7. Verificar que todos los pods están Running
kubectl get pods -n moviemate -w
```

El orden de arranque es:
1. PostgreSQL levanta y pasa el readiness probe (`pg_isready`)
2. El initContainer del backend detecta que el puerto 5432 está disponible
3. Spring Boot arranca (~60-90 segundos, la JVM es lenta en el inicio)
4. El backend pasa el readiness probe (`/actuator/health`)

### 6.3 Acceso desde Windows con WSL2

En WSL2, minikube corre dentro del Docker de WSL2. La IP del cluster (`192.168.49.2`) no es accesible directamente desde el navegador Windows. Solución: port-forward del ingress-nginx al localhost de WSL2, que Windows sí ve.

```bash
# Port-forward del ingress-nginx al localhost (WSL2 → Windows)
kubectl port-forward -n ingress-nginx svc/ingress-nginx-controller 8080:80 --address=0.0.0.0 &
```

Añadir al fichero `C:\Windows\System32\drivers\etc\hosts` (como administrador):
```
127.0.0.1  moviemate.local
```

Acceder desde el navegador: `http://moviemate.local:8080`

> **Nota CORS**: al usar un puerto no estándar (8080), el origen que ve el backend es `http://moviemate.local:8080`. Hay que pasarlo al desplegar:
> ```bash
> helm upgrade moviemate k8s/moviemate --namespace moviemate --reuse-values \
>   --set backend.env.corsAllowedOrigins="http://moviemate.local:8080"
> ```

### 6.4 Problemas encontrados y soluciones

#### Probe de PostgreSQL fallaba con "too many command-line arguments"

**Causa**: la probe usaba `sh -c "pg_isready -U $(POSTGRES_USER) ..."`. En el shell, `$(POSTGRES_USER)` es sustitución de **comando** (intenta ejecutar un binario llamado `POSTGRES_USER`), no de variable. El comando fallaba silenciosamente y dejaba `-U` sin valor, haciendo que pg_isready interpretara el siguiente token `-d` como el username, y `moviemate` sobraba como argumento posicional.

**Solución**: pasar los argumentos como items separados del array `command:` usando valores de Helm directamente, sin `sh -c`:
```yaml
command:
  - pg_isready
  - -h
  - localhost
  - -U
  - {{ .Values.secrets.dbUsername }}
  - -d
  - {{ .Values.postgres.database }}
```

#### CORS bloqueaba todas las peticiones con 403

**Causa**: los orígenes permitidos en `SecurityConfig.java` estaban **hardcodeados** en el código Java (`http://localhost:5173` y `https://tu-dominio.com`). El env var `CORS_ALLOWED_ORIGINS` del ConfigMap de Helm se inyectaba en el pod correctamente, pero Spring Boot nunca lo leía.

**Solución**: inyectar el valor en `SecurityConfig` con `@Value`:
```java
@Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}")
private String corsAllowedOrigins;

// En corsConfigurationSource():
config.setAllowedOrigins(Arrays.asList(corsAllowedOrigins.split(",")));
```

Ahora el origen se puede sobreescribir en cada entorno sin tocar el código:
- Minikube: `--set backend.env.corsAllowedOrigins="http://moviemate.local:8080"`
- Producción: se construye automáticamente desde `ingress.host`

#### ingress.yaml fallaba el helm lint con "bad character U+002D '-'"

**Causa**: el template intentaba acceder a una clave de mapa con guión y punto (`cert-manager\.io/cluster-issuer`) usando la sintaxis `((.Values.ingress.annotations).cert-manager\.io/...)`, que no es válida en Go templates.

**Solución**: añadir un campo limpio `certManagerIssuer` en values.yaml y referenciarlo directamente:
```yaml
{{- if and .Values.ingress.tls.enabled .Values.ingress.certManagerIssuer }}
cert-manager.io/cluster-issuer: {{ .Values.ingress.certManagerIssuer | quote }}
{{- end }}
```

#### StatefulSet de PostgreSQL no se actualiza con helm upgrade

**Comportamiento esperado**: el StatefulSet tiene `updateStrategy: OnDelete` deliberadamente, para evitar reinicios accidentales de la base de datos. Al hacer `helm upgrade`, el spec se actualiza pero el pod NO se reinicia solo.

**Cómo aplicar cambios**: borrar el pod manualmente y el StatefulSet lo recrea con la nueva configuración:
```bash
kubectl delete pod moviemate-postgres-0 -n moviemate
```

---

## 7. Producción — Decisión final

### Decisión: despliegue local con Minikube es suficiente para el TFG

Se evaluaron varias opciones de cloud gratuito para el despliegue en producción:

| Opción | Motivo de descarte |
|---|---|
| **Oracle Cloud Free Tier** (A1 ARM) | Requiere tarjeta de crédito para verificación de identidad |
| **DigitalOcean** (GitHub Student Pack, $200) | Requiere método de pago aunque los créditos cubran el coste |
| **Azure for Students** ($100, sin tarjeta) | Requiere email universitario activo con convenio Microsoft |

**Conclusión**: dado que el objetivo del TFG es demostrar una infraestructura de despliegue bien diseñada y funcional, el entorno local con Minikube cubre completamente ese objetivo. No es necesario un servidor público.

### Qué demuestra el estado actual

| Componente | Estado | Evidencia |
|---|---|---|
| Contenerización | ✅ Completo | Dockerfiles multi-stage (backend + frontend) |
| Orquestación local | ✅ Completo | Minikube + Helm chart funcionando |
| CI — Integración continua | ✅ Completo | `ci.yml` ejecuta tests en cada push a `feat/*` |
| CD — Entrega continua | ✅ Completo | `cd.yml` construye y publica imágenes en GHCR al hacer merge a `main` |
| Deploy automático | ⬜ Preparado | Job `deploy` en `cd.yml` comentado; se activa apuntando `KUBECONFIG` a cualquier cluster |
| TLS / HTTPS | ⬜ Preparado | `ingress.yaml` soporta cert-manager con `ingress.tls.enabled=true` |

### Cómo desplegar en producción si se dispone de un servidor

Si en el futuro se quiere desplegar en un VPS o cloud real, los pasos son:

```bash
# 1. Instalar k3s en la VM (Ubuntu 22.04)
curl -sfL https://get.k3s.io | sh -

# 2. Obtener el kubeconfig
cat /etc/rancher/k3s/k3s.yaml

# 3. Desplegar con Helm
helm upgrade --install moviemate ./k8s/moviemate \
  -f k8s/moviemate/values-prod.yaml \
  --set backend.image.tag=${GIT_SHA} \
  --set frontend.image.tag=${GIT_SHA} \
  --set secrets.tmdbApiKey=${TMDB_API_KEY} \
  --set secrets.jwtSecret=${JWT_SECRET} \
  --set secrets.dbPassword=${DB_PASSWORD} \
  -n moviemate

# 4. Activar el deploy automático en cd.yml:
#    - Descomentar el job "deploy"
#    - Añadir el secret KUBECONFIG en GitHub → Settings → Secrets → Actions
```

Proveedores compatibles con la infraestructura actual (Helm + k3s):
- Oracle Cloud Free Tier (A1 ARM, Always Free)
- DigitalOcean Droplet (GitHub Student Pack)
- Azure for Students (email universitario)
- Cualquier VPS con Ubuntu 22.04

---

## 8. Referencia de comandos útiles

### Minikube

```bash
minikube start                        # arrancar el cluster
minikube stop                         # pausar
minikube delete                       # borrar completamente
minikube addons enable ingress        # habilitar ingress-nginx
eval $(minikube docker-env)           # apuntar Docker CLI al daemon de minikube
minikube ip                           # IP del cluster
```

### Helm

```bash
helm lint k8s/moviemate               # validar sintaxis del chart
helm template moviemate k8s/moviemate # renderizar templates sin desplegar
helm install moviemate k8s/moviemate -n moviemate   # instalación inicial
helm upgrade moviemate k8s/moviemate -n moviemate   # actualizar
helm uninstall moviemate -n moviemate               # desinstalar (PVCs se conservan)
helm history moviemate -n moviemate                 # historial de revisiones
helm rollback moviemate 1 -n moviemate              # volver a la revisión 1
```

### kubectl — operaciones habituales

```bash
# Ver estado general
kubectl get pods -n moviemate
kubectl get all -n moviemate

# Logs
kubectl logs -l app.kubernetes.io/name=moviemate-backend -n moviemate -f
kubectl logs -l app.kubernetes.io/name=moviemate-frontend -n moviemate -f
kubectl logs moviemate-postgres-0 -n moviemate -f

# Reiniciar deployments
kubectl rollout restart deployment/moviemate-backend -n moviemate
kubectl rollout restart deployment/moviemate-frontend -n moviemate
kubectl rollout status deployment/moviemate-backend -n moviemate

# PostgreSQL: aplicar cambios del StatefulSet
kubectl delete pod moviemate-postgres-0 -n moviemate

# Acceso directo a un pod
kubectl exec -it deploy/moviemate-backend -n moviemate -- sh
kubectl exec -it moviemate-postgres-0 -n moviemate -- psql -U moviemate

# Ver variables de entorno del backend
kubectl exec -n moviemate deploy/moviemate-backend -- env | grep CORS

# Port-forward para acceso desde WSL2/Windows
kubectl port-forward -n ingress-nginx svc/ingress-nginx-controller 8080:80 --address=0.0.0.0 &
```

### Flujo completo para actualizar una imagen en minikube

```bash
eval $(minikube docker-env)
docker build -t moviemate-backend:local ./MovieMate/moviemate-backend
kubectl rollout restart deployment/moviemate-backend -n moviemate
kubectl rollout status deployment/moviemate-backend -n moviemate
```
