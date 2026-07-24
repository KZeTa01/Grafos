import java.io.*;
import java.util.HashMap;
import java.util.Map;

import Excepciones.FormatoInvalido;
import Excepciones.NombreNodoInvalido;
import Modelo.Conexion;
import Modelo.Grafo;
import Modelo.NodoGrafo; 
public class PersistenciaGrafos {


    //Método encargado de guardar los grafos
    public static void guardar(Grafo grafo, String rutaArchivo) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            bw.write("NODOS");
            bw.newLine();
            for (NodoGrafo n : grafo.getNodos()) {
                bw.write(n.getID() + ";" + n.getEtiqueta() + ";" + n.getX() + ";" + n.getY());
                bw.newLine();
            }
            bw.write("CONEXIONES");
            bw.newLine();
            for (NodoGrafo n : grafo.getNodos()) {
                for (Conexion c : n.getConexionesSalientes()) {
                    bw.write(c.getOrigen().getID() + ";" + c.getDestino().getID() + ";" + c.getPeso());
                    bw.newLine();
                }
            }

        } catch (IOException e) {
            System.err.println("Error al guardar el grafo: " + e.getMessage());
        }
    }
    public static Grafo cargar(String rutaArchivo) throws FormatoInvalido {
    Grafo grafo = new Grafo();
    Map<Integer, NodoGrafo> mapaPorId = new HashMap<>();
    boolean encontroSeccionNodos = false;

    try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
        String linea;
        String seccionActual = "";

        while ((linea = br.readLine()) != null) {
            if (linea.equals("NODOS")) {
                seccionActual = linea;
                encontroSeccionNodos = true;
                continue;
            }
            if (linea.equals("CONEXIONES")) {
                seccionActual = linea;
                continue;
            }
            if (linea.isBlank()) continue;

            String[] partes = linea.split(";");

            if (seccionActual.equals("NODOS")) {
                if (partes.length != 4) {
                    throw new FormatoInvalido("El archivo está incompleto o dañado (línea de nodo mal formada).");
                }
                try {
                    int id = Integer.parseInt(partes[0]);
                    String valor = partes[1];
                    int x = Integer.parseInt(partes[2]);
                    int y = Integer.parseInt(partes[3]);

                    NodoGrafo nodo = grafo.agregarNodo(valor, x, y);
                    mapaPorId.put(id, nodo);
                } catch (NumberFormatException ex) {
                    throw new FormatoInvalido("El archivo contiene datos no numéricos donde se esperaban coordenadas o ids.");
                }

            } else if (seccionActual.equals("CONEXIONES")) {
                if (partes.length != 3) {
                    throw new FormatoInvalido("El archivo está incompleto o dañado (línea de conexión mal formada).");
                }
                try {
                    int idOrigen = Integer.parseInt(partes[0]);
                    int idDestino = Integer.parseInt(partes[1]);
                    int peso = Integer.parseInt(partes[2]);

                    NodoGrafo origen = mapaPorId.get(idOrigen);
                    NodoGrafo destino = mapaPorId.get(idDestino);

                    if (origen == null || destino == null) {
                        throw new FormatoInvalido("El archivo hace referencia a un nodo que no existe.");
                    }
                    grafo.agregarConexion(origen, destino, peso);
                } catch (NumberFormatException ex) {
                    throw new FormatoInvalido("El archivo contiene datos no numéricos en una conexión.");
                }

            } else {
                // Encontró contenido antes de llegar a "NODOS" -> no es un archivo de grafo
                throw new FormatoInvalido("Este no es un archivo de grafo válido.");
            }
        }

    } catch (IOException e) {
        throw new FormatoInvalido("No se pudo leer el archivo: " + e.getMessage());
    }

    if (!encontroSeccionNodos) {
        throw new FormatoInvalido("Este no es un archivo de grafo válido.");
    }

    return grafo;
}
}
