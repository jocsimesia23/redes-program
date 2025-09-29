package ar.edu.et32;
import java.io.Serializable;
import java.util.Map;

public class guia4 {

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public String type;
    public String sender;
    public String recipient;
    public byte[] encryptedKey; 
    public byte[] payload; 
    public java.util.Map<String, String> meta; 

    public Message() {}
}
}