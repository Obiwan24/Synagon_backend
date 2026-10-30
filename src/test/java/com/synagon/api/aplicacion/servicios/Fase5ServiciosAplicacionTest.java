package com.synagon.api.aplicacion.servicios;

import com.synagon.api.dominio.excepciones.ReglaNegocioExcepcion;
import com.synagon.api.dominio.modelos.Enfrentamiento;
import com.synagon.api.dominio.modelos.EstadoEnfrentamiento;
import com.synagon.api.dominio.modelos.Liga;
import com.synagon.api.dominio.modelos.ModoSedeLiga;
import com.synagon.api.dominio.modelos.ResultadoAltaJugadorEquipo;
import com.synagon.api.dominio.modelos.ResumenMembresiaUsuario;
import com.synagon.api.dominio.modelos.RolMembresia;
import com.synagon.api.dominio.modelos.Usuario;
import com.synagon.api.dominio.puertos.salida.RepositorioAgoraYLigasPuerto;
import com.synagon.api.dominio.puertos.salida.RepositorioEnfrentamientoPuerto;
import com.synagon.api.dominio.puertos.salida.RepositorioUsuarioPuerto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas unitarias de Servicios de Aplicación Fase 5 (Ágora, Ligas, Usuarios y Sede de Jornada)")
class Fase5ServiciosAplicacionTest {

    private final PasswordEncoder codificador = new BCryptPasswordEncoder();

    @Test
    @DisplayName("ServicioAgoraYLigas exige sede oficial en SEDE_LOCAL_POR_EQUIPO y la omite en SEDE_UNICA_CLUB asignando rol CAPITAN")
    void debeValidarModoSedeEInscribirEquipoConCapitan() {
        UUID usuarioId = UUID.randomUUID();
        UUID divisionId = UUID.randomUUID();

        Liga ligaLocal = new Liga(
                UUID.randomUUID(),
                "Liga Interclubes Sevilla",
                "Liga provincial",
                "SEVILLA-2026",
                null,
                3,
                ModoSedeLiga.SEDE_LOCAL_POR_EQUIPO,
                null,
                3, 1, 0, true, true, "Normativa"
        );

        Liga ligaUnica = new Liga(
                UUID.randomUUID(),
                "Liga Nocturna Aljarafe",
                "Liga nocturna",
                "ALJARAFE-2026",
                usuarioId,
                3,
                ModoSedeLiga.SEDE_UNICA_CLUB,
                "Club Pádel Mairena del Aljarafe",
                3, 1, 0, true, true, "Normativa"
        );

        RepositorioAgoraYLigasPuerto repoMemoria = new RepositorioAgoraYLigasPuerto() {
            @Override
            public boolean existeUsuarioPorId(UUID id) { return usuarioId.equals(id); }

            @Override
            public List<ResumenMembresiaUsuario> obtenerMembresiasActivasDeUsuario(UUID id) {
                return List.of(
                        new ResumenMembresiaUsuario(ligaLocal.obtenerId(), ligaLocal.obtenerNombre(), UUID.randomUUID(),
                                "Espartanos de Triana", "Sede local: Triana", 3, RolMembresia.JUGADOR),
                        new ResumenMembresiaUsuario(ligaUnica.obtenerId(), ligaUnica.obtenerNombre(), null,
                                "Presidencia y dirección de liga", "Sede única: Club Pádel Mairena del Aljarafe", 3, RolMembresia.PRESIDENTE),
                        new ResumenMembresiaUsuario(UUID.randomUUID(), "Liga Interclubes Sevilla", UUID.randomUUID(),
                                "Real Betis Pádel Team", "Sede local: Luis del Sol", 3, RolMembresia.CAPITAN)
                );
            }

            @Override
            public Optional<Liga> buscarLigaPorCodigoInvitacion(String codigo) {
                if ("SEVILLA-2026".equalsIgnoreCase(codigo)) return Optional.of(ligaLocal);
                if ("ALJARAFE-2026".equalsIgnoreCase(codigo)) return Optional.of(ligaUnica);
                return Optional.empty();
            }

            @Override
            public Optional<Liga> obtenerLigaPorEquipoId(UUID equipoId) { return Optional.empty(); }

            @Override
            public boolean perteneceUsuarioALiga(UUID uid, UUID lid) { return false; }

            @Override
            public UUID obtenerOCrearDivisionInscripcionLiga(UUID lid, String nombre) { return divisionId; }

            @Override
            public boolean existeEquipoConNombreEnDivision(UUID did, String nombre) { return false; }

            @Override
            public UUID crearEquipoConCapitan(UUID did, String nombre, String clubSede, UUID capitan) {
                return UUID.randomUUID();
            }
        };

        ServicioAgoraYLigas servicio = new ServicioAgoraYLigas(repoMemoria);

        // Ordenación de membresías: PRESIDENTE -> CAPITAN -> JUGADOR
        List<ResumenMembresiaUsuario> membresias = servicio.obtenerMembresiasUsuario(usuarioId);
        assertEquals(3, membresias.size());
        assertEquals(RolMembresia.PRESIDENTE, membresias.get(0).rol());
        assertNull(membresias.get(0).equipoId());
        assertEquals(RolMembresia.CAPITAN, membresias.get(1).rol());
        assertEquals(RolMembresia.JUGADOR, membresias.get(2).rol());

        // Rechazar inscripción sin sede en SEDE_LOCAL_POR_EQUIPO
        assertThrows(ReglaNegocioExcepcion.class, () ->
                servicio.inscribirEquipoConCodigo(usuarioId, "SEVILLA-2026", "Nuevo Equipo Sevilla", "   ")
        );

        // Permitir inscripción con sede en SEDE_LOCAL_POR_EQUIPO
        ResumenMembresiaUsuario inscritoLocal = servicio.inscribirEquipoConCodigo(
                usuarioId, "sevilla-2026", "Nuevo Equipo Sevilla", "Club Bernier"
        );
        assertEquals(RolMembresia.CAPITAN, inscritoLocal.rol());
        assertEquals("Sede local: Club Bernier", inscritoLocal.textoSedeDetalle());

        // Permitir inscripción sin sede en SEDE_UNICA_CLUB
        ResumenMembresiaUsuario inscritoUnica = servicio.inscribirEquipoConCodigo(
                usuarioId, "aljarafe-2026", "Halcones Aljarafe", null
        );
        assertEquals(RolMembresia.CAPITAN, inscritoUnica.rol());
        assertEquals("Sede única: Club Pádel Mairena del Aljarafe", inscritoUnica.textoSedeDetalle());
    }

    @Test
    @DisplayName("ServicioUsuarios gestiona alta por capitán, primer acceso de password y actualización de perfil")
    void debeGestionarCicloDeAltaCapitanPrimerAccesoYPerfil() {
        UUID equipoId = UUID.randomUUID();
        UUID capitanId = UUID.randomUUID();
        Map<UUID, Usuario> almacenUsuarios = new HashMap<>();

        RepositorioUsuarioPuerto repoUsuarios = new RepositorioUsuarioPuerto() {
            @Override
            public boolean existePorEmail(String email) {
                return buscarPorEmail(email).isPresent();
            }

            @Override
            public Optional<Usuario> obtenerPorId(UUID usuarioId) {
                return Optional.ofNullable(almacenUsuarios.get(usuarioId));
            }

            @Override
            public Optional<Usuario> buscarPorEmail(String email) {
                return almacenUsuarios.values().stream()
                        .filter(u -> u.obtenerEmail().equalsIgnoreCase(email))
                        .findFirst();
            }

            @Override
            public boolean existePorDniExcluyendoUsuario(String dni, UUID usuarioExcluidoId) {
                return almacenUsuarios.values().stream()
                        .anyMatch(u -> dni.equalsIgnoreCase(u.obtenerDni())
                                && !u.obtenerId().equals(usuarioExcluidoId));
            }

            @Override
            public Optional<UUID> obtenerEquipoPorDefectoId() { return Optional.of(equipoId); }

            @Override
            public Usuario guardar(Usuario usuario) {
                almacenUsuarios.put(usuario.obtenerId(), usuario);
                return usuario;
            }

            @Override
            public Usuario guardarConMembresiaEquipo(Usuario usuario, UUID eqId) {
                almacenUsuarios.put(usuario.obtenerId(), usuario);
                return usuario;
            }

            @Override
            public boolean existeEquipoPorId(UUID eqId) { return equipoId.equals(eqId); }

            @Override
            public boolean esCapitanActivoDeEquipo(UUID uid, UUID eqId) {
                return capitanId.equals(uid) && equipoId.equals(eqId);
            }

            @Override
            public boolean esMiembroActivoDeEquipo(UUID uid, UUID eqId) { return false; }

            @Override
            public void vincularJugadorAEquipo(UUID uid, UUID eqId) { }
        };

        ServicioUsuarios servicioUsuarios = new ServicioUsuarios(repoUsuarios, codificador);

        // 1. Alta por capitán de jugador nuevo
        ResultadoAltaJugadorEquipo alta = servicioUsuarios.registrarJugadorPorCapitan(
                equipoId, capitanId, "Jugador.Nuevo@Synagon.es", "77889900k", null, null
        );
        assertTrue(alta.usuarioNuevoCreado());
        UUID jugadorId = alta.usuario().obtenerId();
        assertEquals("jugador.nuevo@synagon.es", alta.usuario().obtenerEmail());
        assertEquals("77889900K", alta.usuario().obtenerDni());
        assertTrue(codificador.matches("77889900K", alta.usuario().obtenerContrasenaHash()));
        assertTrue(alta.usuario().requiereCambioPassword());
        assertFalse(alta.usuario().haCompletadoPrimerAcceso());

        // 2. Primer acceso cambiando contraseña
        Usuario trasCambio = servicioUsuarios.gestionarPrimerAccesoPassword(
                jugadorId, true, "MiNuevaPassword2026"
        );
        assertFalse(trasCambio.requiereCambioPassword());
        assertTrue(trasCambio.haCompletadoPrimerAcceso());
        assertTrue(codificador.matches("MiNuevaPassword2026", trasCambio.obtenerContrasenaHash()));

        // 3. Actualizar perfil personal
        Usuario perfilActualizado = servicioUsuarios.actualizarPerfilUsuario(
                jugadorId, "Sergio Ramos García", "+34612345678", "77889900x"
        );
        assertEquals("Sergio Ramos García", perfilActualizado.obtenerNombreCompleto());
        assertEquals("+34612345678", perfilActualizado.obtenerTelefono());
        assertEquals("77889900X", perfilActualizado.obtenerDni());
    }

    @Test
    @DisplayName("ServicioGestionJornada permite al capitán actualizar la sede alternativa de la jornada y rechaza a no capitanes")
    void debeActualizarSedeJornadaSoloSiEsCapitanDelEnfrentamiento() {
        UUID enfrentamientoId = UUID.randomUUID();
        UUID capitanId = UUID.randomUUID();
        UUID jugadorNormalId = UUID.randomUUID();
        UUID localId = UUID.randomUUID();
        UUID visitanteId = UUID.randomUUID();

        Enfrentamiento enfrentamientoBase = new Enfrentamiento(
                enfrentamientoId,
                UUID.randomUUID(),
                localId,
                visitanteId,
                2,
                EstadoEnfrentamiento.CONVOCATORIA_ABIERTA,
                LocalDateTime.now().plusDays(2),
                "Ciudad Deportiva Luis del Sol",
                null,
                null,
                0,
                0,
                false,
                List.of(),
                List.of(),
                List.of()
        );

        RepositorioEnfrentamientoPuerto repoJornada = new RepositorioEnfrentamientoPuerto() {
            @Override
            public Optional<Enfrentamiento> obtenerPorId(UUID id) {
                return enfrentamientoId.equals(id) ? Optional.of(enfrentamientoBase) : Optional.empty();
            }

            @Override
            public Enfrentamiento guardar(Enfrentamiento e) { return e; }

            @Override
            public boolean existeUsuarioPorId(UUID uid) {
                return capitanId.equals(uid) || jugadorNormalId.equals(uid);
            }

            @Override
            public boolean perteneceJugadorAAlgunEquipo(UUID jid, UUID lid, UUID vid) { return true; }

            @Override
            public boolean esCapitanDeAlgunEquipoDelEnfrentamiento(UUID cid, UUID lid, UUID vid) {
                return capitanId.equals(cid);
            }
        };

        ServicioGestionJornada servicioJornada = new ServicioGestionJornada(repoJornada);

        assertThrows(ReglaNegocioExcepcion.class, () ->
                servicioJornada.actualizarSedeJornada(enfrentamientoId, jugadorNormalId, "Pádel Indoor Sevilla")
        );

        Enfrentamiento actualizado = servicioJornada.actualizarSedeJornada(
                enfrentamientoId, capitanId, "Pádel Indoor Sevilla (Lluvia)"
        );
        assertEquals("Ciudad Deportiva Luis del Sol", actualizado.obtenerPistas());
        assertEquals("Pádel Indoor Sevilla (Lluvia)", actualizado.obtenerSedeAlternativaJornada());
        assertEquals("Pádel Indoor Sevilla (Lluvia)", actualizado.obtenerSedeEfectivaJuego());
    }
}
