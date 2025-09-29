package ar.edu.et32;

import javax.swing.JOptionPane;
import java.util.Random;

public class ejercicio2{
    public static void main(String[] args) {
        Random r = new Random();
        int cantidad = r.nextInt(8) + 3;

        for (int i = 1; i <= cantidad; i++) {
            int limite = r.nextInt(20) + 10;
            int tiempo = r.nextInt(801) + 200;
            Thread t = new Thread(new Contador("Contador-" + i, limite, tiempo));
            t.start();
        }
    }
}

class Contador implements Runnable {
    private int contador;
    private String nombre;
    private int limite;
    private int tiempo;

    public Contador(String nombre, int limite, int tiempo) {
        this.nombre = nombre;
        this.limite = limite;
        this.tiempo = tiempo;
        this.contador = 0;
    }

    @Override
    public void run() {
        long inicio = System.currentTimeMillis();
        while (contador < limite) {
            contador++;
            try {
                Thread.sleep(tiempo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long fin = System.currentTimeMillis();
        JOptionPane.showMessageDialog(null,
                nombre + " terminó en " + (fin - inicio) + " ms contando hasta " + limite);
    }
}

