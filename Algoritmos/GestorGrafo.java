package Algoritmos;

import Modelo.Conexion;
import Modelo.EstadoAnimacion;
import Modelo.Grafo;
import Modelo.NodoGrafo;
import Paneles.PanelGrafo;
import Paneles.PanelResultados;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.util.*;

public class GestorGrafo {

    private enum TipoAlgoritmo { BFS, DFS, DIJKSTRA }

    public void animarBFS(PanelGrafo panelGrafo, PanelResultados reporte, NodoGrafo inicio,
                           int velocidadMs, JButton botonEjecutar) {
        ejecutarAnimado(panelGrafo, reporte, inicio, velocidadMs, botonEjecutar, TipoAlgoritmo.BFS);
    }

    public void animarDFS(PanelGrafo panelGrafo, PanelResultados reporte, NodoGrafo inicio,
                           int velocidadMs, JButton botonEjecutar) {
        ejecutarAnimado(panelGrafo, reporte, inicio, velocidadMs, botonEjecutar, TipoAlgoritmo.DFS);
    }

    public void animarDijkstra(PanelGrafo panelGrafo, PanelResultados reporte, NodoGrafo inicio,
                                int velocidadMs, JButton botonEjecutar) {
        ejecutarAnimado(panelGrafo, reporte, inicio, velocidadMs, botonEjecutar, TipoAlgoritmo.DIJKSTRA);
    }

    private void ejecutarAnimado(final PanelGrafo panelGrafo, final PanelResultados reporte,
                                  final NodoGrafo inicio, final int velocidadMs,
                                  final JButton botonEjecutar, final TipoAlgoritmo tipo) {

        // Esto SI corre en el EDT todavía (es el clic del botón "Ejecutar")
        botonEjecutar.setEnabled(false);
        panelGrafo.restablecerEstadosAnimacion();
        reporte.limpiar();

        // NOTA: setEdicionBloqueada(boolean) lo implementa Integrante 1 en PanelGrafo.
        // Evita que se edite el grafo (crear/mover/eliminar nodos) mientras el hilo
        // del algoritmo está recorriendo las mismas listas, para no provocar
        // ConcurrentModificationException.
        panelGrafo.setEdicionBloqueada(true);

        Thread hiloAlgoritmo = new Thread(new Runnable() {
            @Override
            public void run() {
                final List<String> ordenEtiquetas = new ArrayList<>();
                final Set<NodoGrafo> visitados = new LinkedHashSet<>();

                Map<NodoGrafo, Integer> distancias = null;
                Map<NodoGrafo, NodoGrafo> previo = null;

                switch (tipo) {
                    case BFS:
                        recorrerBFS(inicio, panelGrafo, reporte, velocidadMs, ordenEtiquetas, visitados);
                        break;
                    case DFS:
                        recorrerDFS(inicio, panelGrafo, reporte, velocidadMs, ordenEtiquetas, visitados);
                        break;
                    case DIJKSTRA:
                        previo = new HashMap<>();
                        distancias = recorrerDijkstra(inicio, panelGrafo, reporte, velocidadMs,
                                ordenEtiquetas, visitados, previo);
                        break;
                }

                final int totalNodos = panelGrafo.getGrafo().getNodos().size();
                final boolean todosAlcanzados = visitados.size() == totalNodos;
                final Map<NodoGrafo, Integer> distanciasFinales = distancias;
                final Map<NodoGrafo, NodoGrafo> previoFinal = previo;

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (tipo == TipoAlgoritmo.DIJKSTRA && distanciasFinales != null) {
                            reportarCaminosDijkstra(reporte, inicio, distanciasFinales, previoFinal,
                                    panelGrafo.getGrafo());
                        }
                        reporte.registrarResumen(ordenEtiquetas, todosAlcanzados);
                        botonEjecutar.setEnabled(true);
                        panelGrafo.setEdicionBloqueada(false);
                        
                    }
                });
            }
        });

        hiloAlgoritmo.start();
    }

    // ================== BFS ==================
    private void recorrerBFS(NodoGrafo inicio, final PanelGrafo panelGrafo, final PanelResultados reporte,
                              int velocidadMs, final List<String> ordenEtiquetas, final Set<NodoGrafo> visitados) {
        Queue<NodoGrafo> cola = new LinkedList<>();
        cola.add(inicio);
        visitados.add(inicio);

        while (!cola.isEmpty()) {
            final NodoGrafo actual = cola.poll();
            ordenEtiquetas.add(actual.getEtiqueta());

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panelGrafo.marcarEstadoNodo(actual, EstadoAnimacion.ACTUAL);
                    reporte.registrarPaso("Visitando nodo: " + actual.getEtiqueta());
                }
            });
            pausar(velocidadMs);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panelGrafo.marcarEstadoNodo(actual, EstadoAnimacion.VISITADO);
                }
            });

            for (Conexion c : actual.getConexionesSalientes()) {
                NodoGrafo vecino = c.getDestino();
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    cola.add(vecino);
                }
            }
        }
    }

    // ================== DFS ==================
    private void recorrerDFS(final NodoGrafo actual, final PanelGrafo panelGrafo, final PanelResultados reporte,
                              final int velocidadMs, final List<String> ordenEtiquetas, final Set<NodoGrafo> visitados) {
        visitados.add(actual);
        ordenEtiquetas.add(actual.getEtiqueta());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panelGrafo.marcarEstadoNodo(actual, EstadoAnimacion.ACTUAL);
                reporte.registrarPaso("Visitando nodo: " + actual.getEtiqueta());
            }
        });
        pausar(velocidadMs);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panelGrafo.marcarEstadoNodo(actual, EstadoAnimacion.VISITADO);
            }
        });

        for (Conexion c : actual.getConexionesSalientes()) {
            NodoGrafo vecino = c.getDestino();
            if (!visitados.contains(vecino)) {
                recorrerDFS(vecino, panelGrafo, reporte, velocidadMs, ordenEtiquetas, visitados);
            }
        }
    }

    // ================== DIJKSTRA ==================
    // Asume pesos positivos (Integrante 1 valida esto al crear la conexión con PesoInvalido).
    private Map<NodoGrafo, Integer> recorrerDijkstra(final NodoGrafo inicio, final PanelGrafo panelGrafo,
            final PanelResultados reporte, final int velocidadMs, final List<String> ordenEtiquetas,
            final Set<NodoGrafo> visitados, final Map<NodoGrafo, NodoGrafo> previo) {

        final Map<NodoGrafo, Integer> distancias = new HashMap<>();
        for (NodoGrafo n : panelGrafo.getGrafo().getNodos()) {
            distancias.put(n, Integer.MAX_VALUE);
        }
        distancias.put(inicio, 0);

        PriorityQueue<NodoGrafo> cola = new PriorityQueue<>(Comparator.comparingInt(distancias::get));
        cola.add(inicio);

        while (!cola.isEmpty()) {
            final NodoGrafo actual = cola.poll();
            if (visitados.contains(actual)) continue; // entrada obsoleta (PriorityQueue no reordena in-place)
            visitados.add(actual);
            ordenEtiquetas.add(actual.getEtiqueta());
            final int distanciaActual = distancias.get(actual);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panelGrafo.marcarEstadoNodo(actual, EstadoAnimacion.ACTUAL);
                    reporte.registrarPaso("Visitando nodo: " + actual.getEtiqueta()
                            + " (distancia acumulada: " + distanciaActual + ")");
                }
            });
            pausar(velocidadMs);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panelGrafo.marcarEstadoNodo(actual, EstadoAnimacion.VISITADO);
                }
            });

            for (Conexion c : actual.getConexionesSalientes()) {
                NodoGrafo vecino = c.getDestino();
                if (visitados.contains(vecino)) continue;
                int nuevaDistancia = distancias.get(actual) + c.getPeso();
                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    previo.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }
        return distancias;
    }

    private void reportarCaminosDijkstra(PanelResultados reporte, NodoGrafo inicio,
            Map<NodoGrafo, Integer> distancias, Map<NodoGrafo, NodoGrafo> previo, Grafo grafo) {
        reporte.registrarPaso("--- Caminos más cortos desde " + inicio.getEtiqueta() + " ---");
        for (NodoGrafo n : grafo.getNodos()) {
            if (n == inicio) continue;
            Integer distancia = distancias.get(n);
            if (distancia == null || distancia == Integer.MAX_VALUE) {
                reporte.registrarPaso(n.getEtiqueta() + ": inalcanzable");
                continue;
            }
            List<String> camino = new ArrayList<>();
            NodoGrafo actual = n;
            while (actual != null) {
                camino.add(0, actual.getEtiqueta());
                actual = previo.get(actual);
            }
            reporte.registrarPaso(n.getEtiqueta() + ": " + String.join(" -> ", camino)
                    + " (peso total: " + distancia + ")");
        }
    }

    private void pausar(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}