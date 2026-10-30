package com.synagon.api.dominio.modelos;

import com.synagon.api.dominio.servicios.ServicioDominioUsuarios;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas unitarias del modelo de dominio Usuario y ServicioDominioUsuarios")
class UsuarioTest {

    private static final UUID EQUIPO_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
    private final ServicioDominioUsuarios servicioDominio = new ServicioDominioUsuarios();

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
        assertFalse(usuario.requiereCambioPassword());
        assertTrue(usuario.haCompletadoPrimerAcceso());
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

    @Test
    @DisplayName("Alta por capitán de email nuevo crea usuario con email en minúsculas, DNI en mayúsculas en BCrypt y flags de primer acceso")
    void debeCrearJugadorPorCapitanConDniEnMayusculasComoPasswordProvisional() {
        ResultadoAltaJugadorEquipo resultado = servicioDominio.prepararAltaJugadorPorCapitan(
                Optional.empty(),
                EQUIPO_ID,
                "  Nuevo.Jugador@Synagon.ES ",
                " 45678912z ",
                "Mario Cabrera",
                "+34699887766",
                bcrypt::encode
        );

        assertTrue(resultado.usuarioNuevoCreado());
        Usuario creado = resultado.usuario();
        assertEquals("nuevo.jugador@synagon.es", creado.obtenerEmail());
        assertEquals("45678912Z", creado.obtenerDni());
        assertTrue(bcrypt.matches("45678912Z", creado.obtenerContrasenaHash()));
        assertTrue(creado.requiereCambioPassword());
        assertFalse(creado.haCompletadoPrimerAcceso());
    }

    @Test
    @DisplayName("Alta por capitán de email existente conserva intacta su contraseña actual y solo marca vinculación al equipo")
    void debeConservarContrasenaSiEmailYaExisteEnAltaPorCapitan() {
        String hashOriginal = bcrypt.encode("MiClavePersonal2026!");
        Usuario existente = Usuario.registrarNuevoJugador(
                "Carlos Sánchez",
                "carlos.sanchez@synagon.es",
                "+34600100003",
                hashOriginal,
                true,
                UUID.randomUUID()
        );

        UUID nuevoEquipoId = UUID.randomUUID();
        ResultadoAltaJugadorEquipo resultado = servicioDominio.prepararAltaJugadorPorCapitan(
                Optional.of(existente),
                nuevoEquipoId,
                "CARLOS.SANCHEZ@synagon.es",
                "11223344b",
                null,
                null,
                bcrypt::encode
        );

        assertFalse(resultado.usuarioNuevoCreado());
        assertEquals(hashOriginal, resultado.usuario().obtenerContrasenaHash());
        assertTrue(bcrypt.matches("MiClavePersonal2026!", resultado.usuario().obtenerContrasenaHash()));
        assertEquals(nuevoEquipoId, resultado.usuario().obtenerEquipoId());
    }

    @Test
    @DisplayName("Gestiona primer acceso apagando requiereCambioPassword y marcando primerAccesoCompletado")
    void debeGestionarPrimerAccesoConOSinCambioDePassword() {
        String hashInicial = bcrypt.encode("45678912Z");
        Usuario provisional = Usuario.crearAltaPorCapitan(
                "Mario Cabrera",
                "mario@synagon.es",
                "45678912z",
                null,
                hashInicial,
                EQUIPO_ID
        );

        Usuario sinCambio = provisional.gestionarPrimerAccesoPassword(false, null);
        assertFalse(sinCambio.requiereCambioPassword());
        assertTrue(sinCambio.haCompletadoPrimerAcceso());
        assertEquals(hashInicial, sinCambio.obtenerContrasenaHash());

        String nuevoHash = bcrypt.encode("NuevaClaveSegura99");
        Usuario conCambio = provisional.gestionarPrimerAccesoPassword(true, nuevoHash);
        assertFalse(conCambio.requiereCambioPassword());
        assertTrue(conCambio.haCompletadoPrimerAcceso());
        assertTrue(bcrypt.matches("NuevaClaveSegura99", conCambio.obtenerContrasenaHash()));
    }
}
