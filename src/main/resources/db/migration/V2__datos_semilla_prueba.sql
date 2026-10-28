-- ============================================================================
-- MIGRACIÓN V2: V2__datos_semilla_prueba.sql
-- Actualización de estados del flujo de jornada y carga de datos semilla de Sevilla
-- ============================================================================

-- 1. Ampliar restricción de estados en enfrentamientos para soportar todo el ciclo de jornada
ALTER TABLE enfrentamientos DROP CONSTRAINT IF EXISTS ck_enfrentamientos_estado;
ALTER TABLE enfrentamientos ADD CONSTRAINT ck_enfrentamientos_estado
    CHECK (estado IN (
        'PENDIENTE_FECHA',
        'VOTACION_DISPONIBILIDAD_ABIERTA',
        'FECHA_FIJADA',
        'CONVOCATORIA_ABIERTA',
        'ALINEACION_CERRADA',
        'EN_DISPUTA',
        'FINALIZADO',
        'CANCELADO'
    ));

-- 2. Permitir cerrar la alineación de un equipo de forma independiente antes que el rival
ALTER TABLE partidos_enfrentamiento ALTER COLUMN jugador_local_1_id DROP NOT NULL;
ALTER TABLE partidos_enfrentamiento ALTER COLUMN jugador_local_2_id DROP NOT NULL;
ALTER TABLE partidos_enfrentamiento ALTER COLUMN jugador_visitante_1_id DROP NOT NULL;
ALTER TABLE partidos_enfrentamiento ALTER COLUMN jugador_visitante_2_id DROP NOT NULL;

-- ============================================================================
-- DATOS SEMILLA DE PRUEBA (SEVILLA)
-- ============================================================================

-- Liga de Sevilla
INSERT INTO ligas (id, nombre, descripcion)
VALUES (
    '10000000-0000-0000-0000-000000000001',
    'Liga Synagon Sevilla',
    'Liga oficial interclubes por equipos de pádel en la provincia de Sevilla'
) ON CONFLICT (id) DO NOTHING;

-- Temporada activa
INSERT INTO temporadas (id, liga_id, nombre, fecha_inicio, fecha_fin, activa)
VALUES (
    '20000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    'Temporada 2026-2027',
    '2026-09-01',
    '2027-06-30',
    TRUE
) ON CONFLICT (id) DO NOTHING;

-- División
INSERT INTO divisiones (id, temporada_id, nombre, nivel)
VALUES (
    '30000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000001',
    'Primera División Sevilla',
    1
) ON CONFLICT (id) DO NOTHING;

-- 8 Jugadores de mi equipo (1 CAPITAN y 7 JUGADOR)
INSERT INTO usuarios (id, nombre, apellidos, email, telefono, rol) VALUES
    ('40000000-0000-0000-0000-000000000001', 'Álvaro', 'Iglesias Méndez', 'alvaro.capitan@synagon.es', '+34600100001', 'CAPITAN'),
    ('40000000-0000-0000-0000-000000000002', 'Javier', 'Morales Ruiz', 'javier.morales@synagon.es', '+34600100002', 'JUGADOR'),
    ('40000000-0000-0000-0000-000000000003', 'Carlos', 'Sánchez Domínguez', 'carlos.sanchez@synagon.es', '+34600100003', 'JUGADOR'),
    ('40000000-0000-0000-0000-000000000004', 'Pablo', 'Navarro Herrera', 'pablo.navarro@synagon.es', '+34600100004', 'JUGADOR'),
    ('40000000-0000-0000-0000-000000000005', 'Alejandro', 'Romero Gil', 'alejandro.romero@synagon.es', '+34600100005', 'JUGADOR'),
    ('40000000-0000-0000-0000-000000000006', 'Manuel', 'Torres Vargas', 'manuel.torres@synagon.es', '+34600100006', 'JUGADOR'),
    ('40000000-0000-0000-0000-000000000007', 'Sergio', 'Delgado Castro', 'sergio.delgado@synagon.es', '+34600100007', 'JUGADOR'),
    ('40000000-0000-0000-0000-000000000008', 'Fernando', 'Ortega León', 'fernando.ortega@synagon.es', '+34600100008', 'JUGADOR')
ON CONFLICT (id) DO NOTHING;

-- 2 Equipos de la división (Mi equipo + Equipo rival)
INSERT INTO equipos (id, division_id, nombre, club, capitan_id) VALUES
    (
        '50000000-0000-0000-0000-000000000001',
        '30000000-0000-0000-0000-000000000001',
        'Real Club Pádel Triana',
        'Club Pádel Triana Sevilla',
        '40000000-0000-0000-0000-000000000001'
    ),
    (
        '50000000-0000-0000-0000-000000000002',
        '30000000-0000-0000-0000-000000000001',
        'Club Pádel Los Remedios',
        'Instalaciones Deportivas Los Remedios',
        NULL
    )
ON CONFLICT (id) DO NOTHING;

-- Vinculación de los 8 jugadores a mi equipo en miembros_equipo
INSERT INTO miembros_equipo (id, equipo_id, usuario_id, es_capitan, activo) VALUES
    ('60000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', TRUE,  TRUE),
    ('60000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', FALSE, TRUE),
    ('60000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', FALSE, TRUE),
    ('60000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000004', FALSE, TRUE),
    ('60000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000005', FALSE, TRUE),
    ('60000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000006', FALSE, TRUE),
    ('60000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000007', FALSE, TRUE),
    ('60000000-0000-0000-0000-000000000008', '50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000008', FALSE, TRUE)
ON CONFLICT (id) DO NOTHING;

-- 1 Enfrentamiento en la Jornada 1
INSERT INTO enfrentamientos (
    id,
    division_id,
    equipo_local_id,
    equipo_visitante_id,
    numero_jornada,
    estado,
    fecha_definitiva,
    pistas,
    puntos_local,
    puntos_visitante,
    acta_confirmada
) VALUES (
    '70000000-0000-0000-0000-000000000001',
    '30000000-0000-0000-0000-000000000001',
    '50000000-0000-0000-0000-000000000001',
    '50000000-0000-0000-0000-000000000002',
    1,
    'PENDIENTE_FECHA',
    NULL,
    'Pistas Centrales Club Pádel Triana',
    0,
    0,
    FALSE
) ON CONFLICT (id) DO NOTHING;
