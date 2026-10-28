package com.synagon.api.dominio.modelos;

import java.util.UUID;

/**
 * Proyección ligera de dominio con los datos de pertenencia de un jugador a un equipo y su temporada.
 */
public record DatosMiembroEquipo(
        UUID jugadorId,
        String nombreCompleto,
        UUID temporadaId
) {
}
