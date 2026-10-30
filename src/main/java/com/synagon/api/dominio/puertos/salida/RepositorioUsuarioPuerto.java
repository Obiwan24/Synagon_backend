package com.synagon.api.dominio.puertos.salida;

import com.synagon.api.dominio.modelos.Usuario;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de usuarios y su vinculación a equipos.
 */
public interface RepositorioUsuarioPuerto {

    boolean existePorEmail(String email);

    Optional<Usuario> obtenerPorId(UUID usuarioId);

    Optional<Usuario> buscarPorEmail(String email);

    boolean existePorDniExcluyendoUsuario(String dni, UUID usuarioExcluidoId);

    Optional<UUID> obtenerEquipoPorDefectoId();

    Usuario guardar(Usuario usuario);

    Usuario guardarConMembresiaEquipo(Usuario usuario, UUID equipoId);

    boolean existeEquipoPorId(UUID equipoId);

    boolean esCapitanActivoDeEquipo(UUID usuarioId, UUID equipoId);

    boolean esMiembroActivoDeEquipo(UUID usuarioId, UUID equipoId);

    void vincularJugadorAEquipo(UUID usuarioId, UUID equipoId);
}
