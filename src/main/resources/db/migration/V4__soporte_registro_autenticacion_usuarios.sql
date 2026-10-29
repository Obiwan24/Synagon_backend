-- ============================================================================
-- MIGRACIÓN V4: V4__soporte_registro_autenticacion_usuarios.sql
-- Añade soporte de contraseña cifrada (BCrypt) y consentimiento RGPD en la
-- tabla usuarios para el flujo de acceso y registro de nuevos jugadores (Fase 4)
-- ============================================================================

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS contrasena_hash VARCHAR(255),
    ADD COLUMN IF NOT EXISTS acepto_privacidad BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS fecha_aceptacion_privacidad TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS ck_usuarios_rol;
ALTER TABLE usuarios ADD CONSTRAINT ck_usuarios_rol
    CHECK (rol IN ('JUGADOR', 'CAPITAN', 'ADMINISTRADOR'));

-- Índice funcional para búsquedas rápidas de unicidad de email sin distinción de mayúsculas
CREATE INDEX IF NOT EXISTS idx_usuarios_email_minusculas ON usuarios (LOWER(email));
