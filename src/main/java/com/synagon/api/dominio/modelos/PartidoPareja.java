package com.synagon.api.dominio.modelos;

import com.synagon.api.dominio.excepciones.ConvocatoriaInvalidaExcepcion;
import com.synagon.api.dominio.excepciones.ResultadoPadelIlegalExcepcion;

import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio inmutable que representa uno de los tres partidos por parejas
 * que componen un enfrentamiento de liga de pádel.
 * Incluye la validación reglamentaria oficial del tanteo delegada en {@link ValidadorTanteoPadel}.
 */
public final class PartidoPareja {

    private final UUID id;
    private final UUID enfrentamientoId;
    private final int numeroPareja; // 1, 2 o 3
    private final UUID jugadorLocal1Id;
    private final UUID jugadorLocal2Id;
    private final UUID jugadorVisitante1Id;
    private final UUID jugadorVisitante2Id;
    private final Integer set1Local;
    private final Integer set1Visitante;
    private final Integer set2Local;
    private final Integer set2Visitante;
    private final Integer set3Local;
    private final Integer set3Visitante;
    private final GanadorPareja parejaGanadora;
    private final boolean completado;

    public PartidoPareja(
            UUID id,
            UUID enfrentamientoId,
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
            boolean completado
    ) {
        if (numeroPareja < 1 || numeroPareja > 3) {
            throw new ConvocatoriaInvalidaExcepcion("El número de pareja debe ser 1, 2 o 3.");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.enfrentamientoId = Objects.requireNonNull(enfrentamientoId, "El id del enfrentamiento no puede ser nulo.");
        this.numeroPareja = numeroPareja;

        boolean tieneParejaLocal = jugadorLocal1Id != null || jugadorLocal2Id != null;
        boolean tieneParejaVisitante = jugadorVisitante1Id != null || jugadorVisitante2Id != null;

        if (!tieneParejaLocal && !tieneParejaVisitante) {
            throw new ConvocatoriaInvalidaExcepcion("El partido debe tener al menos una pareja asignada.");
        }

        if (tieneParejaLocal) {
            if (jugadorLocal1Id == null || jugadorLocal2Id == null) {
                throw new ConvocatoriaInvalidaExcepcion("Ambos jugadores de la pareja local son obligatorios.");
            }
            if (jugadorLocal1Id.equals(jugadorLocal2Id)) {
                throw new ConvocatoriaInvalidaExcepcion("La pareja debe estar compuesta por dos jugadores distintos.");
            }
        }

        if (tieneParejaVisitante) {
            if (jugadorVisitante1Id == null || jugadorVisitante2Id == null) {
                throw new ConvocatoriaInvalidaExcepcion("Ambos jugadores de la pareja visitante son obligatorios.");
            }
            if (jugadorVisitante1Id.equals(jugadorVisitante2Id)) {
                throw new ConvocatoriaInvalidaExcepcion("La pareja visitante debe estar compuesta por dos jugadores distintos.");
            }
        }

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
    }

    public static PartidoPareja crear(
            UUID enfrentamientoId,
            int numeroPareja,
            UUID jugadorLocal1Id,
            UUID jugadorLocal2Id,
            UUID jugadorVisitante1Id,
            UUID jugadorVisitante2Id
    ) {
        return new PartidoPareja(
                UUID.randomUUID(),
                enfrentamientoId,
                numeroPareja,
                Objects.requireNonNull(jugadorLocal1Id, "El jugador local 1 es obligatorio."),
                Objects.requireNonNull(jugadorLocal2Id, "El jugador local 2 es obligatorio."),
                Objects.requireNonNull(jugadorVisitante1Id, "El jugador visitante 1 es obligatorio."),
                Objects.requireNonNull(jugadorVisitante2Id, "El jugador visitante 2 es obligatorio."),
                null, null, null, null, null, null,
                null,
                false
        );
    }

    /**
     * Crea una pareja convocada (números 1, 2 o 3) con los 2 jugadores seleccionados en la convocatoria.
     */
    public static PartidoPareja crearParejaConvocada(
            UUID enfrentamientoId,
            int numeroPareja,
            UUID jugador1Id,
            UUID jugador2Id
    ) {
        return new PartidoPareja(
                UUID.randomUUID(),
                enfrentamientoId,
                numeroPareja,
                Objects.requireNonNull(jugador1Id, "El jugador 1 de la pareja es obligatorio."),
                Objects.requireNonNull(jugador2Id, "El jugador 2 de la pareja es obligatorio."),
                null,
                null,
                null, null, null, null, null, null,
                null,
                false
        );
    }

    /**
     * Valida si el tanteo de un único set de pádel cumple con el reglamento oficial.
     */
    public static boolean esSetReglamentario(int juegosLocal, int juegosVisitante) {
        return ValidadorTanteoPadel.esSetReglamentario(juegosLocal, juegosVisitante);
    }

    /**
     * Comprueba si el resultado global en sets de este partido es reglamentario.
     */
    public boolean esResultadoReglamentario() {
        return ValidadorTanteoPadel.esPartidoReglamentario(
                set1Local, set1Visitante, set2Local, set2Visitante, set3Local, set3Visitante
        );
    }

    /**
     * Valida de manera estricta el resultado en sets del partido lanzando
     * {@link ResultadoPadelIlegalExcepcion} si existe cualquier infracción reglamentaria.
     */
    public void validarResultadoReglamentario() {
        ValidadorTanteoPadel.validarPartido(
                set1Local, set1Visitante, set2Local, set2Visitante, set3Local, set3Visitante
        );
    }

    /**
     * Determina la pareja ganadora en base al cómputo de sets reglamentarios.
     */
    public GanadorPareja determinarGanador() {
        return ValidadorTanteoPadel.determinarGanador(
                set1Local, set1Visitante, set2Local, set2Visitante, set3Local, set3Visitante
        );
    }

    public int calcularSetsGanadosLocal() {
        int contador = 0;
        if (set1Local != null && set1Visitante != null && set1Local > set1Visitante) contador++;
        if (set2Local != null && set2Visitante != null && set2Local > set2Visitante) contador++;
        if (set3Local != null && set3Visitante != null && set3Local > set3Visitante) contador++;
        return contador;
    }

    public int calcularSetsGanadosVisitante() {
        int contador = 0;
        if (set1Local != null && set1Visitante != null && set1Visitante > set1Local) contador++;
        if (set2Local != null && set2Visitante != null && set2Visitante > set2Local) contador++;
        if (set3Local != null && set3Visitante != null && set3Visitante > set3Local) contador++;
        return contador;
    }

    public int calcularJuegosGanadosLocal() {
        int total = 0;
        if (set1Local != null) total += set1Local;
        if (set2Local != null) total += set2Local;
        if (set3Local != null) total += set3Local;
        return total;
    }

    public int calcularJuegosGanadosVisitante() {
        int total = 0;
        if (set1Visitante != null) total += set1Visitante;
        if (set2Visitante != null) total += set2Visitante;
        if (set3Visitante != null) total += set3Visitante;
        return total;
    }

    /**
     * Registra el resultado del partido, valida el reglamento y retorna una nueva instancia completada.
     */
    public PartidoPareja registrarResultado(
            int set1Local,
            int set1Visitante,
            int set2Local,
            int set2Visitante,
            Integer set3Local,
            Integer set3Visitante
    ) {
        GanadorPareja ganador = ValidadorTanteoPadel.determinarGanador(
                set1Local, set1Visitante, set2Local, set2Visitante, set3Local, set3Visitante
        );

        return new PartidoPareja(
                this.id,
                this.enfrentamientoId,
                this.numeroPareja,
                this.jugadorLocal1Id,
                this.jugadorLocal2Id,
                this.jugadorVisitante1Id,
                this.jugadorVisitante2Id,
                set1Local,
                set1Visitante,
                set2Local,
                set2Visitante,
                set3Local,
                set3Visitante,
                ganador,
                true
        );
    }

    public boolean involucraJugador(UUID jugadorId) {
        if (jugadorId == null) return false;
        return Objects.equals(jugadorLocal1Id, jugadorId) ||
                Objects.equals(jugadorLocal2Id, jugadorId) ||
                Objects.equals(jugadorVisitante1Id, jugadorId) ||
                Objects.equals(jugadorVisitante2Id, jugadorId);
    }

    public boolean esJugadorLocal(UUID jugadorId) {
        if (jugadorId == null) return false;
        return Objects.equals(jugadorLocal1Id, jugadorId) || Objects.equals(jugadorLocal2Id, jugadorId);
    }

    public boolean esJugadorVisitante(UUID jugadorId) {
        if (jugadorId == null) return false;
        return Objects.equals(jugadorVisitante1Id, jugadorId) || Objects.equals(jugadorVisitante2Id, jugadorId);
    }

    public UUID obtenerId() { return id; }
    public UUID obtenerEnfrentamientoId() { return enfrentamientoId; }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartidoPareja that = (PartidoPareja) o;
        return numeroPareja == that.numeroPareja &&
                completado == that.completado &&
                Objects.equals(id, that.id) &&
                Objects.equals(enfrentamientoId, that.enfrentamientoId) &&
                Objects.equals(jugadorLocal1Id, that.jugadorLocal1Id) &&
                Objects.equals(jugadorLocal2Id, that.jugadorLocal2Id) &&
                Objects.equals(jugadorVisitante1Id, that.jugadorVisitante1Id) &&
                Objects.equals(jugadorVisitante2Id, that.jugadorVisitante2Id) &&
                Objects.equals(set1Local, that.set1Local) &&
                Objects.equals(set1Visitante, that.set1Visitante) &&
                Objects.equals(set2Local, that.set2Local) &&
                Objects.equals(set2Visitante, that.set2Visitante) &&
                Objects.equals(set3Local, that.set3Local) &&
                Objects.equals(set3Visitante, that.set3Visitante) &&
                parejaGanadora == that.parejaGanadora;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, enfrentamientoId, numeroPareja, jugadorLocal1Id, jugadorLocal2Id,
                jugadorVisitante1Id, jugadorVisitante2Id, set1Local, set1Visitante, set2Local,
                set2Visitante, set3Local, set3Visitante, parejaGanadora, completado);
    }

    @Override
    public String toString() {
        return "PartidoPareja{" +
                "id=" + id +
                ", enfrentamientoId=" + enfrentamientoId +
                ", numeroPareja=" + numeroPareja +
                ", set1=" + set1Local + "-" + set1Visitante +
                ", set2=" + set2Local + "-" + set2Visitante +
                ", set3=" + set3Local + "-" + set3Visitante +
                ", parejaGanadora=" + parejaGanadora +
                ", completado=" + completado +
                '}';
    }
}
