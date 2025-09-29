package ar.edu.et32;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public class guia {

    // ANSI color codes for console
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";

    private static final int PORT = 5000;
    private static final int BUFFER_SIZE = 4096;
    private static final String RECEIVED_DIR = "received";

    public static void main(String[] args) {
        // create received directory if not exists
        try {
            Path dir = Paths.get(RECEIVED_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);
        } catch (IOException e) {
            System.out.println(RED + "No pude crear la carpeta '" + RECEIVED_DIR + "': " + e.getMessage() + RESET);
            return;
        }

        System.out.println(BLUE + "Servidor escuchando en el puerto " + PORT + " ..." + RESET);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                System.out.println(BLUE + "Esperando conexión de cliente..." + RESET);
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println(GREEN + "Cliente conectado desde " + clientSocket.getRemoteSocketAddress() + RESET);
                    handleClient(clientSocket);
                } catch (Exception e) {
                    System.out.println(RED + "Error en conexión/recepción: " + e.getMessage() + RESET);
                }
            }
        } catch (IOException e) {
            System.out.println(RED + "Error creando ServerSocket: " + e.getMessage() + RESET);
        }
    }

    private static void handleClient(Socket socket) throws IOException {
        // Use DataInputStream for simple protocol (string length, name, long size, bytes)
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
            while (true) {
                // Protocol:
                // 1) client sends boolean (true) if a file will be sent, false -> end of session
                boolean hasFile;
                try {
                    hasFile = dis.readBoolean();
                } catch (EOFException eof) {
                    // client closed connection
                    System.out.println(BLUE + "Cliente cerró la conexión." + RESET);
                    break;
                }

                if (!hasFile) {
                    System.out.println(BLUE + "Cliente indica fin de sesión." + RESET);
                    break;
                }

                // read filename length and filename (we'll use readUTF for simplicity)
                String filename = dis.readUTF();
                long fileSize = dis.readLong();

                System.out.println(BLUE + "Recibiendo archivo: " + filename + " (" + fileSize + " bytes)" + RESET);

                // Prepare output file (avoid overwrite: append timestamp if exists)
                Path outPath = Paths.get(RECEIVED_DIR, filename);
                if (Files.exists(outPath)) {
                    String base = filename;
                    String newName = base + "_" + Instant.now().getEpochSecond();
                    outPath = Paths.get(RECEIVED_DIR, newName);
                }

                // Read file content in chunks
                try (OutputStream fos = new BufferedOutputStream(Files.newOutputStream(outPath))) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    long remaining = fileSize;
                    while (remaining > 0) {
                        int read = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                        if (read == -1) throw new EOFException("Se cortó la conexión antes de terminar la transferencia");
                        fos.write(buffer, 0, read);
                        remaining -= read;
                    }
                    fos.flush();
                } catch (Exception e) {
                    System.out.println(RED + "Error escribiendo archivo: " + e.getMessage() + RESET);
                    // optionally continue to next file
                    continue;
                }

                System.out.println(GREEN + "Archivo recibido y guardado en: " + outPath.toAbsolutePath() + RESET);
            }
        }
    }
}
