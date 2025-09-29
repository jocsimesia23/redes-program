package ar.edu.et32;

import javax.swing.*;
import java.util.Random;

public class ejercicio3 {
    public static void main(String[] args) {
        JTextArea area = new JTextArea();
        JFrame frame = new JFrame("Carrera Liebre y Tortuga");
        frame.setSize(800, 600);
        frame.add(new JScrollPane(area));
        frame.setVisible(true);

        Thread tortuga = new Thread(new Animal("Tortuga", area));
        Thread liebre = new Thread(new Animal("Liebre", area));

        area.append("¡Comienza la carrera!\n");
        tortuga.start();
        liebre.start();
    }
}

class Animal implements Runnable {
    private String nombre;
    private JTextArea area;
    private int posicion = 1;
    private static final int META = 70;
    private static boolean terminado = false;

    public Animal(String nombre, JTextArea area) {
        this.nombre = nombre;
        this.area = area;
    }

    @Override
    public void run() {
        Random r = new Random();
        while (!terminado) {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}

            int prob = r.nextInt(100) + 1;
            if (nombre.equals("Tortuga")) {
                if (prob <= 50) posicion += 3;
                else if (prob <= 70) posicion = Math.max(1, posicion - 6);
                else posicion += 1;
            } else {
                if (prob <= 20) {} // duerme
                else if (prob <= 40) posicion += 9;
                else if (prob <= 50) posicion = Math.max(1, posicion - 12);
                else if (prob <= 80) posicion += 1;
                else posicion = Math.max(1, posicion - 2);
            }

            if (posicion >= META) {
                posicion = META;
                area.append(nombre + " llegó a la meta!\n");
                terminado = true;
            }

            StringBuilder linea = new StringBuilder(".".repeat(META));
            linea.setCharAt(posicion - 1, nombre.charAt(0));
            area.append(nombre + ": " + linea + "\n");
        }
    }
}
