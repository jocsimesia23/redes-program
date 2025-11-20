package ar.edu.et32;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class servidor {

    public static void main(String[] args) {

        ServerSocket server = null;
        Socket sock1 = null;
        Socket sock2 = null;

        DataInputStream in1 = null;
        DataOutputStream out1 = null;
        DataInputStream in2 = null;
        DataOutputStream out2 = null;

        try {
            server = new ServerSocket(5000);
            System.out.println("Servidor escuchando en el puerto 5000");

            System.out.println("Esperando jugador 1...");
            sock1 = server.accept();
            in1 = new DataInputStream(sock1.getInputStream());
            out1 = new DataOutputStream(sock1.getOutputStream());
            out1.writeUTF("SIMBOLO:X");
            out1.flush();
            System.out.println("Jugador 1 conectado");

            System.out.println("Esperando jugador 2...");
            sock2 = server.accept();
            in2 = new DataInputStream(sock2.getInputStream());
            out2 = new DataOutputStream(sock2.getOutputStream());
            out2.writeUTF("SIMBOLO:O");
            out2.flush();
            System.out.println("Jugador 2 conectado");

            Tablero t = new Tablero();

            boolean fin = false;
            int turno = 1;
            while (!fin) {

                if (turno == 1) {
                    out1.writeUTF("TURNO");
                    out1.flush();

                    String mov = in1.readUTF();
                    String[] partes = mov.split(",");
                    int f = Integer.parseInt(partes[0]);
                    int c = Integer.parseInt(partes[1]);

                    boolean ok = t.colocarSimbolo(f, c, "X");
                    if (!ok) {
                        out1.writeUTF("INVALIDO");
                        out1.flush();
                    } else {
                        String tableroString = "TABLERO" + t.toString();
                        out1.writeUTF(tableroString);
                        out2.writeUTF(tableroString);
                        out1.flush();
                        out2.flush();

                        if (t.esGanador("X")) {
                            out1.writeUTF("GANADOR:X");
                            out2.writeUTF("GANADOR:X");
                            out1.flush();
                            out2.flush();
                            fin = true;
                        } else if (t.esTableroCompleto()) {
                            out1.writeUTF("hay empate");
                            out2.writeUTF("hay empate");
                            out1.flush();
                            out2.flush();
                            fin = true;
                        } else {
                            turno = 2;
                        }
                    }
                } else {
                    out2.writeUTF("TURNO");
                    out2.flush();

                    String mov = in2.readUTF();
                    String[] partes = mov.split(",");
                    int f = Integer.parseInt(partes[0]);
                    int c = Integer.parseInt(partes[1]);

                    boolean ok = t.colocarSimbolo(f, c, "O");
                    if (!ok) {
                        out2.writeUTF("INVALIDO");
                        out2.flush();
                    } else {
                        String tableroString = "TABLERO" + t.toString();
                        out1.writeUTF(tableroString);
                        out2.writeUTF(tableroString);
                        out1.flush();
                        out2.flush();

                        if (t.esGanador("O")) {
                            out1.writeUTF("GANADOR:O");
                            out2.writeUTF("GANADOR:O");
                            out1.flush();
                            out2.flush();
                            fin = true;
                        } else if (t.esTableroCompleto()) {
                            out1.writeUTF("hay empate");
                            out2.writeUTF("hay empate");
                            out1.flush();
                            out2.flush();
                            fin = true;
                        } else {
                            turno = 1;
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        } finally {
            try {
                if (in1 != null) in1.close();
                if (out1 != null) out1.close();
                if (sock1 != null) sock1.close();
                if (in2 != null) in2.close();
                if (out2 != null) out2.close();
                if (sock2 != null) sock2.close();
                if (server != null) server.close();
            } catch (IOException e2) {
            }
        }
    }
}
