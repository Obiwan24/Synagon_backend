package com.synagon.api.dominio.modelos;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio inmutable que representa la disponibilidad comunicada por un jugador.
 * Aplica para la fase de votación de fechas propuestas o la confirmación definitiva para la jornada.
 */
public final class DisponibilidadJugador {

    private final UUID id;
    private final UUID enfrentamientoId;
    private final UUID propuestaFechaId; // Nulo cuando corresponde a la confirmación de la fecha definitiva
    private final UUID jugadorId;
    private final EstadoDisponibilidad estado;
    private final boolean esDisponibilidadFinal;
    private final LocalDateTime creadoEn;

    public DisponibilidadJugador(
            UUID id,
            UUID enfrentamientoId,
            UUID propuestaFechaId,
            UUID jugadorId,
            EstadoDisponibilidad estado,
            boolean esDisponibilidadFinal,
            LocalDateTime creadoEn
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.enfrentamientoId = Objects.requireNonNull(enfrentamientoId, "El id del enfrentamiento no puede ser nulo.");
        this.propuestaFechaId = propuestaFechaId;
        this.jugadorId = Objects.requireNonNull(jugadorId, "El id del jugador no puede ser nulo.");
        this.estado = Objects.requireNonNull(estado, "El estado de disponibilidad no puede ser nulo.");
        this.esDisponibilidadFinal = esDisponibilidadFinal;
        this.creadoEn = creadoEn != null ? creadoEn : LocalDateTime.now();
    }

    public static DisponibilidadJugador paraPropuesta(
            UUID enfrentamientoId,
            UUID propuestaFechaId,
            UUID jugadorId,
            EstadoDisponibilidad estado
    ) {
        Objects.requireNonNull(propuestaFechaId, "La propuesta de fecha es obligatoria en votación preliminar.");
        return new DisponibilidadJugador(
                UUID.randomUUID(),
                enfrentamientoId,
                propuestaFechaId,
                jugadorId,
                estado,
                false,
                LocalDateTime.now()
        );
    }

    public static DisponibilidadJugador paraFechaDefinitiva(
            UUID enfrentamientoId,
            UUID jugadorId,
            EstadoDisponibilidad estado
    ) {
        return new DisponibilidadJugador(
                UUID.randomUUID(),
                enfrentamientoId,
                null,
                jugadorId,
                estado,
                true,
                LocalDateTime.now()
        );
    }

    public DisponibilidadJugador cambiarEstado(EstadoDisponibilidad nuevoEstado) {
        return new DisponibilidadJugador(
                this.id,
                this.enfrentamientoId,
                this.propuestaFechaId,
                this.jugadorId,
                nuevoEstado,
                this.esDisponibilidadFinal,
                this.creadoEn
        );
    }

    public UUID obtenerId() {
        return id;
    }

    public UUID obtenerEnfrentamientoId() {
        return enfrentamientoId;
    }

    public UUID obtenerPropuestaFechaId() {
        return propuestaFechaId;
    }

    public UUID obtenerJugadorId() {
        return jugadorId;
    }

    public EstadoDisponibilidad obtenerEstado() {
        return estado;
    }

    public boolean esDisponibilidadFinal() {
        return esDisponibilidadFinal;
    }

    public LocalDateTime obtenerCreadoEn() {
        return creadoEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisponibilidadJugador that = (DisponibilidadJugador) o;
        return esDisponibilidadFinal == that.esDisponibilidadFinal &&
                Objects.equals(id, that.id) &&
                Objects.equals(enfrentamientoId, that.enfrentamientoId) &&
                Objects.equals(propuestaFechaId, that.propuestaFechaId) &&
                Objects.equals(jugadorId, that.jugadorId) &&
                estado == that.estado;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, enfrentamientoId, propuestaFechaId, jugadorId, estado, esDisponibilidadFinal);
    }

    @Override
    public String toString() {
        return "DisponibilidadJugador{" +
                "id=" + id +
                ", enfrentamientoId=" + enfrentamientoId +
                ", propuestaFechaId=" + propuestaFechaId +
                ", jugadorId=" + jugadorId +
                ", estado=" + estado +
                ", esDisponibilidadFinal=" + esDisponibilidadFinal +
                ", creadoEn=" + creadoEn +
                '}';
    }
}
