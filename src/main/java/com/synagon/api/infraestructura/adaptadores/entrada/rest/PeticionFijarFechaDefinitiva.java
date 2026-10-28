package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO de petición para fijar la fecha definitiva del enfrentamiento a partir de una propuesta.
 */
public record PeticionFijarFechaDefinitiva(
        @NotNull(message = "El identificador de la propuesta de fecha elegida es obligatorio.")
        UUID propuestaFechaElegidaId
) {
}
