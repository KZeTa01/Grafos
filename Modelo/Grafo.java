package Modelo;

import java.util.*;

public class Grafo {
    private List<NodoGrafo> nodos;
    private int contadorIDs; //Generador de id unicos

    public Grafo(){
        this.nodos = new ArrayList<>();
        this.contadorIDs=1;
    }

    public List<NodoGrafo> getNodos(){
        return nodos;
    }
    //Verificar si el nodo existe
    public boolean existeNodoConEtiqueta(String etiqueta){
        for (NodoGrafo n : nodos) {
            if (n.getEtiqueta().equalsIgnoreCase(etiqueta)){
                return true;
            }
        }
        return false;
    }

    //Crear nuevo nodo
    public NodoGrafo agregarNodo(String etiqueta, int x, int y){
        if (existeNodoConEtiqueta(etiqueta)) {
            return null;
        }
        NodoGrafo nuevo = new NodoGrafo(contadorIDs++, etiqueta, x, y);
        nodos.add(nuevo);
        return nuevo;
    }

    //Conectar nodos
    public boolean agregarConexion(NodoGrafo origen, NodoGrafo destino, int peso){
        //Validar si el nodo origen es igual al nodo destino
        if (origen.equals(destino)) {
            return false;
        }
        //Validar si la conexión ya fue registrada (verificar las conexiones de origen una 
        // por una y ver si ya está el destino mencionado)
        for (Conexion conexion : origen.getConexionesSalientes()) {
            if (conexion.getDestino().equals(destino)) {
                return false;
            }
        }
        //Si pasa las validaciones, se crea la conexion
        origen.agregarConexion(new Conexion(origen, destino, peso));
        return true;
    }
    
    //Eliminar nodo
    public void eliminarNodo(NodoGrafo nodoAEliminar){
        //Elimina el nodo de la lista de nodos
        nodos.remove(nodoAEliminar);

        //Eliminar cualquier conexión con el nodo eliminado
        //Identificar conexión que apunte al nodo borrado
        for (NodoGrafo nodo : nodos) {
            nodo.eliminarConexionHacia(nodoAEliminar);
        }
    }

    //Método para buscar nodo en el canva, al hacer click
    //Se comparte las coordenadas del click y se verifica si hizo click en el rango del nodo
    public NodoGrafo getNodoEnCoordenadas(int clickX, int clickY, int radio){
        for (NodoGrafo n : nodos) {
            int dx = n.getX()-clickX; //Distancia entre la coordeanda del click y la coordenada real en x;
            int dy = n.getY()-clickY; //Distancia entre la coordeanda del click y la coordenada real en y;
            if (dx*dx + dy*dy <= radio*radio) {
                return n;
            }
        }
        return null; //No se hizo click en algún nodo (o se dio click fuera del rango)
    }
}
