package com.synagon.api.dominio.puertos.salida;

import com.synagon.api.dominio.modelos.Enfrentamiento;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de enfrentamientos y consultas asociadas.
 */
public interface RepositorioEnfrentamientoPuerto {

    Optional<Enfrentamiento> obtenerPorId(UUID enfrentamientoId);

    Enfrentamiento guardar(Enfrentamiento enfrentamiento);

    boolean existeUsuarioPorId(UUID usuarioId);

    boolean perteneceJugadorAAlgunEquipo(UUID jugadorId, UUID equipoLocalId, UUID equipoVisitanteId);
}
