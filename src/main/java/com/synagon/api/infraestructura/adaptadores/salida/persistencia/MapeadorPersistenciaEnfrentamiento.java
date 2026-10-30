package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import com.synagon.api.dominio.modelos.DisponibilidadJugador;
import com.synagon.api.dominio.modelos.Enfrentamiento;
import com.synagon.api.dominio.modelos.PartidoPareja;
import com.synagon.api.dominio.modelos.PropuestaFecha;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

/**
 * Mapeador de persistencia encargado de convertir entre las entidades JPA y los modelos puros de dominio.
 */
@Component
public class MapeadorPersistenciaEnfrentamiento {

    public Enfrentamiento aDominio(EntidadEnfrentamiento entidad) {
        if (entidad == null) {
            return null;
        }

        List<PropuestaFecha> propuestas = entidad.obtenerPropuestasFecha().stream()
                .sorted(Comparator.comparingInt(EntidadPropuestaFecha::obtenerOrden))
                .map(this::aDominioPropuestaFecha)
                .toList();

        List<DisponibilidadJugador> disponibilidades = entidad.obtenerDisponibilidades().stream()
                .sorted(Comparator.comparing(EntidadDisponibilidadJugador::obtenerCreadoEn))
                .map(this::aDominioDisponibilidad)
                .toList();

        List<PartidoPareja> partidos = entidad.obtenerPartidos().stream()
                .sorted(Comparator.comparingInt(EntidadPartidoEnfrentamiento::obtenerNumeroPareja))
                .map(this::aDominioPartido)
                .toList();

        return new Enfrentamiento(
                entidad.obtenerId(),
                entidad.obtenerDivisionId(),
                entidad.obtenerEquipoLocalId(),
                entidad.obtenerEquipoVisitanteId(),
                entidad.obtenerNumeroJornada(),
                entidad.obtenerEstado(),
                aLocalDateTime(entidad.obtenerFechaDefinitiva()),
                entidad.obtenerPistas(),
                entidad.obtenerSedeAlternativaJornada(),
                entidad.obtenerUrlFotoActa(),
                entidad.obtenerPuntosLocal(),
                entidad.obtenerPuntosVisitante(),
                entidad.esActaConfirmada(),
                propuestas,
                disponibilidades,
                partidos
        );
    }

    public PropuestaFecha aDominioPropuestaFecha(EntidadPropuestaFecha entidad) {
        return new PropuestaFecha(
                entidad.obtenerId(),
                entidad.obtenerEnfrentamiento().obtenerId(),
                aLocalDateTime(entidad.obtenerFechaPropuesta()),
                entidad.obtenerOrden(),
                entidad.esSeleccionada()
        );
    }

    public DisponibilidadJugador aDominioDisponibilidad(EntidadDisponibilidadJugador entidad) {
        return new DisponibilidadJugador(
                entidad.obtenerId(),
                entidad.obtenerEnfrentamiento().obtenerId(),
                entidad.obtenerPropuestaFechaId(),
                entidad.obtenerJugadorId(),
                entidad.obtenerEstadoDisponibilidad(),
                entidad.esDisponibilidadFinal(),
                aLocalDateTime(entidad.obtenerCreadoEn())
        );
    }

    public PartidoPareja aDominioPartido(EntidadPartidoEnfrentamiento entidad) {
        return new PartidoPareja(
                entidad.obtenerId(),
                entidad.obtenerEnfrentamiento().obtenerId(),
                entidad.obtenerNumeroPareja(),
                entidad.obtenerJugadorLocal1Id(),
                entidad.obtenerJugadorLocal2Id(),
                entidad.obtenerJugadorVisitante1Id(),
                entidad.obtenerJugadorVisitante2Id(),
                entidad.obtenerSet1Local(),
                entidad.obtenerSet1Visitante(),
                entidad.obtenerSet2Local(),
                entidad.obtenerSet2Visitante(),
                entidad.obtenerSet3Local(),
                entidad.obtenerSet3Visitante(),
                entidad.obtenerParejaGanadora(),
                entidad.estaCompletado()
        );
    }

    public EntidadEnfrentamiento crearNuevaEntidadEnfrentamiento(Enfrentamiento dominio) {
        OffsetDateTime ahora = OffsetDateTime.now();
        return new EntidadEnfrentamiento(
                dominio.obtenerId(),
                dominio.obtenerDivisionId(),
                dominio.obtenerEquipoLocalId(),
                dominio.obtenerEquipoVisitanteId(),
                dominio.obtenerNumeroJornada(),
                dominio.obtenerEstado(),
                aOffsetDateTime(dominio.obtenerFechaDefinitiva()),
                dominio.obtenerPistas(),
                dominio.obtenerSedeAlternativaJornada(),
                dominio.obtenerUrlFotoActa(),
                dominio.obtenerPuntosLocal(),
                dominio.obtenerPuntosVisitante(),
                dominio.esActaConfirmada(),
                ahora,
                ahora
        );
    }

    public EntidadPropuestaFecha aEntidadPropuestaFecha(PropuestaFecha dominio, EntidadEnfrentamiento padre) {
        OffsetDateTime ahora = OffsetDateTime.now();
        return new EntidadPropuestaFecha(
                dominio.obtenerId(),
                padre,
                aOffsetDateTime(dominio.obtenerFechaPropuesta()),
                dominio.obtenerOrden(),
                dominio.esSeleccionada(),
                ahora,
                ahora
        );
    }

    public EntidadDisponibilidadJugador aEntidadDisponibilidad(DisponibilidadJugador dominio, EntidadEnfrentamiento padre) {
        OffsetDateTime ahora = OffsetDateTime.now();
        OffsetDateTime creado = dominio.obtenerCreadoEn() != null ? aOffsetDateTime(dominio.obtenerCreadoEn()) : ahora;
        return new EntidadDisponibilidadJugador(
                dominio.obtenerId(),
                padre,
                dominio.obtenerPropuestaFechaId(),
                dominio.obtenerJugadorId(),
                dominio.obtenerEstado(),
                dominio.esDisponibilidadFinal(),
                creado,
                ahora
        );
    }

    public EntidadPartidoEnfrentamiento aEntidadPartido(PartidoPareja dominio, EntidadEnfrentamiento padre) {
        OffsetDateTime ahora = OffsetDateTime.now();
        return new EntidadPartidoEnfrentamiento(
                dominio.obtenerId(),
                padre,
                dominio.obtenerNumeroPareja(),
                dominio.obtenerJugadorLocal1Id(),
                dominio.obtenerJugadorLocal2Id(),
                dominio.obtenerJugadorVisitante1Id(),
                dominio.obtenerJugadorVisitante2Id(),
                dominio.obtenerSet1Local(),
                dominio.obtenerSet1Visitante(),
                dominio.obtenerSet2Local(),
                dominio.obtenerSet2Visitante(),
                dominio.obtenerSet3Local(),
                dominio.obtenerSet3Visitante(),
                dominio.obtenerParejaGanadora(),
                dominio.estaCompletado(),
                ahora,
                ahora
        );
    }

    public OffsetDateTime aOffsetDateTime(LocalDateTime fechaHora) {
        if (fechaHora == null) {
            return null;
        }
        return fechaHora.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    public LocalDateTime aLocalDateTime(OffsetDateTime fechaHoraConZona) {
        if (fechaHoraConZona == null) {
            return null;
        }
        return fechaHoraConZona.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }
}
