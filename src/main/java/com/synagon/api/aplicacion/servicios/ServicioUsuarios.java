package com.synagon.api.aplicacion.servicios;

import com.synagon.api.dominio.excepciones.EmailYaRegistradoExcepcion;
import com.synagon.api.dominio.excepciones.RecursoNoEncontradoExcepcion;
import com.synagon.api.dominio.modelos.DatosRegistroUsuario;
import com.synagon.api.dominio.modelos.Usuario;
import com.synagon.api.dominio.puertos.entrada.RegistroUsuarioCasoUso;
import com.synagon.api.dominio.puertos.salida.RepositorioUsuarioPuerto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio transaccional encargado del registro y alta de nuevos jugadores en Synagon.
 * Verifica cumplimiento RGPD, unicidad de correo electrónico, cifrado BCrypt de contraseña
 * y vinculación automática al equipo por defecto de pruebas con rol {@code JUGADOR}.
 */
@Service
@Transactional
public class ServicioUsuarios implements RegistroUsuarioCasoUso {

    private final RepositorioUsuarioPuerto repositorioUsuario;
    private final PasswordEncoder codificadorContrasena;

    public ServicioUsuarios(
            RepositorioUsuarioPuerto repositorioUsuario,
            PasswordEncoder codificadorContrasena
    ) {
        this.repositorioUsuario = repositorioUsuario;
        this.codificadorContrasena = codificadorContrasena;
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
}
