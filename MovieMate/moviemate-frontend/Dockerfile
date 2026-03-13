# ── Stage 1: Build ──────────────────────────────────────────────
FROM node:22-alpine AS build
WORKDIR /app

# Instalar dependencias primero (mejor cache de capas de Docker)
COPY package.json package-lock.json ./
RUN npm ci --ignore-scripts

# Copiar el resto del código fuente
COPY . .

# Variables de entorno de Vite (compile-time; se embeben en el bundle)
# VITE_API_BASE_URL=/api → las llamadas a la API son relativas al origen,
# así nginx (o el Ingress de k8s) puede enrutarlas al backend.
ARG VITE_API_BASE_URL=/api
ARG VITE_TMDB_IMAGE_BASE=https://image.tmdb.org/t/p
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_TMDB_IMAGE_BASE=$VITE_TMDB_IMAGE_BASE

RUN npm run build

# ── Stage 2: Serve ──────────────────────────────────────────────
FROM nginx:1.27-alpine AS runtime

# Copiar el build de Vite al directorio de nginx
COPY --from=build /app/dist /usr/share/nginx/html

# La imagen oficial de nginx lee /etc/nginx/templates/*.conf.template,
# ejecuta envsubst sobre ellos y los deja en /etc/nginx/conf.d/
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
