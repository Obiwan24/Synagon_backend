package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.EstadisticaJugador;

import java.util.UUID;

/**
 * DTO de respuesta con las estadísticas completas de un jugador miembro de un equipo.
 */
public record RespuestaEstadisticaJugador(
        UUID jugadorId,
        String nombreJugador,
        UUID temporadaId,
        int partidosApuntados,
        int partidosJugados,
        int partidosGanados,
        int partidosPerdidos,
        int setsGanados,
        int setsPerdidos,
        int diferenciaSets,
        int juegosGanados,
        int juegosPerdidos,
        int diferenciaJuegos,
        double efectividadVictorias
) {
    public static RespuestaEstadisticaJugador desdeDominio(EstadisticaJugador dominio) {
        return new RespuestaEstadisticaJugador(
                dominio.obtenerJugadorId(),
                dominio.obtenerNombreJugador(),
                dominio.obtenerTemporadaId(),
                dominio.obtenerPartidosApuntados(),
                dominio.obtenerPartidosJugados(),
                dominio.obtenerPartidosGanados(),
                dominio.obtenerPartidosPerdidos(),
                dominio.obtenerSetsGanados(),
                dominio.obtenerSetsPerdidos(),
                dominio.calcularDiferenciaSets(),
                dominio.obtenerJuegosGanados(),
                dominio.obtenerJuegosPerdidos(),
                dominio.calcularDiferenciaJuegos(),
                dominio.calcularEfectividadVictorias()
        );
    }
}
