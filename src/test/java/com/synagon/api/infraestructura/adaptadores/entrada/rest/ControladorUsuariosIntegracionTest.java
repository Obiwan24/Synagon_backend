package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.modelos.RolUsuario;
import com.synagon.api.infraestructura.adaptadores.salida.persistencia.EntidadMiembroEquipo;
import com.synagon.api.infraestructura.adaptadores.salida.persistencia.EntidadUsuario;
import com.synagon.api.infraestructura.adaptadores.salida.persistencia.RepositorioJpaMiembroEquipo;
import com.synagon.api.infraestructura.adaptadores.salida.persistencia.RepositorioJpaUsuario;
import com.synagon.api.infraestructura.adaptadores.salida.persistencia.RepositorioUsuarioPostgres;
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

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DisplayName("Pruebas de integración de Seguridad, CORS y Registro de Usuarios (Fase 4)")
class ControladorUsuariosIntegracionTest {

    private static final String URL_REGISTRO = "/api/v1/usuarios/registro";

    @Autowired
    private WebApplicationContext contextoWeb;

    @Autowired
    private RepositorioJpaUsuario repositorioJpaUsuario;

    @Autowired
    private RepositorioJpaMiembroEquipo repositorioJpaMiembroEquipo;

    @Autowired
    private PasswordEncoder codificadorContrasena;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvcConSeguridad() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(contextoWeb)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Permite registro público sin JWT, cifra la contraseña con BCrypt y guarda en usuarios y miembros_equipo con rol JUGADOR")
    void debeRegistrarNuevoUsuarioPublicamenteConBcryptYMembresiaPorDefecto() throws Exception {
        String cuerpoPeticion = """
                {
                  "nombreCompleto": "Elena Jiménez Vargas",
                  "email": "Elena.Jimenez@Synagon.es",
                  "telefono": "+34655443322",
                  "password": "miClaveSegura2026",
                  "aceptoPrivacidad": true
                }
                """;

        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPeticion))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombreCompleto", is("Elena Jiménez Vargas")))
                .andExpect(jsonPath("$.nombre", is("Elena")))
                .andExpect(jsonPath("$.apellidos", is("Jiménez Vargas")))
                .andExpect(jsonPath("$.email", is("elena.jimenez@synagon.es")))
                .andExpect(jsonPath("$.telefono", is("+34655443322")))
                .andExpect(jsonPath("$.rol", is("JUGADOR")))
                .andExpect(jsonPath("$.equipoId", is(RepositorioUsuarioPostgres.EQUIPO_PRUEBAS_POR_DEFECTO_ID.toString())))
                .andExpect(jsonPath("$.aceptoPrivacidad", is(true)));

        // Verificar persistencia real en tabla usuarios con BCrypt
        EntidadUsuario entidadGuardada = repositorioJpaUsuario
                .buscarPorEmailIgnorandoMayusculas("elena.jimenez@synagon.es")
                .orElseThrow();

        assertEquals(RolUsuario.JUGADOR, entidadGuardada.obtenerRol());
        assertTrue(entidadGuardada.haAceptadoPrivacidad());
        assertNotEquals("miClaveSegura2026", entidadGuardada.obtenerContrasenaHash());
        assertTrue(entidadGuardada.obtenerContrasenaHash().startsWith("$2"));
        assertTrue(codificadorContrasena.matches("miClaveSegura2026", entidadGuardada.obtenerContrasenaHash()));

        // Verificar alta automática en miembros_equipo en el equipo por defecto de pruebas
        List<EntidadMiembroEquipo> membresias = repositorioJpaMiembroEquipo.findByUsuarioId(entidadGuardada.obtenerId());
        assertEquals(1, membresias.size());
        assertEquals(RepositorioUsuarioPostgres.EQUIPO_PRUEBAS_POR_DEFECTO_ID, membresias.get(0).obtenerEquipoId());
        assertFalse(membresias.get(0).esCapitan());
        assertTrue(membresias.get(0).estaActivo());
    }

    @Test
    @DisplayName("Devuelve HTTP 409 Conflict cuando el email ya está registrado en la base de datos")
    void debeDevolver409CuandoEmailYaExiste() throws Exception {
        String cuerpoDuplicadoSemilla = """
                {
                  "nombreCompleto": "Álvaro Duplicado",
                  "email": "ALVARO.CAPITAN@synagon.es",
                  "password": "claveValida123",
                  "aceptoPrivacidad": true
                }
                """;

        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoDuplicadoSemilla))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.title", is("Correo electrónico ya registrado")))
                .andExpect(jsonPath("$.detail", containsString("alvaro.capitan@synagon.es")));
    }

    @Test
    @DisplayName("Devuelve HTTP 400 Bad Request descriptivo cuando aceptoPrivacidad es false o no se envía (RGPD)")
    void debeRechazarCon400CuandoNoAceptaPrivacidadRgpd() throws Exception {
        String cuerpoSinConsentimiento = """
                {
                  "nombreCompleto": "Roberto Díaz",
                  "email": "roberto.diaz@synagon.es",
                  "password": "claveValida123",
                  "aceptoPrivacidad": false
                }
                """;

        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSinConsentimiento))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", containsString("RGPD")))
                .andExpect(jsonPath("$.erroresCampos.aceptoPrivacidad", containsString("RGPD")));
    }

    @Test
    @DisplayName("Devuelve HTTP 400 Bad Request ante nombre corto, formato email inválido o password menor a 6 caracteres")
    void debeValidarFormatoDeCamposEnRegistro() throws Exception {
        String cuerpoInvalido = """
                {
                  "nombreCompleto": "Ab",
                  "email": "formato_invalido",
                  "password": "12345",
                  "aceptoPrivacidad": true
                }
                """;

        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.erroresCampos.nombreCompleto", notNullValue()))
                .andExpect(jsonPath("$.erroresCampos.email", notNullValue()))
                .andExpect(jsonPath("$.erroresCampos.password", notNullValue()));
    }

    @Test
    @DisplayName("Permite peticiones preflight CORS y acceso sin autenticación a recursos de prueba para clientes Android/iOS")
    void debePermitirCorsYRecursosDePruebaSinAutenticacion() throws Exception {
        mockMvc.perform(options(URL_REGISTRO)
                        .header("Origin", "http://localhost:8081")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));

        mockMvc.perform(get("/api/divisiones/30000000-0000-0000-0000-000000000001/clasificacion"))
                .andExpect(status().isOk());
    }
}
