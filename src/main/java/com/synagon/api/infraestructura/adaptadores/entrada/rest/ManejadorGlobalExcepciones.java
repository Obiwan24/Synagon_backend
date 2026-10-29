package com.synagon.api.infraestructura.adaptadores.entrada.rest;

import com.synagon.api.dominio.excepciones.ConvocatoriaInvalidaExcepcion;
import com.synagon.api.dominio.excepciones.EmailYaRegistradoExcepcion;
import com.synagon.api.dominio.excepciones.RecursoNoEncontradoExcepcion;
import com.synagon.api.dominio.excepciones.ReglaNegocioExcepcion;
import com.synagon.api.dominio.excepciones.ResultadoPadelIlegalExcepcion;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones que traduce errores de validación y violaciones de
 * reglas de negocio al estándar RFC 9457 / RFC 7807 ({@link ProblemDetail}).
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(RecursoNoEncontradoExcepcion.class)
    public ProblemDetail manejarRecursoNoEncontrado(
            RecursoNoEncontradoExcepcion excepcion,
            HttpServletRequest peticion
    ) {
        return construirDetalleProblema(
                HttpStatus.NOT_FOUND,
                "Recurso no encontrado",
                excepcion.getMessage(),
                "https://synagon.com/errores/recurso-no-encontrado",
                peticion
        );
    }

    @ExceptionHandler(EmailYaRegistradoExcepcion.class)
    public ProblemDetail manejarEmailYaRegistrado(
            EmailYaRegistradoExcepcion excepcion,
            HttpServletRequest peticion
    ) {
        return construirDetalleProblema(
                HttpStatus.CONFLICT,
                "Correo electrónico ya registrado",
                excepcion.getMessage(),
                "https://synagon.com/errores/email-ya-registrado",
                peticion
        );
    }

    @ExceptionHandler(ConvocatoriaInvalidaExcepcion.class)
    public ProblemDetail manejarConvocatoriaInvalida(
            ConvocatoriaInvalidaExcepcion excepcion,
            HttpServletRequest peticion
    ) {
        return construirDetalleProblema(
                HttpStatusCode.valueOf(422),
                "Convocatoria o alineación inválida",
                excepcion.getMessage(),
                "https://synagon.com/errores/convocatoria-invalida",
                peticion
        );
    }

    @ExceptionHandler(ResultadoPadelIlegalExcepcion.class)
    public ProblemDetail manejarResultadoPadelIlegal(
            ResultadoPadelIlegalExcepcion excepcion,
            HttpServletRequest peticion
    ) {
        return construirDetalleProblema(
                HttpStatusCode.valueOf(422),
                "Resultado de pádel no reglamentario",
                excepcion.getMessage(),
                "https://synagon.com/errores/resultado-padel-ilegal",
                peticion
        );
    }

    @ExceptionHandler(ReglaNegocioExcepcion.class)
    public ProblemDetail manejarReglaNegocio(
            ReglaNegocioExcepcion excepcion,
            HttpServletRequest peticion
    ) {
        return construirDetalleProblema(
                HttpStatus.CONFLICT,
                "Violación de regla de negocio",
                excepcion.getMessage(),
                "https://synagon.com/errores/regla-negocio",
                peticion
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail manejarArgumentoIlegal(
            IllegalArgumentException excepcion,
            HttpServletRequest peticion
    ) {
        return construirDetalleProblema(
                HttpStatus.BAD_REQUEST,
                "Parámetro de entrada inválido",
                excepcion.getMessage(),
                "https://synagon.com/errores/argumento-invalido",
                peticion
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacionPeticion(
            MethodArgumentNotValidException excepcion,
            HttpServletRequest peticion
    ) {
        Map<String, String> erroresCampos = new LinkedHashMap<>();
        for (FieldError error : excepcion.getBindingResult().getFieldErrors()) {
            erroresCampos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        String mensajeDescriptivo = erroresCampos.isEmpty()
                ? "Uno o más campos enviados no superan las validaciones requeridas."
                : String.join(" ", erroresCampos.values());

        ProblemDetail detalle = construirDetalleProblema(
                HttpStatus.BAD_REQUEST,
                "Datos de petición inválidos",
                mensajeDescriptivo,
                "https://synagon.com/errores/validacion-entrada",
                peticion
        );
        detalle.setProperty("erroresCampos", erroresCampos);
        return detalle;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail manejarFormatoIncorrecto(
            Exception excepcion,
            HttpServletRequest peticion
    ) {
        return construirDetalleProblema(
                HttpStatus.BAD_REQUEST,
                "Formato de petición inválido",
                "El cuerpo JSON o los parámetros de la URL no tienen un formato válido.",
                "https://synagon.com/errores/formato-invalido",
                peticion
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail manejarIntegridadDatos(
            DataIntegrityViolationException excepcion,
            HttpServletRequest peticion
    ) {
        return construirDetalleProblema(
                HttpStatus.CONFLICT,
                "Conflicto de integridad de datos",
                "La operación infringe una restricción de integridad en la base de datos.",
                "https://synagon.com/errores/integridad-datos",
                peticion
        );
    }

    private ProblemDetail construirDetalleProblema(
            HttpStatusCode estadoHttp,
            String titulo,
            String detalleMensaje,
            String tipoUri,
            HttpServletRequest peticion
    ) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estadoHttp, detalleMensaje);
        problema.setTitle(titulo);
        problema.setType(URI.create(tipoUri));
        problema.setInstance(URI.create(peticion.getRequestURI()));
        problema.setProperty("instante", Instant.now());
        return problema;
    }
}
