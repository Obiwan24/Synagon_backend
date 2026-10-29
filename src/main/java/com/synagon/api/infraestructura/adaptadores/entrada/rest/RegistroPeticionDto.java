package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de petición para el registro público de un nuevo usuario en Synagon (`POST /api/v1/usuarios/registro`).
 */
public record RegistroPeticionDto(
        @NotBlank(message = "El nombre completo es obligatorio.")
        @Size(min = 3, max = 200, message = "El nombre completo debe tener al menos 3 caracteres.")
        String nombreCompleto,

        @NotBlank(message = "El correo electrónico es obligatorio.")
        @Email(message = "El formato del correo electrónico no es válido.")
        String email,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres.")
        String telefono,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 6, max = 128, message = "La contraseña debe tener al menos 6 caracteres.")
        String password,

        @NotNull(message = "Es obligatorio aceptar la política de privacidad en cumplimiento del RGPD.")
        @AssertTrue(message = "Es obligatorio aceptar la política de privacidad en cumplimiento del RGPD.")
        Boolean aceptoPrivacidad
) {
}
