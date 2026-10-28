package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.Enfrentamiento;
import com.synagon.api.dominio.modelos.EstadoEnfrentamiento;
import com.synagon.api.dominio.puertos.entrada.GestionJornadaCasoUso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DisplayName("Pruebas de integración de ControladorJornada y ServicioGestionJornada")
class ControladorJornadaIntegracionTest {

    private static final String URL_BASE = "/api/v1/jornadas/enfrentamientos";
    private static final UUID ENFRENTAMIENTO_SEMILLA_ID = UUID.fromString("70000000-0000-0000-0000-000000000001");

    private static final UUID CAPITAN_ID = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID JUGADOR_2_ID = UUID.fromString("40000000-0000-0000-0000-000000000002");
    private static final UUID JUGADOR_3_ID = UUID.fromString("40000000-0000-0000-0000-000000000003");
    private static final UUID JUGADOR_4_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final UUID JUGADOR_5_ID = UUID.fromString("40000000-0000-0000-0000-000000000005");
    private static final UUID JUGADOR_6_ID = UUID.fromString("40000000-0000-0000-0000-000000000006");
    private static final UUID JUGADOR_7_ID = UUID.fromString("40000000-0000-0000-0000-000000000007");

    @Autowired
    private WebApplicationContext contextoWeb;

    @Autowired
    private GestionJornadaCasoUso gestionJornadaCasoUso;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(contextoWeb).build();
    }

    @Test
    @DisplayName("Ciclo completo: proponer 2 fechas -> votar disponibilidad -> fijar fecha definitiva -> cerrar convocatoria y alineación")
    void debeCompletarFlujoCompletoDeJornadaCorrectamente() throws Exception {
        // 0. Verificar enfrentamiento semilla en PENDIENTE_FECHA
        mockMvc.perform(get(URL_BASE + "/" + ENFRENTAMIENTO_SEMILLA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("PENDIENTE_FECHA")))
                .andExpect(jsonPath("$.numeroJornada", is(1)));

        // 1. Proponer 2 fechas futuras -> pasa a VOTACION_DISPONIBILIDAD_ABIERTA
        LocalDateTime fecha1 = LocalDateTime.now().plusDays(7).withNano(0);
        LocalDateTime fecha2 = LocalDateTime.now().plusDays(8).withNano(0);

        String cuerpoPropuestas = String.format("""
                {
                  "fechas": ["%s", "%s"]
                }
                """, fecha1, fecha2);

        mockMvc.perform(post(URL_BASE + "/" + ENFRENTAMIENTO_SEMILLA_ID + "/propuestas-fecha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPropuestas))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("VOTACION_DISPONIBILIDAD_ABIERTA")))
                .andExpect(jsonPath("$.propuestasFecha", hasSize(2)));

        Enfrentamiento trasPropuesta = gestionJornadaCasoUso.obtenerEnfrentamientoPorId(ENFRENTAMIENTO_SEMILLA_ID);
        UUID propuestaElegidaId = trasPropuesta.obtenerPropuestasFecha().get(0).obtenerId();

        // 2. Votar disponibilidad de 6 jugadores (incluyendo actualización de voto de DUDA a DISPONIBLE)
        enviarVoto(CAPITAN_ID, propuestaElegidaId, "DUDA");
        enviarVoto(CAPITAN_ID, propuestaElegidaId, "DISPONIBLE"); // actualiza voto existente
        enviarVoto(JUGADOR_2_ID, propuestaElegidaId, "DISPONIBLE");
        enviarVoto(JUGADOR_3_ID, propuestaElegidaId, "DISPONIBLE");
        enviarVoto(JUGADOR_4_ID, propuestaElegidaId, "DISPONIBLE");
        enviarVoto(JUGADOR_5_ID, propuestaElegidaId, "DISPONIBLE");
        enviarVoto(JUGADOR_6_ID, propuestaElegidaId, "DISPONIBLE");
        enviarVoto(JUGADOR_7_ID, propuestaElegidaId, "NO_DISPONIBLE");

        Enfrentamiento trasVotos = gestionJornadaCasoUso.obtenerEnfrentamientoPorId(ENFRENTAMIENTO_SEMILLA_ID);
        assertEquals(7, trasVotos.obtenerDisponibilidades().size());

        // 3. Fijar fecha definitiva -> pasa a CONVOCATORIA_ABIERTA
        String cuerpoFechaDefinitiva = String.format("""
                {
                  "propuestaFechaElegidaId": "%s"
                }
                """, propuestaElegidaId);

        mockMvc.perform(put(URL_BASE + "/" + ENFRENTAMIENTO_SEMILLA_ID + "/fecha-definitiva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoFechaDefinitiva))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CONVOCATORIA_ABIERTA")))
                .andExpect(jsonPath("$.propuestasFecha[0].esSeleccionada", is(true)));

        // 4a. Intentar convocar a JUGADOR_7 que votó NO_DISPONIBLE -> ProblemDetail 422
        String cuerpoAlineacionInvalida = String.format("""
                {
                  "parejas": [
                    { "numeroPareja": 1, "jugador1Id": "%s", "jugador2Id": "%s" },
                    { "numeroPareja": 2, "jugador1Id": "%s", "jugador2Id": "%s" },
                    { "numeroPareja": 3, "jugador1Id": "%s", "jugador2Id": "%s" }
                  ]
                }
                """, CAPITAN_ID, JUGADOR_2_ID, JUGADOR_3_ID, JUGADOR_4_ID, JUGADOR_5_ID, JUGADOR_7_ID);

        mockMvc.perform(post(URL_BASE + "/" + ENFRENTAMIENTO_SEMILLA_ID + "/alineacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoAlineacionInvalida))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.title", is("Convocatoria o alineación inválida")))
                .andExpect(jsonPath("$.status", is(422)));

        // 4b. Intentar repetir a CAPITAN_ID en dos parejas -> ProblemDetail 422
        String cuerpoJugadorRepetido = String.format("""
                {
                  "parejas": [
                    { "numeroPareja": 1, "jugador1Id": "%s", "jugador2Id": "%s" },
                    { "numeroPareja": 2, "jugador1Id": "%s", "jugador2Id": "%s" },
                    { "numeroPareja": 3, "jugador1Id": "%s", "jugador2Id": "%s" }
                  ]
                }
                """, CAPITAN_ID, JUGADOR_2_ID, CAPITAN_ID, JUGADOR_4_ID, JUGADOR_5_ID, JUGADOR_6_ID);

        mockMvc.perform(post(URL_BASE + "/" + ENFRENTAMIENTO_SEMILLA_ID + "/alineacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoJugadorRepetido))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.title", is("Convocatoria o alineación inválida")));

        // 4c. Cerrar convocatoria y alineación con los 6 jugadores DISPONIBLES -> ALINEACION_CERRADA
        String cuerpoAlineacionValida = String.format("""
                {
                  "parejas": [
                    { "numeroPareja": 1, "jugador1Id": "%s", "jugador2Id": "%s" },
                    { "numeroPareja": 2, "jugador1Id": "%s", "jugador2Id": "%s" },
                    { "numeroPareja": 3, "jugador1Id": "%s", "jugador2Id": "%s" }
                  ]
                }
                """, CAPITAN_ID, JUGADOR_2_ID, JUGADOR_3_ID, JUGADOR_4_ID, JUGADOR_5_ID, JUGADOR_6_ID);

        mockMvc.perform(post(URL_BASE + "/" + ENFRENTAMIENTO_SEMILLA_ID + "/alineacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoAlineacionValida))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("ALINEACION_CERRADA")))
                .andExpect(jsonPath("$.partidos", hasSize(3)))
                .andExpect(jsonPath("$.partidos[0].numeroPareja", is(1)))
                .andExpect(jsonPath("$.partidos[1].numeroPareja", is(2)))
                .andExpect(jsonPath("$.partidos[2].numeroPareja", is(3)));

        Enfrentamiento finalEnBd = gestionJornadaCasoUso.obtenerEnfrentamientoPorId(ENFRENTAMIENTO_SEMILLA_ID);
        assertEquals(EstadoEnfrentamiento.ALINEACION_CERRADA, finalEnBd.obtenerEstado());
    }

    @Test
    @DisplayName("Devolver ProblemDetail 400 cuando se proponen fechas pasadas o cantidad distinta de 2")
    void debeRechazarFechasInvalidasConProblemDetail() throws Exception {
        LocalDateTime fechaPasada = LocalDateTime.now().minusDays(2).withNano(0);
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(5).withNano(0);

        String cuerpoInvalido = String.format("""
                {
                  "fechas": ["%s", "%s"]
                }
                """, fechaPasada, fechaFutura);

        mockMvc.perform(post(URL_BASE + "/" + ENFRENTAMIENTO_SEMILLA_ID + "/propuestas-fecha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Datos de petición inválidos")))
                .andExpect(jsonPath("$.status", is(400)));
    }

    private void enviarVoto(UUID jugadorId, UUID propuestaId, String disponibilidad) throws Exception {
        String cuerpo = String.format("""
                {
                  "jugadorId": "%s",
                  "propuestaFechaId": "%s",
                  "tipoDisponibilidad": "%s"
                }
                """, jugadorId, propuestaId, disponibilidad);

        mockMvc.perform(post(URL_BASE + "/" + ENFRENTAMIENTO_SEMILLA_ID + "/disponibilidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk());
    }
}
