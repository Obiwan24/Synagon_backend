package com.synagon.api.aplicacion.servicios;

import com.synagon.api.dominio.excepciones.EmailYaRegistradoExcepcion;
import com.synagon.api.dominio.excepciones.RecursoNoEncontradoExcepcion;
import com.synagon.api.dominio.excepciones.ReglaNegocioExcepcion;
import com.synagon.api.dominio.modelos.DatosRegistroUsuario;
import com.synagon.api.dominio.modelos.ResultadoAltaJugadorEquipo;
import com.synagon.api.dominio.modelos.Usuario;
import com.synagon.api.dominio.puertos.entrada.RegistroUsuarioCasoUso;
import com.synagon.api.dominio.puertos.salida.RepositorioUsuarioPuerto;
import com.synagon.api.dominio.servicios.ServicioDominioUsuarios;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Servicio transaccional encargado del registro público de usuarios, el alta de jugadores
 * por parte de capitanes con email/DNI, el control de primer acceso y la actualización de perfil.
 */
@Service
@Transactional
public class ServicioUsuarios implements RegistroUsuarioCasoUso {

    private final RepositorioUsuarioPuerto repositorioUsuario;
    private final PasswordEncoder codificadorContrasena;
    private final ServicioDominioUsuarios servicioDominioUsuarios;

    public ServicioUsuarios(
            RepositorioUsuarioPuerto repositorioUsuario,
            PasswordEncoder codificadorContrasena
    ) {
        this.repositorioUsuario = repositorioUsuario;
        this.codificadorContrasena = codificadorContrasena;
        this.servicioDominioUsuarios = new ServicioDominioUsuarios();
    }

    @Override
    public Usuario registrarUsuario(DatosRegistroUsuario datosRegistro) {
        if (datosRegistro == null) {
            throw new IllegalArgumentException("Los datos de registro del usuario son obligatorios.");
        }

        if (!Boolean.TRUE.equals(datosRegistro.aceptoPrivacidad())) {
            throw new IllegalArgumentException(
                    "Es obligatorio aceptar la política de privacidad en cumplimiento del RGPD."
            );
        }

        if (datosRegistro.contrasenaEnClaro() == null || datosRegistro.contrasenaEnClaro().length() < 6) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria y debe tener al menos 6 caracteres."
            );
        }

        String emailNormalizado = Usuario.normalizarYValidarEmail(datosRegistro.email());

        if (repositorioUsuario.existePorEmail(emailNormalizado)) {
            throw new EmailYaRegistradoExcepcion(emailNormalizado);
        }

        UUID equipoPorDefectoId = repositorioUsuario.obtenerEquipoPorDefectoId()
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        "No se encontró ningún equipo por defecto disponible para asignar al nuevo jugador."
                ));

        String contrasenaCifrada = codificadorContrasena.encode(datosRegistro.contrasenaEnClaro());

        Usuario nuevoJugador = Usuario.registrarNuevoJugador(
                datosRegistro.nombreCompleto(),
                emailNormalizado,
                datosRegistro.telefono(),
                contrasenaCifrada,
                datosRegistro.aceptoPrivacidad(),
                equipoPorDefectoId
        );

        return repositorioUsuario.guardarConMembresiaEquipo(nuevoJugador, equipoPorDefectoId);
    }

    @Override
    public ResultadoAltaJugadorEquipo registrarJugadorPorCapitan(
            UUID equipoId,
            UUID capitanId,
            String email,
            String dni,
            String nombreCompletoOpcional,
            String telefonoOpcional
    ) {
        if (equipoId == null) {
            throw new IllegalArgumentException("El identificador del equipo es obligatorio.");
        }
        if (capitanId == null) {
            throw new IllegalArgumentException("El identificador del capitán es obligatorio.");
        }
        if (!repositorioUsuario.existeEquipoPorId(equipoId)) {
            throw new RecursoNoEncontradoExcepcion(
                    String.format("No se encontró el equipo con id %s.", equipoId)
            );
        }
        if (!repositorioUsuario.esCapitanActivoDeEquipo(capitanId, equipoId)) {
            throw new ReglaNegocioExcepcion(
                    "Solo el capitán activo del equipo puede dar de alta a nuevos jugadores en su plantilla."
            );
        }

        String emailNormalizado = Usuario.normalizarYValidarEmail(email);
        String dniNormalizado = Usuario.normalizarYValidarDniObligatorio(dni);

        Optional<Usuario> usuarioExistente = repositorioUsuario.buscarPorEmail(emailNormalizado);

        if (usuarioExistente.isPresent()) {
            if (repositorioUsuario.esMiembroActivoDeEquipo(usuarioExistente.get().obtenerId(), equipoId)) {
                throw new ReglaNegocioExcepcion(
                        "El jugador indicado ya forma parte activa de la plantilla de este equipo."
                );
            }
        } else if (repositorioUsuario.existePorDniExcluyendoUsuario(dniNormalizado, null)) {
            throw new ReglaNegocioExcepcion(
                    String.format("El DNI '%s' ya está registrado por otro usuario en el sistema.", dniNormalizado)
            );
        }

        ResultadoAltaJugadorEquipo resultadoDominio = servicioDominioUsuarios.prepararAltaJugadorPorCapitan(
                usuarioExistente,
                equipoId,
                emailNormalizado,
                dniNormalizado,
                nombreCompletoOpcional,
                telefonoOpcional,
                codificadorContrasena::encode
        );

        if (resultadoDominio.usuarioNuevoCreado()) {
            Usuario guardado = repositorioUsuario.guardarConMembresiaEquipo(resultadoDominio.usuario(), equipoId);
            return new ResultadoAltaJugadorEquipo(guardado, equipoId, true);
        }

        repositorioUsuario.vincularJugadorAEquipo(resultadoDominio.usuario().obtenerId(), equipoId);
        return resultadoDominio;
    }

    @Override
    public Usuario gestionarPrimerAccesoPassword(
            UUID usuarioId,
            boolean deseaCambiar,
            String nuevaPasswordOpcional
    ) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El identificador del usuario es obligatorio.");
        }

        Usuario usuarioActual = repositorioUsuario.obtenerPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        String.format("No se encontró el usuario con id %s.", usuarioId)
                ));

        if (!deseaCambiar) {
            Usuario actualizado = usuarioActual.gestionarPrimerAccesoPassword(false, null);
            return repositorioUsuario.guardar(actualizado);
        }

        if (nuevaPasswordOpcional == null || nuevaPasswordOpcional.length() < 6) {
            throw new IllegalArgumentException(
                    "La nueva contraseña es obligatoria y debe tener al menos 6 caracteres."
            );
        }

        String nuevoHashBcrypt = codificadorContrasena.encode(nuevaPasswordOpcional);
        Usuario actualizado = usuarioActual.gestionarPrimerAccesoPassword(true, nuevoHashBcrypt);
        return repositorioUsuario.guardar(actualizado);
    }

    @Override
    public Usuario actualizarPerfilUsuario(
            UUID usuarioId,
            String nombreCompleto,
            String telefono,
            String dni
    ) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El identificador del usuario es obligatorio.");
        }

        Usuario usuarioActual = repositorioUsuario.obtenerPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        String.format("No se encontró el usuario con id %s.", usuarioId)
                ));

        String dniNormalizado = Usuario.normalizarDni(dni);
        if (dniNormalizado != null && repositorioUsuario.existePorDniExcluyendoUsuario(dniNormalizado, usuarioId)) {
            throw new ReglaNegocioExcepcion(
                    String.format("El DNI '%s' ya está asignado a otro usuario.", dniNormalizado)
            );
        }

        Usuario actualizado = usuarioActual.actualizarPerfil(nombreCompleto, telefono, dniNormalizado);
        return repositorioUsuario.guardar(actualizado);
    }
}
