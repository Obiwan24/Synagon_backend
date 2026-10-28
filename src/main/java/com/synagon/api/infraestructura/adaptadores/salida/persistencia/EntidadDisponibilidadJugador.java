package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import com.synagon.api.dominio.modelos.EstadoDisponibilidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA que representa la tabla 'disponibilidades_jugador' en PostgreSQL.
 */
@Entity
@Table(name = "disponibilidades_jugador")
public class EntidadDisponibilidadJugador {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enfrentamiento_id", nullable = false)
    private EntidadEnfrentamiento enfrentamiento;

    @Column(name = "propuesta_fecha_id")
    private UUID propuestaFechaId;

    @Column(name = "jugador_id", nullable = false)
    private UUID jugadorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_disponibilidad", nullable = false, length = 50)
    private EstadoDisponibilidad estadoDisponibilidad;

    @Column(name = "es_disponibilidad_final", nullable = false)
    private boolean esDisponibilidadFinal;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EntidadDisponibilidadJugador() {
        // Requerido por JPA
    }

    public EntidadDisponibilidadJugador(
            UUID id,
            EntidadEnfrentamiento enfrentamiento,
            UUID propuestaFechaId,
            UUID jugadorId,
            EstadoDisponibilidad estadoDisponibilidad,
            boolean esDisponibilidadFinal,
            OffsetDateTime creadoEn,
            OffsetDateTime actualizadoEn
    ) {
        this.id = Objects.requireNonNull(id);
        this.enfrentamiento = Objects.requireNonNull(enfrentamiento);
        this.propuestaFechaId = propuestaFechaId;
        this.jugadorId = Objects.requireNonNull(jugadorId);
        this.estadoDisponibilidad = Objects.requireNonNull(estadoDisponibilidad);
        this.esDisponibilidadFinal = esDisponibilidadFinal;
        this.creadoEn = creadoEn != null ? creadoEn : OffsetDateTime.now();
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : OffsetDateTime.now();
    }

    public UUID obtenerId() { return id; }
    public EntidadEnfrentamiento obtenerEnfrentamiento() { return enfrentamiento; }
    public UUID obtenerPropuestaFechaId() { return propuestaFechaId; }
    public UUID obtenerJugadorId() { return jugadorId; }
    public EstadoDisponibilidad obtenerEstadoDisponibilidad() { return estadoDisponibilidad; }
    public boolean esDisponibilidadFinal() { return esDisponibilidadFinal; }
    public OffsetDateTime obtenerCreadoEn() { return creadoEn; }
    public OffsetDateTime obtenerActualizadoEn() { return actualizadoEn; }

    public void actualizar(
            UUID propuestaFechaId,
            EstadoDisponibilidad estadoDisponibilidad,
            boolean esDisponibilidadFinal,
            OffsetDateTime actualizadoEn
    ) {
        this.propuestaFechaId = propuestaFechaId;
        this.estadoDisponibilidad = Objects.requireNonNull(estadoDisponibilidad);
        this.esDisponibilidadFinal = esDisponibilidadFinal;
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : OffsetDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntidadDisponibilidadJugador that = (EntidadDisponibilidadJugador) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
