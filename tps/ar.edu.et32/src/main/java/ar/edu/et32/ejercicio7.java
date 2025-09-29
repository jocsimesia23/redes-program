package ar.edu.et32;

import javax.swing.JOptionPane;
import java.time.LocalTime;

public class ejercicio7 {
    public static void main(String[] args) {
        int cantidad = Integer.parseInt(JOptionPane.showInputDialog("Cantidad de empleados:"));
        Empleado[] empleados = new Empleado[cantidad];

        Thread carga = new Thread(() -> {
            for (int i = 0; i < cantidad; i++) {
                String nombre = JOptionPane.showInputDialog("Nombre del empleado " + (i + 1));
                String hora = JOptionPane.showInputDialog("Hora de ingreso (HH:MM)");
                empleados[i] = new Empleado(nombre, hora);

                final int index = i; // ✅ ahora sí se puede usar dentro del hilo
                new Thread(() -> empleados[index].verificar()).start();
            }
        });

        carga.start();
        try { 
            carga.join(); 
        } catch (InterruptedException e) {}
    }
}

class Empleado {
    String nombre;
    LocalTime ingreso;
    static final LocalTime HORA_ENTRADA = LocalTime.of(8, 0);

    Empleado(String n, String h) {
        nombre = n;
        ingreso = LocalTime.parse(h);
    }

    void verificar() {
        if (ingreso.isAfter(HORA_ENTRADA))
            JOptionPane.showMessageDialog(null, nombre + " llegó tarde a las " + ingreso);
        else
            JOptionPane.showMessageDialog(null, nombre + " llegó temprano a las " + ingreso);
    }
}
