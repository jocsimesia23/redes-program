package ar.edu.et32;
public class celda {
    public String estado;
    public celda() {
        estado = "esta vacia";
    }
    public void vaciar() {
        estado = "esta vacia";
    }

    public boolean esVacia() {
        return estado.equals("esta vacia");
    }

    public void marcar(String simbolo) {
        if (simbolo != null) {
            if (simbolo.equals("X") || simbolo.equals("O")) {
                estado = simbolo;
            }
        }
    }
    public String toString() {
        if (estado.equals("esta vacia")) {
            return "_";
        } else {
            return estado;
        }
    }
}
