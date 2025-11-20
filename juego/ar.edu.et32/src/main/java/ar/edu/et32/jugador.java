package ar.edu.et32;
public class jugador {
    public String nombre;
    public String simbolo;
    public jugador(String n, String s) {
        nombre = n;
        simbolo = s;
    }
    public String getNombre() {
        return nombre;
    }
    public String getSimbolo() {
        return simbolo;
    }
}
