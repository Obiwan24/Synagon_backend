package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import com.synagon.api.dominio.modelos.ModoSedeLiga;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA que representa la tabla 'ligas' en PostgreSQL.
 */
@Entity
@Table(name = "ligas")
public class EntidadLiga {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "codigo_invitacion", nullable = false, unique = true, length = 30)
    private String codigoInvitacion;

    @Column(name = "presidente_id")
    private UUID presidenteId;

    @Column(name = "partidos_por_jornada", nullable = false)
    private int partidosPorJornada;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_sede", nullable = false, length = 30)
    private ModoSedeLiga modoSede;

    @Column(name = "sede_unica_liga", length = 150)
    private String sedeUnicaLiga;

    @Column(name = "puntos_victoria", nullable = false)
    private int puntosVictoria;

    @Column(name = "puntos_empate", nullable = false)
    private int puntosEmpate;

    @Column(name = "puntos_derrota", nullable = false)
    private int puntosDerrota;

    @Column(name = "exigir_dni_jugador", nullable = false)
    private boolean exigirDniJugador;

    @Column(name = "exigir_telefono_jugador", nullable = false)
    private boolean exigirTelefonoJugador;

    @Column(name = "normativa_texto", columnDefinition = "TEXT")
    private String normativaTexto;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EntidadLiga() {
        // Requerido por JPA
    }

    public EntidadLiga(
            UUID id,
            String nombre,
            String descripcion,
            String codigoInvitacion,
            UUID presidenteId,
            int partidosPorJornada,
            ModoSedeLiga modoSede,
            String sedeUnicaLiga,
            int puntosVictoria,
            int puntosEmpate,
            int puntosDerrota,
            boolean exigirDniJugador,
            boolean exigirTelefonoJugador,
            String normativaTexto,
            OffsetDateTime creadoEn,
            OffsetDateTime actualizadoEn
    ) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre);
        this.descripcion = descripcion;
        this.codigoInvitacion = Objects.requireNonNull(codigoInvitacion);
        this.presidenteId = presidenteId;
        this.partidosPorJornada = partidosPorJornada;
        this.modoSede = Objects.requireNonNull(modoSede);
        this.sedeUnicaLiga = sedeUnicaLiga;
        this.puntosVictoria = puntosVictoria;
        this.puntosEmpate = puntosEmpate;
        this.puntosDerrota = puntosDerrota;
        this.exigirDniJugador = exigirDniJugador;
        this.exigirTelefonoJugador = exigirTelefonoJugador;
        this.normativaTexto = normativaTexto;
        this.creadoEn = creadoEn != null ? creadoEn : OffsetDateTime.now();
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : OffsetDateTime.now();
    }

    public UUID obtenerId() { return id; }
    public String obtenerNombre() { return nombre; }
    public String obtenerDescripcion() { return descripcion; }
    public String obtenerCodigoInvitacion() { return codigoInvitacion; }
    public UUID obtenerPresidenteId() { return presidenteId; }
    public int obtenerPartidosPorJornada() { return partidosPorJornada; }
    public ModoSedeLiga obtenerModoSede() { return modoSede; }
    public String obtenerSedeUnicaLiga() { return sedeUnicaLiga; }
    public int obtenerPuntosVictoria() { return puntosVictoria; }
    public int obtenerPuntosEmpate() { return puntosEmpate; }
    public int obtenerPuntosDerrota() { return puntosDerrota; }
    public boolean exigeDniJugador() { return exigirDniJugador; }
    public boolean exigeTelefonoJugador() { return exigirTelefonoJugador; }
    public String obtenerNormativaTexto() { return normativaTexto; }
    public OffsetDateTime obtenerCreadoEn() { return creadoEn; }
    public OffsetDateTime obtenerActualizadoEn() { return actualizadoEn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntidadLiga that = (EntidadLiga) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
