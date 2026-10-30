package com.synagon.api.dominio.modelos;

import com.synagon.api.dominio.excepciones.ConvocatoriaInvalidaExcepcion;
import com.synagon.api.dominio.excepciones.ReglaNegocioExcepcion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Modelo Enfrentamiento")
class EnfrentamientoTest {

    private final UUID divisionId = UUID.randomUUID();
    private final UUID localId = UUID.randomUUID();
    private final UUID visitanteId = UUID.randomUUID();

    @Test
    @DisplayName("Negociación de fechas con exactamente 2 propuestas de fechas orden 1 y 2")
    void debePermitirProponerExactamenteDosFechas() {
        Enfrentamiento enfrentamiento = Enfrentamiento.crear(divisionId, localId, visitanteId, 1);

        PropuestaFecha p1 = PropuestaFecha.crearNueva(enfrentamiento.obtenerId(), LocalDateTime.now().plusDays(2), 1);
        PropuestaFecha p2 = PropuestaFecha.crearNueva(enfrentamiento.obtenerId(), LocalDateTime.now().plusDays(3), 2);

        Enfrentamiento conPropuestas = enfrentamiento.ofrecerPropuestasFecha(p1, p2);

        assertEquals(2, conPropuestas.obtenerPropuestasFecha().size());
        assertEquals(EstadoEnfrentamiento.PENDIENTE_FECHA, conPropuestas.obtenerEstado());
    }

    @Test
    @DisplayName("Rechazar propuestas con órdenes incorrectos")
    void debeRechazarPropuestasConOrdenesRepetidos() {
        Enfrentamiento enfrentamiento = Enfrentamiento.crear(divisionId, localId, visitanteId, 1);

        PropuestaFecha p1 = PropuestaFecha.crearNueva(enfrentamiento.obtenerId(), LocalDateTime.now().plusDays(2), 1);
        PropuestaFecha p2 = new PropuestaFecha(UUID.randomUUID(), enfrentamiento.obtenerId(), LocalDateTime.now().plusDays(3), 1, false);

        assertThrows(IllegalArgumentException.class, () ->
                enfrentamiento.ofrecerPropuestasFecha(p1, p2)
        );
    }

    @Test
    @DisplayName("Validar convocatoria reglamentaria de 6 jugadores distintos por equipo")
    void debeAceptarConvocatoriaValidaDeSeisJugadoresPorEquipo() {
        Enfrentamiento enfrentamiento = Enfrentamiento.crear(divisionId, localId, visitanteId, 1);
        List<PartidoPareja> partidos = crearPartidosValidos(enfrentamiento.obtenerId());

        assertDoesNotThrow(() -> enfrentamiento.validarAlineacion(partidos));
    }

    @Test
    @DisplayName("Rechazar si un jugador local se repite en dos parejas de la jornada")
    void debeRechazarJugadorRepetidoEnMismoEquipo() {
        Enfrentamiento enfrentamiento = Enfrentamiento.crear(divisionId, localId, visitanteId, 1);

        UUID jugadorRepetido = UUID.randomUUID();

        PartidoPareja p1 = PartidoPareja.crear(enfrentamiento.obtenerId(), 1, jugadorRepetido, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        PartidoPareja p2 = PartidoPareja.crear(enfrentamiento.obtenerId(), 2, jugadorRepetido, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        PartidoPareja p3 = PartidoPareja.crear(enfrentamiento.obtenerId(), 3, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        assertThrows(ConvocatoriaInvalidaExcepcion.class, () ->
                enfrentamiento.validarAlineacion(List.of(p1, p2, p3))
        );
    }

    @Test
    @DisplayName("Confirmar acta y cerrar enfrentamiento calculando puntos")
    void debeConfirmarActaYCalcularPuntos() {
        Enfrentamiento enfrentamiento = Enfrentamiento.crear(divisionId, localId, visitanteId, 1);
        List<PartidoPareja> partidos = crearPartidosValidos(enfrentamiento.obtenerId());

        // Pareja 1 gana local 2-0
        PartidoPareja part1 = partidos.get(0).registrarResultado(6, 4, 6, 3, null, null);
        // Pareja 2 gana local 2-1
        PartidoPareja part2 = partidos.get(1).registrarResultado(6, 2, 4, 6, 7, 5);
        // Pareja 3 gana visitante 0-2
        PartidoPareja part3 = partidos.get(2).registrarResultado(3, 6, 2, 6, null, null);

        Enfrentamiento cerrado = enfrentamiento.confirmarActa(List.of(part1, part2, part3));

        assertTrue(cerrado.esActaConfirmada());
        assertEquals(EstadoEnfrentamiento.FINALIZADO, cerrado.obtenerEstado());
        assertEquals(2, cerrado.obtenerPuntosLocal());
        assertEquals(1, cerrado.obtenerPuntosVisitante());
    }

    @Test
    @DisplayName("Sede efectiva de juego devuelve la sede alternativa si existe o la sede oficial en caso contrario")
    void debeCalcularSedeEfectivaJuegoYConservarSedeOficial() {
        Enfrentamiento inicial = new Enfrentamiento(
                UUID.randomUUID(),
                divisionId,
                localId,
                visitanteId,
                1,
                EstadoEnfrentamiento.FECHA_FIJADA,
                LocalDateTime.now().plusDays(3),
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

        assertNull(inicial.obtenerSedeAlternativaJornada());
        assertEquals("Ciudad Deportiva Luis del Sol", inicial.obtenerSedeEfectivaJuego());

        Enfrentamiento conAlternativa = inicial.actualizarSedeAlternativaJornada("  Pádel Indoor Cartuja  ");
        assertEquals("Ciudad Deportiva Luis del Sol", conAlternativa.obtenerPistas());
        assertEquals("Pádel Indoor Cartuja", conAlternativa.obtenerSedeAlternativaJornada());
        assertEquals("Pádel Indoor Cartuja", conAlternativa.obtenerSedeEfectivaJuego());

        Enfrentamiento restaurado = conAlternativa.actualizarSedeAlternativaJornada("   ");
        assertNull(restaurado.obtenerSedeAlternativaJornada());
        assertEquals("Ciudad Deportiva Luis del Sol", restaurado.obtenerSedeEfectivaJuego());
    }

    @Test
    @DisplayName("No permite cambiar la sede alternativa si el enfrentamiento ya está finalizado")
    void debeRechazarCambioDeSedeEnEnfrentamientoFinalizado() {
        Enfrentamiento enfrentamiento = Enfrentamiento.crear(divisionId, localId, visitanteId, 1);
        List<PartidoPareja> partidos = crearPartidosValidos(enfrentamiento.obtenerId());
        PartidoPareja part1 = partidos.get(0).registrarResultado(6, 4, 6, 3, null, null);
        PartidoPareja part2 = partidos.get(1).registrarResultado(6, 2, 6, 4, null, null);
        PartidoPareja part3 = partidos.get(2).registrarResultado(3, 6, 2, 6, null, null);
        Enfrentamiento finalizado = enfrentamiento.confirmarActa(List.of(part1, part2, part3));

        assertThrows(ReglaNegocioExcepcion.class, () ->
                finalizado.actualizarSedeAlternativaJornada("Pistas Cubiertas Aljarafe")
        );
    }

    private List<PartidoPareja> crearPartidosValidos(UUID enfrentamientoId) {
        List<UUID> locales = new ArrayList<>();
        List<UUID> visitantes = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            locales.add(UUID.randomUUID());
            visitantes.add(UUID.randomUUID());
        }

        return List.of(
                PartidoPareja.crear(enfrentamientoId, 1, locales.get(0), locales.get(1), visitantes.get(0), visitantes.get(1)),
                PartidoPareja.crear(enfrentamientoId, 2, locales.get(2), locales.get(3), visitantes.get(2), visitantes.get(3)),
                PartidoPareja.crear(enfrentamientoId, 3, locales.get(4), locales.get(5), visitantes.get(4), visitantes.get(5))
        );
    }
}
