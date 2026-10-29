package com.synagon.api.dominio.puertos.salida;

import com.synagon.api.dominio.modelos.Usuario;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de usuarios y su vinculación a equipos.
 */
public interface RepositorioUsuarioPuerto {

    boolean existePorEmail(String email);

    Optional<UUID> obtenerEquipoPorDefectoId();

    Usuario guardarConMembresiaEquipo(Usuario usuario, UUID equipoId);
}
