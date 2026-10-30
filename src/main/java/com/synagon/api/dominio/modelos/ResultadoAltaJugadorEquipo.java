package com.synagon.api.dominio.modelos;

import java.util.Objects;
import java.util.UUID;

/**
 * Resultado de dominio tras el alta o vinculación de un jugador en un equipo por parte del capitán.
 */
public record ResultadoAltaJugadorEquipo(
        Usuario usuario,
        UUID equipoId,
        boolean usuarioNuevoCreado
) {
    public ResultadoAltaJugadorEquipo {
        Objects.requireNonNull(usuario, "El usuario dado de alta es obligatorio.");
        Objects.requireNonNull(equipoId, "El identificador del equipo es obligatorio.");
    }
}
