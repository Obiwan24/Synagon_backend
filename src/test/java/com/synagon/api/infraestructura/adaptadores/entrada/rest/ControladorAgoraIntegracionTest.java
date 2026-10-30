package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.infraestructura.adaptadores.salida.persistencia.EntidadUsuario;
import com.synagon.api.infraestructura.adaptadores.salida.persistencia.RepositorioJpaUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DisplayName("Pruebas de integración de Ágora Multi-Rol, Ligas, Alta por Capitán y Sede Alternativa (Fase 5)")
class ControladorAgoraIntegracionTest {

    private static final UUID USUARIO_PRUEBA_ID = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID JUGADOR_EXISTENTE_ID = UUID.fromString("40000000-0000-0000-0000-000000000102");
    private static final UUID EQUIPO_BETIS_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");
    private static final UUID ENFRENTAMIENTO_SEMILLA_ID = UUID.fromString("70000000-0000-0000-0000-000000000001");

    @Autowired
    private WebApplicationContext contextoWeb;

    @Autowired
    private RepositorioJpaUsuario repositorioJpaUsuario;

    @Autowired
    private PasswordEncoder codificadorContrasena;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(contextoWeb)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Usuario semilla tiene las 3 membresías activas de ejemplo en el Ágora Principal")
    void debeRetornarLasTresMembresiasSemillaDelUsuarioDePrueba() throws Exception {
        mockMvc.perform(get("/api/v1/agora/usuarios/" + USUARIO_PRUEBA_ID + "/membresias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].rol", is("PRESIDENTE")))
                .andExpect(jsonPath("$[0].nombreLiga", is("Liga Nocturna Aljarafe")))
                .andExpect(jsonPath("$[0].equipoId", is(nullValue())))
                .andExpect(jsonPath("$[1].rol", is("CAPITAN")))
                .andExpect(jsonPath("$[1].nombreLiga", is("Liga Interclubes Sevilla")))
                .andExpect(jsonPath("$[1].nombreEquipoOSubtitulo", is("Real Betis Pádel Team")))
                .andExpect(jsonPath("$[2].rol", is("JUGADOR")))
                .andExpect(jsonPath("$[2].nombreLiga", is("Torneo Otoño Híspalis")))
                .andExpect(jsonPath("$[2].nombreEquipoOSubtitulo", is("Espartanos de Triana")));
    }

    @Test
    @DisplayName("Valida código de invitación e inscribe equipo en liga de sede única asignando rol CAPITAN")
    void debeValidarCodigoEInscribirEquipoEnLigaSedeUnica() throws Exception {
        mockMvc.perform(get("/api/v1/agora/ligas/validar-codigo/ALJARAFE-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreLiga", is("Liga Nocturna Aljarafe")))
                .andExpect(jsonPath("$.modoSede", is("SEDE_UNICA_CLUB")))
                .andExpect(jsonPath("$.requiereSedeEquipo", is(false)))
                .andExpect(jsonPath("$.partidosPorJornada", is(3)));

        String peticionInscripcion = String.format("""
                {
                  "usuarioId": "%s",
                  "codigoInvitacion": "ALJARAFE-2026",
                  "nombreEquipo": "Gladiadores del Aljarafe"
                }
                """, JUGADOR_EXISTENTE_ID);

        mockMvc.perform(post("/api/v1/agora/equipos/inscribir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(peticionInscripcion))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol", is("CAPITAN")))
                .andExpect(jsonPath("$.nombreEquipoOSubtitulo", is("Gladiadores del Aljarafe")));
    }

    @Test
    @DisplayName("Alta por capitán con email nuevo usa DNI en mayúsculas como password BCrypt y soporta primer acceso y edición de perfil")
    void debeCompletarFlujoAltaCapitanPrimerAccesoYEdicionPerfil() throws Exception {
        String cuerpoAlta = String.format("""
                {
                  "capitanId": "%s",
                  "email": "Ignacio.Ramos@Synagon.ES",
                  "dni": "33445566m",
                  "nombreCompleto": "Ignacio Ramos"
                }
                """, USUARIO_PRUEBA_ID);

        mockMvc.perform(post("/api/v1/agora/equipos/" + EQUIPO_BETIS_ID + "/jugadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoAlta))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("ignacio.ramos@synagon.es")))
                .andExpect(jsonPath("$.dni", is("33445566M")))
                .andExpect(jsonPath("$.usuarioNuevoCreado", is(true)))
                .andExpect(jsonPath("$.requiereCambioPassword", is(true)))
                .andExpect(jsonPath("$.primerAccesoCompletado", is(false)));

        EntidadUsuario creado = repositorioJpaUsuario
                .buscarPorEmailIgnorandoMayusculas("ignacio.ramos@synagon.es")
                .orElseThrow();
        assertTrue(codificadorContrasena.matches("33445566M", creado.obtenerContrasenaHash()));

        // Primer acceso cambiando contraseña
        mockMvc.perform(post("/api/v1/usuarios/" + creado.obtenerId() + "/primer-acceso-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deseaCambiar": true,
                                  "nuevaPasswordOpcional": "ClaveDefinitiva2026"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiereCambioPassword", is(false)))
                .andExpect(jsonPath("$.primerAccesoCompletado", is(true)));

        EntidadUsuario trasPrimerAcceso = repositorioJpaUsuario.findById(creado.obtenerId()).orElseThrow();
        assertFalse(trasPrimerAcceso.requiereCambioPassword());
        assertTrue(trasPrimerAcceso.haCompletadoPrimerAcceso());
        assertTrue(codificadorContrasena.matches("ClaveDefinitiva2026", trasPrimerAcceso.obtenerContrasenaHash()));
    }

    @Test
    @DisplayName("Capitán actualiza sede alternativa de jornada conservando la sede oficial del enfrentamiento")
    void debeActualizarSedeAlternativaDeJornada() throws Exception {
        String cuerpoSede = String.format("""
                {
                  "capitanId": "%s",
                  "nuevaSedeAlternativa": "Pádel Indoor Cartuja (Cubiertas)"
                }
                """, USUARIO_PRUEBA_ID);

        mockMvc.perform(put("/api/v1/jornadas/enfrentamientos/" + ENFRENTAMIENTO_SEMILLA_ID + "/sede")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSede))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pistas", is("Pistas Centrales Club Pádel Triana")))
                .andExpect(jsonPath("$.sedeAlternativaJornada", is("Pádel Indoor Cartuja (Cubiertas)")))
                .andExpect(jsonPath("$.sedeEfectivaJuego", is("Pádel Indoor Cartuja (Cubiertas)")));
    }
}
