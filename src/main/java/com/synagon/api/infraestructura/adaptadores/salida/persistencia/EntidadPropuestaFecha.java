package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA que representa la tabla 'propuestas_fecha' en PostgreSQL.
 */
@Entity
@Table(name = "propuestas_fecha")
public class EntidadPropuestaFecha {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enfrentamiento_id", nullable = false)
    private EntidadEnfrentamiento enfrentamiento;

    @Column(name = "fecha_propuesta", nullable = false)
    private OffsetDateTime fechaPropuesta;

    @Column(name = "orden", nullable = false)
    private int orden;

    @Column(name = "es_seleccionada", nullable = false)
    private boolean esSeleccionada;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EntidadPropuestaFecha() {
        // Requerido por JPA
    }

    public EntidadPropuestaFecha(
            UUID id,
            EntidadEnfrentamiento enfrentamiento,
            OffsetDateTime fechaPropuesta,
            int orden,
            boolean esSeleccionada,
            OffsetDateTime creadoEn,
            OffsetDateTime actualizadoEn
    ) {
        this.id = Objects.requireNonNull(id);
        this.enfrentamiento = Objects.requireNonNull(enfrentamiento);
        this.fechaPropuesta = Objects.requireNonNull(fechaPropuesta);
        this.orden = orden;
        this.esSeleccionada = esSeleccionada;
        this.creadoEn = creadoEn != null ? creadoEn : OffsetDateTime.now();
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : OffsetDateTime.now();
    }

    public UUID obtenerId() { return id; }
    public EntidadEnfrentamiento obtenerEnfrentamiento() { return enfrentamiento; }
    public OffsetDateTime obtenerFechaPropuesta() { return fechaPropuesta; }
    public int obtenerOrden() { return orden; }
    public boolean esSeleccionada() { return esSeleccionada; }
    public OffsetDateTime obtenerCreadoEn() { return creadoEn; }
    public OffsetDateTime obtenerActualizadoEn() { return actualizadoEn; }

    public void actualizar(OffsetDateTime fechaPropuesta, int orden, boolean esSeleccionada, OffsetDateTime actualizadoEn) {
        this.fechaPropuesta = Objects.requireNonNull(fechaPropuesta);
        this.orden = orden;
        this.esSeleccionada = esSeleccionada;
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : OffsetDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntidadPropuestaFecha that = (EntidadPropuestaFecha) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
