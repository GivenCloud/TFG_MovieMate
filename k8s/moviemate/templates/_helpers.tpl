{{/*
Nombre completo del release.
Evita repetir el nombre si el release ya lo contiene.
*/}}
{{- define "moviemate.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Nombre del chart (para las etiquetas helm.sh/chart).
*/}}
{{- define "moviemate.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Etiquetas comunes que van en todos los recursos.
*/}}
{{- define "moviemate.labels" -}}
helm.sh/chart: {{ include "moviemate.chart" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}

{{/*
Etiquetas de selector para el backend.
*/}}
{{- define "moviemate.backend.selectorLabels" -}}
app.kubernetes.io/name: {{ include "moviemate.fullname" . }}-backend
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Etiquetas de selector para el frontend.
*/}}
{{- define "moviemate.frontend.selectorLabels" -}}
app.kubernetes.io/name: {{ include "moviemate.fullname" . }}-frontend
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Etiquetas de selector para PostgreSQL.
*/}}
{{- define "moviemate.postgres.selectorLabels" -}}
app.kubernetes.io/name: {{ include "moviemate.fullname" . }}-postgres
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Nombre del Secret que contiene las credenciales.
*/}}
{{- define "moviemate.secretName" -}}
{{- printf "%s-secrets" (include "moviemate.fullname" .) }}
{{- end }}

{{/*
Nombre del ConfigMap del backend.
*/}}
{{- define "moviemate.backend.configMapName" -}}
{{- printf "%s-backend-config" (include "moviemate.fullname" .) }}
{{- end }}
