package ar.edu.et32;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.util.concurrent.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.swing.*;
import java.util.Base64;


public class clienete {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";
    private static final String YELLOW = "\u001B[33m";

    private final String host;
    private final int port;
    private final String username;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;


    private final KeyPair rsaKeys;

  
    private final ConcurrentHashMap<String, PublicKey> publicKeys = new ConcurrentHashMap<>();


    private Thread readerThread;

    public ChatClient(String host, int port, String username) throws Exception {
        this.host = host; this.port = port; this.username = username;
        this.rsaKeys = generateRSAKeyPair();
    }

    public static void main(String[] args) throws Exception {
        String host = JOptionPane.showInputDialog(null, "IP del servidor:", "localhost");
        if (host == null) return;

        String user = JOptionPane.showInputDialog(null, "Nombre de usuario para chat (sin espacios):", "user" + new Random().nextInt(1000));
        if (user == null || user.trim().isEmpty()) return;

        ChatClient client = new ChatClient(host, 6000, user.trim());
        client.start();
    }

    void start() {
        try {
            socket = new Socket(host, port);
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(socket.getInputStream());

            System.out.println(GREEN + "Conectado al servidor " + host + ":" + port + RESET);

          
            Message join = new Message();
            join.type = "JOIN";
            join.sender = username;
            join.meta = new HashMap<>();
            join.meta.put("publicKey", Base64.getEncoder().encodeToString(rsaKeys.getPublic().getEncoded()));
            oos.writeObject(join);
            oos.flush();

     
            readerThread = new Thread(this::readerLoop);
            readerThread.start();

            // CLI loop: leer linea por linea
            cliLoop();

        } catch (Exception e) {
            System.err.println(RED + "Error conectando: " + e.getMessage() + RESET);
        } finally {
            closeEverything();
        }
    }

    private void cliLoop() {
        printHelp();
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print(YELLOW + "> " + RESET);
            String line = sc.hasNextLine() ? sc.nextLine().trim() : null;
            if (line == null) break;
            if (line.isEmpty()) continue;

            if (line.startsWith("/")) {
                String[] parts = line.split("\\s+", 2);
                String cmd = parts[0].toLowerCase();
                try {
                    switch (cmd) {
                        case "/salir":
                            doLeave();
                            return;
                        case "/listar":
                            requestList();
                            break;
                        case "/vercomandos":
                            printHelp();
                            break;
                        case "/ayuda":
                            printHelp();
                            break;
                        case "/msg":
               
                            if (parts.length < 2) { System.out.println(RED + "Formato: /msg usuario mensaje" + RESET); break; }
                            String[] p = parts[1].split("\\s+",2);
                            if (p.length<2) { System.out.println(RED + "Falta usuario o mensaje" + RESET); break; }
                            sendPrivateMessage(p[0], p[1]);
                            break;
                        case "/enviararchivo":
                           
                            if (parts.length < 2) { System.out.println(RED + "Formato: /enviarArchivo usuario ruta" + RESET); break; }
                            String[] q = parts[1].split("\\s+",2);
                            if (q.length<2) { System.out.println(RED + "Falta usuario o ruta" + RESET); break; }
                            sendFileToUser(q[0], q[1]);
                            break;
                        default:
                            System.out.println(RED + "Comando no válido. Usa /verComandos" + RESET);
                    }
                } catch (Exception e) {
                    System.out.println(RED + "Error al ejecutar comando: " + e.getMessage() + RESET);
                }
            } else {
             
                try {
                    sendBroadcast(line);
                } catch (Exception e) {
                    System.out.println(RED + "No se pudo enviar mensaje: " + e.getMessage() + RESET);
                }
            }
        }
    }

    private void readerLoop() {
        try {
            while (true) {
                Message m = (Message) ois.readObject();
                if (m == null) break;
                switch (m.type) {
                    case "WELCOME":

                        if (m.meta != null && m.meta.containsKey("publicKeys")) {
                            loadKeysFromMeta(m.meta.get("publicKeys"));
                        }
                        break;
                    case "KEYS":
                        if (m.meta != null && m.meta.containsKey("publicKeys")) {
                            loadKeysFromMeta(m.meta.get("publicKeys"));
                            System.out.println(BLUE + "[server] Lista de claves pública actualizada." + RESET);
                        }
                        break;
                    case "LIST":
                        String list = new String(m.payload != null ? m.payload : new byte[0]);
                        System.out.println(BLUE + "[server] Usuarios conectados: " + list + RESET);
                        break;
                    case "TEXT":
                        // descifrar
                        try {
                            String clear = decryptPayloadForMe(m);
                            if (m.sender != null && m.recipient != null && !m.recipient.trim().isEmpty()) {
                                // privado para mi
                                System.out.println(GREEN + "[PRIVADO] " + m.sender + " -> tú: " + clear + RESET);
                            } else {
                                System.out.println(GREEN + m.sender + ": " + clear + RESET);
                            }
                        } catch (Exception e) {
                            System.out.println(RED + "[error descifrar] " + e.getMessage() + RESET);
                        }
                        break;
                    case "FILE":
                        try {
                            // meta.filename
                            String filename = (m.meta != null && m.meta.containsKey("filename")) ? m.meta.get("filename") : "received_file";
                            byte[] plaintext = decryptPayloadBytesForMe(m);
                            // guardar
                            File out = new File("received_"+System.currentTimeMillis()+"_"+filename);
                            Files.write(out.toPath(), plaintext);
                            System.out.println(GREEN + "[FILE] Recibido de " + m.sender + " -> guardado en: " + out.getAbsolutePath() + RESET);
                        } catch (Exception e) {
                            System.out.println(RED + "[error file] " + e.getMessage() + RESET);
                        }
                        break;
                    case "ERROR":
                        System.out.println(RED + "[server] " + new String(m.payload != null ? m.payload : new byte[0]) + RESET);
                        break;
                    default:
                        System.out.println(BLUE + "[info] Mensaje tipo " + m.type + " recibido." + RESET);
                }
            }
        } catch (EOFException eof) {
            System.out.println(RED + "Conexión cerrada por servidor." + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Error lector: " + e.getMessage() + RESET);
        }
    }


    void sendBroadcast(String text) throws Exception {

        requestKeys();


        List<String> recipients = new ArrayList<>(publicKeys.keySet());
        recipients.remove(username);
        if (recipients.isEmpty()) {
            System.out.println(YELLOW + "No hay otros usuarios conectados para enviar. Aún así envíe localmente." + RESET);
            System.out.println(GREEN + "Tú: " + text + RESET);
            return;
        }

        for (String rec : recipients) {
            PublicKey pk = publicKeys.get(rec);
            if (pk == null) continue;
    
            byte[] aesEncrypted = null;
            byte[] encryptedAesKey = null;
            SecretKey aes = generateAESKey();
            aesEncrypted = aesEncrypt(text.getBytes("UTF-8"), aes);
            encryptedAesKey = rsaEncrypt(aes.getEncoded(), pk);
            Message m = new Message();
            m.type = "TEXT";
            m.sender = username;
            m.recipient = rec;
            m.encryptedKey = encryptedAesKey;
            m.payload = aesEncrypted;
        
            oos.writeObject(m);
            oos.flush();
        }
        System.out.println(GREEN + "Mensaje enviado (broadcast) a " + recipients.size() + " usuarios." + RESET);
    }

    void sendPrivateMessage(String toUser, String text) throws Exception {
        requestKeys();
        PublicKey pk = publicKeys.get(toUser);
        if (pk == null) {
            System.out.println(RED + "Usuario no encontrado o sin clave pública: " + toUser + RESET);
            return;
        }
        SecretKey aes = generateAESKey();
        byte[] aesEncrypted = aesEncrypt(text.getBytes("UTF-8"), aes);
        byte[] encryptedAesKey = rsaEncrypt(aes.getEncoded(), pk);

        Message m = new Message();
        m.type = "TEXT";
        m.sender = username;
        m.recipient = toUser;
        m.encryptedKey = encryptedAesKey;
        m.payload = aesEncrypted;
        oos.writeObject(m);
        oos.flush();
        System.out.println(GREEN + "Mensaje privado enviado a " + toUser + RESET);
    }

    void sendFileToUser(String toUser, String filePath) throws Exception {
        requestKeys();
        PublicKey pk = publicKeys.get(toUser);
        if (pk == null) {
            System.out.println(RED + "Usuario no encontrado: " + toUser + RESET);
            return;
        }
        File f = new File(filePath);
        if (!f.exists() || !f.isFile()) {
            System.out.println(RED + "Archivo no existe: " + filePath + RESET);
            return;
        }
        byte[] data = Files.readAllBytes(f.toPath());
        SecretKey aes = generateAESKey();
        byte[] aesEncrypted = aesEncrypt(data, aes);
        byte[] encryptedAesKey = rsaEncrypt(aes.getEncoded(), pk);

        Message m = new Message();
        m.type = "FILE";
        m.sender = username;
        m.recipient = toUser;
        m.encryptedKey = encryptedAesKey;
        m.payload = aesEncrypted;
        m.meta = new HashMap<>();
        m.meta.put("filename", f.getName());
        oos.writeObject(m);
        oos.flush();
        System.out.println(GREEN + "Archivo enviado a " + toUser + " (tamaño: " + data.length + " bytes)" + RESET);
    }

    void doLeave() {
        try {
            Message m = new Message();
            m.type = "LEAVE";
            m.sender = username;
            oos.writeObject(m);
            oos.flush();
        } catch (Exception ignored) {}
        closeEverything();
    }

    void requestList() throws IOException {
        Message m = new Message();
        m.type = "LIST_REQUEST";
        m.sender = username;
        oos.writeObject(m);
        oos.flush();
    }

    void requestKeys() throws IOException {
        Message m = new Message();
        m.type = "GET_KEYS";
        m.sender = username;
        oos.writeObject(m);
        oos.flush();
    }


    void loadKeysFromMeta(String metaStr) {
        if (metaStr == null || metaStr.trim().isEmpty()) return;
        String[] items = metaStr.split(";");
        for (String it : items) {
            if (it.trim().isEmpty()) continue;
            int idx = it.indexOf(':');
            if (idx <= 0) continue;
            String u = it.substring(0, idx);
            String b = it.substring(idx+1);
            try {
                byte[] keyBytes = Base64.getDecoder().decode(b);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PublicKey pk = kf.generatePublic(spec);
                publicKeys.put(u, pk);
            } catch (Exception e) {
      
            }
        }
    }


    String decryptPayloadForMe(Message m) throws Exception {
        byte[] plain = decryptPayloadBytesForMe(m);
        return new String(plain, "UTF-8");
    }

    byte[] decryptPayloadBytesForMe(Message m) throws Exception {
        if (m.encryptedKey == null) throw new IllegalArgumentException("No encryptedKey");
        byte[] aesKey = rsaDecrypt(m.encryptedKey, rsaKeys.getPrivate());
        SecretKeySpec sk = new SecretKeySpec(aesKey, "AES");
        return aesDecrypt(m.payload, sk);
    }


    static KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    static SecretKey generateAESKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        return kg.generateKey();
    }

    static byte[] rsaEncrypt(byte[] data, PublicKey key) throws Exception {
        Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsa.init(Cipher.ENCRYPT_MODE, key);
        return rsa.doFinal(data);
    }

    static byte[] rsaDecrypt(byte[] data, PrivateKey key) throws Exception {
        Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsa.init(Cipher.DECRYPT_MODE, key);
        return rsa.doFinal(data);
    }


    static byte[] aesEncrypt(byte[] plain, SecretKey sk) throws Exception {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        c.init(Cipher.ENCRYPT_MODE, sk, ivspec);
        byte[] ct = c.doFinal(plain);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(iv);
        bos.write(ct);
        return bos.toByteArray();
    }

    static byte[] aesDecrypt(byte[] ivAndCt, SecretKeySpec sks) throws Exception {
        byte[] iv = Arrays.copyOfRange(ivAndCt, 0, 16);
        byte[] ct = Arrays.copyOfRange(ivAndCt, 16, ivAndCt.length);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, sks, new IvParameterSpec(iv));
        return c.doFinal(ct);
    }

s
    static byte[] rsaDecrypt(byte[] data, Key key) throws Exception {
        Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsa.init(Cipher.DECRYPT_MODE, (PrivateKey) key);
        return rsa.doFinal(data);
    }

    void closeEverything() {
        try { if (ois != null) ois.close(); } catch (Exception ignored) {}
        try { if (oos != null) oos.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        System.out.println(BLUE + "Desconectado." + RESET);
    }

    void printHelp() {
        System.out.println(BLUE + "Comandos disponibles:" + RESET);
        System.out.println(BLUE + "/salir                     : Cierra sesión y desconecta." + RESET);
        System.out.println(BLUE + "/listar                    : Lista usuarios conectados." + RESET);
        System.out.println(BLUE + "/verComandos               : Muestra comandos." + RESET);
        System.out.println(BLUE + "/msg [usuario] [mensaje]   : Envía mensaje privado." + RESET);
        System.out.println(BLUE + "/enviarArchivo [usuario] [ruta] : Envía archivo a usuario." + RESET);
        System.out.println(BLUE + "/ayuda                     : Muestra esta ayuda." + RESET);
        System.out.println(BLUE + "Escribe texto normal para enviar mensaje público." + RESET);
    }
}

