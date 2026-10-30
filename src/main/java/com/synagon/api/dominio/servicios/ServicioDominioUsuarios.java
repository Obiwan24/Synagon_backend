package com.synagon.api.dominio.servicios;

import com.synagon.api.dominio.modelos.ResultadoAltaJugadorEquipo;
import com.synagon.api.dominio.modelos.Usuario;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Servicio puro de dominio para las reglas de alta de jugadores por parte del capitán.
 * Sin dependencias de frameworks de infraestructura.
 */
public final class ServicioDominioUsuarios {

    /**
     * Resuelve el alta de un jugador registrado por su capitán mediante email y DNI:
     * <ul>
     *     <li>Si el {@code email} no existe en {@code usuarios}, crea el usuario con el email en minúsculas,
     *     el DNI normalizado en mayúsculas (cifrado como contraseña por defecto),
     *     {@code requiereCambioPassword = true} y {@code primerAccesoCompletado = false}.</li>
     *     <li>Si el {@code email} ya existe en {@code usuarios}, conserva intacto al usuario y su contraseña
     *     actual para vincularlo únicamente en {@code miembros_equipo}.</li>
     * </ul>
     */
    public ResultadoAltaJugadorEquipo prepararAltaJugadorPorCapitan(
            Optional<Usuario> usuarioExistentePorEmail,
            UUID equipoId,
            String email,
            String dni,
            String nombreCompletoOpcional,
            String telefonoOpcional,
            Function<String, String> cifradorContrasena
    ) {
        Objects.requireNonNull(usuarioExistentePorEmail, "La consulta de usuario existente no puede ser nula.");
        Objects.requireNonNull(equipoId, "El identificador del equipo es obligatorio.");
        Objects.requireNonNull(cifradorContrasena, "La función de cifrado de contraseña es obligatoria.");

        String emailNormalizado = Usuario.normalizarYValidarEmail(email);
        String dniNormalizado = Usuario.normalizarYValidarDniObligatorio(dni);

        if (usuarioExistentePorEmail.isPresent()) {
            Usuario existente = usuarioExistentePorEmail.get().conEquipo(equipoId);
            return new ResultadoAltaJugadorEquipo(existente, equipoId, false);
        }

        String hashDniPorDefecto = cifradorContrasena.apply(dniNormalizado);
        Usuario nuevoUsuario = Usuario.crearAltaPorCapitan(
                nombreCompletoOpcional,
                emailNormalizado,
                dniNormalizado,
                telefonoOpcional,
                hashDniPorDefecto,
                equipoId
        );

        return new ResultadoAltaJugadorEquipo(nuevoUsuario, equipoId, true);
    }
}
