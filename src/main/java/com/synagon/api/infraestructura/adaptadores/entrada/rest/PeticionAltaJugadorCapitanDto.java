package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO de petición para que el capitán dé de alta o vincule a un jugador mediante su email y DNI.
 */
public record PeticionAltaJugadorCapitanDto(
        @NotNull(message = "El identificador del capitán es obligatorio.")
        UUID capitanId,

        @NotBlank(message = "El correo electrónico del jugador es obligatorio.")
        @Email(message = "El formato del correo electrónico no es válido.")
        String email,

        @NotBlank(message = "El DNI del jugador es obligatorio.")
        @Size(min = 4, max = 20, message = "El DNI debe tener entre 4 y 20 caracteres.")
        String dni,

        @Size(max = 200, message = "El nombre completo no puede superar los 200 caracteres.")
        String nombreCompleto,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres.")
        String telefono
) {
}
