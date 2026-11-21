package com.luxe.store.model;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(name="usuarios", uniqueConstraints={@UniqueConstraint(columnNames="usuario"), @UniqueConstraint(columnNames="correo_usuario")})
public class Usuario implements Serializable {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String nombreCompleto;
    @Column(name="correo_usuario", unique=true) private String correoUsuario;
    @Column(nullable=false, unique=true) private String usuario;
    @Column(nullable=false) private String contrasena;
    private String rol; // "ADMIN", "VENDEDOR"
    private String estado;

    public Usuario() {}

    public Usuario(Long id, String nombreCompleto, String correoUsuario, String usuario, String contrasena, String rol, String estado) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.correoUsuario = correoUsuario;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String correoUsuario) { this.correoUsuario = correoUsuario; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
