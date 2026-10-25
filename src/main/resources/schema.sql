-- =======================================================================
-- PROYECTO: CiudadActiva - Reporte y seguimiento ciudadano de incidencias
-- METODOLOGÍA: RUP (Rational Unified Process)
-- COMPONENTE: Script DDL de Base de Datos para PostgreSQL / Supabase
-- =======================================================================

-- 1. Tabla de Roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(30) UNIQUE NOT NULL,
    descripcion VARCHAR(200)
);

-- 2. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(30),
    direccion VARCHAR(200),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    rol_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    creado_en TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- 3. Tabla de Categorías de Incidencias
CREATE TABLE IF NOT EXISTS categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(80) UNIQUE NOT NULL,
    descripcion VARCHAR(255),
    icono VARCHAR(60),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- 4. Tabla de Reportes
CREATE TABLE IF NOT EXISTS reportes (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    direccion VARCHAR(255),
    imagen_url TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    prioridad VARCHAR(30) DEFAULT 'MEDIA',
    justificacion_rechazo TEXT,
    evidencia_cierre_url TEXT,
    categoria_id BIGINT NOT NULL REFERENCES categorias(id) ON DELETE RESTRICT,
    ciudadano_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    creado_en TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    fecha_cierre TIMESTAMP WITHOUT TIME ZONE
);

-- 5. Tabla de Asignaciones de Responsables (RF05)
CREATE TABLE IF NOT EXISTS asignaciones (
    id BIGSERIAL PRIMARY KEY,
    reporte_id BIGINT NOT NULL REFERENCES reportes(id) ON DELETE CASCADE,
    operador_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    gestor_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    notas VARCHAR(500),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- 6. Tabla de Seguimientos / Trazabilidad Inmutable (RF06, RF07, RNF04)
CREATE TABLE IF NOT EXISTS seguimientos (
    id BIGSERIAL PRIMARY KEY,
    reporte_id BIGINT NOT NULL REFERENCES reportes(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30) NOT NULL,
    accion VARCHAR(60) NOT NULL,
    observaciones TEXT NOT NULL,
    evidencia_url TEXT,
    creado_en TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- 7. Tabla de Comentarios (RF08, Casos de uso Ciudadano)
CREATE TABLE IF NOT EXISTS comentarios (
    id BIGSERIAL PRIMARY KEY,
    reporte_id BIGINT NOT NULL REFERENCES reportes(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    contenido TEXT NOT NULL,
    creado_en TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- Índices para optimización de consultas y filtros (RF03, RF09)
CREATE INDEX IF NOT EXISTS idx_reportes_estado ON reportes(estado);
CREATE INDEX IF NOT EXISTS idx_reportes_prioridad ON reportes(prioridad);
CREATE INDEX IF NOT EXISTS idx_reportes_categoria ON reportes(categoria_id);
CREATE INDEX IF NOT EXISTS idx_reportes_ciudadano ON reportes(ciudadano_id);
CREATE INDEX IF NOT EXISTS idx_asignaciones_activa ON asignaciones(activa);
CREATE INDEX IF NOT EXISTS idx_seguimientos_reporte ON seguimientos(reporte_id);
