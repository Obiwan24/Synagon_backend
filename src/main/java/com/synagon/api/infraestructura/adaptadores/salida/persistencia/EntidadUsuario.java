package com.synagon.api.infraestructura.adaptadores.salida.persistencia;

import com.synagon.api.dominio.modelos.RolUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA que representa la tabla 'usuarios' en PostgreSQL.
 */
@Entity
@Table(name = "usuarios")
public class EntidadUsuario {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 50)
    private RolUsuario rol;

    @Column(name = "contrasena_hash", length = 255)
    private String contrasenaHash;

    @Column(name = "acepto_privacidad", nullable = false)
    private boolean aceptoPrivacidad;

    @Column(name = "fecha_aceptacion_privacidad")
    private OffsetDateTime fechaAceptacionPrivacidad;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EntidadUsuario() {
        // Requerido por JPA
    }

    public EntidadUsuario(
            UUID id,
            String nombre,
            String apellidos,
            String email,
            String telefono,
            RolUsuario rol,
            String contrasenaHash,
            boolean aceptoPrivacidad,
            OffsetDateTime fechaAceptacionPrivacidad,
            OffsetDateTime creadoEn,
            OffsetDateTime actualizadoEn
    ) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre);
        this.apellidos = Objects.requireNonNull(apellidos);
        this.email = Objects.requireNonNull(email);
        this.telefono = telefono;
        this.rol = Objects.requireNonNull(rol);
        this.contrasenaHash = contrasenaHash;
        this.aceptoPrivacidad = aceptoPrivacidad;
        this.fechaAceptacionPrivacidad = fechaAceptacionPrivacidad;
        this.creadoEn = creadoEn != null ? creadoEn : OffsetDateTime.now();
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : OffsetDateTime.now();
    }

    public UUID obtenerId() { return id; }
    public String obtenerNombre() { return nombre; }
    public String obtenerApellidos() { return apellidos; }
    public String obtenerEmail() { return email; }
    public String obtenerTelefono() { return telefono; }
    public RolUsuario obtenerRol() { return rol; }
    public String obtenerContrasenaHash() { return contrasenaHash; }
    public boolean haAceptadoPrivacidad() { return aceptoPrivacidad; }
    public OffsetDateTime obtenerFechaAceptacionPrivacidad() { return fechaAceptacionPrivacidad; }
    public OffsetDateTime obtenerCreadoEn() { return creadoEn; }
    public OffsetDateTime obtenerActualizadoEn() { return actualizadoEn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntidadUsuario that = (EntidadUsuario) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
