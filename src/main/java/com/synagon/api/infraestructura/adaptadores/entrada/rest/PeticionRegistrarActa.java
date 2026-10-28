package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO de petición para registrar el acta arbitral de un enfrentamiento con la foto
 * y los resultados en sets de los 3 partidos.
 */
public record PeticionRegistrarActa(
        @NotBlank(message = "La URL de la foto del acta es obligatoria.")
        String urlFotoActa,

        @JsonAlias({"resultadosPartidos", "partidos"})
        @NotNull(message = "Los resultados de los tres partidos son obligatorios.")
        @Size(min = 3, max = 3, message = "Se deben enviar los resultados de exactamente 3 partidos.")
        List<@Valid @NotNull(message = "El resultado del partido no puede ser nulo.") PeticionResultadoPartido> resultadosTresPartidos
) {
}
