package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.Enfrentamiento;
import com.synagon.api.dominio.modelos.EstadoEnfrentamiento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta completa de un enfrentamiento de jornada con sus propuestas,
 * sede oficial y sede efectiva de juego, votaciones de disponibilidad, parejas alineadas y acta arbitral.
 */
public record RespuestaEnfrentamiento(
        UUID id,
        UUID divisionId,
        UUID equipoLocalId,
        UUID equipoVisitanteId,
        int numeroJornada,
        EstadoEnfrentamiento estado,
        LocalDateTime fechaDefinitiva,
        String pistas,
        String sedeAlternativaJornada,
        String sedeEfectivaJuego,
        String urlFotoActa,
        int puntosLocal,
        int puntosVisitante,
        boolean actaConfirmada,
        List<RespuestaPropuestaFecha> propuestasFecha,
        List<RespuestaDisponibilidadJugador> disponibilidades,
        List<RespuestaPartidoPareja> partidos
) {
    public static RespuestaEnfrentamiento desdeDominio(Enfrentamiento dominio) {
        return new RespuestaEnfrentamiento(
                dominio.obtenerId(),
                dominio.obtenerDivisionId(),
                dominio.obtenerEquipoLocalId(),
                dominio.obtenerEquipoVisitanteId(),
                dominio.obtenerNumeroJornada(),
                dominio.obtenerEstado(),
                dominio.obtenerFechaDefinitiva(),
                dominio.obtenerPistas(),
                dominio.obtenerSedeAlternativaJornada(),
                dominio.obtenerSedeEfectivaJuego(),
                dominio.obtenerUrlFotoActa(),
                dominio.obtenerPuntosLocal(),
                dominio.obtenerPuntosVisitante(),
                dominio.esActaConfirmada(),
                dominio.obtenerPropuestasFecha().stream()
                        .map(RespuestaPropuestaFecha::desdeDominio)
                        .toList(),
                dominio.obtenerDisponibilidades().stream()
                        .map(RespuestaDisponibilidadJugador::desdeDominio)
                        .toList(),
                dominio.obtenerPartidos().stream()
                        .map(RespuestaPartidoPareja::desdeDominio)
                        .toList()
        );
    }
}
