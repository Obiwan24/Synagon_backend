package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.EstadoDisponibilidad;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO de petición para registrar o actualizar el voto de disponibilidad de un jugador.
 */
public record PeticionVotarDisponibilidad(
        @NotNull(message = "El identificador del jugador es obligatorio.")
        UUID jugadorId,

        @NotNull(message = "El identificador de la propuesta de fecha es obligatorio.")
        UUID propuestaFechaId,

        @NotNull(message = "El tipo de disponibilidad es obligatorio.")
        EstadoDisponibilidad tipoDisponibilidad
) {
}
