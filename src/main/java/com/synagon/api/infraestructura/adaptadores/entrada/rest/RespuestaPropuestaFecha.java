package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.PropuestaFecha;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para una propuesta de fecha de un enfrentamiento.
 */
public record RespuestaPropuestaFecha(
        UUID id,
        LocalDateTime fechaPropuesta,
        int orden,
        boolean esSeleccionada
) {
    public static RespuestaPropuestaFecha desdeDominio(PropuestaFecha dominio) {
        return new RespuestaPropuestaFecha(
                dominio.obtenerId(),
                dominio.obtenerFechaPropuesta(),
                dominio.obtenerOrden(),
                dominio.esSeleccionada()
        );
    }
}
