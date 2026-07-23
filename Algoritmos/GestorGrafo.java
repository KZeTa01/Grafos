package Algoritmos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import Modelo.Conexion;
import Modelo.Grafo;
import Modelo.NodoGrafo;

public class GestorGrafo {

    // Recorrido BFS: devuelve el orden de visita de los nodos
    public static List<NodoGrafo> bfs(Grafo grafo, NodoGrafo inicio) {
        List<NodoGrafo> orden = new ArrayList<>();
        Set<NodoGrafo> visitados = new HashSet<>();
        Queue<NodoGrafo> cola = new LinkedList<>();

        cola.add(inicio);
        visitados.add(inicio);

        while (!cola.isEmpty()) {
            NodoGrafo actual = cola.poll();
            orden.add(actual);

            for (Conexion c : actual.getConexionesSalientes()) {
                NodoGrafo vecino = c.getDestino();
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }
        return orden;
    }

    // Recorrido DFS: devuelve el orden de visita de los nodos
    public static List<NodoGrafo> dfs(Grafo grafo, NodoGrafo inicio) {
        List<NodoGrafo> orden = new ArrayList<>();
        Set<NodoGrafo> visitados = new HashSet<>();
        dfsRecursivo(inicio, visitados, orden);
        return orden;
    }

    private static void dfsRecursivo(NodoGrafo actual, Set<NodoGrafo> visitados, List<NodoGrafo> orden) {
        visitados.add(actual);
        orden.add(actual);

        for (Conexion c : actual.getConexionesSalientes()) {
            NodoGrafo vecino = c.getDestino();
            if (!visitados.contains(vecino)) {
                dfsRecursivo(vecino, visitados, orden);
            }
        }
    }
}