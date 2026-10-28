package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import com.synagon.api.dominio.modelos.EstadoEnfrentamiento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad JPA que representa la tabla 'enfrentamientos' en PostgreSQL.
 */
@Entity
@Table(name = "enfrentamientos")
public class EntidadEnfrentamiento {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "division_id", nullable = false)
    private UUID divisionId;

    @Column(name = "equipo_local_id", nullable = false)
    private UUID equipoLocalId;

    @Column(name = "equipo_visitante_id", nullable = false)
    private UUID equipoVisitanteId;

    @Column(name = "numero_jornada", nullable = false)
    private int numeroJornada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoEnfrentamiento estado;

    @Column(name = "fecha_definitiva")
    private OffsetDateTime fechaDefinitiva;

    @Column(name = "pistas", length = 150)
    private String pistas;

    @Column(name = "url_foto_acta", length = 500)
    private String urlFotoActa;

    @Column(name = "puntos_local", nullable = false)
    private int puntosLocal;

    @Column(name = "puntos_visitante", nullable = false)
    private int puntosVisitante;

    @Column(name = "acta_confirmada", nullable = false)
    private boolean actaConfirmada;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @OneToMany(mappedBy = "enfrentamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private Set<EntidadPropuestaFecha> propuestasFecha = new LinkedHashSet<>();

    @OneToMany(mappedBy = "enfrentamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("creadoEn ASC")
    private Set<EntidadDisponibilidadJugador> disponibilidades = new LinkedHashSet<>();

    @OneToMany(mappedBy = "enfrentamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroPareja ASC")
    private Set<EntidadPartidoEnfrentamiento> partidos = new LinkedHashSet<>();

    protected EntidadEnfrentamiento() {
        // Requerido por JPA
    }

    public EntidadEnfrentamiento(
            UUID id,
            UUID divisionId,
            UUID equipoLocalId,
            UUID equipoVisitanteId,
            int numeroJornada,
            EstadoEnfrentamiento estado,
            OffsetDateTime fechaDefinitiva,
            String pistas,
            String urlFotoActa,
            int puntosLocal,
            int puntosVisitante,
            boolean actaConfirmada,
            OffsetDateTime creadoEn,
            OffsetDateTime actualizadoEn
    ) {
        this.id = Objects.requireNonNull(id);
        this.divisionId = Objects.requireNonNull(divisionId);
        this.equipoLocalId = Objects.requireNonNull(equipoLocalId);
        this.equipoVisitanteId = Objects.requireNonNull(equipoVisitanteId);
        this.numeroJornada = numeroJornada;
        this.estado = Objects.requireNonNull(estado);
        this.fechaDefinitiva = fechaDefinitiva;
        this.pistas = pistas;
        this.urlFotoActa = urlFotoActa;
        this.puntosLocal = puntosLocal;
        this.puntosVisitante = puntosVisitante;
        this.actaConfirmada = actaConfirmada;
        this.creadoEn = creadoEn != null ? creadoEn : OffsetDateTime.now();
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : OffsetDateTime.now();
    }

    public UUID obtenerId() { return id; }
    public UUID obtenerDivisionId() { return divisionId; }
    public UUID obtenerEquipoLocalId() { return equipoLocalId; }
    public UUID obtenerEquipoVisitanteId() { return equipoVisitanteId; }
    public int obtenerNumeroJornada() { return numeroJornada; }
    public EstadoEnfrentamiento obtenerEstado() { return estado; }
    public OffsetDateTime obtenerFechaDefinitiva() { return fechaDefinitiva; }
    public String obtenerPistas() { return pistas; }
    public String obtenerUrlFotoActa() { return urlFotoActa; }
    public int obtenerPuntosLocal() { return puntosLocal; }
    public int obtenerPuntosVisitante() { return puntosVisitante; }
    public boolean esActaConfirmada() { return actaConfirmada; }
    public OffsetDateTime obtenerCreadoEn() { return creadoEn; }
    public OffsetDateTime obtenerActualizadoEn() { return actualizadoEn; }
    public Set<EntidadPropuestaFecha> obtenerPropuestasFecha() { return propuestasFecha; }
    public Set<EntidadDisponibilidadJugador> obtenerDisponibilidades() { return disponibilidades; }
    public Set<EntidadPartidoEnfrentamiento> obtenerPartidos() { return partidos; }

    public void actualizarDatosPrincipales(
            EstadoEnfrentamiento estado,
            OffsetDateTime fechaDefinitiva,
            String pistas,
            String urlFotoActa,
            int puntosLocal,
            int puntosVisitante,
            boolean actaConfirmada,
            OffsetDateTime actualizadoEn
    ) {
        this.estado = Objects.requireNonNull(estado);
        this.fechaDefinitiva = fechaDefinitiva;
        this.pistas = pistas;
        this.urlFotoActa = urlFotoActa;
        this.puntosLocal = puntosLocal;
        this.puntosVisitante = puntosVisitante;
        this.actaConfirmada = actaConfirmada;
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : OffsetDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntidadEnfrentamiento that = (EntidadEnfrentamiento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
