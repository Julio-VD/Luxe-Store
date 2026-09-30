import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from './services/api.service';
import { Producto, Cliente, Venta, DetalleVenta } from './models/luxe.models';

declare var Chart: any;

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  title = 'LUXE Store Angular';

  // Navegación y Sesión
  currentView: 'LOGIN' | 'ADMIN' | 'VENDEDOR' | 'REPORTES' = 'LOGIN';
  usuarioInput: string = '';
  passwordInput: string = '';
  loginError: string = '';
  usuarioLogueado: any = null;

  // Secciones Admin
  adminSeccion: 'dashboard' | 'productos' | 'stock' | 'ventas' | 'clientes' = 'dashboard';

  // Datos desde API REST
  productos: Producto[] = [];
  clientes: Cliente[] = [];
  ventas: Venta[] = [];
  totalVentasMonto: number = 0;
  stockBajoCount: number = 0;
  stockProductoId: number | null = null;
  stockCantidad: number = 1;

  // Formulario Nuevo Producto (Admin)
  nuevoProducto: Producto = {
    codigo: '',
    nombre: '',
    categoria: 'Polos',
    talla: 'M',
    color: '',
    precio: 0,
    stock: 10
  };

  // Formulario Nuevo Cliente (Modal POS)
  nuevoCliente: Cliente = {
    tipoDocumento: 'DNI',
    numeroDocumento: '',
    nombres: '',
    telefono: '',
    correo: ''
  };

  // POS Vendedor
  buscarPosQuery: string = '';
  clienteSeleccionadoDoc: string = '';
  metodoPagoPos: string = 'Efectivo';
  montoRecibidoPos: number = 0;
  carritoPos: { producto: Producto; cantidad: number; subtotal: number }[] = [];

  // Paginación de Gráficos (Reportes)
  paginaGraficos: number = 1;

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.cargarDatosBackend();
  }

  cargarDatosBackend(): void {
    this.apiService.getProductos().subscribe(data => {
      this.productos = data;
      this.stockBajoCount = this.productos.filter(p => p.stock <= 5).length;
    });

    this.apiService.getClientes().subscribe(data => {
      this.clientes = data;
    });

    this.apiService.getVentas().subscribe(data => {
      this.ventas = data;
      this.totalVentasMonto = this.ventas.reduce((sum, v) => sum + v.montoTotal, 0);
    });
  }

  // --- LOGIN ---
  login(): void {
    if (this.usuarioInput === 'admin' && this.passwordInput === '1234') {
      this.usuarioLogueado = { nombreCompleto: 'Administrador Principal', usuario: 'admin', rol: 'ADMIN' };
      this.currentView = 'ADMIN';
      this.loginError = '';
    } else if (this.usuarioInput === 'vendedor' && this.passwordInput === '1234') {
      this.usuarioLogueado = { nombreCompleto: 'Juan Pérez (Cajero)', usuario: 'vendedor', rol: 'VENDEDOR' };
      this.currentView = 'VENDEDOR';
      this.loginError = '';
    } else {
      this.loginError = 'Usuario o contraseña incorrectos.';
    }
  }

  logout(): void {
    this.usuarioLogueado = null;
    this.usuarioInput = '';
    this.passwordInput = '';
    this.currentView = 'LOGIN';
  }

  // --- ADMIN ACTIONS ---
  guardarProducto(): void {
    this.apiService.guardarProducto(this.nuevoProducto).subscribe(() => {
      alert('¡Prenda registrada en el backend con éxito!');
      this.nuevoProducto = { codigo: '', nombre: '', categoria: 'Polos', talla: 'M', color: '', precio: 0, stock: 10 };
      this.cargarDatosBackend();
    });
  }

  cambiarEstadoProducto(producto: Producto): void {
    if (!producto.id) return;

    if (producto.estado === 'Activo') {
      this.apiService.inhabilitarProducto(producto.id).subscribe(() => {
        this.cargarDatosBackend();
      });
    } else {
      if (producto.stock <= 0) {
        alert('Primero aumente el stock para poder activar esta prenda.');
        return;
      }
      this.apiService.activarProducto(producto.id).subscribe(() => {
        this.cargarDatosBackend();
      });
    }
  }

  aumentarStock(): void {
    if (!this.stockProductoId || this.stockCantidad <= 0) {
      alert('Seleccione una prenda e indique una cantidad válida.');
      return;
    }

    this.apiService.aumentarStock(this.stockProductoId, this.stockCantidad).subscribe(() => {
      alert('Stock aumentado correctamente.');
      this.stockProductoId = null;
      this.stockCantidad = 1;
      this.cargarDatosBackend();
    });
  }

  eliminarProducto(id?: number): void {
    if (!id) return;
    if (confirm('¿Seguro de eliminar esta prenda?')) {
      this.apiService.eliminarProducto(id).subscribe(() => {
        this.cargarDatosBackend();
      });
    }
  }

  // --- POS VENDEDOR ACTIONS ---
  getProductosFiltradosPos(): Producto[] {
    let q = this.buscarPosQuery.toLowerCase();
    return this.productos.filter(p => p.estado === 'Activo' &&
      (p.nombre.toLowerCase().includes(q) || p.codigo.toLowerCase().includes(q) || p.categoria.toLowerCase().includes(q)));
  }

  agregarAlCarrito(producto: Producto): void {
    let item = this.carritoPos.find(i => i.producto.id === producto.id);
    if (item) {
      if (item.cantidad < producto.stock) {
        item.cantidad++;
        item.subtotal = item.cantidad * producto.precio;
      } else {
        alert('Stock máximo alcanzado.');
      }
    } else {
      this.carritoPos.push({ producto, cantidad: 1, subtotal: producto.precio });
    }
  }

  modificarCantidad(item: any, cambio: number): void {
    item.cantidad += cambio;
    if (item.cantidad <= 0) {
      this.carritoPos = this.carritoPos.filter(i => i !== item);
    } else if (item.cantidad > item.producto.stock) {
      item.cantidad = item.producto.stock;
      alert('Stock máximo alcanzado.');
    }
    item.subtotal = item.cantidad * item.producto.precio;
  }

  eliminarDelCarrito(item: any): void {
    this.carritoPos = this.carritoPos.filter(i => i !== item);
  }

  vaciarCarrito(): void {
    this.carritoPos = [];
  }

  getPosTotal(): number {
    return this.carritoPos.reduce((sum, i) => sum + i.subtotal, 0);
  }

  getPosVuelto(): number {
    let total = this.getPosTotal();
    return Math.max(0, this.montoRecibidoPos - total);
  }

  guardarClienteModal(): void {
    this.apiService.guardarCliente(this.nuevoCliente).subscribe(res => {
      alert('Cliente registrado con éxito.');
      this.clienteSeleccionadoDoc = res.numeroDocumento;
      this.nuevoCliente = { tipoDocumento: 'DNI', numeroDocumento: '', nombres: '', telefono: '', correo: '' };
      this.cargarDatosBackend();
    });
  }

  procesarVentaPos(): void {
    if (this.carritoPos.length === 0) {
      alert('Agregue prendas al ticket antes de procesar.');
      return;
    }

    let total = this.getPosTotal();
    if (this.metodoPagoPos === 'Efectivo' && this.montoRecibidoPos < total) {
      alert('El monto recibido es menor al total de la venta.');
      return;
    }

    let clienteObj = this.clientes.find(c => c.numeroDocumento === this.clienteSeleccionadoDoc);

    let detalles: DetalleVenta[] = this.carritoPos.map(i => ({
      producto: i.producto,
      cantidad: i.cantidad,
      precioUnitario: i.producto.precio,
      subtotal: i.subtotal
    }));

    let ventaPayload: Venta = {
      vendedor: this.usuarioLogueado ? this.usuarioLogueado.nombreCompleto : 'Cajero POS',
      cliente: clienteObj,
      metodoPago: this.metodoPagoPos,
      montoTotal: total,
      montoRecibido: this.montoRecibidoPos || total,
      vuelto: this.getPosVuelto(),
      detalles: detalles,
      fecha: new Date().toLocaleDateString('es-PE')
    };

    this.apiService.registrarVenta(ventaPayload).subscribe(res => {
      alert('¡Venta procesada con éxito en el backend!\nTicket: ' + res.numeroTicket);
      this.vaciarCarrito();
      this.montoRecibidoPos = 0;
      this.cargarDatosBackend();
    });
  }

  // --- REPORTES Y GRÁFICOS ---
  irAReportes(): void {
    this.currentView = 'REPORTES';
    setTimeout(() => {
      this.renderizarGraficos();
    }, 200);
  }

  setPaginaGraficos(pag: number): void {
    this.paginaGraficos = pag;
    setTimeout(() => {
      this.renderizarGraficos();
    }, 100);
  }

  renderizarGraficos(): void {
    if (typeof Chart === 'undefined') return;

    if (this.paginaGraficos === 1) {
      let canvasBarras = document.getElementById('chartBarrasAngular') as HTMLCanvasElement;
      if (canvasBarras) {
        new Chart(canvasBarras, {
          type: 'bar',
          data: {
            labels: ['Polos', 'Camisas', 'Pantalones', 'Casacas'],
            datasets: [{
              label: 'Ventas en Soles (S/)',
              data: [1450, 2150, 1890, 980],
              backgroundColor: ['#1e293b', '#d97706', '#2563eb', '#16a34a'],
              borderRadius: 6
            }]
          },
          options: { responsive: true }
        });
      }

      let canvasLineal = document.getElementById('chartLinealAngular') as HTMLCanvasElement;
      if (canvasLineal) {
        new Chart(canvasLineal, {
          type: 'line',
          data: {
            labels: ['Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre'],
            datasets: [{
              label: 'Ingresos Mensuales (S/)',
              data: [3200, 4500, 5100, 6800, 7450],
              borderColor: '#d97706',
              backgroundColor: 'rgba(217, 119, 6, 0.15)',
              fill: true,
              tension: 0.3
            }]
          },
          options: { responsive: true }
        });
      }
    } else if (this.paginaGraficos === 2) {
      let canvasDona = document.getElementById('chartCircularAngular') as HTMLCanvasElement;
      if (canvasDona) {
        new Chart(canvasDona, {
          type: 'doughnut',
          data: {
            labels: ['Yape / Plin (38.5%)', 'Efectivo (33.3%)', 'Tarjeta (28.2%)'],
            datasets: [{
              data: [52, 45, 38],
              backgroundColor: ['#8b5cf6', '#10b981', '#3b82f6']
            }]
          },
          options: { responsive: true }
        });
      }

      let canvasHoriz = document.getElementById('chartStockAngular') as HTMLCanvasElement;
      if (canvasHoriz) {
        new Chart(canvasHoriz, {
          type: 'bar',
          data: {
            labels: ['Polos', 'Camisas', 'Pantalones', 'Casacas'],
            datasets: [{
              label: 'Stock Disponible',
              data: [43, 20, 14, 8],
              backgroundColor: '#64748b'
            }]
          },
          options: { indexAxis: 'y', responsive: true }
        });
      }
    }
  }
}
