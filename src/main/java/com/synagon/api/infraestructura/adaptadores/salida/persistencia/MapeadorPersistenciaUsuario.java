package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import com.synagon.api.dominio.modelos.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Mapeador entre el modelo de dominio {@link Usuario} y la entidad JPA {@link EntidadUsuario}.
 */
@Component
public class MapeadorPersistenciaUsuario {

    private static final ZoneId ZONA_MADRID = ZoneId.of("Europe/Madrid");

    public EntidadUsuario aEntidad(Usuario usuario, OffsetDateTime ahora) {
        OffsetDateTime instanteCreado = usuario.obtenerCreadoEn() != null
                ? aOffsetDateTime(usuario.obtenerCreadoEn())
                : ahora;
        OffsetDateTime instanteAceptacion = usuario.obtenerFechaAceptacionPrivacidad() != null
                ? aOffsetDateTime(usuario.obtenerFechaAceptacionPrivacidad())
                : ahora;

        return new EntidadUsuario(
                usuario.obtenerId(),
                usuario.obtenerNombre(),
                usuario.obtenerApellidos(),
                usuario.obtenerEmail(),
                usuario.obtenerTelefono(),
                usuario.obtenerRol(),
                usuario.obtenerContrasenaHash(),
                usuario.haAceptadoPrivacidad(),
                instanteAceptacion,
                instanteCreado,
                ahora
        );
    }

    public Usuario aDominio(EntidadUsuario entidad, UUID equipoId) {
        return new Usuario(
                entidad.obtenerId(),
                entidad.obtenerNombre(),
                entidad.obtenerApellidos(),
                entidad.obtenerEmail(),
                entidad.obtenerTelefono(),
                entidad.obtenerContrasenaHash(),
                entidad.obtenerRol(),
                entidad.haAceptadoPrivacidad(),
                aLocalDateTime(entidad.obtenerFechaAceptacionPrivacidad()),
                equipoId,
                aLocalDateTime(entidad.obtenerCreadoEn())
        );
    }

    private OffsetDateTime aOffsetDateTime(LocalDateTime fechaLocal) {
        if (fechaLocal == null) {
            return null;
        }
        return fechaLocal.atZone(ZONA_MADRID).toOffsetDateTime();
    }

    private LocalDateTime aLocalDateTime(OffsetDateTime fechaOffset) {
        if (fechaOffset == null) {
            return null;
        }
        return fechaOffset.atZoneSameInstant(ZONA_MADRID).toLocalDateTime();
    }
}
