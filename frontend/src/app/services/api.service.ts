import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Producto, Cliente, Venta } from '../models/luxe.models';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  // PRODUCTOS
  getProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.baseUrl}/productos`);
  }

  getProductosActivos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.baseUrl}/productos/activos`);
  }

  guardarProducto(producto: Producto): Observable<Producto> {
    return this.http.post<Producto>(`${this.baseUrl}/productos`, producto);
  }

  inhabilitarProducto(id: number): Observable<boolean> {
    return this.http.patch<boolean>(`${this.baseUrl}/productos/${id}/inhabilitar`, {});
  }

  eliminarProducto(id: number): Observable<boolean> {
    return this.http.delete<boolean>(`${this.baseUrl}/productos/${id}`);
  }

  // CLIENTES
  getClientes(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.baseUrl}/clientes`);
  }

  getClientesActivos(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.baseUrl}/clientes/activos`);
  }

  guardarCliente(cliente: Cliente): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.baseUrl}/clientes`, cliente);
  }

  // VENTAS
  getVentas(): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${this.baseUrl}/ventas`);
  }

  registrarVenta(venta: Venta): Observable<Venta> {
    return this.http.post<Venta>(`${this.baseUrl}/ventas`, venta);
  }

  // REPORTES
  getReporteCategorias(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.baseUrl}/reportes/categorias`);
  }

  getReporteMensual(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.baseUrl}/reportes/mensual`);
  }

  getReporteMetodosPago(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.baseUrl}/reportes/metodos-pago`);
  }

  getResumen(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/reportes/resumen`);
  }
}
