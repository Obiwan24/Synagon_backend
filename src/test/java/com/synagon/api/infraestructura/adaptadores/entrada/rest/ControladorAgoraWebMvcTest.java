package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.Liga;
import com.synagon.api.dominio.modelos.ModoSedeLiga;
import com.synagon.api.dominio.modelos.ResumenMembresiaUsuario;
import com.synagon.api.dominio.modelos.RolMembresia;
import com.synagon.api.dominio.modelos.RolUsuario;
import com.synagon.api.dominio.modelos.Usuario;
import com.synagon.api.dominio.puertos.entrada.AgoraYLigasCasoUso;
import com.synagon.api.dominio.puertos.entrada.RegistroUsuarioCasoUso;
import com.synagon.api.infraestructura.configuracion.ConfiguracionSeguridad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ControladorAgora.class)
@Import({ConfiguracionSeguridad.class, ManejadorGlobalExcepciones.class})
@DisplayName("Pruebas WebMvc de ControladorAgora y ConfiguracionSeguridad para cliente KMP")
class ControladorAgoraWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgoraYLigasCasoUso agoraYLigasCasoUso;

    @MockitoBean
    private RegistroUsuarioCasoUso registroUsuarioCasoUso;

    @Test
    @DisplayName("GET /api/v1/agora/usuarios/{usuarioId}/membresias devuelve las 3 membresías multi-rol sin bloqueo de seguridad")
    void debeDevolverMembresiasMultiRolEnAgora() throws Exception {
        UUID usuarioId = UUID.fromString("40000000-0000-0000-0000-000000000001");
        UUID liga1 = UUID.fromString("10000000-0000-0000-0000-000000000001");
        UUID liga2 = UUID.fromString("10000000-0000-0000-0000-000000000002");
        UUID liga3 = UUID.fromString("10000000-0000-0000-0000-000000000003");
        UUID equipoBetis = UUID.fromString("50000000-0000-0000-0000-000000000001");
        UUID equipoEspartanos = UUID.fromString("50000000-0000-0000-0000-000000000301");

        when(agoraYLigasCasoUso.obtenerMembresiasUsuario(usuarioId)).thenReturn(List.of(
                new ResumenMembresiaUsuario(
                        liga2, "Liga Nocturna Aljarafe", null,
                        "Presidencia y dirección de liga", "Sede única: Club Pádel Mairena del Aljarafe",
                        3, RolMembresia.PRESIDENTE
                ),
                new ResumenMembresiaUsuario(
                        liga1, "Liga Interclubes Sevilla", equipoBetis,
                        "Real Betis Pádel Team", "Sede local: Ciudad Deportiva Luis del Sol",
                        3, RolMembresia.CAPITAN
                ),
                new ResumenMembresiaUsuario(
                        liga3, "Torneo Otoño Híspalis", equipoEspartanos,
                        "Espartanos de Triana", "Sede local: Club Pádel Triana Sevilla",
                        3, RolMembresia.JUGADOR
                )
        ));

        mockMvc.perform(get("/api/v1/agora/usuarios/" + usuarioId + "/membresias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].rol", is("PRESIDENTE")))
                .andExpect(jsonPath("$[0].equipoId", is(nullValue())))
                .andExpect(jsonPath("$[0].nombreLiga", is("Liga Nocturna Aljarafe")))
                .andExpect(jsonPath("$[1].rol", is("CAPITAN")))
                .andExpect(jsonPath("$[1].nombreEquipoOSubtitulo", is("Real Betis Pádel Team")))
                .andExpect(jsonPath("$[2].rol", is("JUGADOR")))
                .andExpect(jsonPath("$[2].nombreEquipoOSubtitulo", is("Espartanos de Triana")));
    }

    @Test
    @DisplayName("GET /api/v1/agora/ligas/validar-codigo/{codigo} y POST /api/v1/agora/equipos/inscribir son accesibles con CORS")
    void debeValidarCodigoEInscribirEquipoDesdeKmp() throws Exception {
        UUID ligaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID equipoId = UUID.randomUUID();

        Liga liga = new Liga(
                ligaId, "Liga Interclubes Sevilla", "Descripción", "SEVILLA-2026",
                null, 3, ModoSedeLiga.SEDE_LOCAL_POR_EQUIPO, null,
                3, 1, 0, true, true, "Normativa oficial"
        );

        when(agoraYLigasCasoUso.validarCodigoInvitacionLiga("SEVILLA-2026")).thenReturn(liga);
        when(agoraYLigasCasoUso.inscribirEquipoConCodigo(
                eq(usuarioId), eq("SEVILLA-2026"), eq("Real Betis Pádel Team"), eq("Ciudad Deportiva Luis del Sol")
        )).thenReturn(new ResumenMembresiaUsuario(
                ligaId, "Liga Interclubes Sevilla", equipoId,
                "Real Betis Pádel Team", "Sede local: Ciudad Deportiva Luis del Sol",
                3, RolMembresia.CAPITAN
        ));

        mockMvc.perform(options("/api/v1/agora/equipos/inscribir")
                        .header("Origin", "http://localhost:8081")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));

        mockMvc.perform(get("/api/v1/agora/ligas/validar-codigo/SEVILLA-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modoSede", is("SEDE_LOCAL_POR_EQUIPO")))
                .andExpect(jsonPath("$.requiereSedeEquipo", is(true)))
                .andExpect(jsonPath("$.partidosPorJornada", is(3)));

        String jsonInscripcion = String.format("""
                {
                  "usuarioId": "%s",
                  "codigoInvitacion": "SEVILLA-2026",
                  "nombreEquipo": "Real Betis Pádel Team",
                  "clubSedeOpcional": "Ciudad Deportiva Luis del Sol"
                }
                """, usuarioId);

        mockMvc.perform(post("/api/v1/agora/equipos/inscribir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInscripcion))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipoId", is(equipoId.toString())))
                .andExpect(jsonPath("$.rol", is("CAPITAN")));
    }

    @Test
    @DisplayName("POST /api/v1/usuarios/{usuarioId}/primer-acceso-password y PUT /api/v1/usuarios/{usuarioId}/perfil responden 200 OK")
    void debeGestionarPrimerAccesoYActualizarPerfilViaRest() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuarioActualizado = new Usuario(
                usuarioId, "Álvaro", "Iglesias Méndez", "alvaro.capitan@synagon.es",
                "28765432A", "+34600100001", "$2a$10$hash", RolUsuario.CAPITAN,
                false, true, true, LocalDateTime.now(), null, LocalDateTime.now()
        );

        when(registroUsuarioCasoUso.gestionarPrimerAccesoPassword(eq(usuarioId), eq(false), isNull()))
                .thenReturn(usuarioActualizado);
        when(registroUsuarioCasoUso.actualizarPerfilUsuario(
                eq(usuarioId), eq("Álvaro Iglesias Méndez"), eq("+34600100001"), eq("28765432A")
        )).thenReturn(usuarioActualizado);

        mockMvc.perform(post("/api/v1/usuarios/" + usuarioId + "/primer-acceso-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deseaCambiar\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiereCambioPassword", is(false)))
                .andExpect(jsonPath("$.primerAccesoCompletado", is(true)));

        mockMvc.perform(put("/api/v1/usuarios/" + usuarioId + "/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombreCompleto": "Álvaro Iglesias Méndez",
                                  "telefono": "+34600100001",
                                  "dni": "28765432A"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCompleto", is("Álvaro Iglesias Méndez")))
                .andExpect(jsonPath("$.dni", is("28765432A")));
    }
}
