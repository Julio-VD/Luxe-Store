package com.luxe.store.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.web.multipart.MultipartFile;

public final class ImagenJpgService {
    private ImagenJpgService() {}

    public static byte[] convertirAJpg(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) throw new IllegalArgumentException("Seleccione una imagen.");
        if (archivo.getSize() > 5L * 1024 * 1024) throw new IllegalArgumentException("La imagen no debe superar 5 MB.");
        BufferedImage origen = ImageIO.read(archivo.getInputStream());
        if (origen == null) throw new IllegalArgumentException("El archivo no es una imagen válida.");
        if ((long) origen.getWidth() * origen.getHeight() > 20_000_000L) throw new IllegalArgumentException("La imagen es demasiado grande.");
        BufferedImage jpg = new BufferedImage(origen.getWidth(), origen.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = jpg.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, jpg.getWidth(), jpg.getHeight());
            g.drawImage(origen, 0, 0, null);
        } finally { g.dispose(); }
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        if (!ImageIO.write(jpg, "jpg", salida)) throw new IOException("No se pudo convertir la imagen a JPG.");
        return salida.toByteArray();
    }
}
