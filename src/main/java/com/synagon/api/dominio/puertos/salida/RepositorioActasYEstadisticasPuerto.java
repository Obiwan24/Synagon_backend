package com.synagon.api.dominio.puertos.salida;

import com.synagon.api.dominio.modelos.DatosEquipoDivision;
import com.synagon.api.dominio.modelos.DatosMiembroEquipo;
import com.synagon.api.dominio.modelos.Enfrentamiento;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Puerto de salida para las consultas de clasificación por división y estadísticas de equipo.
 */
public interface RepositorioActasYEstadisticasPuerto {

    boolean existeDivisionPorId(UUID divisionId);

    List<DatosEquipoDivision> obtenerEquiposPorDivisionId(UUID divisionId);

    List<Enfrentamiento> obtenerEnfrentamientosConActaPorDivisionId(UUID divisionId);

    boolean existeEquipoPorId(UUID equipoId);

    List<DatosMiembroEquipo> obtenerMiembrosPorEquipoId(UUID equipoId);

    List<Enfrentamiento> obtenerEnfrentamientosConActaPorEquipoId(UUID equipoId);

    Map<UUID, Integer> obtenerPartidosApuntadosPorMiembrosDeEquipo(UUID equipoId);
}
