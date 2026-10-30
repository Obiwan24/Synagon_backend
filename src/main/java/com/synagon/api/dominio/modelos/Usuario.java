package com.synagon.api.dominio.modelos;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Modelo de dominio puro e inmutable que representa a un usuario de Synagon.
 * Contiene las invariantes de validación de nombre completo, correo electrónico,
 * DNI, contraseña cifrada, control de primer acceso y consentimiento legal RGPD.
 */
public final class Usuario {

    private static final Pattern PATRON_EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$"
    );

    private final UUID id;
    private final String nombre;
    private final String apellidos;
    private final String email;
    private final String dni;
    private final String telefono;
    private final String contrasenaHash;
    private final RolUsuario rol;
    private final boolean requiereCambioPassword;
    private final boolean primerAccesoCompletado;
    private final boolean aceptoPrivacidad;
    private final LocalDateTime fechaAceptacionPrivacidad;
    private final UUID equipoId;
    private final LocalDateTime creadoEn;

    public Usuario(
            UUID id,
            String nombre,
            String apellidos,
            String email,
            String telefono,
            String contrasenaHash,
            RolUsuario rol,
            boolean aceptoPrivacidad,
            LocalDateTime fechaAceptacionPrivacidad,
            UUID equipoId,
            LocalDateTime creadoEn
    ) {
        this(
                id,
                nombre,
                apellidos,
                email,
                null,
                telefono,
                contrasenaHash,
                rol,
                false,
                true,
                aceptoPrivacidad,
                fechaAceptacionPrivacidad,
                equipoId,
                creadoEn
        );
    }

    public Usuario(
            UUID id,
            String nombre,
            String apellidos,
            String email,
            String dni,
            String telefono,
            String contrasenaHash,
            RolUsuario rol,
            boolean requiereCambioPassword,
            boolean primerAccesoCompletado,
            boolean aceptoPrivacidad,
            LocalDateTime fechaAceptacionPrivacidad,
            UUID equipoId,
            LocalDateTime creadoEn
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.nombre = Objects.requireNonNull(nombre, "El nombre es obligatorio.").trim();
        this.apellidos = apellidos != null ? apellidos.trim() : "";
        this.email = normalizarYValidarEmail(email);
        this.dni = normalizarDni(dni);
        this.telefono = normalizarTelefono(telefono);
        this.contrasenaHash = contrasenaHash;
        this.rol = Objects.requireNonNull(rol, "El rol del usuario es obligatorio.");
        this.requiereCambioPassword = requiereCambioPassword;
        this.primerAccesoCompletado = primerAccesoCompletado;
        this.aceptoPrivacidad = aceptoPrivacidad;
        this.fechaAceptacionPrivacidad = fechaAceptacionPrivacidad;
        this.equipoId = equipoId;
        this.creadoEn = creadoEn != null ? creadoEn : LocalDateTime.now();

        if (this.nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre del usuario no puede estar vacío.");
        }
    }

    /**
     * Crea una nueva instancia de {@link Usuario} con rol por defecto {@link RolUsuario#JUGADOR},
     * validando todas las reglas de registro público y consentimiento RGPD.
     */
    public static Usuario registrarNuevoJugador(
            String nombreCompleto,
            String email,
            String telefono,
            String contrasenaHash,
            Boolean aceptoPrivacidad,
            UUID equipoPorDefectoId
    ) {
        if (!Boolean.TRUE.equals(aceptoPrivacidad)) {
            throw new IllegalArgumentException(
                    "Es obligatorio aceptar la política de privacidad en cumplimiento del RGPD."
            );
        }

        if (nombreCompleto == null || nombreCompleto.trim().replaceAll("\\s+", " ").length() < 3) {
            throw new IllegalArgumentException(
                    "El nombre completo es obligatorio y debe tener al menos 3 caracteres."
            );
        }

        if (contrasenaHash == null || contrasenaHash.isBlank()) {
            throw new IllegalArgumentException("El hash de la contraseña no puede estar vacío.");
        }

        String[] partesNombre = separarNombreYApellidos(nombreCompleto);
        LocalDateTime ahora = LocalDateTime.now();

        return new Usuario(
                UUID.randomUUID(),
                partesNombre[0],
                partesNombre[1],
                email,
                null,
                telefono,
                contrasenaHash,
                RolUsuario.JUGADOR,
                false,
                true,
                true,
                ahora,
                equipoPorDefectoId,
                ahora
        );
    }

    /**
     * Crea automáticamente un nuevo jugador dado de alta por el capitán de un equipo:
     * - Login con {@code email} normalizado en minúsculas.
     * - {@code dni} normalizado en mayúsculas.
     * - {@code contrasenaHash} correspondiente al DNI en mayúsculas cifrado con BCrypt.
     * - Activa {@code requiereCambioPassword = true} y {@code primerAccesoCompletado = false}.
     */
    public static Usuario crearAltaPorCapitan(
            String nombreCompletoOpcional,
            String email,
            String dni,
            String telefonoOpcional,
            String contrasenaPorDefectoHash,
            UUID equipoId
    ) {
        String emailNormalizado = normalizarYValidarEmail(email);
        String dniNormalizado = normalizarYValidarDniObligatorio(dni);

        if (contrasenaPorDefectoHash == null || contrasenaPorDefectoHash.isBlank()) {
            throw new IllegalArgumentException("El hash de la contraseña por defecto no puede estar vacío.");
        }

        String[] partesNombre;
        if (nombreCompletoOpcional != null && nombreCompletoOpcional.trim().replaceAll("\\s+", " ").length() >= 3) {
            partesNombre = separarNombreYApellidos(nombreCompletoOpcional);
        } else {
            partesNombre = derivarNombreDesdeEmail(emailNormalizado);
        }

        LocalDateTime ahora = LocalDateTime.now();

        return new Usuario(
                UUID.randomUUID(),
                partesNombre[0],
                partesNombre[1],
                emailNormalizado,
                dniNormalizado,
                telefonoOpcional,
                contrasenaPorDefectoHash,
                RolUsuario.JUGADOR,
                true,
                false,
                true,
                ahora,
                equipoId,
                ahora
        );
    }

    /**
     * Gestiona la decisión del usuario en su primer acceso respecto al cambio de contraseña provisional.
     */
    public Usuario gestionarPrimerAccesoPassword(boolean deseaCambiar, String nuevoHashContrasenaOpcional) {
        String hashFinal = this.contrasenaHash;
        if (deseaCambiar) {
            if (nuevoHashContrasenaOpcional == null || nuevoHashContrasenaOpcional.isBlank()) {
                throw new IllegalArgumentException("El hash de la nueva contraseña es obligatorio cuando se solicita cambiarla.");
            }
            hashFinal = nuevoHashContrasenaOpcional;
        }

        return new Usuario(
                this.id,
                this.nombre,
                this.apellidos,
                this.email,
                this.dni,
                this.telefono,
                hashFinal,
                this.rol,
                false,
                true,
                this.aceptoPrivacidad,
                this.fechaAceptacionPrivacidad,
                this.equipoId,
                this.creadoEn
        );
    }

    /**
     * Actualiza los datos editables del perfil personal del usuario.
     */
    public Usuario actualizarPerfil(String nombreCompleto, String telefono, String nuevoDni) {
        if (nombreCompleto == null || nombreCompleto.trim().replaceAll("\\s+", " ").length() < 3) {
            throw new IllegalArgumentException(
                    "El nombre completo es obligatorio y debe tener al menos 3 caracteres."
            );
        }

        String[] partesNombre = separarNombreYApellidos(nombreCompleto);
        String dniFinal = nuevoDni != null && !nuevoDni.isBlank()
                ? normalizarDni(nuevoDni)
                : this.dni;

        return new Usuario(
                this.id,
                partesNombre[0],
                partesNombre[1],
                this.email,
                dniFinal,
                telefono,
                this.contrasenaHash,
                this.rol,
                this.requiereCambioPassword,
                this.primerAccesoCompletado,
                this.aceptoPrivacidad,
                this.fechaAceptacionPrivacidad,
                this.equipoId,
                this.creadoEn
        );
    }

    public static String normalizarYValidarEmail(String emailSinNormalizar) {
        if (emailSinNormalizar == null || emailSinNormalizar.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }
        String limpio = emailSinNormalizar.trim().toLowerCase(Locale.ROOT);
        if (!PATRON_EMAIL.matcher(limpio).matches()) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido.");
        }
        return limpio;
    }

    public static String normalizarDni(String dniSinNormalizar) {
        if (dniSinNormalizar == null || dniSinNormalizar.isBlank()) {
            return null;
        }
        String limpio = dniSinNormalizar.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if (limpio.length() > 20) {
            throw new IllegalArgumentException("El DNI no puede superar los 20 caracteres.");
        }
        return limpio;
    }

    public static String normalizarYValidarDniObligatorio(String dniSinNormalizar) {
        String normalizado = normalizarDni(dniSinNormalizar);
        if (normalizado == null || normalizado.length() < 4) {
            throw new IllegalArgumentException("El DNI del jugador es obligatorio y debe tener al menos 4 caracteres.");
        }
        return normalizado;
    }

    private static String normalizarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return null;
        }
        String limpio = telefono.trim();
        if (limpio.length() > 30) {
            throw new IllegalArgumentException("El teléfono no puede superar los 30 caracteres.");
        }
        return limpio;
    }

    private static String[] separarNombreYApellidos(String nombreCompleto) {
        String normalizado = nombreCompleto.trim().replaceAll("\\s+", " ");
        int primerEspacio = normalizado.indexOf(' ');
        if (primerEspacio < 0) {
            return new String[]{recortarLongitud(normalizado, 100), ""};
        }
        String nombre = recortarLongitud(normalizado.substring(0, primerEspacio), 100);
        String apellidos = recortarLongitud(normalizado.substring(primerEspacio + 1), 100);
        return new String[]{nombre, apellidos};
    }

    private static String[] derivarNombreDesdeEmail(String emailNormalizado) {
        String prefijo = emailNormalizado.substring(0, emailNormalizado.indexOf('@'))
                .replace('.', ' ')
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
                .replaceAll("\\s+", " ");

        if (prefijo.isBlank()) {
            return new String[]{"Jugador", ""};
        }

        String[] palabras = prefijo.split(" ");
        StringBuilder reconstruido = new StringBuilder();
        for (String palabra : palabras) {
            if (palabra.isEmpty()) continue;
            if (!reconstruido.isEmpty()) reconstruido.append(' ');
            reconstruido.append(Character.toUpperCase(palabra.charAt(0)));
            if (palabra.length() > 1) {
                reconstruido.append(palabra.substring(1));
            }
        }
        return separarNombreYApellidos(reconstruido.toString());
    }

    private static String recortarLongitud(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo);
    }

    public String obtenerNombreCompleto() {
        if (apellidos == null || apellidos.isBlank()) {
            return nombre;
        }
        return nombre + " " + apellidos;
    }

    public Usuario conEquipo(UUID nuevoEquipoId) {
        return new Usuario(
                this.id,
                this.nombre,
                this.apellidos,
                this.email,
                this.dni,
                this.telefono,
                this.contrasenaHash,
                this.rol,
                this.requiereCambioPassword,
                this.primerAccesoCompletado,
                this.aceptoPrivacidad,
                this.fechaAceptacionPrivacidad,
                nuevoEquipoId,
                this.creadoEn
        );
    }

    public UUID obtenerId() { return id; }
    public String obtenerNombre() { return nombre; }
    public String obtenerApellidos() { return apellidos; }
    public String obtenerEmail() { return email; }
    public String obtenerDni() { return dni; }
    public String obtenerTelefono() { return telefono; }
    public String obtenerContrasenaHash() { return contrasenaHash; }
    public RolUsuario obtenerRol() { return rol; }
    public boolean requiereCambioPassword() { return requiereCambioPassword; }
    public boolean haCompletadoPrimerAcceso() { return primerAccesoCompletado; }
    public boolean haAceptadoPrivacidad() { return aceptoPrivacidad; }
    public LocalDateTime obtenerFechaAceptacionPrivacidad() { return fechaAceptacionPrivacidad; }
    public UUID obtenerEquipoId() { return equipoId; }
    public LocalDateTime obtenerCreadoEn() { return creadoEn; }
}
