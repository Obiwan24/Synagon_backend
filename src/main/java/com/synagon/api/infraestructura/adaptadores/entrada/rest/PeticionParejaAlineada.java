package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO que representa una pareja convocada (Pareja 1, 2 o 3) con sus dos jugadores.
 */
public record PeticionParejaAlineada(
        @NotNull(message = "El número de pareja es obligatorio.")
        @Min(value = 1, message = "El número de pareja debe estar entre 1 y 3.")
        @Max(value = 3, message = "El número de pareja debe estar entre 1 y 3.")
        Integer numeroPareja,

        @NotNull(message = "El identificador del jugador 1 es obligatorio.")
        UUID jugador1Id,

        @NotNull(message = "El identificador del jugador 2 es obligatorio.")
        UUID jugador2Id
) {
}
