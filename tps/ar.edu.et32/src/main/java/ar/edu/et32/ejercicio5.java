package ar.edu.et32;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
public class ejercicio5 {
    public static void main(String[] args) {
        File carpeta = new File("archivos");
        File[] archivos = carpeta.listFiles((dir, name) -> name.endsWith(".txt"));
        if (archivos == null) return;

        AtomicInteger total = new AtomicInteger(0);

        Thread[] hilos = new Thread[archivos.length];
        for (int i = 0; i < archivos.length; i++) {
            File archivo = archivos[i];
            hilos[i] = new Thread(() -> {
                int lineas = 0;
                try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                    while (br.readLine() != null) lineas++;
                } catch (Exception e) { e.printStackTrace(); }
                total.addAndGet(lineas);
                System.out.println("Archivo: " + archivo.getName() + " tiene " + lineas + " líneas.");
            });
            hilos[i].start();
        }

        try { for (Thread t : hilos) t.join(); } catch (InterruptedException e) {}
        System.out.println("Total de archivos: " + archivos.length);
        System.out.println("Total de líneas: " + total.get());
    }
}
