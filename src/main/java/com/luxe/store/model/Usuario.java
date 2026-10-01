package com.luxe.store.model;

import java.io.Serializable;

public class Usuario implements Serializable {

    private Long id;
    private String nombreCompleto;
    private String correoUsuario;
    private String usuario;
    private String contrasena;
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
