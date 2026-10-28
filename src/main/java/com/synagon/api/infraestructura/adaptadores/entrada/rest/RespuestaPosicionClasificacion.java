package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.PosicionClasificacionEquipo;

import java.util.UUID;

/**
 * DTO de respuesta que representa la posición y métricas de un equipo en la clasificación de una división.
 */
public record RespuestaPosicionClasificacion(
        int posicion,
        UUID equipoId,
        String nombreEquipo,
        int puntos,
        int enfrentamientosJugados,
        int enfrentamientosGanados,
        int enfrentamientosPerdidos,
        int partidosGanados,
        int partidosPerdidos,
        int diferenciaPartidos,
        int setsAFavor,
        int setsEnContra,
        int diferenciaSets,
        int juegosAFavor,
        int juegosEnContra,
        int diferenciaJuegos
) {
    public static RespuestaPosicionClasificacion desdeDominio(PosicionClasificacionEquipo dominio) {
        return new RespuestaPosicionClasificacion(
                dominio.obtenerPosicion(),
                dominio.obtenerEquipoId(),
                dominio.obtenerNombreEquipo(),
                dominio.obtenerPuntos(),
                dominio.obtenerEnfrentamientosJugados(),
                dominio.obtenerEnfrentamientosGanados(),
                dominio.obtenerEnfrentamientosPerdidos(),
                dominio.obtenerPartidosGanados(),
                dominio.obtenerPartidosPerdidos(),
                dominio.calcularDiferenciaPartidos(),
                dominio.obtenerSetsAFavor(),
                dominio.obtenerSetsEnContra(),
                dominio.calcularDiferenciaSets(),
                dominio.obtenerJuegosAFavor(),
                dominio.obtenerJuegosEnContra(),
                dominio.calcularDiferenciaJuegos()
        );
    }
}
