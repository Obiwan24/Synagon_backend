package com.synagon.api.dominio.puertos.salida;

import com.synagon.api.dominio.modelos.Liga;
import com.synagon.api.dominio.modelos.ResumenMembresiaUsuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para consultas del Ágora Principal, parametrización de ligas e inscripción de equipos.
 */
public interface RepositorioAgoraYLigasPuerto {

    boolean existeUsuarioPorId(UUID usuarioId);

    List<ResumenMembresiaUsuario> obtenerMembresiasActivasDeUsuario(UUID usuarioId);

    Optional<Liga> buscarLigaPorCodigoInvitacion(String codigoInvitacion);

    Optional<Liga> obtenerLigaPorEquipoId(UUID equipoId);

    boolean perteneceUsuarioALiga(UUID usuarioId, UUID ligaId);

    UUID obtenerOCrearDivisionInscripcionLiga(UUID ligaId, String nombreLiga);

    boolean existeEquipoConNombreEnDivision(UUID divisionId, String nombreEquipo);

    UUID crearEquipoConCapitan(
            UUID divisionId,
            String nombreEquipo,
            String clubSede,
            UUID capitanUsuarioId
    );
}
