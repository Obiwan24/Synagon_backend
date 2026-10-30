package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA que representa la tabla 'miembros_equipo' en PostgreSQL.
 */
@Entity
@Table(name = "miembros_equipo")
public class EntidadMiembroEquipo {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "equipo_id", nullable = false)
    private UUID equipoId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "es_capitan", nullable = false)
    private boolean esCapitan;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EntidadMiembroEquipo() {
        // Requerido por JPA
    }

    public EntidadMiembroEquipo(
            UUID id,
            UUID equipoId,
            UUID usuarioId,
            boolean esCapitan,
            boolean activo,
            OffsetDateTime creadoEn,
            OffsetDateTime actualizadoEn
    ) {
        this.id = Objects.requireNonNull(id);
        this.equipoId = Objects.requireNonNull(equipoId);
        this.usuarioId = Objects.requireNonNull(usuarioId);
        this.esCapitan = esCapitan;
        this.activo = activo;
        this.creadoEn = creadoEn != null ? creadoEn : OffsetDateTime.now();
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : OffsetDateTime.now();
    }

    public void reactivarMembresia(boolean comoCapitan, OffsetDateTime ahora) {
        this.activo = true;
        if (comoCapitan) {
            this.esCapitan = true;
        }
        this.actualizadoEn = ahora != null ? ahora : OffsetDateTime.now();
    }

    public UUID obtenerId() { return id; }
    public UUID obtenerEquipoId() { return equipoId; }
    public UUID obtenerUsuarioId() { return usuarioId; }
    public boolean esCapitan() { return esCapitan; }
    public boolean estaActivo() { return activo; }
    public OffsetDateTime obtenerCreadoEn() { return creadoEn; }
    public OffsetDateTime obtenerActualizadoEn() { return actualizadoEn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntidadMiembroEquipo that = (EntidadMiembroEquipo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
