package com.synagon.api.dominio.modelos;

import java.util.UUID;

/**
 * Proyección ligera de dominio con los datos básicos de un equipo en una división.
 */
public record DatosEquipoDivision(
        UUID equipoId,
        String nombreEquipo
) {
}
