package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.GanadorPareja;
import com.synagon.api.dominio.modelos.PartidoPareja;

import java.util.UUID;

/**
 * DTO de respuesta para un partido por parejas del enfrentamiento.
 */
public record RespuestaPartidoPareja(
        UUID id,
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
    public static RespuestaPartidoPareja desdeDominio(PartidoPareja dominio) {
        return new RespuestaPartidoPareja(
                dominio.obtenerId(),
                dominio.obtenerNumeroPareja(),
                dominio.obtenerJugadorLocal1Id(),
                dominio.obtenerJugadorLocal2Id(),
                dominio.obtenerJugadorVisitante1Id(),
                dominio.obtenerJugadorVisitante2Id(),
                dominio.obtenerSet1Local(),
                dominio.obtenerSet1Visitante(),
                dominio.obtenerSet2Local(),
                dominio.obtenerSet2Visitante(),
                dominio.obtenerSet3Local(),
                dominio.obtenerSet3Visitante(),
                dominio.obtenerParejaGanadora(),
                dominio.estaCompletado()
        );
    }
}
