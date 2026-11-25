package com.luxe.store.service;

import com.luxe.store.model.Venta;
import com.luxe.store.model.DetalleVenta;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.io.InputStream;

/** Gmail API OAuth 2.0. Sin credenciales, se omite el envío sin afectar la venta. */
@Service
public class BoletaCorreoService {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    private static String env(String key) { return System.getenv(key); }
    private static String esc(String s) { return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;"); }
    private static String money(Double n) { return String.format(Locale.US,"S/ %.2f",n==null?0:n); }
    private static String json(String s) { return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n")+"\""; }
    private static String b64(String s) { return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8)); }
    private static String encodeHeader(String s) { return "=?UTF-8?B?"+b64(s)+"?="; }
    public void enviarDespuesDeVenta(Venta venta) {
        if (venta.getCliente()==null || venta.getCliente().getCorreo()==null) {System.out.println("LUXE Gmail: venta sin correo de cliente; no se envía ticket.");return;}
        String to=venta.getCliente().getCorreo().trim();
        if (!to.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {System.err.println("LUXE Gmail: correo del cliente inválido.");return;}
        if (env("GOOGLE_CLIENT_ID")==null || env("GOOGLE_CLIENT_SECRET")==null || env("GOOGLE_REFRESH_TOKEN")==null || env("GMAIL_SENDER")==null) {System.err.println("LUXE Gmail: faltan variables de configuración.");return;}
        // Capturar HTML antes de salir de la transacción JPA; no acceder a entidades desde el hilo asíncrono.
        String html=crearHtml(venta);
        String ticket=venta.getNumeroTicket();
        CompletableFuture.runAsync(()->{
            try { enviar(to,ticket,html); }
            catch(Exception e) { System.err.println("LUXE Gmail: no se pudo enviar ticket "+ticket+" ("+e.getClass().getSimpleName()+")."); }
        });
    }
    private String crearHtml(Venta v) {
        StringBuilder filas=new StringBuilder();
        for(DetalleVenta d:v.getDetalles()) {
            String nombre=d.getProducto()==null?"Prenda":d.getProducto().getNombre();
            String talla=d.getProducto()==null?null:d.getProducto().getTalla();
            filas.append("<tr><td style='padding:13px 8px;border-bottom:1px solid #e5e7eb'>").append(esc(nombre));
            if(talla!=null&&!talla.isBlank())filas.append("<span style='color:#64748b'> · Talla ").append(esc(talla)).append("</span>");
            filas.append("</td><td style='padding:13px 8px;text-align:center;border-bottom:1px solid #e5e7eb'>").append(d.getCantidad())
            .append("</td><td style='padding:13px 8px;text-align:right;border-bottom:1px solid #e5e7eb'>").append(money(d.getSubtotal())).append("</td></tr>");
        }
        return "<!doctype html><html><head><meta charset='UTF-8'></head><body style='margin:0;background:#f1f3f6;font-family:Arial,sans-serif;color:#192230'>"
        +"<div style='padding:24px 10px'><div style='max-width:640px;margin:auto;background:white;border-radius:12px;overflow:hidden;border:1px solid #e5e7eb'>"
        +"<div style='background:#111824;text-align:center;padding:16px 8px'><img src='cid:luxe-logo' width='200' alt='LUXE Store' style='max-width:100%;height:auto;display:block;margin:auto'></div>"
        +"<div style='padding:28px 24px'><h2 style='color:#202a37;margin:0 0 10px'>¡Compra registrada con éxito!</h2>"
        +"<p>Hola, "+esc(v.getCliente().getNombres())+". Gracias por elegir LUXE. Este es el detalle de tu compra.</p>"
        +"<div style='background:#f8fafc;border-radius:8px;padding:14px;line-height:1.9'><b>Ticket:</b> "+esc(v.getNumeroTicket())+"<br><b>Fecha:</b> "+esc(v.getFecha())+"<br><b>Medio de pago:</b> "+esc(v.getMetodoPago())+"</div>"
        +"<table style='width:100%;border-collapse:collapse;font-size:13px;margin-top:20px'><thead><tr style='background:#f3f4f6'><th style='text-align:left;padding:10px'>Prenda</th><th>Cant.</th><th style='text-align:right;padding:10px'>Importe</th></tr></thead><tbody>"+filas+"</tbody></table>"
        +"<p style='text-align:right;font-size:13px;line-height:1.8'>Subtotal sin IGV: "+money(Math.round(v.getMontoTotal()/1.18*100)/100.0)+"<br>IGV (18%): "+money(Math.round((v.getMontoTotal()-Math.round(v.getMontoTotal()/1.18*100)/100.0)*100)/100.0)+"<br>Recibido: "+money(v.getMontoRecibido())+"<br>Vuelto: "+money(v.getVuelto())+"</p>"
        +"<div style='background:#f7f1e7;padding:18px;border-radius:8px;text-align:right;color:#8e671f;font-size:23px'><b>TOTAL "+money(v.getMontoTotal())+"</b></div>"
        +"<p style='font-size:12px;color:#6b7280;line-height:1.5;margin-top:25px'>Esta es una constancia de compra de LUXE, no una boleta electrónica autorizada por SUNAT.</p>"
        +"</div></div></div></body></html>";
    }
    private void enviar(String to,String ticket,String html) throws Exception {
        String form="client_id="+URLEncoder.encode(env("GOOGLE_CLIENT_ID"),"UTF-8")
          +"&client_secret="+URLEncoder.encode(env("GOOGLE_CLIENT_SECRET"),"UTF-8")
          +"&refresh_token="+URLEncoder.encode(env("GOOGLE_REFRESH_TOKEN"),"UTF-8")+"&grant_type=refresh_token";
        HttpRequest tokenReq=HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/token"))
          .timeout(Duration.ofSeconds(15)).header("Content-Type","application/x-www-form-urlencoded")
          .POST(HttpRequest.BodyPublishers.ofString(form)).build();
        HttpResponse<String> tokenResponse=http.send(tokenReq,HttpResponse.BodyHandlers.ofString());
        if(tokenResponse.statusCode()!=200)throw new IllegalStateException("OAuth HTTP "+tokenResponse.statusCode());
        java.util.regex.Matcher matcher=java.util.regex.Pattern.compile("\"access_token\"\\s*:\\s*\"([^\"]+)\"").matcher(tokenResponse.body());
        if(!matcher.find())throw new IllegalStateException("OAuth sin access_token");
        String token=matcher.group(1);
        byte[] logo;
        try(InputStream in=getClass().getResourceAsStream("/static/img/luxe-correo.png")) {
            if(in==null)throw new IllegalStateException("Logo no encontrado");
            logo=in.readAllBytes();
        }
        String boundary="luxe_"+UUID.randomUUID().toString().replace("-","");
        String mime="From: "+encodeHeader("LUXE Store")+" <"+env("GMAIL_SENDER")+">\r\n"
           +"To: "+to+"\r\nSubject: "+encodeHeader("LUXE | Confirmación de compra "+ticket)+"\r\n"
           +"MIME-Version: 1.0\r\nContent-Type: multipart/related; boundary=\""+boundary+"\"\r\n\r\n"
           +"--"+boundary+"\r\nContent-Type: text/html; charset=UTF-8\r\nContent-Transfer-Encoding: base64\r\n\r\n"
           +Base64.getMimeEncoder(76,"\r\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(html.getBytes(StandardCharsets.UTF_8))+"\r\n"
           +"--"+boundary+"\r\nContent-Type: image/png\r\nContent-Transfer-Encoding: base64\r\nContent-ID: <luxe-logo>\r\nContent-Disposition: inline; filename=\"luxe-logo.png\"\r\n\r\n"
           +Base64.getMimeEncoder(76,"\r\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(logo)+"\r\n--"+boundary+"--\r\n";
        String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(mime.getBytes(StandardCharsets.UTF_8));
        HttpRequest sendReq=HttpRequest.newBuilder(URI.create("https://gmail.googleapis.com/gmail/v1/users/me/messages/send"))
           .timeout(Duration.ofSeconds(20)).header("Authorization","Bearer "+token).header("Content-Type","application/json")
           .POST(HttpRequest.BodyPublishers.ofString("{\"raw\":"+json(raw)+"}")).build();
        HttpResponse<String> result=http.send(sendReq,HttpResponse.BodyHandlers.ofString());
        if(result.statusCode()<200||result.statusCode()>=300)throw new IllegalStateException("Gmail HTTP "+result.statusCode());
        System.out.println("LUXE: correo de ticket "+ticket+" aceptado por Gmail API.");
    }
}
