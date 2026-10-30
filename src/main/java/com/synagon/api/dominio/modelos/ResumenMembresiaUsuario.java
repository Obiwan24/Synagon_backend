package com.synagon.api.dominio.modelos;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio inmutable que resume una membresía activa del usuario
 * para la pantalla del Ágora Principal (Home Multi-Rol).
 */
public record ResumenMembresiaUsuario(
        UUID ligaId,
        String nombreLiga,
        UUID equipoId,
        String nombreEquipoOSubtitulo,
        String textoSedeDetalle,
        int partidosPorJornada,
        RolMembresia rol
) {

    public ResumenMembresiaUsuario {
        Objects.requireNonNull(ligaId, "El identificador de la liga es obligatorio.");
        Objects.requireNonNull(nombreLiga, "El nombre de la liga es obligatorio.");
        Objects.requireNonNull(nombreEquipoOSubtitulo, "El nombre del equipo o subtítulo es obligatorio.");
        Objects.requireNonNull(textoSedeDetalle, "El detalle de sede es obligatorio.");
        Objects.requireNonNull(rol, "El rol de la membresía es obligatorio.");

        if (partidosPorJornada < 1) {
            throw new IllegalArgumentException("El número de partidos por jornada debe ser positivo.");
        }
        if (rol != RolMembresia.PRESIDENTE && equipoId == null) {
            throw new IllegalArgumentException("Las membresías de capitán o jugador requieren un equipo asociado.");
        }
    }

    /**
     * Comparador para ordenar las tarjetas del Ágora Principal:
     * primero por jerarquía de rol (PRESIDENTE, CAPITAN, JUGADOR) y después por nombre de liga.
     */
    public static Comparator<ResumenMembresiaUsuario> comparadorOrdenAgora() {
        Collator coladorEspanol = Collator.getInstance(Locale.forLanguageTag("es-ES"));
        coladorEspanol.setStrength(Collator.PRIMARY);

        return Comparator.comparingInt((ResumenMembresiaUsuario m) -> m.rol().ordinal())
                .thenComparing(ResumenMembresiaUsuario::nombreLiga, coladorEspanol)
                .thenComparing(ResumenMembresiaUsuario::nombreEquipoOSubtitulo, coladorEspanol);
    }
}
