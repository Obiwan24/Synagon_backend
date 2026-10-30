package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.ResultadoAltaJugadorEquipo;
import com.synagon.api.dominio.modelos.Usuario;

import java.util.UUID;

/**
 * DTO de respuesta tras dar de alta o vincular a un jugador en un equipo por parte del capitán.
 */
public record RespuestaAltaJugadorEquipoDto(
        UUID usuarioId,
        UUID equipoId,
        String nombreCompleto,
        String email,
        String dni,
        String telefono,
        boolean usuarioNuevoCreado,
        boolean requiereCambioPassword,
        boolean primerAccesoCompletado,
        String mensaje
) {
    public static RespuestaAltaJugadorEquipoDto desdeDominio(ResultadoAltaJugadorEquipo resultado) {
        Usuario u = resultado.usuario();
        String mensaje = resultado.usuarioNuevoCreado()
                ? "Jugador creado con contraseña provisional (DNI en mayúsculas) y vinculado al equipo."
                : "Jugador existente vinculado al equipo sin modificar su contraseña actual.";
        return new RespuestaAltaJugadorEquipoDto(
                u.obtenerId(),
                resultado.equipoId(),
                u.obtenerNombreCompleto(),
                u.obtenerEmail(),
                u.obtenerDni(),
                u.obtenerTelefono(),
                resultado.usuarioNuevoCreado(),
                u.requiereCambioPassword(),
                u.haCompletadoPrimerAcceso(),
                mensaje
        );
    }
}
