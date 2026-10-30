package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA que representa la tabla 'equipos' en PostgreSQL.
 */
@Entity
@Table(name = "equipos")
public class EntidadEquipo {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "division_id", nullable = false)
    private UUID divisionId;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "club", length = 150)
    private String club;

    @Column(name = "capitan_id")
    private UUID capitanId;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EntidadEquipo() {
        // Requerido por JPA
    }

    public EntidadEquipo(
            UUID id,
            UUID divisionId,
            String nombre,
            String club,
            UUID capitanId,
            OffsetDateTime creadoEn,
            OffsetDateTime actualizadoEn
    ) {
        this.id = Objects.requireNonNull(id);
        this.divisionId = Objects.requireNonNull(divisionId);
        this.nombre = Objects.requireNonNull(nombre);
        this.club = club;
        this.capitanId = capitanId;
        this.creadoEn = creadoEn != null ? creadoEn : OffsetDateTime.now();
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : OffsetDateTime.now();
    }

    public UUID obtenerId() { return id; }
    public UUID obtenerDivisionId() { return divisionId; }
    public String obtenerNombre() { return nombre; }
    public String obtenerClub() { return club; }
    public UUID obtenerCapitanId() { return capitanId; }
    public OffsetDateTime obtenerCreadoEn() { return creadoEn; }
    public OffsetDateTime obtenerActualizadoEn() { return actualizadoEn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntidadEquipo that = (EntidadEquipo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
