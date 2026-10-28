package com.synagon.api.dominio.modelos;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio inmutable que representa la fila de un equipo en la tabla de clasificación
 * de una división, incluyendo criterios de puntuación y desempate reglamentarios.
 */
public final class PosicionClasificacionEquipo {

    public static final int PUNTOS_POR_VICTORIA_ENFRENTAMIENTO = 3;

    private final int posicion;
    private final UUID equipoId;
    private final String nombreEquipo;
    private final int puntos;
    private final int enfrentamientosJugados;
    private final int enfrentamientosGanados;
    private final int enfrentamientosPerdidos;
    private final int partidosGanados;
    private final int partidosPerdidos;
    private final int setsAFavor;
    private final int setsEnContra;
    private final int juegosAFavor;
    private final int juegosEnContra;

    public PosicionClasificacionEquipo(
            int posicion,
            UUID equipoId,
            String nombreEquipo,
            int puntos,
            int enfrentamientosJugados,
            int enfrentamientosGanados,
            int enfrentamientosPerdidos,
            int partidosGanados,
            int partidosPerdidos,
            int setsAFavor,
            int setsEnContra,
            int juegosAFavor,
            int juegosEnContra
    ) {
        this.posicion = Math.max(0, posicion);
        this.equipoId = Objects.requireNonNull(equipoId, "El identificador del equipo es obligatorio.");
        this.nombreEquipo = Objects.requireNonNull(nombreEquipo, "El nombre del equipo es obligatorio.");
        this.puntos = Math.max(0, puntos);
        this.enfrentamientosJugados = Math.max(0, enfrentamientosJugados);
        this.enfrentamientosGanados = Math.max(0, enfrentamientosGanados);
        this.enfrentamientosPerdidos = Math.max(0, enfrentamientosPerdidos);
        this.partidosGanados = Math.max(0, partidosGanados);
        this.partidosPerdidos = Math.max(0, partidosPerdidos);
        this.setsAFavor = Math.max(0, setsAFavor);
        this.setsEnContra = Math.max(0, setsEnContra);
        this.juegosAFavor = Math.max(0, juegosAFavor);
        this.juegosEnContra = Math.max(0, juegosEnContra);
    }

    public PosicionClasificacionEquipo conPosicion(int nuevaPosicion) {
        return new PosicionClasificacionEquipo(
                nuevaPosicion,
                this.equipoId,
                this.nombreEquipo,
                this.puntos,
                this.enfrentamientosJugados,
                this.enfrentamientosGanados,
                this.enfrentamientosPerdidos,
                this.partidosGanados,
                this.partidosPerdidos,
                this.setsAFavor,
                this.setsEnContra,
                this.juegosAFavor,
                this.juegosEnContra
        );
    }

    /**
     * Comparador oficial de clasificación:
     * 1. Puntos (descendente)
     * 2. Diferencia de partidos (descendente)
     * 3. Diferencia de sets (descendente)
     * 4. Diferencia de juegos (descendente)
     * 5. Partidos ganados (descendente)
     * 6. Nombre del equipo en orden alfabético español (ascendente)
     */
    public static Comparator<PosicionClasificacionEquipo> comparadorClasificacion() {
        Collator coladorEspanol = Collator.getInstance(Locale.forLanguageTag("es-ES"));
        coladorEspanol.setStrength(Collator.PRIMARY);

        return Comparator.comparingInt(PosicionClasificacionEquipo::obtenerPuntos).reversed()
                .thenComparing(Comparator.comparingInt(PosicionClasificacionEquipo::calcularDiferenciaPartidos).reversed())
                .thenComparing(Comparator.comparingInt(PosicionClasificacionEquipo::calcularDiferenciaSets).reversed())
                .thenComparing(Comparator.comparingInt(PosicionClasificacionEquipo::calcularDiferenciaJuegos).reversed())
                .thenComparing(Comparator.comparingInt(PosicionClasificacionEquipo::obtenerPartidosGanados).reversed())
                .thenComparing(PosicionClasificacionEquipo::obtenerNombreEquipo, coladorEspanol);
    }

    public int calcularDiferenciaPartidos() {
        return partidosGanados - partidosPerdidos;
    }

    public int calcularDiferenciaSets() {
        return setsAFavor - setsEnContra;
    }

    public int calcularDiferenciaJuegos() {
        return juegosAFavor - juegosEnContra;
    }

    public int obtenerPosicion() { return posicion; }
    public UUID obtenerEquipoId() { return equipoId; }
    public String obtenerNombreEquipo() { return nombreEquipo; }
    public int obtenerPuntos() { return puntos; }
    public int obtenerEnfrentamientosJugados() { return enfrentamientosJugados; }
    public int obtenerEnfrentamientosGanados() { return enfrentamientosGanados; }
    public int obtenerEnfrentamientosPerdidos() { return enfrentamientosPerdidos; }
    public int obtenerPartidosGanados() { return partidosGanados; }
    public int obtenerPartidosPerdidos() { return partidosPerdidos; }
    public int obtenerSetsAFavor() { return setsAFavor; }
    public int obtenerSetsEnContra() { return setsEnContra; }
    public int obtenerJuegosAFavor() { return juegosAFavor; }
    public int obtenerJuegosEnContra() { return juegosEnContra; }
}
