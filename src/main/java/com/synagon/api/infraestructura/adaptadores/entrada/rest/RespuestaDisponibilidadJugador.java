package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.DisponibilidadJugador;
import com.synagon.api.dominio.modelos.EstadoDisponibilidad;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para el voto de disponibilidad de un jugador.
 */
public record RespuestaDisponibilidadJugador(
        UUID id,
        UUID propuestaFechaId,
        UUID jugadorId,
        EstadoDisponibilidad estado,
        boolean esDisponibilidadFinal,
        LocalDateTime creadoEn
) {
    public static RespuestaDisponibilidadJugador desdeDominio(DisponibilidadJugador dominio) {
        return new RespuestaDisponibilidadJugador(
                dominio.obtenerId(),
                dominio.obtenerPropuestaFechaId(),
                dominio.obtenerJugadorId(),
                dominio.obtenerEstado(),
                dominio.esDisponibilidadFinal(),
                dominio.obtenerCreadoEn()
        );
    }
}
