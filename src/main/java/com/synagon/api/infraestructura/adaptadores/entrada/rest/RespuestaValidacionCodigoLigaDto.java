package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.Liga;
import com.synagon.api.dominio.modelos.ModoSedeLiga;

import java.util.UUID;

/**
 * DTO de respuesta tras validar un código privado de invitación de liga desde el cliente móvil KMP.
 */
public record RespuestaValidacionCodigoLigaDto(
        UUID ligaId,
        String nombreLiga,
        String descripcion,
        String codigoInvitacion,
        ModoSedeLiga modoSede,
        String sedeUnicaLiga,
        boolean requiereSedeEquipo,
        int partidosPorJornada,
        int puntosVictoria,
        int puntosEmpate,
        int puntosDerrota,
        boolean exigirDniJugador,
        boolean exigirTelefonoJugador,
        String normativaTexto
) {
    public static RespuestaValidacionCodigoLigaDto desdeDominio(Liga liga) {
        return new RespuestaValidacionCodigoLigaDto(
                liga.obtenerId(),
                liga.obtenerNombre(),
                liga.obtenerDescripcion(),
                liga.obtenerCodigoInvitacion(),
                liga.obtenerModoSede(),
                liga.obtenerSedeUnicaLiga(),
                liga.requiereSedeEquipo(),
                liga.obtenerPartidosPorJornada(),
                liga.obtenerPuntosVictoria(),
                liga.obtenerPuntosEmpate(),
                liga.obtenerPuntosDerrota(),
                liga.exigeDniJugador(),
                liga.exigeTelefonoJugador(),
                liga.obtenerNormativaTexto()
        );
    }
}
