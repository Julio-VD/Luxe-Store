export interface Producto {
  id?: number;
  codigo: string;
  nombre: string;
  categoria: string;
  talla: string;
  color: string;
  precio: number;
  stock: number;
  estado?: string;
}

export interface Cliente {
  id?: number;
  tipoDocumento: string;
  numeroDocumento: string;
  nombres: string;
  telefono: string;
  correo: string;
  estado?: string;
}

export interface DetalleVenta {
  id?: number;
  producto: Producto;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Venta {
  id?: number;
  numeroTicket?: string;
  fecha?: string;
  cliente?: Cliente;
  vendedor: string;
  detalles: DetalleVenta[];
  metodoPago: string;
  montoTotal: number;
  montoRecibido: number;
  vuelto: number;
  estado?: string;
}

export interface Usuario {
  id?: number;
  nombreCompleto: string;
  correoUsuario: string;
  usuario: string;
  contrasena: string;
  rol: string;
  estado?: string;
}
