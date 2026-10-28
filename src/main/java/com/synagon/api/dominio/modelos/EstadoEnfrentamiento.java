package com.synagon.api.dominio.modelos;

/**
 * Estados posibles del ciclo de vida de un enfrentamiento en Synagon.
 */
public enum EstadoEnfrentamiento {
    PENDIENTE_FECHA,
    VOTACION_DISPONIBILIDAD_ABIERTA,
    FECHA_FIJADA,
    CONVOCATORIA_ABIERTA,
    ALINEACION_CERRADA,
    EN_DISPUTA,
    ACTA_SUBIDA,
    FINALIZADO_CONFIRMADO,
    FINALIZADO,
    CANCELADO;

    public boolean tieneActaValidaParaEstadisticas() {
        return this == ACTA_SUBIDA || this == FINALIZADO_CONFIRMADO || this == FINALIZADO;
    }
}
