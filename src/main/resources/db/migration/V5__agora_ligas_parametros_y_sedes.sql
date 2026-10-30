-- ============================================================================
-- MIGRACIÓN V5: V5__agora_ligas_parametros_y_sedes.sql
-- Fase 5: Ágora Principal (Home Multi-Rol), parametrización dinámica de ligas,
-- inscripción por código de invitación, alta con email/DNI y sede alternativa.
-- (Nota: se numera como V5 porque V4__soporte_registro_autenticacion_usuarios.sql
-- ya existe en el historial de migraciones de la Fase 4).
-- ============================================================================

-- 1. ACTUALIZACIÓN DE LA TABLA usuarios
UPDATE usuarios SET email = LOWER(TRIM(email));

DROP INDEX IF EXISTS idx_usuarios_email_minusculas;
CREATE UNIQUE INDEX IF NOT EXISTS idx_usuarios_email_lower ON usuarios (LOWER(email));

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS dni VARCHAR(20) UNIQUE,
    ADD COLUMN IF NOT EXISTS requiere_cambio_password BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS primer_acceso_completado BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS ck_usuarios_rol;
ALTER TABLE usuarios ADD CONSTRAINT ck_usuarios_rol
    CHECK (rol IN ('JUGADOR', 'CAPITAN', 'PRESIDENTE', 'ADMINISTRADOR'));

-- 2. ACTUALIZACIÓN DE LA TABLA ligas
ALTER TABLE ligas
    ADD COLUMN IF NOT EXISTS codigo_invitacion VARCHAR(30),
    ADD COLUMN IF NOT EXISTS presidente_id UUID REFERENCES usuarios(id),
    ADD COLUMN IF NOT EXISTS partidos_por_jornada INT NOT NULL DEFAULT 3,
    ADD COLUMN IF NOT EXISTS modo_sede VARCHAR(30) NOT NULL DEFAULT 'SEDE_LOCAL_POR_EQUIPO',
    ADD COLUMN IF NOT EXISTS sede_unica_liga VARCHAR(150) NULL,
    ADD COLUMN IF NOT EXISTS puntos_victoria INT NOT NULL DEFAULT 3,
    ADD COLUMN IF NOT EXISTS puntos_empate INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS puntos_derrota INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS exigir_dni_jugador BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS exigir_telefono_jugador BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS normativa_texto TEXT;

-- Poblar código de invitación en ligas existentes antes de aplicar NOT NULL y UNIQUE
UPDATE ligas
SET codigo_invitacion = 'SEVILLA-2026'
WHERE id = '10000000-0000-0000-0000-000000000001'
  AND codigo_invitacion IS NULL;

UPDATE ligas
SET codigo_invitacion = 'LIGA-' || UPPER(SUBSTRING(REPLACE(id::text, '-', ''), 1, 8))
WHERE codigo_invitacion IS NULL;

ALTER TABLE ligas ALTER COLUMN codigo_invitacion SET NOT NULL;

ALTER TABLE ligas DROP CONSTRAINT IF EXISTS uk_ligas_codigo_invitacion;
ALTER TABLE ligas ADD CONSTRAINT uk_ligas_codigo_invitacion UNIQUE (codigo_invitacion);

ALTER TABLE ligas DROP CONSTRAINT IF EXISTS ck_ligas_modo_sede;
ALTER TABLE ligas ADD CONSTRAINT ck_ligas_modo_sede
    CHECK (modo_sede IN ('SEDE_UNICA_CLUB', 'SEDE_LOCAL_POR_EQUIPO'));

CREATE INDEX IF NOT EXISTS idx_ligas_presidente_id ON ligas(presidente_id);

-- 3. ACTUALIZACIÓN DE LA TABLA enfrentamientos
ALTER TABLE enfrentamientos
    ADD COLUMN IF NOT EXISTS sede_alternativa_jornada VARCHAR(150) NULL;

-- ============================================================================
-- 4. ACTUALIZACIÓN DE DATOS SEMILLA PARA EL ÁGORA MULTI-ROL
-- Usuario de prueba: 40000000-0000-0000-0000-000000000001 (Álvaro Iglesias Méndez)
--   1) CAPITAN en "Liga Interclubes Sevilla" con "Real Betis Pádel Team"
--   2) PRESIDENTE en "Liga Nocturna Aljarafe"
--   3) JUGADOR en "Torneo Otoño Híspalis" con "Espartanos de Triana"
-- ============================================================================

UPDATE usuarios
SET dni = '28765432A'
WHERE id = '40000000-0000-0000-0000-000000000001'
  AND dni IS NULL;

-- Membresía 1: CAPITAN en "Liga Interclubes Sevilla" con "Real Betis Pádel Team"
UPDATE ligas
SET nombre = 'Liga Interclubes Sevilla',
    descripcion = 'Liga oficial interclubes por equipos de pádel en la provincia de Sevilla',
    codigo_invitacion = 'SEVILLA-2026',
    presidente_id = '40000000-0000-0000-0000-000000000101',
    partidos_por_jornada = 3,
    modo_sede = 'SEDE_LOCAL_POR_EQUIPO',
    sede_unica_liga = NULL,
    puntos_victoria = 3,
    puntos_empate = 1,
    puntos_derrota = 0,
    exigir_dni_jugador = TRUE,
    exigir_telefono_jugador = TRUE,
    normativa_texto = 'Cada equipo local aporta sus pistas oficiales. Enfrentamientos a 3 partidos al mejor de 3 sets.',
    actualizado_en = CURRENT_TIMESTAMP
WHERE id = '10000000-0000-0000-0000-000000000001';

UPDATE equipos
SET nombre = 'Real Betis Pádel Team',
    club = 'Ciudad Deportiva Luis del Sol',
    capitan_id = '40000000-0000-0000-0000-000000000001',
    actualizado_en = CURRENT_TIMESTAMP
WHERE id = '50000000-0000-0000-0000-000000000001';

INSERT INTO miembros_equipo (id, equipo_id, usuario_id, es_capitan, activo)
VALUES (
    '60000000-0000-0000-0000-000000000001',
    '50000000-0000-0000-0000-000000000001',
    '40000000-0000-0000-0000-000000000001',
    TRUE,
    TRUE
)
ON CONFLICT (equipo_id, usuario_id)
DO UPDATE SET es_capitan = TRUE, activo = TRUE, actualizado_en = CURRENT_TIMESTAMP;

-- Membresía 2: PRESIDENTE en "Liga Nocturna Aljarafe" (sede única de club)
INSERT INTO ligas (
    id, nombre, descripcion, codigo_invitacion, presidente_id,
    partidos_por_jornada, modo_sede, sede_unica_liga,
    puntos_victoria, puntos_empate, puntos_derrota,
    exigir_dni_jugador, exigir_telefono_jugador, normativa_texto
) VALUES (
    '10000000-0000-0000-0000-000000000002',
    'Liga Nocturna Aljarafe',
    'Competición nocturna entre semana organizada en sede única en el Aljarafe sevillano',
    'ALJARAFE-2026',
    '40000000-0000-0000-0000-000000000001',
    3,
    'SEDE_UNICA_CLUB',
    'Club Pádel Mairena del Aljarafe',
    3,
    1,
    0,
    TRUE,
    TRUE,
    'Todos los encuentros se disputan en las pistas cubiertas del Club Pádel Mairena del Aljarafe.'
) ON CONFLICT (id) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    codigo_invitacion = EXCLUDED.codigo_invitacion,
    presidente_id = EXCLUDED.presidente_id,
    partidos_por_jornada = EXCLUDED.partidos_por_jornada,
    modo_sede = EXCLUDED.modo_sede,
    sede_unica_liga = EXCLUDED.sede_unica_liga,
    normativa_texto = EXCLUDED.normativa_texto,
    actualizado_en = CURRENT_TIMESTAMP;

INSERT INTO temporadas (id, liga_id, nombre, fecha_inicio, fecha_fin, activa)
VALUES (
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    'Temporada Nocturna 2026-2027',
    '2026-09-15',
    '2027-06-15',
    TRUE
) ON CONFLICT (id) DO NOTHING;

INSERT INTO divisiones (id, temporada_id, nombre, nivel)
VALUES (
    '30000000-0000-0000-0000-000000000002',
    '20000000-0000-0000-0000-000000000002',
    'Primera División Nocturna Aljarafe',
    1
) ON CONFLICT (id) DO NOTHING;

-- Membresía 3: JUGADOR en "Torneo Otoño Híspalis" con "Espartanos de Triana"
INSERT INTO ligas (
    id, nombre, descripcion, codigo_invitacion, presidente_id,
    partidos_por_jornada, modo_sede, sede_unica_liga,
    puntos_victoria, puntos_empate, puntos_derrota,
    exigir_dni_jugador, exigir_telefono_jugador, normativa_texto
) VALUES (
    '10000000-0000-0000-0000-000000000003',
    'Torneo Otoño Híspalis',
    'Torneo de otoño por equipos en clubes de Sevilla capital',
    'HISPALIS-2026',
    '40000000-0000-0000-0000-000000000201',
    3,
    'SEDE_LOCAL_POR_EQUIPO',
    NULL,
    3,
    1,
    0,
    TRUE,
    TRUE,
    'Torneo de otoño con formato de 3 parejas por jornada en sede del equipo local.'
) ON CONFLICT (id) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    codigo_invitacion = EXCLUDED.codigo_invitacion,
    presidente_id = EXCLUDED.presidente_id,
    partidos_por_jornada = EXCLUDED.partidos_por_jornada,
    modo_sede = EXCLUDED.modo_sede,
    sede_unica_liga = EXCLUDED.sede_unica_liga,
    normativa_texto = EXCLUDED.normativa_texto,
    actualizado_en = CURRENT_TIMESTAMP;

INSERT INTO temporadas (id, liga_id, nombre, fecha_inicio, fecha_fin, activa)
VALUES (
    '20000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000003',
    'Edición Otoño 2026',
    '2026-10-01',
    '2026-12-20',
    TRUE
) ON CONFLICT (id) DO NOTHING;

INSERT INTO divisiones (id, temporada_id, nombre, nivel)
VALUES (
    '30000000-0000-0000-0000-000000000003',
    '20000000-0000-0000-0000-000000000003',
    'División Honor Híspalis',
    1
) ON CONFLICT (id) DO NOTHING;

INSERT INTO equipos (id, division_id, nombre, club, capitan_id)
VALUES (
    '50000000-0000-0000-0000-000000000301',
    '30000000-0000-0000-0000-000000000003',
    'Espartanos de Triana',
    'Club Pádel Triana Sevilla',
    '40000000-0000-0000-0000-000000000002'
) ON CONFLICT (id) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    club = EXCLUDED.club,
    capitan_id = EXCLUDED.capitan_id,
    actualizado_en = CURRENT_TIMESTAMP;

INSERT INTO miembros_equipo (id, equipo_id, usuario_id, es_capitan, activo)
VALUES
    ('60000000-0000-0000-0000-000000000301', '50000000-0000-0000-0000-000000000301', '40000000-0000-0000-0000-000000000002', TRUE,  TRUE),
    ('60000000-0000-0000-0000-000000000302', '50000000-0000-0000-0000-000000000301', '40000000-0000-0000-0000-000000000001', FALSE, TRUE)
ON CONFLICT (equipo_id, usuario_id)
DO UPDATE SET es_capitan = EXCLUDED.es_capitan, activo = TRUE, actualizado_en = CURRENT_TIMESTAMP;
