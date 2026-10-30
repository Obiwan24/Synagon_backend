package com.synagon.api.dominio.puertos.entrada;

import com.synagon.api.dominio.modelos.Enfrentamiento;
import com.synagon.api.dominio.modelos.EstadoDisponibilidad;
import com.synagon.api.dominio.modelos.PartidoPareja;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (casos de uso) para la gestión integral de fechas,
 * votaciones de disponibilidad, convocatorias, sede alternativa y alineación de una jornada.
 */
public interface GestionJornadaCasoUso {

    Enfrentamiento obtenerEnfrentamientoPorId(UUID enfrentamientoId);

    Enfrentamiento proponerFechas(UUID enfrentamientoId, List<LocalDateTime> listaDosFechas);

    Enfrentamiento votarDisponibilidad(
            UUID enfrentamientoId,
            UUID jugadorId,
            UUID propuestaFechaId,
            EstadoDisponibilidad tipoDisponibilidad
    );

    Enfrentamiento fijarFechaDefinitiva(UUID enfrentamientoId, UUID propuestaFechaElegidaId);

    Enfrentamiento cerrarConvocatoriaYAlineacion(UUID enfrentamientoId, List<PartidoPareja> listaTresParejas);

    Enfrentamiento actualizarSedeJornada(
            UUID enfrentamientoId,
            UUID capitanId,
            String nuevaSedeAlternativa
    );
}
