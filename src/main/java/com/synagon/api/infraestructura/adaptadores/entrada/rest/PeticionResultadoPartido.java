package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de petición para el tanteo en sets de uno de los 3 partidos del enfrentamiento.
 */
public record PeticionResultadoPartido(
        @NotNull(message = "El número de pareja es obligatorio.")
        @Min(value = 1, message = "El número de pareja debe estar entre 1 y 3.")
        @Max(value = 3, message = "El número de pareja debe estar entre 1 y 3.")
        Integer numeroPareja,

        @NotNull(message = "Los juegos del set 1 local son obligatorios.")
        Integer set1Local,

        @NotNull(message = "Los juegos del set 1 visitante son obligatorios.")
        Integer set1Visitante,

        @NotNull(message = "Los juegos del set 2 local son obligatorios.")
        Integer set2Local,

        @NotNull(message = "Los juegos del set 2 visitante son obligatorios.")
        Integer set2Visitante,

        Integer set3Local,

        Integer set3Visitante
) {
}
