package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO de petición para actualizar la sede alternativa de un enfrentamiento en una jornada concreta.
 */
public record PeticionActualizarSedeJornadaDto(
        @NotNull(message = "El identificador del capitán es obligatorio.")
        UUID capitanId,

        @Size(max = 150, message = "La sede alternativa no puede superar los 150 caracteres.")
        String nuevaSedeAlternativa
) {
}
