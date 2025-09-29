package ar.edu.et32;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class cliente {
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";

    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        String host = JOptionPane.showInputDialog(null, "Ingrese la IP del servidor (o 'localhost'):", "localhost");
        if (host == null) return;

        String portStr = JOptionPane.showInputDialog(null, "Ingrese puerto del servidor:", "5000");
        if (portStr == null) return;

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Puerto inválido");
            return;
        }

  
        try (Socket socket = new Socket(host, port);
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {

            System.out.println(GREEN + "Conexión establecida con el servidor " + host + ":" + port + RESET);

            boolean enviarOtro = true;
            while (enviarOtro) {
               
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Seleccione el archivo a enviar");
                int selection = chooser.showOpenDialog(null);
                if (selection != JFileChooser.APPROVE_OPTION) {
                    System.out.println(BLUE + "No se seleccionó archivo. Finalizando." + RESET);
                    
                    dos.writeBoolean(false);
                    dos.flush();
                    break;
                }

                File file = chooser.getSelectedFile();
                if (!file.exists() || !file.isFile()) {
                    System.out.println(RED + "Archivo inválido." + RESET);
                    continue;
                }

                String filename = file.getName();
                long fileSize = file.length();

                System.out.println(BLUE + "Preparando envío: " + filename + " (" + fileSize + " bytes)" + RESET);

                try {
                   
                    dos.writeBoolean(true);
                  
                    dos.writeUTF(filename);
                    dos.writeLong(fileSize);

                
                    try (InputStream fis = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        long remaining = fileSize;
                        int read;
                        while ((read = fis.read(buffer)) > 0) {
                            dos.write(buffer, 0, read);
                            remaining -= read;
                        }
                    }

                    dos.flush();
                    System.out.println(GREEN + "Envío completado: " + filename + RESET);

                } catch (IOException e) {
                    System.out.println(RED + "Error durante la transmisión: " + e.getMessage() + RESET);
                
                    int resp = JOptionPane.showConfirmDialog(null, "Error al enviar. ¿Desea intentar otro archivo?", "Error", JOptionPane.YES_NO_OPTION);
                    if (resp != JOptionPane.YES_OPTION) {
                    
                        try { dos.writeBoolean(false); dos.flush(); } catch (IOException ignored) {}
                        break;
                    } else {
                        continue;
                    }
                }

                int option = JOptionPane.showConfirmDialog(null, "Archivo enviado correctamente. ¿Desea enviar otro archivo?", "Continuar", JOptionPane.YES_NO_OPTION);
                enviarOtro = (option == JOptionPane.YES_OPTION);
                if (!enviarOtro) {
                    dos.writeBoolean(false);
                    dos.flush();
                    System.out.println(BLUE + "Finalizando sesión." + RESET);
                }
            }

        } catch (IOException e) {
            System.out.println(RED + "No se pudo conectar al servidor: " + e.getMessage() + RESET);
            JOptionPane.showMessageDialog(null, "Error de conexión: " + e.getMessage());
        }
    }
}
