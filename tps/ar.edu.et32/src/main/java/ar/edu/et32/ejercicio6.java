package ar.edu.et32;
import java.util.*;
public class ejercicio6 {

    public static void main(String[] args) {
        List<Alumno> alumnos = new ArrayList<>();
        alumnos.add(new Alumno("Ana","Lopez"));
        alumnos.add(new Alumno("Juan","Perez"));
        alumnos.add(new Alumno("Maria","Diaz"));
        alumnos.add(new Alumno("Luis","Gomez"));

        Thread preceptor = new Thread(new Preceptor(alumnos));
        Thread docente = new Thread(new Docente(alumnos));

        preceptor.start();
        docente.start();

        try {
            preceptor.join();
            docente.join();
        } catch (InterruptedException e) {}

        for (Alumno a : alumnos) {
            a.calcularRegularidad();
            System.out.println(a);
        }
    }
}

class Alumno {
    String nombre, apellido;
    int[] notas = new int[3];
    int asistencia;
    boolean esRegular;

    Alumno(String n, String a){ nombre=n; apellido=a; }

    void calcularRegularidad() {
        double promedio = (notas[0]+notas[1]+notas[2])/3.0;
        esRegular = asistencia >= 75 && promedio >= 6;
    }

    public String toString() {
        return nombre+" "+apellido+" | Asistencia:"+asistencia+"% | Prom:"+(
            (notas[0]+notas[1]+notas[2])/3.0)+" | Regular:"+esRegular;
    }
}

class Preceptor implements Runnable {
    List<Alumno> alumnos;
    Preceptor(List<Alumno> a){ alumnos=a; }
    public void run() {
        Random r = new Random();
        for (Alumno al: alumnos) {
            al.asistencia = r.nextInt(101);
        }
    }
}

class Docente implements Runnable {
    List<Alumno> alumnos;
    Docente(List<Alumno> a){ alumnos=a; }
    public void run() {
        Random r = new Random();
        for (Alumno al: alumnos) {
            for (int i=0;i<3;i++) al.notas[i]=r.nextInt(11);
        }
    }
}
