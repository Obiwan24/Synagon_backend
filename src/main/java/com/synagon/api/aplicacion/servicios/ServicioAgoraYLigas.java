package com.synagon.api.aplicacion.servicios;

import com.synagon.api.dominio.excepciones.RecursoNoEncontradoExcepcion;
import com.synagon.api.dominio.excepciones.ReglaNegocioExcepcion;
import com.synagon.api.dominio.modelos.Liga;
import com.synagon.api.dominio.modelos.ResumenMembresiaUsuario;
import com.synagon.api.dominio.modelos.RolMembresia;
import com.synagon.api.dominio.puertos.entrada.AgoraYLigasCasoUso;
import com.synagon.api.dominio.puertos.salida.RepositorioAgoraYLigasPuerto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio transaccional de aplicación encargado del Ágora Principal (Home Multi-Rol),
 * la consulta de configuración de ligas mediante código privado y la inscripción de equipos.
 */
@Service
@Transactional
public class ServicioAgoraYLigas implements AgoraYLigasCasoUso {

    private final RepositorioAgoraYLigasPuerto repositorioAgoraYLigas;

    public ServicioAgoraYLigas(RepositorioAgoraYLigasPuerto repositorioAgoraYLigas) {
        this.repositorioAgoraYLigas = repositorioAgoraYLigas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumenMembresiaUsuario> obtenerMembresiasUsuario(UUID usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El identificador del usuario es obligatorio.");
        }
        if (!repositorioAgoraYLigas.existeUsuarioPorId(usuarioId)) {
            throw new RecursoNoEncontradoExcepcion(
                    String.format("No se encontró el usuario con id %s.", usuarioId)
            );
        }

        return repositorioAgoraYLigas.obtenerMembresiasActivasDeUsuario(usuarioId).stream()
                .sorted(ResumenMembresiaUsuario.comparadorOrdenAgora())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Liga validarCodigoInvitacionLiga(String codigoInvitacion) {
        String codigoNormalizado = Liga.normalizarCodigoInvitacion(codigoInvitacion);
        return repositorioAgoraYLigas.buscarLigaPorCodigoInvitacion(codigoNormalizado)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        String.format("No existe ninguna liga activa con el código de invitación '%s'.", codigoNormalizado)
                ));
    }

    @Override
    public ResumenMembresiaUsuario inscribirEquipoConCodigo(
            UUID usuarioId,
            String codigoInvitacion,
            String nombreEquipo,
            String clubSedeOpcional
    ) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El identificador del usuario creador es obligatorio.");
        }
        if (nombreEquipo == null || nombreEquipo.trim().replaceAll("\\s+", " ").length() < 3) {
            throw new IllegalArgumentException("El nombre del equipo es obligatorio y debe tener al menos 3 caracteres.");
        }
        String nombreEquipoLimpio = nombreEquipo.trim().replaceAll("\\s+", " ");
        if (nombreEquipoLimpio.length() > 150) {
            throw new IllegalArgumentException("El nombre del equipo no puede superar los 150 caracteres.");
        }

        if (!repositorioAgoraYLigas.existeUsuarioPorId(usuarioId)) {
            throw new RecursoNoEncontradoExcepcion(
                    String.format("No se encontró el usuario con id %s.", usuarioId)
            );
        }

        Liga liga = validarCodigoInvitacionLiga(codigoInvitacion);
        String sedeEfectivaEquipo = liga.validarYResolverSedeInscripcionEquipo(clubSedeOpcional);

        if (repositorioAgoraYLigas.perteneceUsuarioALiga(usuarioId, liga.obtenerId())) {
            throw new ReglaNegocioExcepcion(
                    "El usuario ya forma parte de un equipo activo inscrito en esta liga."
            );
        }

        UUID divisionId = repositorioAgoraYLigas.obtenerOCrearDivisionInscripcionLiga(
                liga.obtenerId(),
                liga.obtenerNombre()
        );

        if (repositorioAgoraYLigas.existeEquipoConNombreEnDivision(divisionId, nombreEquipoLimpio)) {
            throw new ReglaNegocioExcepcion(
                    String.format("Ya existe un equipo inscrito con el nombre '%s' en esta división.", nombreEquipoLimpio)
            );
        }

        UUID equipoId = repositorioAgoraYLigas.crearEquipoConCapitan(
                divisionId,
                nombreEquipoLimpio,
                sedeEfectivaEquipo,
                usuarioId
        );

        return new ResumenMembresiaUsuario(
                liga.obtenerId(),
                liga.obtenerNombre(),
                equipoId,
                nombreEquipoLimpio,
                liga.obtenerTextoSedeDetalleParaEquipo(sedeEfectivaEquipo),
                liga.obtenerPartidosPorJornada(),
                RolMembresia.CAPITAN
        );
    }
}
