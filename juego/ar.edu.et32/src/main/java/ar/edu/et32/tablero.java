package ar.edu.et32;
 class Tablero {

    public celda[][] tablero;

    public Tablero() {
        tablero = new celda[3][3];
        int i, j;
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 3; j++) {
                tablero[i][j] = new celda();
            }
        }
    }
    public boolean colocarSimbolo(int fila, int col, String simbolo) {
        if (fila < 0 || fila > 2) {
            return false;
        }
        if (col < 0 || col > 2) {
            return false;
        }
        if (!tablero[fila][col].esVacia()) {
            return false;
        }
        tablero[fila][col].marcar(simbolo);
        return true;
    }

    public boolean esGanador(String s) {
        if (tablero[0][0].estado.equals(s) && tablero[0][1].estado.equals(s) && tablero[0][2].estado.equals(s)) return true;
        if (tablero[1][0].estado.equals(s) && tablero[1][1].estado.equals(s) && tablero[1][2].estado.equals(s)) return true;
        if (tablero[2][0].estado.equals(s) && tablero[2][1].estado.equals(s) && tablero[2][2].estado.equals(s)) return true;

        
        if (tablero[0][0].estado.equals(s) && tablero[1][0].estado.equals(s) && tablero[2][0].estado.equals(s)) return true;
        if (tablero[0][1].estado.equals(s) && tablero[1][1].estado.equals(s) && tablero[2][1].estado.equals(s)) return true;
        if (tablero[0][2].estado.equals(s) && tablero[1][2].estado.equals(s) && tablero[2][2].estado.equals(s)) return true;

        
        if (tablero[0][0].estado.equals(s) && tablero[1][1].estado.equals(s) && tablero[2][2].estado.equals(s)) return true;
        if (tablero[0][2].estado.equals(s) && tablero[1][1].estado.equals(s) && tablero[2][0].estado.equals(s)) return true;

        return false;
    }

    public boolean esTableroCompleto() {
        int i, j;
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 3; j++) {
                if (tablero[i][j].esVacia()) {
                    return false;
                }
            }
        }
        return true;
    }

    public String toString() {
        String r = "\n";
        int i, j;
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 3; j++) {
                r = r + tablero[i][j].toString() + " ";
            }
            r = r + "\n";
        }
        return r;
    }
}
