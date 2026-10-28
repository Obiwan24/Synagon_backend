package com.synagon.api.dominio.puertos.entrada;

import com.synagon.api.dominio.modelos.Enfrentamiento;
import com.synagon.api.dominio.modelos.EstadisticaJugador;
import com.synagon.api.dominio.modelos.PosicionClasificacionEquipo;
import com.synagon.api.dominio.modelos.ResultadoPartido;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada para el registro de actas arbitrales, consulta de clasificación
 * en tiempo real por división y estadísticas detalladas de jugadores por equipo.
 */
public interface ActasYEstadisticasCasoUso {

    Enfrentamiento registrarActaEnfrentamiento(
            UUID enfrentamientoId,
            String urlFotoActa,
            List<ResultadoPartido> resultadosTresPartidos
    );

    List<PosicionClasificacionEquipo> obtenerClasificacionDivision(UUID divisionId);

    List<EstadisticaJugador> obtenerEstadisticasJugadoresEquipo(UUID equipoId);
}
