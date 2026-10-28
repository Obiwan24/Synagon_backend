package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.Enfrentamiento;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DisplayName("Pruebas de integración de Actas, Clasificación en tiempo real y Estadísticas de Jugadores")
class ControladorActasYEstadisticasIntegracionTest {

    private static final UUID DIVISION_SEVILLA_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID EQUIPO_TRIANA_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");
    private static final UUID EQUIPO_REMEDIOS_ID = UUID.fromString("50000000-0000-0000-0000-000000000002");
    private static final UUID EQUIPO_NERVION_ID = UUID.fromString("50000000-0000-0000-0000-000000000003");
    private static final UUID ENFRENTAMIENTO_PENDIENTE_ID = UUID.fromString("70000000-0000-0000-0000-000000000001");

    private static final UUID J1 = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID J2 = UUID.fromString("40000000-0000-0000-0000-000000000002");
    private static final UUID J3 = UUID.fromString("40000000-0000-0000-0000-000000000003");
    private static final UUID J4 = UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final UUID J5 = UUID.fromString("40000000-0000-0000-0000-000000000005");
    private static final UUID J6 = UUID.fromString("40000000-0000-0000-0000-000000000006");

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
    @DisplayName("Clasificación histórica inicial ordena por puntos y desempata por diferencia de partidos/sets/juegos")
    void debeCalcularClasificacionHistoricaConDesempateExacto() throws Exception {
        mockMvc.perform(get("/api/divisiones/" + DIVISION_SEVILLA_ID + "/clasificacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                // 1º Real Club Pádel Triana: 6 puntos (2G-0P), partidos 4-2 (+2), sets 8-6 (+2), juegos 73-70 (+3)
                .andExpect(jsonPath("$[0].posicion", is(1)))
                .andExpect(jsonPath("$[0].equipoId", is(EQUIPO_TRIANA_ID.toString())))
                .andExpect(jsonPath("$[0].puntos", is(6)))
                .andExpect(jsonPath("$[0].enfrentamientosJugados", is(2)))
                .andExpect(jsonPath("$[0].enfrentamientosGanados", is(2)))
                .andExpect(jsonPath("$[0].enfrentamientosPerdidos", is(0)))
                .andExpect(jsonPath("$[0].partidosGanados", is(4)))
                .andExpect(jsonPath("$[0].partidosPerdidos", is(2)))
                .andExpect(jsonPath("$[0].diferenciaPartidos", is(2)))
                .andExpect(jsonPath("$[0].setsAFavor", is(8)))
                .andExpect(jsonPath("$[0].setsEnContra", is(6)))
                .andExpect(jsonPath("$[0].diferenciaSets", is(2)))
                .andExpect(jsonPath("$[0].diferenciaJuegos", is(3)))
                // 2º Club Pádel Los Remedios: 0 puntos, diffPartidos -1, diffSets -1, diffJuegos -1 (34-35)
                .andExpect(jsonPath("$[1].posicion", is(2)))
                .andExpect(jsonPath("$[1].equipoId", is(EQUIPO_REMEDIOS_ID.toString())))
                .andExpect(jsonPath("$[1].puntos", is(0)))
                .andExpect(jsonPath("$[1].diferenciaPartidos", is(-1)))
                .andExpect(jsonPath("$[1].diferenciaSets", is(-1)))
                .andExpect(jsonPath("$[1].diferenciaJuegos", is(-1)))
                // 3º Sevilla Pádel Club Nervión: 0 puntos, diffPartidos -1, diffSets -1, diffJuegos -2 (36-38)
                .andExpect(jsonPath("$[2].posicion", is(3)))
                .andExpect(jsonPath("$[2].equipoId", is(EQUIPO_NERVION_ID.toString())))
                .andExpect(jsonPath("$[2].puntos", is(0)))
                .andExpect(jsonPath("$[2].diferenciaPartidos", is(-1)))
                .andExpect(jsonPath("$[2].diferenciaSets", is(-1)))
                .andExpect(jsonPath("$[2].diferenciaJuegos", is(-2)));
    }

    @Test
    @DisplayName("Estadísticas históricas de los 8 jugadores de mi equipo se calculan con exactitud")
    void debeCalcularEstadisticasDeTodosLosMiembrosDelEquipo() throws Exception {
        mockMvc.perform(get("/api/equipos/" + EQUIPO_TRIANA_ID + "/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                // Álvaro (2 apuntados, 2 jugados, 2 ganados, 0 perdidos, sets 4-0, juegos 24-14)
                .andExpect(jsonPath("$[0].jugadorId", is(J1.toString())))
                .andExpect(jsonPath("$[0].partidosApuntados", is(2)))
                .andExpect(jsonPath("$[0].partidosJugados", is(2)))
                .andExpect(jsonPath("$[0].partidosGanados", is(2)))
                .andExpect(jsonPath("$[0].partidosPerdidos", is(0)))
                .andExpect(jsonPath("$[0].setsGanados", is(4)))
                .andExpect(jsonPath("$[0].setsPerdidos", is(0)))
                .andExpect(jsonPath("$[0].juegosGanados", is(24)))
                .andExpect(jsonPath("$[0].juegosPerdidos", is(14)));
    }

    @Test
    @DisplayName("Registrar acta valida estado ALINEACION_CERRADA, rechaza tanteos ilegales y actualiza clasificación y estadísticas en tiempo real")
    void debeRegistrarActaYActualizarClasificacionYEstadisticasEnTiempoReal() throws Exception {
        // 1. Intentar subir acta antes de cerrar alineación -> 409 Conflict ProblemDetail
        String cuerpoActaValida = """
                {
                  "urlFotoActa": "https://cdn.synagon.es/actas/nueva_acta_j1.jpg",
                  "resultadosTresPartidos": [
                    { "numeroPareja": 1, "set1Local": 6, "set1Visitante": 2, "set2Local": 6, "set2Visitante": 4 },
                    { "numeroPareja": 2, "set1Local": 7, "set1Visitante": 6, "set2Local": 3, "set2Visitante": 6, "set3Local": 6, "set3Visitante": 4 },
                    { "numeroPareja": 3, "set1Local": 4, "set1Visitante": 6, "set2Local": 5, "set2Visitante": 7 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/enfrentamientos/" + ENFRENTAMIENTO_PENDIENTE_ID + "/acta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoActaValida))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Violación de regla de negocio")));

        // 2. Preparar enfrentamiento hasta ALINEACION_CERRADA
        prepararEnfrentamientoConAlineacionCerrada();

        // 3. Intentar subir acta con tanteo ilegal (6-5 en el primer set) -> 422 ProblemDetail ResultadoPadelIlegalExcepcion
        String cuerpoActaIlegal = """
                {
                  "urlFotoActa": "https://cdn.synagon.es/actas/acta_ilegal.jpg",
                  "resultadosTresPartidos": [
                    { "numeroPareja": 1, "set1Local": 6, "set1Visitante": 5, "set2Local": 6, "set2Visitante": 4 },
                    { "numeroPareja": 2, "set1Local": 6, "set1Visitante": 3, "set2Local": 6, "set2Visitante": 4 },
                    { "numeroPareja": 3, "set1Local": 4, "set1Visitante": 6, "set2Local": 5, "set2Visitante": 7 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/enfrentamientos/" + ENFRENTAMIENTO_PENDIENTE_ID + "/acta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoActaIlegal))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.title", is("Resultado de pádel no reglamentario")));

        // 4. Registrar acta reglamentaria -> 200 OK (FINALIZADO_CONFIRMADO, 2-1 para Triana)
        mockMvc.perform(post("/api/enfrentamientos/" + ENFRENTAMIENTO_PENDIENTE_ID + "/acta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoActaValida))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("FINALIZADO_CONFIRMADO")))
                .andExpect(jsonPath("$.actaConfirmada", is(true)))
                .andExpect(jsonPath("$.puntosLocal", is(2)))
                .andExpect(jsonPath("$.puntosVisitante", is(1)))
                .andExpect(jsonPath("$.urlFotoActa", is("https://cdn.synagon.es/actas/nueva_acta_j1.jpg")));

        // 5. Verificar actualización inmediata de clasificación (Triana sube a 9 puntos y 3 enfrentamientos ganados)
        mockMvc.perform(get("/api/divisiones/" + DIVISION_SEVILLA_ID + "/clasificacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].equipoId", is(EQUIPO_TRIANA_ID.toString())))
                .andExpect(jsonPath("$[0].puntos", is(9)))
                .andExpect(jsonPath("$[0].enfrentamientosJugados", is(3)))
                .andExpect(jsonPath("$[0].enfrentamientosGanados", is(3)))
                .andExpect(jsonPath("$[0].partidosGanados", is(6)))
                .andExpect(jsonPath("$[0].partidosPerdidos", is(3)));

        // 6. Verificar actualización inmediata de estadísticas de Álvaro (J1: pasa a 3 apuntados, 3 jugados, 3 ganados)
        mockMvc.perform(get("/api/equipos/" + EQUIPO_TRIANA_ID + "/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jugadorId", is(J1.toString())))
                .andExpect(jsonPath("$[0].partidosApuntados", is(3)))
                .andExpect(jsonPath("$[0].partidosJugados", is(3)))
                .andExpect(jsonPath("$[0].partidosGanados", is(3)));
    }

    private void prepararEnfrentamientoConAlineacionCerrada() throws Exception {
        LocalDateTime f1 = LocalDateTime.now().plusDays(5).withNano(0);
        LocalDateTime f2 = LocalDateTime.now().plusDays(6).withNano(0);

        gestionJornadaCasoUso.proponerFechas(ENFRENTAMIENTO_PENDIENTE_ID, List.of(f1, f2));
        Enfrentamiento conPropuestas = gestionJornadaCasoUso.obtenerEnfrentamientoPorId(ENFRENTAMIENTO_PENDIENTE_ID);
        UUID propuestaId = conPropuestas.obtenerPropuestasFecha().get(0).obtenerId();

        for (UUID jugador : List.of(J1, J2, J3, J4, J5, J6)) {
            gestionJornadaCasoUso.votarDisponibilidad(
                    ENFRENTAMIENTO_PENDIENTE_ID,
                    jugador,
                    propuestaId,
                    com.synagon.api.dominio.modelos.EstadoDisponibilidad.DISPONIBLE
            );
        }

        gestionJornadaCasoUso.fijarFechaDefinitiva(ENFRENTAMIENTO_PENDIENTE_ID, propuestaId);

        String cuerpoAlineacion = String.format("""
                {
                  "parejas": [
                    { "numeroPareja": 1, "jugador1Id": "%s", "jugador2Id": "%s" },
                    { "numeroPareja": 2, "jugador1Id": "%s", "jugador2Id": "%s" },
                    { "numeroPareja": 3, "jugador1Id": "%s", "jugador2Id": "%s" }
                  ]
                }
                """, J1, J2, J3, J4, J5, J6);

        mockMvc.perform(post("/api/v1/jornadas/enfrentamientos/" + ENFRENTAMIENTO_PENDIENTE_ID + "/alineacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoAlineacion))
                .andExpect(status().isOk());
    }
}
