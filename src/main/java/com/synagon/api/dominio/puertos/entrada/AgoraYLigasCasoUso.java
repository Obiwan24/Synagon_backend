package com.synagon.api.dominio.puertos.entrada;

import com.synagon.api.dominio.modelos.Liga;
import com.synagon.api.dominio.modelos.ResumenMembresiaUsuario;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada para los casos de uso del Ágora Principal (Home Multi-Rol),
 * validación de códigos privados de liga e inscripción de equipos.
 */
public interface AgoraYLigasCasoUso {

    List<ResumenMembresiaUsuario> obtenerMembresiasUsuario(UUID usuarioId);

    Liga validarCodigoInvitacionLiga(String codigoInvitacion);

    ResumenMembresiaUsuario inscribirEquipoConCodigo(
            UUID usuarioId,
            String codigoInvitacion,
            String nombreEquipo,
            String clubSedeOpcional
    );
}
