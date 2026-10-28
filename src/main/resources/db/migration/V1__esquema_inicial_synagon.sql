-- ============================================================================
-- MIGRACIÓN INICIAL SYNAGON: V1__esquema_inicial_synagon.sql
-- Base de datos para la gestión integral de ligas de pádel por equipos
-- Claves primarias UUID y nomenclatura 100% en español
-- ============================================================================

-- Habilitar extensión para generación de UUIDs si no está disponible gen_random_uuid
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. TABLA: ligas
CREATE TABLE ligas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. TABLA: temporadas
CREATE TABLE temporadas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    liga_id UUID NOT NULL REFERENCES ligas(id) ON DELETE CASCADE,
    nombre VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. TABLA: divisiones
CREATE TABLE divisiones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    temporada_id UUID NOT NULL REFERENCES temporadas(id) ON DELETE CASCADE,
    nombre VARCHAR(100) NOT NULL,
    nivel INT NOT NULL DEFAULT 1,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. TABLA: usuarios
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telefono VARCHAR(30),
    rol VARCHAR(50) NOT NULL DEFAULT 'JUGADOR',
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. TABLA: equipos
CREATE TABLE equipos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    division_id UUID NOT NULL REFERENCES divisiones(id) ON DELETE RESTRICT,
    nombre VARCHAR(150) NOT NULL,
    club VARCHAR(150),
    capitan_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. TABLA: miembros_equipo
CREATE TABLE miembros_equipo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipo_id UUID NOT NULL REFERENCES equipos(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    es_capitan BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_miembros_equipo_usuario UNIQUE (equipo_id, usuario_id)
);

-- 7. TABLA: enfrentamientos
CREATE TABLE enfrentamientos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    division_id UUID NOT NULL REFERENCES divisiones(id) ON DELETE RESTRICT,
    equipo_local_id UUID NOT NULL REFERENCES equipos(id) ON DELETE RESTRICT,
    equipo_visitante_id UUID NOT NULL REFERENCES equipos(id) ON DELETE RESTRICT,
    numero_jornada INT NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE_FECHA',
    fecha_definitiva TIMESTAMP WITH TIME ZONE,
    pistas VARCHAR(150),
    puntos_local INT NOT NULL DEFAULT 0,
    puntos_visitante INT NOT NULL DEFAULT 0,
    acta_confirmada BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_enfrentamientos_equipos_distintos CHECK (equipo_local_id <> equipo_visitante_id),
    CONSTRAINT ck_enfrentamientos_estado CHECK (estado IN ('PENDIENTE_FECHA', 'FECHA_FIJADA', 'EN_DISPUTA', 'FINALIZADO', 'CANCELADO'))
);

-- 8. TABLA: propuestas_fecha
CREATE TABLE propuestas_fecha (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enfrentamiento_id UUID NOT NULL REFERENCES enfrentamientos(id) ON DELETE CASCADE,
    fecha_propuesta TIMESTAMP WITH TIME ZONE NOT NULL,
    orden INT NOT NULL,
    es_seleccionada BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_propuestas_fecha_orden CHECK (orden IN (1, 2)),
    CONSTRAINT uk_propuestas_fecha_enfrentamiento_orden UNIQUE (enfrentamiento_id, orden)
);

-- 9. TABLA: disponibilidades_jugador
CREATE TABLE disponibilidades_jugador (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enfrentamiento_id UUID NOT NULL REFERENCES enfrentamientos(id) ON DELETE CASCADE,
    propuesta_fecha_id UUID REFERENCES propuestas_fecha(id) ON DELETE CASCADE,
    jugador_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    estado_disponibilidad VARCHAR(50) NOT NULL,
    es_disponibilidad_final BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_disponibilidad_estado CHECK (estado_disponibilidad IN ('DISPONIBLE', 'NO_DISPONIBLE', 'DUDA'))
);

-- 10. TABLA: partidos_enfrentamiento
CREATE TABLE partidos_enfrentamiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enfrentamiento_id UUID NOT NULL REFERENCES enfrentamientos(id) ON DELETE CASCADE,
    numero_pareja INT NOT NULL,
    jugador_local_1_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    jugador_local_2_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    jugador_visitante_1_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    jugador_visitante_2_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    set1_local INT,
    set1_visitante INT,
    set2_local INT,
    set2_visitante INT,
    set3_local INT,
    set3_visitante INT,
    pareja_ganadora VARCHAR(20),
    completado BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_partidos_numero_pareja CHECK (numero_pareja IN (1, 2, 3)),
    CONSTRAINT uk_partidos_enfrentamiento_pareja UNIQUE (enfrentamiento_id, numero_pareja),
    CONSTRAINT ck_partidos_pareja_ganadora CHECK (pareja_ganadora IS NULL OR pareja_ganadora IN ('LOCAL', 'VISITANTE'))
);

-- ============================================================================
-- ÍNDICES EXPLÍCITOS EN CLAVES FORÁNEAS (Rendimiento general)
-- ============================================================================
CREATE INDEX idx_temporadas_liga_id ON temporadas(liga_id);
CREATE INDEX idx_divisiones_temporada_id ON divisiones(temporada_id);
CREATE INDEX idx_equipos_division_id ON equipos(division_id);
CREATE INDEX idx_equipos_capitan_id ON equipos(capitan_id);
CREATE INDEX idx_miembros_equipo_equipo_id ON miembros_equipo(equipo_id);
CREATE INDEX idx_miembros_equipo_usuario_id ON miembros_equipo(usuario_id);
CREATE INDEX idx_enfrentamientos_division_id ON enfrentamientos(division_id);
CREATE INDEX idx_enfrentamientos_equipo_local_id ON enfrentamientos(equipo_local_id);
CREATE INDEX idx_enfrentamientos_equipo_visitante_id ON enfrentamientos(equipo_visitante_id);
CREATE INDEX idx_propuestas_fecha_enfrentamiento_id ON propuestas_fecha(enfrentamiento_id);
CREATE INDEX idx_disponibilidades_enfrentamiento_id ON disponibilidades_jugador(enfrentamiento_id);
CREATE INDEX idx_disponibilidades_propuesta_id ON disponibilidades_jugador(propuesta_fecha_id);
CREATE INDEX idx_disponibilidades_jugador_id ON disponibilidades_jugador(jugador_id);
CREATE INDEX idx_partidos_enfrentamiento_id ON partidos_enfrentamiento(enfrentamiento_id);

-- ============================================================================
-- ÍNDICES OPTIMIZADOS PARA CONSULTAS DE CLASIFICACIÓN
-- ============================================================================
-- Permite agilizar el cálculo de la tabla de clasificación por división y estado de acta
CREATE INDEX idx_enfrentamientos_clasificacion ON enfrentamientos(division_id, estado, acta_confirmada);
CREATE INDEX idx_enfrentamientos_jornada ON enfrentamientos(division_id, numero_jornada);
CREATE INDEX idx_enfrentamientos_equipos_estado ON enfrentamientos(equipo_local_id, equipo_visitante_id, estado);
CREATE INDEX idx_partidos_enfrentamiento_completado ON partidos_enfrentamiento(enfrentamiento_id, completado);

-- ============================================================================
-- ÍNDICES OPTIMIZADOS PARA CONSULTAS DE ESTADÍSTICAS DE JUGADORES
-- ============================================================================
-- Consultas rápidas de partidos jugados y ganados por cada jugador en cualquier posición
CREATE INDEX idx_partidos_jugador_local_1 ON partidos_enfrentamiento(jugador_local_1_id);
CREATE INDEX idx_partidos_jugador_local_2 ON partidos_enfrentamiento(jugador_local_2_id);
CREATE INDEX idx_partidos_jugador_visitante_1 ON partidos_enfrentamiento(jugador_visitante_1_id);
CREATE INDEX idx_partidos_jugador_visitante_2 ON partidos_enfrentamiento(jugador_visitante_2_id);
CREATE INDEX idx_partidos_estadisticas_resumen ON partidos_enfrentamiento(completado, pareja_ganadora);
CREATE INDEX idx_disponibilidades_votacion ON disponibilidades_jugador(enfrentamiento_id, propuesta_fecha_id, jugador_id);
