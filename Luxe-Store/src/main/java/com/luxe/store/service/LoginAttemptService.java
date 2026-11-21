package com.luxe.store.service;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class LoginAttemptService {
 private static final long VENTANA=15*60*1000L;
 private final ConcurrentHashMap<String,Intentos> datos=new ConcurrentHashMap<>();
 private static class Intentos {int fallos;long desde;Intentos(long t){desde=t;}}
 public boolean bloqueado(String clave){Intentos i=datos.get(clave);if(i==null)return false; synchronized(i){if(System.currentTimeMillis()-i.desde>VENTANA){datos.remove(clave,i);return false;}return i.fallos>=5;}}
 public void fallo(String clave){long ahora=System.currentTimeMillis();datos.compute(clave,(k,i)->{if(i==null||ahora-i.desde>VENTANA)i=new Intentos(ahora);i.fallos++;return i;});}
 public void exito(String clave){datos.remove(clave);}
}
