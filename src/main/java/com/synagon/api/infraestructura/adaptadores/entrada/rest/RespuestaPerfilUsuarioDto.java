package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.RolUsuario;
import com.synagon.api.dominio.modelos.Usuario;

import java.util.UUID;

/**
 * DTO de respuesta con los datos de perfil y estado de acceso de un usuario.
 */
public record RespuestaPerfilUsuarioDto(
        UUID id,
        String nombreCompleto,
        String nombre,
        String apellidos,
        String email,
        String dni,
        String telefono,
        RolUsuario rol,
        boolean requiereCambioPassword,
        boolean primerAccesoCompletado,
        String mensaje
) {
    public static RespuestaPerfilUsuarioDto desdeDominio(Usuario usuario, String mensaje) {
        return new RespuestaPerfilUsuarioDto(
                usuario.obtenerId(),
                usuario.obtenerNombreCompleto(),
                usuario.obtenerNombre(),
                usuario.obtenerApellidos(),
                usuario.obtenerEmail(),
                usuario.obtenerDni(),
                usuario.obtenerTelefono(),
                usuario.obtenerRol(),
                usuario.requiereCambioPassword(),
                usuario.haCompletadoPrimerAcceso(),
                mensaje
        );
    }
}
