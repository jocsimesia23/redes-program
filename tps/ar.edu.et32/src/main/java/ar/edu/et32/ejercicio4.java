package ar.edu.et32;
import javax.swing.JOptionPane;

public class ejercicio4 {

	public static void ejercicio(String[] args) {
		// TODO Auto-generated method stub

	}


    private static final int N = 4;

    public static void main(String[] args) {
        int[][] A = new int[N][N];
        int[][] B = new int[N][N];
        int[][] Cseq = new int[N][N];
        int[][] Cpar = new int[N][N];

        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++) {
                A[i][j] = (int)(Math.random() * 10);
                B[i][j] = (int)(Math.random() * 10);
            }
        long inicio = System.currentTimeMillis();
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                for (int k = 0; k < N; k++)
                    Cseq[i][j] += A[i][k] * B[k][j];
        long fin = System.currentTimeMillis();
        JOptionPane.showMessageDialog(null,"Tiempo secuencial: "+(fin-inicio)+" ms");
        Thread[] hilos = new Thread[N];
        inicio = System.currentTimeMillis();
        for (int i = 0; i < N; i++) {
            int fila = i;
            hilos[i] = new Thread(() -> {
                for (int j = 0; j < N; j++)
                    for (int k = 0; k < N; k++)
                        Cpar[fila][j] += A[fila][k] * B[k][j];
            });
            hilos[i].start();
        }
        try { for (Thread t: hilos) t.join(); } catch (InterruptedException e) {}
        fin = System.currentTimeMillis();
        JOptionPane.showMessageDialog(null,"Tiempo paralelo: "+(fin-inicio)+" ms");
    }
}
