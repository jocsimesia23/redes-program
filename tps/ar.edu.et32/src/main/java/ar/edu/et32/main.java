package ar.edu.et32;
import javax.swing.JOptionPane;

public class main {

    public static void Main(String[] args) {
        int tipo = 0;

        while (tipo != 1 && tipo != 2) {
            try {
                tipo = Integer.parseInt(JOptionPane.showInputDialog(
                        "Ingrese el tipo de hilo:\n1 - Números\n2 - Letras"));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Debe ingresar un número válido (1 o 2).");
            }
        }

        Thread hilo = new Thread(new HiloAlfanumerico(tipo));
        hilo.start();
    }
}

class HiloAlfanumerico implements Runnable {
    private int tipo;

    public HiloAlfanumerico(int tipo) {
        this.tipo = tipo;
    }

    @Override
    public void run() {
        try {
            if (tipo == 1) {
                for (int i = 1; i <= 30; i++) {
                    System.out.print(i + " ");
                    Thread.sleep(100);
                }
            } else if (tipo == 2) {
                for (char c = 'a'; c <= 'z'; c++) {
                    System.out.print(c + " ");
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
