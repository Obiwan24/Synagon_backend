package com.synagon.api.dominio.modelos;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio inmutable que representa una propuesta de fecha para un enfrentamiento.
 * Según las reglas de negocio, el capitán local ofrece exactamente 2 propuestas de fecha (orden 1 y orden 2).
 */
public final class PropuestaFecha {

    private final UUID id;
    private final UUID enfrentamientoId;
    private final LocalDateTime fechaPropuesta;
    private final int orden;
    private final boolean esSeleccionada;

    public PropuestaFecha(
            UUID id,
            UUID enfrentamientoId,
            LocalDateTime fechaPropuesta,
            int orden,
            boolean esSeleccionada
    ) {
        if (orden != 1 && orden != 2) {
            throw new IllegalArgumentException("El orden de la propuesta de fecha debe ser 1 o 2.");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.enfrentamientoId = Objects.requireNonNull(enfrentamientoId, "El id del enfrentamiento no puede ser nulo.");
        this.fechaPropuesta = Objects.requireNonNull(fechaPropuesta, "La fecha propuesta no puede ser nula.");
        this.orden = orden;
        this.esSeleccionada = esSeleccionada;
    }

    public static PropuestaFecha crearNueva(UUID enfrentamientoId, LocalDateTime fechaPropuesta, int orden) {
        return new PropuestaFecha(UUID.randomUUID(), enfrentamientoId, fechaPropuesta, orden, false);
    }

    public PropuestaFecha marcarComoSeleccionada() {
        return new PropuestaFecha(this.id, this.enfrentamientoId, this.fechaPropuesta, this.orden, true);
    }

    public PropuestaFecha desmarcarSeleccion() {
        return new PropuestaFecha(this.id, this.enfrentamientoId, this.fechaPropuesta, this.orden, false);
    }

    public UUID obtenerId() {
        return id;
    }

    public UUID obtenerEnfrentamientoId() {
        return enfrentamientoId;
    }

    public LocalDateTime obtenerFechaPropuesta() {
        return fechaPropuesta;
    }

    public int obtenerOrden() {
        return orden;
    }

    public boolean esSeleccionada() {
        return esSeleccionada;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropuestaFecha that = (PropuestaFecha) o;
        return orden == that.orden &&
                esSeleccionada == that.esSeleccionada &&
                Objects.equals(id, that.id) &&
                Objects.equals(enfrentamientoId, that.enfrentamientoId) &&
                Objects.equals(fechaPropuesta, that.fechaPropuesta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, enfrentamientoId, fechaPropuesta, orden, esSeleccionada);
    }

    @Override
    public String toString() {
        return "PropuestaFecha{" +
                "id=" + id +
                ", enfrentamientoId=" + enfrentamientoId +
                ", fechaPropuesta=" + fechaPropuesta +
                ", orden=" + orden +
                ", esSeleccionada=" + esSeleccionada +
                '}';
    }
}
