package com.synagon.api.dominio.modelos;

import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio inmutable que almacena las estadísticas acumuladas de un jugador en un equipo/temporada.
 * Incluye partidosApuntados (veces que marcó DISPONIBLE), partidosJugados, partidosGanados,
 * partidosPerdidos, setsGanados, setsPerdidos, juegosGanados y juegosPerdidos.
 */
public final class EstadisticaJugador {

    private final UUID id;
    private final UUID jugadorId;
    private final String nombreJugador;
    private final UUID temporadaId;
    private final int partidosApuntados;
    private final int partidosJugados;
    private final int partidosGanados;
    private final int partidosPerdidos;
    private final int setsGanados;
    private final int setsPerdidos;
    private final int juegosGanados;
    private final int juegosPerdidos;
    private final int puntos;

    public EstadisticaJugador(
            UUID id,
            UUID jugadorId,
            UUID temporadaId,
            int partidosJugados,
            int partidosGanados,
            int partidosPerdidos,
            int setsGanados,
            int setsPerdidos,
            int juegosGanados,
            int juegosPerdidos,
            int puntos
    ) {
        this(
                id,
                jugadorId,
                null,
                temporadaId,
                0,
                partidosJugados,
                partidosGanados,
                partidosPerdidos,
                setsGanados,
                setsPerdidos,
                juegosGanados,
                juegosPerdidos,
                puntos
        );
    }

    public EstadisticaJugador(
            UUID id,
            UUID jugadorId,
            String nombreJugador,
            UUID temporadaId,
            int partidosApuntados,
            int partidosJugados,
            int partidosGanados,
            int partidosPerdidos,
            int setsGanados,
            int setsPerdidos,
            int juegosGanados,
            int juegosPerdidos,
            int puntos
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.jugadorId = Objects.requireNonNull(jugadorId, "El id del jugador no puede ser nulo.");
        this.nombreJugador = nombreJugador;
        this.temporadaId = Objects.requireNonNull(temporadaId, "El id de la temporada no puede ser nulo.");
        this.partidosApuntados = Math.max(0, partidosApuntados);
        this.partidosJugados = Math.max(0, partidosJugados);
        this.partidosGanados = Math.max(0, partidosGanados);
        this.partidosPerdidos = Math.max(0, partidosPerdidos);
        this.setsGanados = Math.max(0, setsGanados);
        this.setsPerdidos = Math.max(0, setsPerdidos);
        this.juegosGanados = Math.max(0, juegosGanados);
        this.juegosPerdidos = Math.max(0, juegosPerdidos);
        this.puntos = Math.max(0, puntos);
    }

    public static EstadisticaJugador inicial(UUID jugadorId, UUID temporadaId) {
        return new EstadisticaJugador(
                UUID.randomUUID(),
                jugadorId,
                temporadaId,
                0, 0, 0, 0, 0, 0, 0, 0
        );
    }

    public static EstadisticaJugador inicialConDisponibilidad(
            UUID jugadorId,
            String nombreJugador,
            UUID temporadaId,
            int partidosApuntados
    ) {
        return new EstadisticaJugador(
                UUID.randomUUID(),
                jugadorId,
                nombreJugador,
                temporadaId,
                partidosApuntados,
                0, 0, 0, 0, 0, 0, 0, 0
        );
    }

    /**
     * Retorna una nueva instancia acumulando el resultado de un partido disputado.
     */
    public EstadisticaJugador acumularPartido(
            boolean victoria,
            int setsGanadosPartido,
            int setsPerdidosPartido,
            int juegosGanadosPartido,
            int juegosPerdidosPartido,
            int puntosObtenidos
    ) {
        return new EstadisticaJugador(
                this.id,
                this.jugadorId,
                this.nombreJugador,
                this.temporadaId,
                this.partidosApuntados,
                this.partidosJugados + 1,
                this.partidosGanados + (victoria ? 1 : 0),
                this.partidosPerdidos + (victoria ? 0 : 1),
                this.setsGanados + setsGanadosPartido,
                this.setsPerdidos + setsPerdidosPartido,
                this.juegosGanados + juegosGanadosPartido,
                this.juegosPerdidos + juegosPerdidosPartido,
                this.puntos + puntosObtenidos
        );
    }

    /**
     * Porcentaje de partidos ganados sobre disputados (0 a 100).
     */
    public double calcularEfectividadVictorias() {
        if (partidosJugados == 0) return 0.0;
        return ((double) partidosGanados / partidosJugados) * 100.0;
    }

    public int calcularDiferenciaSets() {
        return setsGanados - setsPerdidos;
    }

    public int calcularDiferenciaJuegos() {
        return juegosGanados - juegosPerdidos;
    }

    public UUID obtenerId() { return id; }
    public UUID obtenerJugadorId() { return jugadorId; }
    public String obtenerNombreJugador() { return nombreJugador; }
    public UUID obtenerTemporadaId() { return temporadaId; }
    public int obtenerPartidosApuntados() { return partidosApuntados; }
    public int obtenerPartidosJugados() { return partidosJugados; }
    public int obtenerPartidosGanados() { return partidosGanados; }
    public int obtenerPartidosPerdidos() { return partidosPerdidos; }
    public int obtenerSetsGanados() { return setsGanados; }
    public int obtenerSetsPerdidos() { return setsPerdidos; }
    public int obtenerJuegosGanados() { return juegosGanados; }
    public int obtenerJuegosPerdidos() { return juegosPerdidos; }
    public int obtenerPuntos() { return puntos; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstadisticaJugador that = (EstadisticaJugador) o;
        return partidosApuntados == that.partidosApuntados &&
                partidosJugados == that.partidosJugados &&
                partidosGanados == that.partidosGanados &&
                partidosPerdidos == that.partidosPerdidos &&
                setsGanados == that.setsGanados &&
                setsPerdidos == that.setsPerdidos &&
                juegosGanados == that.juegosGanados &&
                juegosPerdidos == that.juegosPerdidos &&
                puntos == that.puntos &&
                Objects.equals(id, that.id) &&
                Objects.equals(jugadorId, that.jugadorId) &&
                Objects.equals(temporadaId, that.temporadaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, jugadorId, temporadaId, partidosApuntados, partidosJugados,
                partidosGanados, partidosPerdidos, setsGanados, setsPerdidos,
                juegosGanados, juegosPerdidos, puntos);
    }

    @Override
    public String toString() {
        return "EstadisticaJugador{" +
                "id=" + id +
                ", jugadorId=" + jugadorId +
                ", nombre='" + nombreJugador + '\'' +
                ", temporadaId=" + temporadaId +
                ", apuntados=" + partidosApuntados +
                ", jugados=" + partidosJugados +
                ", ganados=" + partidosGanados +
                ", perdidos=" + partidosPerdidos +
                ", sets=" + setsGanados + "/" + setsPerdidos +
                ", juegos=" + juegosGanados + "/" + juegosPerdidos +
                ", puntos=" + puntos +
                '}';
    }
}
