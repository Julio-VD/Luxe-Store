package com.luxe.store.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity @Table(name="arqueos_caja",indexes={@Index(name="idx_arqueo_vendedor_fecha",columnList="vendedor_id,fecha")})
public class ArqueoCaja {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="vendedor_id",nullable=false) public Long vendedorId;
 @Column(nullable=false) public LocalDate fecha;
 @Column(nullable=false) public LocalDateTime registradoEn;
 @Column(nullable=false,precision=14,scale=2) public BigDecimal esperado;
 @Column(nullable=false,precision=14,scale=2) public BigDecimal contado;
 @Column(nullable=false,precision=14,scale=2) public BigDecimal diferencia;
 @Column(nullable=false) public boolean forzoso;
 @Column(nullable=false) public boolean definitivo;
 @Column(nullable=false,length=25) public String estado;
public ArqueoCaja() {
}
}
