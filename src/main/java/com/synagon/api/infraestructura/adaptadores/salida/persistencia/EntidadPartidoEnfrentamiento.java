package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import com.synagon.api.dominio.modelos.GanadorPareja;
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
 * Entidad JPA que representa la tabla 'partidos_enfrentamiento' en PostgreSQL.
 */
@Entity
@Table(name = "partidos_enfrentamiento")
public class EntidadPartidoEnfrentamiento {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enfrentamiento_id", nullable = false)
    private EntidadEnfrentamiento enfrentamiento;

    @Column(name = "numero_pareja", nullable = false)
    private int numeroPareja;

    @Column(name = "jugador_local_1_id")
    private UUID jugadorLocal1Id;

    @Column(name = "jugador_local_2_id")
    private UUID jugadorLocal2Id;

    @Column(name = "jugador_visitante_1_id")
    private UUID jugadorVisitante1Id;

    @Column(name = "jugador_visitante_2_id")
    private UUID jugadorVisitante2Id;

    @Column(name = "set1_local")
    private Integer set1Local;

    @Column(name = "set1_visitante")
    private Integer set1Visitante;

    @Column(name = "set2_local")
    private Integer set2Local;

    @Column(name = "set2_visitante")
    private Integer set2Visitante;

    @Column(name = "set3_local")
    private Integer set3Local;

    @Column(name = "set3_visitante")
    private Integer set3Visitante;

    @Enumerated(EnumType.STRING)
    @Column(name = "pareja_ganadora", length = 20)
    private GanadorPareja parejaGanadora;

    @Column(name = "completado", nullable = false)
    private boolean completado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EntidadPartidoEnfrentamiento() {
        // Requerido por JPA
    }

    public EntidadPartidoEnfrentamiento(
            UUID id,
            EntidadEnfrentamiento enfrentamiento,
            int numeroPareja,
            UUID jugadorLocal1Id,
            UUID jugadorLocal2Id,
            UUID jugadorVisitante1Id,
            UUID jugadorVisitante2Id,
            Integer set1Local,
            Integer set1Visitante,
            Integer set2Local,
            Integer set2Visitante,
            Integer set3Local,
            Integer set3Visitante,
            GanadorPareja parejaGanadora,
            boolean completado,
            OffsetDateTime creadoEn,
            OffsetDateTime actualizadoEn
    ) {
        this.id = Objects.requireNonNull(id);
        this.enfrentamiento = Objects.requireNonNull(enfrentamiento);
        this.numeroPareja = numeroPareja;
        this.jugadorLocal1Id = jugadorLocal1Id;
        this.jugadorLocal2Id = jugadorLocal2Id;
        this.jugadorVisitante1Id = jugadorVisitante1Id;
        this.jugadorVisitante2Id = jugadorVisitante2Id;
        this.set1Local = set1Local;
        this.set1Visitante = set1Visitante;
        this.set2Local = set2Local;
        this.set2Visitante = set2Visitante;
        this.set3Local = set3Local;
        this.set3Visitante = set3Visitante;
        this.parejaGanadora = parejaGanadora;
        this.completado = completado;
        this.creadoEn = creadoEn != null ? creadoEn : OffsetDateTime.now();
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : OffsetDateTime.now();
    }

    public UUID obtenerId() { return id; }
    public EntidadEnfrentamiento obtenerEnfrentamiento() { return enfrentamiento; }
    public int obtenerNumeroPareja() { return numeroPareja; }
    public UUID obtenerJugadorLocal1Id() { return jugadorLocal1Id; }
    public UUID obtenerJugadorLocal2Id() { return jugadorLocal2Id; }
    public UUID obtenerJugadorVisitante1Id() { return jugadorVisitante1Id; }
    public UUID obtenerJugadorVisitante2Id() { return jugadorVisitante2Id; }
    public Integer obtenerSet1Local() { return set1Local; }
    public Integer obtenerSet1Visitante() { return set1Visitante; }
    public Integer obtenerSet2Local() { return set2Local; }
    public Integer obtenerSet2Visitante() { return set2Visitante; }
    public Integer obtenerSet3Local() { return set3Local; }
    public Integer obtenerSet3Visitante() { return set3Visitante; }
    public GanadorPareja obtenerParejaGanadora() { return parejaGanadora; }
    public boolean estaCompletado() { return completado; }
    public OffsetDateTime obtenerCreadoEn() { return creadoEn; }
    public OffsetDateTime obtenerActualizadoEn() { return actualizadoEn; }

    public void actualizar(
            int numeroPareja,
            UUID jugadorLocal1Id,
            UUID jugadorLocal2Id,
            UUID jugadorVisitante1Id,
            UUID jugadorVisitante2Id,
            Integer set1Local,
            Integer set1Visitante,
            Integer set2Local,
            Integer set2Visitante,
            Integer set3Local,
            Integer set3Visitante,
            GanadorPareja parejaGanadora,
            boolean completado,
            OffsetDateTime actualizadoEn
    ) {
        this.numeroPareja = numeroPareja;
        this.jugadorLocal1Id = jugadorLocal1Id;
        this.jugadorLocal2Id = jugadorLocal2Id;
        this.jugadorVisitante1Id = jugadorVisitante1Id;
        this.jugadorVisitante2Id = jugadorVisitante2Id;
        this.set1Local = set1Local;
        this.set1Visitante = set1Visitante;
        this.set2Local = set2Local;
        this.set2Visitante = set2Visitante;
        this.set3Local = set3Local;
        this.set3Visitante = set3Visitante;
        this.parejaGanadora = parejaGanadora;
        this.completado = completado;
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : OffsetDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntidadPartidoEnfrentamiento that = (EntidadPartidoEnfrentamiento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
