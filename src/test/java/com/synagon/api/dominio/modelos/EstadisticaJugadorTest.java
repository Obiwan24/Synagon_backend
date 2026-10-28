package com.synagon.api.dominio.modelos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Modelo EstadisticaJugador")
class EstadisticaJugadorTest {

    @Test
    @DisplayName("Acumulación de estadísticas de partidos y cálculo de efectividad")
    void debeAcumularEstadisticasCorrectamente() {
        UUID jugadorId = UUID.randomUUID();
        UUID temporadaId = UUID.randomUUID();

        EstadisticaJugador stats = EstadisticaJugador.inicial(jugadorId, temporadaId);
        assertEquals(0, stats.obtenerPartidosJugados());
        assertEquals(0.0, stats.calcularEfectividadVictorias());

        // Partido 1: Victoria 2-0 (6-4, 6-3) -> sets: 2-0, juegos: 12-7
        stats = stats.acumularPartido(true, 2, 0, 12, 7, 3);
        assertEquals(1, stats.obtenerPartidosJugados());
        assertEquals(1, stats.obtenerPartidosGanados());
        assertEquals(0, stats.obtenerPartidosPerdidos());
        assertEquals(2, stats.obtenerSetsGanados());
        assertEquals(0, stats.obtenerSetsPerdidos());
        assertEquals(12, stats.obtenerJuegosGanados());
        assertEquals(7, stats.obtenerJuegosPerdidos());
        assertEquals(3, stats.obtenerPuntos());
        assertEquals(100.0, stats.calcularEfectividadVictorias());
        assertEquals(2, stats.calcularDiferenciaSets());
        assertEquals(5, stats.calcularDiferenciaJuegos());

        // Partido 2: Derrota 1-2 (6-4, 4-6, 5-7) -> sets: 1-2, juegos: 15-17
        stats = stats.acumularPartido(false, 1, 2, 15, 17, 1);
        assertEquals(2, stats.obtenerPartidosJugados());
        assertEquals(1, stats.obtenerPartidosGanados());
        assertEquals(1, stats.obtenerPartidosPerdidos());
        assertEquals(3, stats.obtenerSetsGanados());
        assertEquals(2, stats.obtenerSetsPerdidos());
        assertEquals(27, stats.obtenerJuegosGanados());
        assertEquals(24, stats.obtenerJuegosPerdidos());
        assertEquals(4, stats.obtenerPuntos());
        assertEquals(50.0, stats.calcularEfectividadVictorias());
        assertEquals(1, stats.calcularDiferenciaSets());
        assertEquals(3, stats.calcularDiferenciaJuegos());
    }
}
