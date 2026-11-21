package com.luxe.store.model;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(name="clientes", uniqueConstraints=@UniqueConstraint(columnNames="numero_documento"))
public class Cliente implements Serializable {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String tipoDocumento; // "DNI", "RUC"
    @Column(name="numero_documento", unique=true, length=20) private String numeroDocumento;
    private String nombres;
    private String telefono;
    private String correo;
    private String estado; // "Activo", "Inactivo"

    public Cliente() {}

    public Cliente(Long id, String tipoDocumento, String numeroDocumento, String nombres, String telefono, String correo, String estado) {
        this.id = id;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombres = nombres;
        this.telefono = telefono;
        this.correo = correo;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
