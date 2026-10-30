package com.synagon.api.dominio.modelos;

import com.synagon.api.dominio.excepciones.ReglaNegocioExcepcion;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio inmutable que representa una liga parametrizable en Synagon,
 * incluyendo su código de invitación privado, modo de sede, puntuación y requisitos de jugadores.
 */
public final class Liga {

    private final UUID id;
    private final String nombre;
    private final String descripcion;
    private final String codigoInvitacion;
    private final UUID presidenteId;
    private final int partidosPorJornada;
    private final ModoSedeLiga modoSede;
    private final String sedeUnicaLiga;
    private final int puntosVictoria;
    private final int puntosEmpate;
    private final int puntosDerrota;
    private final boolean exigirDniJugador;
    private final boolean exigirTelefonoJugador;
    private final String normativaTexto;

    public Liga(
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
            String normativaTexto
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.nombre = Objects.requireNonNull(nombre, "El nombre de la liga es obligatorio.").trim();
        if (this.nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la liga no puede estar vacío.");
        }
        this.descripcion = descripcion != null ? descripcion.trim() : null;
        this.codigoInvitacion = normalizarCodigoInvitacion(codigoInvitacion);
        this.presidenteId = presidenteId;
        if (partidosPorJornada < 1) {
            throw new IllegalArgumentException("El número de partidos por jornada debe ser al menos 1.");
        }
        this.partidosPorJornada = partidosPorJornada;
        this.modoSede = modoSede != null ? modoSede : ModoSedeLiga.SEDE_LOCAL_POR_EQUIPO;
        this.sedeUnicaLiga = sedeUnicaLiga != null && !sedeUnicaLiga.isBlank() ? sedeUnicaLiga.trim() : null;
        this.puntosVictoria = puntosVictoria;
        this.puntosEmpate = puntosEmpate;
        this.puntosDerrota = puntosDerrota;
        this.exigirDniJugador = exigirDniJugador;
        this.exigirTelefonoJugador = exigirTelefonoJugador;
        this.normativaTexto = normativaTexto != null && !normativaTexto.isBlank() ? normativaTexto.trim() : null;
    }

    public static String normalizarCodigoInvitacion(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de invitación de la liga es obligatorio.");
        }
        String limpio = codigo.trim().toUpperCase(Locale.ROOT);
        if (limpio.length() > 30) {
            throw new IllegalArgumentException("El código de invitación no puede superar los 30 caracteres.");
        }
        return limpio;
    }

    public boolean requiereSedeEquipo() {
        return this.modoSede == ModoSedeLiga.SEDE_LOCAL_POR_EQUIPO;
    }

    /**
     * Valida y resuelve la sede oficial del equipo en el momento de su inscripción según el modo de sede de la liga.
     */
    public String validarYResolverSedeInscripcionEquipo(String clubSedeOpcional) {
        String sedeLimpia = clubSedeOpcional != null && !clubSedeOpcional.isBlank()
                ? clubSedeOpcional.trim()
                : null;

        if (sedeLimpia != null && sedeLimpia.length() > 150) {
            throw new IllegalArgumentException("El club o sede oficial del equipo no puede superar los 150 caracteres.");
        }

        if (this.modoSede == ModoSedeLiga.SEDE_LOCAL_POR_EQUIPO) {
            if (sedeLimpia == null) {
                throw new ReglaNegocioExcepcion(
                        "Esta liga exige indicar el club o sede oficial del equipo porque funciona en modo SEDE_LOCAL_POR_EQUIPO."
                );
            }
            return sedeLimpia;
        }

        return sedeLimpia != null ? sedeLimpia : this.sedeUnicaLiga;
    }

    public String obtenerTextoSedeDetalleParaEquipo(String clubEquipo) {
        if (this.modoSede == ModoSedeLiga.SEDE_UNICA_CLUB) {
            return this.sedeUnicaLiga != null
                    ? "Sede única: " + this.sedeUnicaLiga
                    : "Sede única de liga";
        }
        if (clubEquipo != null && !clubEquipo.isBlank()) {
            return "Sede local: " + clubEquipo.trim();
        }
        return "Sede local por equipo";
    }

    public String obtenerTextoSedeDetallePresidencia() {
        if (this.modoSede == ModoSedeLiga.SEDE_UNICA_CLUB) {
            return this.sedeUnicaLiga != null
                    ? "Sede única: " + this.sedeUnicaLiga
                    : "Sede única de club";
        }
        return "Sede local por equipo";
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
}
