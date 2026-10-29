package com.synagon.api.dominio.modelos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas unitarias del modelo de dominio Usuario")
class UsuarioTest {

    private static final UUID EQUIPO_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");

    @Test
    @DisplayName("Separa correctamente nombre y apellidos, normaliza email y asigna rol JUGADOR por defecto")
    void debeCrearNuevoJugadorValido() {
        Usuario usuario = Usuario.registrarNuevoJugador(
                "  Lucía   Gómez Martín  ",
                "  Lucia.Gomez@Synagon.ES ",
                "   ",
                "$2a$10$hashDePruebaValido",
                true,
                EQUIPO_ID
        );

        assertEquals("Lucía", usuario.obtenerNombre());
        assertEquals("Gómez Martín", usuario.obtenerApellidos());
        assertEquals("Lucía Gómez Martín", usuario.obtenerNombreCompleto());
        assertEquals("lucia.gomez@synagon.es", usuario.obtenerEmail());
        assertNull(usuario.obtenerTelefono());
        assertEquals(RolUsuario.JUGADOR, usuario.obtenerRol());
        assertTrue(usuario.haAceptadoPrivacidad());
        assertEquals(EQUIPO_ID, usuario.obtenerEquipoId());
    }

    @Test
    @DisplayName("Permite nombre completo de una sola palabra si tiene al menos 3 caracteres")
    void debePermitirNombreDeUnaSolaPalabra() {
        Usuario usuario = Usuario.registrarNuevoJugador(
                "Ana",
                "ana@synagon.es",
                "+34611223344",
                "$2a$10$hashDePruebaValido",
                true,
                EQUIPO_ID
        );

        assertEquals("Ana", usuario.obtenerNombre());
        assertEquals("", usuario.obtenerApellidos());
        assertEquals("Ana", usuario.obtenerNombreCompleto());
        assertEquals("+34611223344", usuario.obtenerTelefono());
    }

    @Test
    @DisplayName("Rechaza el registro si aceptoPrivacidad es false o nulo por cumplimiento RGPD")
    void debeRechazarSinAceptacionRgpd() {
        IllegalArgumentException exFalso = assertThrows(
                IllegalArgumentException.class,
                () -> Usuario.registrarNuevoJugador(
                        "Pedro López",
                        "pedro@synagon.es",
                        null,
                        "$2a$10$hash",
                        false,
                        EQUIPO_ID
                )
        );
        assertTrue(exFalso.getMessage().contains("RGPD"));

        assertThrows(
                IllegalArgumentException.class,
                () -> Usuario.registrarNuevoJugador(
                        "Pedro López",
                        "pedro@synagon.es",
                        null,
                        "$2a$10$hash",
                        null,
                        EQUIPO_ID
                )
        );
    }

    @Test
    @DisplayName("Rechaza nombre menor de 3 caracteres o formato de email inválido")
    void debeRechazarNombreCortoOEmailInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Usuario.registrarNuevoJugador(
                        "Al",
                        "al@synagon.es",
                        null,
                        "$2a$10$hash",
                        true,
                        EQUIPO_ID
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> Usuario.registrarNuevoJugador(
                        "Alberto Ruiz",
                        "correo-sin-dominio",
                        null,
                        "$2a$10$hash",
                        true,
                        EQUIPO_ID
                )
        );
    }
}
