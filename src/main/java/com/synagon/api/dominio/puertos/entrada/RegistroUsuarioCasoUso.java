package com.synagon.api.dominio.puertos.entrada;

import com.synagon.api.dominio.modelos.DatosRegistroUsuario;
import com.synagon.api.dominio.modelos.ResultadoAltaJugadorEquipo;
import com.synagon.api.dominio.modelos.Usuario;

import java.util.UUID;

/**
 * Puerto de entrada para el registro público, el alta de jugadores por capitán,
 * la gestión del primer acceso de contraseña y la edición del perfil de usuario.
 */
public interface RegistroUsuarioCasoUso {

    /**
     * Registra un nuevo usuario con rol {@code JUGADOR}, valida el consentimiento RGPD,
     * verifica la unicidad del correo electrónico, cifra su contraseña y lo vincula al equipo por defecto.
     */
    Usuario registrarUsuario(DatosRegistroUsuario datosRegistro);

    /**
     * Da de alta o vincula a un jugador en un equipo por parte de su capitán mediante email y DNI.
     */
    ResultadoAltaJugadorEquipo registrarJugadorPorCapitan(
            UUID equipoId,
            UUID capitanId,
            String email,
            String dni,
            String nombreCompletoOpcional,
            String telefonoOpcional
    );

    /**
     * Gestiona el flujo de cambio opcional u obligatorio de contraseña en el primer acceso.
     */
    Usuario gestionarPrimerAccesoPassword(
            UUID usuarioId,
            boolean deseaCambiar,
            String nuevaPasswordOpcional
    );

    /**
     * Actualiza los datos personales de perfil del usuario.
     */
    Usuario actualizarPerfilUsuario(
            UUID usuarioId,
            String nombreCompleto,
            String telefono,
            String dni
    );
}
