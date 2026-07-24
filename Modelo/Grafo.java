package Modelo;

import java.util.*;

import Excepciones.NombreNodoInvalido;

public class Grafo {

    // ------------------------------------------------------------------
    // Estado del grafo
    // ------------------------------------------------------------------
    private final List<NodoGrafo> nodos;
    private int contadorIDs;
    private final int LONGITUD_MAXIMA_NOMBRE = 9;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public Grafo() {
        this.nodos = new ArrayList<>();
        this.contadorIDs = 1;
    }

    // ------------------------------------------------------------------
    // Accesores
    // ------------------------------------------------------------------
    public List<NodoGrafo> getNodos() {
        return nodos;
    }

    // ------------------------------------------------------------------
    // Validaciones y operaciones de nodos
    // ------------------------------------------------------------------
    public boolean existeNodoConEtiqueta(String etiqueta) {
        for (NodoGrafo n : nodos) {
            if (n.getEtiqueta().equalsIgnoreCase(etiqueta)) {
                return true;
            }
        }
        return false;
    }

    public NodoGrafo agregarNodo(String etiqueta, int x, int y) throws NombreNodoInvalido {
        String nombre = (etiqueta == null) ? "" : etiqueta;

        if (existeNodoConEtiqueta(nombre)) {
            throw new NombreNodoInvalido("Ya existe un nodo con el nombre: " + nombre);
        }
        if (nombre.isEmpty()) {
            throw new NombreNodoInvalido("El nombre del nodo no puede estar vacío.");
        }
        if (nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new NombreNodoInvalido("El nombre no puede tener más de " + LONGITUD_MAXIMA_NOMBRE + " carácteres.");
        }

        NodoGrafo nuevo = new NodoGrafo(contadorIDs++, nombre, x, y);
        nodos.add(nuevo);
        return nuevo;
    }

    // ------------------------------------------------------------------
    // Operaciones de conexiones
    // ------------------------------------------------------------------
    public boolean agregarConexion(NodoGrafo origen, NodoGrafo destino, int peso) {
        if (origen.equals(destino)) {
            return false;
        }

        for (Conexion conexion : origen.getConexionesSalientes()) {
            if (conexion.getDestino().equals(destino)) {
                return false;
            }
        }

        origen.agregarConexion(new Conexion(origen, destino, peso));
        return true;
    }

    public void eliminarNodo(NodoGrafo nodoAEliminar) {
        nodos.remove(nodoAEliminar);

        for (NodoGrafo nodo : nodos) {
            nodo.eliminarConexionHacia(nodoAEliminar);
        }
    }

    // ------------------------------------------------------------------
    // Utilidades de interacción
    // ------------------------------------------------------------------
    public NodoGrafo getNodoEnCoordenadas(int clickX, int clickY, int radio) {
        for (NodoGrafo n : nodos) {
            int dx = n.getX() - clickX;
            int dy = n.getY() - clickY;
            if (dx * dx + dy * dy <= radio * radio) {
                return n;
            }
        }
        return null;
    }
}
