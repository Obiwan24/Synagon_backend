package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO de petición para inscribir un nuevo equipo en una liga mediante código de invitación (`POST /api/v1/agora/equipos/inscribir`).
 */
public record PeticionInscribirEquipoDto(
        @NotNull(message = "El identificador del usuario creador es obligatorio.")
        UUID usuarioId,

        @NotBlank(message = "El código de invitación de la liga es obligatorio.")
        @Size(max = 30, message = "El código de invitación no puede superar los 30 caracteres.")
        String codigoInvitacion,

        @NotBlank(message = "El nombre del equipo es obligatorio.")
        @Size(min = 3, max = 150, message = "El nombre del equipo debe tener entre 3 y 150 caracteres.")
        String nombreEquipo,

        @Size(max = 150, message = "El club o sede oficial del equipo no puede superar los 150 caracteres.")
        String clubSedeOpcional
) {
}
