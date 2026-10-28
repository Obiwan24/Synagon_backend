package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO de petición para cerrar la convocatoria y alineación de las 3 parejas de la jornada.
 */
public record PeticionCerrarConvocatoria(
        @NotNull(message = "La lista de parejas es obligatoria.")
        @Size(min = 3, max = 3, message = "Se deben enviar exactamente 3 parejas.")
        List<@Valid @NotNull(message = "Ninguna pareja de la lista puede ser nula.") PeticionParejaAlineada> parejas
) {
}
