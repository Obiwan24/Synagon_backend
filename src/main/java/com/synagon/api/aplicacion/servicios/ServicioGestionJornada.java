package com.synagon.api.aplicacion.servicios;

import com.synagon.api.dominio.excepciones.ConvocatoriaInvalidaExcepcion;
import com.synagon.api.dominio.excepciones.RecursoNoEncontradoExcepcion;
import com.synagon.api.dominio.excepciones.ReglaNegocioExcepcion;
import com.synagon.api.dominio.modelos.Enfrentamiento;
import com.synagon.api.dominio.modelos.EstadoDisponibilidad;
import com.synagon.api.dominio.modelos.PartidoPareja;
import com.synagon.api.dominio.puertos.entrada.GestionJornadaCasoUso;
import com.synagon.api.dominio.puertos.salida.RepositorioEnfrentamientoPuerto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicación transaccional que orquesta la negociación de fechas,
 * votación de disponibilidad, fijación de fecha definitiva y cierre de convocatoria/alineación.
 */
@Service
@Transactional
public class ServicioGestionJornada implements GestionJornadaCasoUso {

    private final RepositorioEnfrentamientoPuerto repositorioEnfrentamiento;

    public ServicioGestionJornada(RepositorioEnfrentamientoPuerto repositorioEnfrentamiento) {
        this.repositorioEnfrentamiento = repositorioEnfrentamiento;
    }

    @Override
    @Transactional(readOnly = true)
    public Enfrentamiento obtenerEnfrentamientoPorId(UUID enfrentamientoId) {
        if (enfrentamientoId == null) {
            throw new IllegalArgumentException("El identificador del enfrentamiento es obligatorio.");
        }
        return repositorioEnfrentamiento.obtenerPorId(enfrentamientoId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        String.format("No se encontró el enfrentamiento con id %s.", enfrentamientoId)
                ));
    }

    @Override
    public Enfrentamiento proponerFechas(UUID enfrentamientoId, List<LocalDateTime> listaDosFechas) {
        Enfrentamiento enfrentamiento = obtenerEnfrentamientoPorId(enfrentamientoId);
        Enfrentamiento actualizado = enfrentamiento.proponerFechas(listaDosFechas, LocalDateTime.now());
        return repositorioEnfrentamiento.guardar(actualizado);
    }

    @Override
    public Enfrentamiento votarDisponibilidad(
            UUID enfrentamientoId,
            UUID jugadorId,
            UUID propuestaFechaId,
            EstadoDisponibilidad tipoDisponibilidad
    ) {
        if (jugadorId == null) {
            throw new IllegalArgumentException("El identificador del jugador es obligatorio.");
        }
        if (propuestaFechaId == null) {
            throw new IllegalArgumentException("El identificador de la propuesta de fecha es obligatorio.");
        }
        if (tipoDisponibilidad == null) {
            throw new IllegalArgumentException("El tipo de disponibilidad es obligatorio.");
        }

        Enfrentamiento enfrentamiento = obtenerEnfrentamientoPorId(enfrentamientoId);

        if (!repositorioEnfrentamiento.existeUsuarioPorId(jugadorId)) {
            throw new RecursoNoEncontradoExcepcion(
                    String.format("No existe ningún jugador registrado con id %s.", jugadorId)
            );
        }

        if (!repositorioEnfrentamiento.perteneceJugadorAAlgunEquipo(
                jugadorId,
                enfrentamiento.obtenerEquipoLocalId(),
                enfrentamiento.obtenerEquipoVisitanteId()
        )) {
            throw new ReglaNegocioExcepcion(
                    String.format("El jugador %s no pertenece activamente a ninguno de los equipos del enfrentamiento.", jugadorId)
            );
        }

        Enfrentamiento actualizado = enfrentamiento.registrarVotoDisponibilidad(
                jugadorId,
                propuestaFechaId,
                tipoDisponibilidad
        );

        return repositorioEnfrentamiento.guardar(actualizado);
    }

    @Override
    public Enfrentamiento fijarFechaDefinitiva(UUID enfrentamientoId, UUID propuestaFechaElegidaId) {
        if (propuestaFechaElegidaId == null) {
            throw new IllegalArgumentException("El identificador de la propuesta de fecha elegida es obligatorio.");
        }
        Enfrentamiento enfrentamiento = obtenerEnfrentamientoPorId(enfrentamientoId);
        Enfrentamiento actualizado = enfrentamiento.seleccionarFechaDefinitiva(propuestaFechaElegidaId);
        return repositorioEnfrentamiento.guardar(actualizado);
    }

    @Override
    public Enfrentamiento cerrarConvocatoriaYAlineacion(UUID enfrentamientoId, List<PartidoPareja> listaTresParejas) {
        Enfrentamiento enfrentamiento = obtenerEnfrentamientoPorId(enfrentamientoId);

        if (listaTresParejas == null || listaTresParejas.size() != 3) {
            throw new ConvocatoriaInvalidaExcepcion("Se deben enviar exactamente 3 parejas para cerrar la convocatoria.");
        }

        Enfrentamiento actualizado = enfrentamiento.cerrarConvocatoriaYAlineacion(listaTresParejas);
        return repositorioEnfrentamiento.guardar(actualizado);
    }
}
