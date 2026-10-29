package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.RolUsuario;
import com.synagon.api.dominio.modelos.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta tras completar el registro de un nuevo jugador en Synagon.
 */
public record RegistroRespuestaDto(
        UUID id,
        String nombreCompleto,
        String nombre,
        String apellidos,
        String email,
        String telefono,
        RolUsuario rol,
        UUID equipoId,
        boolean aceptoPrivacidad,
        LocalDateTime creadoEn,
        String mensaje
) {
    public static RegistroRespuestaDto desdeDominio(Usuario usuario) {
        return new RegistroRespuestaDto(
                usuario.obtenerId(),
                usuario.obtenerNombreCompleto(),
                usuario.obtenerNombre(),
                usuario.obtenerApellidos(),
                usuario.obtenerEmail(),
                usuario.obtenerTelefono(),
                usuario.obtenerRol(),
                usuario.obtenerEquipoId(),
                usuario.haAceptadoPrivacidad(),
                usuario.obtenerCreadoEn(),
                "Usuario registrado correctamente."
        );
    }
}
