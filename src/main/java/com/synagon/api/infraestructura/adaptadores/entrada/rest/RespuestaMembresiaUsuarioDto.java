package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.ResumenMembresiaUsuario;
import com.synagon.api.dominio.modelos.RolMembresia;

import java.util.UUID;

/**
 * DTO de respuesta con el resumen de una membresía activa del usuario para el Ágora Principal.
 */
public record RespuestaMembresiaUsuarioDto(
        UUID ligaId,
        String nombreLiga,
        UUID equipoId,
        String nombreEquipoOSubtitulo,
        String textoSedeDetalle,
        int partidosPorJornada,
        RolMembresia rol
) {
    public static RespuestaMembresiaUsuarioDto desdeDominio(ResumenMembresiaUsuario dominio) {
        return new RespuestaMembresiaUsuarioDto(
                dominio.ligaId(),
                dominio.nombreLiga(),
                dominio.equipoId(),
                dominio.nombreEquipoOSubtitulo(),
                dominio.textoSedeDetalle(),
                dominio.partidosPorJornada(),
                dominio.rol()
        );
    }
}
