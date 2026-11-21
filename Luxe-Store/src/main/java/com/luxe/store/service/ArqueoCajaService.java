package com.luxe.store.service;
import com.luxe.store.model.*;
import com.luxe.store.repository.*;
import org.springframework.stereotype.Service;
import java.math.*;
import java.time.*;
import java.util.*;
@Service
public class ArqueoCajaService {
 private final ArqueoCajaRepository repo;
 private final VentaRepository ventas;
 public ArqueoCajaService(ArqueoCajaRepository repo,VentaRepository ventas){this.repo=repo;this.ventas=ventas;}
 public static LocalDate hoy(){return LocalDate.now(ZoneId.of("America/Lima"));}
 public BigDecimal esperado(Usuario u,LocalDate fecha){
  String prefijo=fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
  return ventas.findAll().stream().filter(v->v.getVendedor()!=null&&v.getVendedor().equalsIgnoreCase(u.getNombreCompleto()))
   .filter(v->v.getFecha()!=null&&v.getFecha().startsWith(prefijo))
   .filter(v->"Efectivo".equalsIgnoreCase(v.getMetodoPago()))
   .filter(v->v.getMontoTotal()!=null).map(v->BigDecimal.valueOf(v.getMontoTotal())).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,RoundingMode.HALF_UP);
 }
 public synchronized ArqueoCaja contar(Usuario u,BigDecimal contado){
  if(contado==null||contado.signum()<0||contado.scale()>2||contado.compareTo(new BigDecimal("999999999.99"))>0)throw new IllegalArgumentException("Ingrese un monto válido, máximo dos decimales.");
  LocalDate fecha=hoy();
  List<ArqueoCaja> previos=repo.findByVendedorIdAndFechaOrderByIdDesc(u.getId(),fecha);
  if(!previos.isEmpty()&&previos.get(0).definitivo)throw new IllegalArgumentException("El cuadre del día ya fue confirmado como definitivo.");
  BigDecimal esperado=esperado(u,fecha), dif=contado.setScale(2).subtract(esperado);
  ArqueoCaja a=new ArqueoCaja();a.vendedorId=u.getId();a.fecha=fecha;a.registradoEn=LocalDateTime.now(ZoneId.of("America/Lima"));a.esperado=esperado;a.contado=contado.setScale(2);a.diferencia=dif;a.forzoso=false;a.definitivo=dif.signum()==0;
  a.estado=dif.signum()==0?"CONFORME":dif.signum()<0?"FALTANTE":"SOBRANTE";
  return repo.save(a);
 }
 public synchronized ArqueoCaja forzar(Usuario u){
  List<ArqueoCaja> lista=repo.findByVendedorIdAndFechaOrderByIdDesc(u.getId(),hoy());
  if(lista.isEmpty())throw new IllegalArgumentException("Primero registre un conteo de efectivo.");
  ArqueoCaja ultimo=lista.get(0);
  if(ultimo.definitivo)throw new IllegalArgumentException("El arqueo ya es definitivo.");
  ArqueoCaja a=new ArqueoCaja();a.vendedorId=u.getId();a.fecha=hoy();a.registradoEn=LocalDateTime.now(ZoneId.of("America/Lima"));a.esperado=esperado(u,hoy());a.contado=ultimo.contado;a.diferencia=a.contado.subtract(a.esperado);a.forzoso=true;a.definitivo=true;a.estado="FORZOSO";
  return repo.save(a);
 }
 public List<ArqueoCaja> historial(Long id,YearMonth mes){
  List<ArqueoCaja> todos=repo.findByVendedorIdAndFechaBetweenOrderByFechaDescIdDesc(id,mes.atDay(1),mes.atEndOfMonth());
  Map<LocalDate,ArqueoCaja> ultimos=new LinkedHashMap<>();for(ArqueoCaja a:todos)ultimos.putIfAbsent(a.fecha,a);
  return new ArrayList<>(ultimos.values());
 }
 public Map<String,Object> resumen(Long id,YearMonth mes){
  List<ArqueoCaja> lista=historial(id,mes);BigDecimal faltante=BigDecimal.ZERO,sobrante=BigDecimal.ZERO;
  for(ArqueoCaja a:lista){if(a.diferencia.signum()<0)faltante=faltante.add(a.diferencia.abs());else sobrante=sobrante.add(a.diferencia);}
  Map<String,Object> r=new LinkedHashMap<>();r.put("mes",mes.toString());r.put("faltante",faltante);r.put("sobrante",sobrante);r.put("registros",lista);r.put("nota","Días sin arqueo: cierre automático conforme, sin diferencia imputada.");return r;
 }
}
