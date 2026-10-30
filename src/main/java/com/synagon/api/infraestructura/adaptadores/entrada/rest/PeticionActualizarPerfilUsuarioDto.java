package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de petición para actualizar el perfil personal de un usuario (`PUT /api/v1/usuarios/{usuarioId}/perfil`).
 */
public record PeticionActualizarPerfilUsuarioDto(
        @NotBlank(message = "El nombre completo es obligatorio.")
        @Size(min = 3, max = 200, message = "El nombre completo debe tener entre 3 y 200 caracteres.")
        String nombreCompleto,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres.")
        String telefono,

        @Size(max = 20, message = "El DNI no puede superar los 20 caracteres.")
        String dni
) {
}
