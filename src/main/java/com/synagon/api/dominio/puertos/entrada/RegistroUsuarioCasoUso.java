package com.synagon.api.dominio.puertos.entrada;

import com.synagon.api.dominio.modelos.DatosRegistroUsuario;
import com.synagon.api.dominio.modelos.Usuario;

/**
 * Puerto de entrada para la gestión y el registro de usuarios en Synagon.
 */
public interface RegistroUsuarioCasoUso {

    /**
     * Registra un nuevo usuario con rol {@code JUGADOR}, valida el consentimiento RGPD,
     * verifica la unicidad del correo electrónico, cifra su contraseña y lo vincula al equipo por defecto.
     */
    Usuario registrarUsuario(DatosRegistroUsuario datosRegistro);
}
