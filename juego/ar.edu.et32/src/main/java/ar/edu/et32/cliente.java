package ar.edu.et32;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class cliente {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Socket socket = null;
        DataInputStream in = null;
        DataOutputStream out = null;

        try {
            System.out.print("IP del servidor (ej: 127.0.0.1): ");
            String ip = sc.nextLine();

            socket = new Socket(ip, 5000);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            String simbolo = "";
            String mensajeSimbolo = in.readUTF();
            String[] partes = mensajeSimbolo.split(":");
            if (partes.length == 2) {
                simbolo = partes[1];
            }
            System.out.println("Tu simbolo es: " + simbolo);

            boolean fin = false;

            while (!fin) {

                String msg = in.readUTF();

                if (msg.equals("TURNO")) {
                    System.out.println("Es tu turno. Ingresar fila y columna (0,1,2)");
                    System.out.print("Fila: ");
                    int f = sc.nextInt();
                    System.out.print("Columna: ");
                    int c = sc.nextInt();
                    sc.nextLine();

                    out.writeUTF(f + "," + c);
                    out.flush();
                } else if (msg.startsWith("TABLERO")) {
                    String soloTablero = msg.replace("TABLERO", "");
                    System.out.println(soloTablero);
                } else if (msg.equals("INVALIDO")) {
                    System.out.println("Movimiento invalido, probá otra casilla.");
                } else if (msg.startsWith("GANADOR")) {
                    String[] p2 = msg.split(":");
                    String gana = (p2.length == 2) ? p2[1] : "?";
                    System.out.println("Ganador: " + gana);
                    fin = true;
                } else if (msg.equals("EMPATE")) {
                    System.out.println("Empate, tablero completo.");
                    fin = true;
                }
            }

        } catch (IOException e) {
            System.out.println("Error en el cliente: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e2) {
            }
            sc.close();
        }
    }
}
