package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de petición para la resolución de cambio de contraseña en el primer acceso (`POST /api/v1/usuarios/{usuarioId}/primer-acceso-password`).
 */
public record PeticionPrimerAccesoPasswordDto(
        @NotNull(message = "Es obligatorio indicar si se desea cambiar la contraseña.")
        Boolean deseaCambiar,

        @Size(max = 128, message = "La nueva contraseña no puede superar los 128 caracteres.")
        String nuevaPasswordOpcional
) {
}
