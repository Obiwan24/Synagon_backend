package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de petición para proponer las 2 fechas candidatas de un enfrentamiento.
 */
public record PeticionProponerFechas(
        @NotNull(message = "La lista de fechas propuestas es obligatoria.")
        @Size(min = 2, max = 2, message = "Se deben proponer exactamente 2 fechas.")
        List<
                @NotNull(message = "La fecha propuesta no puede ser nula.")
                @Future(message = "Toda fecha propuesta debe ser futura.")
                LocalDateTime
        > fechas
) {
}
