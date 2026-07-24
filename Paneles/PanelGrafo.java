package Paneles;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import Excepciones.*;
import Modelo.*;

public class PanelGrafo extends JPanel {

    // ------------------------------------------------------------------
    // Estado visual y de selección del panel
    // ------------------------------------------------------------------
    private NodoGrafo nodoInicial;
    private NodoGrafo nodoHorver;
    private JPopupMenu menuContextual;
    private NodoGrafo nodoSeleccionado;
    private boolean edicionBloqueada = false;

    // ------------------------------------------------------------------
    // Estado del grafo y del historial de acciones
    // ------------------------------------------------------------------
    private Stack<Runnable> historialDeshacer = new Stack<>();
    private Grafo grafo;

    // ------------------------------------------------------------------
    // Estado de interacción del ratón
    // ------------------------------------------------------------------
    private final int RADIO_NODO = 20;
    private NodoGrafo nodoSeleccionadoParaMover = null;
    private NodoGrafo nodoOrigenConexion = null;
    private Point puntoRatonArrastre = null;
    private boolean modoConectar;

    // ------------------------------------------------------------------
    // Constructor y configuración inicial
    // ------------------------------------------------------------------
    public PanelGrafo(Grafo grafo) {
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.blue, 1), "Visualización"));
        this.grafo = grafo;
        inicializarMenuContextual();

        // Centraliza todos los eventos del mouse en un único manejador
        ManejadorRaton manejador = new ManejadorRaton();
        addMouseListener(manejador);
        addMouseMotionListener(manejador);

        // Configuración de Ctrl + Z para deshacer
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Deshacer");

        actionMap.put("Deshacer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deshacerAccion();
            }
        });
    }

    // ------------------------------------------------------------------
    // Métodos públicos de control del panel
    // ------------------------------------------------------------------
    public void limpiar() {
        grafo = new Grafo();
        nodoInicial = null;
        nodoHorver = null;
        nodoSeleccionado = null;
        nodoSeleccionadoParaMover = null;
        nodoOrigenConexion = null;
        puntoRatonArrastre = null;
        historialDeshacer.clear();
        repaint();
    }

    // Habilita o deshabilita la edición del lienzo mientras se anima un algoritmo
    public void setEdicionBloqueada(boolean bloqueada) {
        this.edicionBloqueada = bloqueada;
    }

    public boolean isEdicionBloqueada() {
        return edicionBloqueada;
    }

    public void setModoConectar(boolean modo) {
        this.modoConectar = modo;

        // Limpia el estado residual si se desactiva el modo de conexión
        if (!modo) {
            nodoOrigenConexion = null;
            puntoRatonArrastre = null;
            repaint();
        }
    }

    public Grafo getGrafo() {
        return grafo;
    }

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        repaint();
    }

    public NodoGrafo getNodoInicial() {
        return nodoInicial;
    }

    public boolean tieneNodos() {
        return !grafo.getNodos().isEmpty();
    }

    // ------------------------------------------------------------------
    // Menú contextual y acciones de edición
    // ------------------------------------------------------------------
    private void inicializarMenuContextual() {
        menuContextual = new JPopupMenu();

        // Opción para marcar al nodo como el inicio del grafo
        JMenuItem itemInicio = new JMenuItem("Marcar como inicio");
        itemInicio.addActionListener(e -> {
            if (nodoSeleccionado != null) {
                nodoInicial = nodoSeleccionado;
                repaint();
            }
        });

        // Opción para eliminar el nodo
        JMenuItem itemEliminar = new JMenuItem("Eliminar Nodo");
        itemEliminar.addActionListener(e -> {
            if (nodoSeleccionado != null) {
                grafo.eliminarNodo(nodoSeleccionado);
                if (nodoInicial == nodoSeleccionado) {
                    nodoInicial = null;
                }
                repaint();
            }
        });

        JMenuItem itemEliminarConexion = new JMenuItem("Eliminar conexión saliente...");
        itemEliminarConexion.addActionListener(e -> {
            if (nodoSeleccionado != null) {
                // Obtiene la lista de flechas que salen de este nodo
                java.util.List<Conexion> salientes = nodoSeleccionado.getConexionesSalientes();

                if (salientes.isEmpty()) {
                    JOptionPane.showMessageDialog(PanelGrafo.this,
                            "Este nodo no tiene conexiones salientes.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Extrae los nombres de los nodos destino para mostrarlos en la lista
                String[] opcionesDestino = new String[salientes.size()];
                for (int i = 0; i < salientes.size(); i++) {
                    opcionesDestino[i] = salientes.get(i).getDestino().getEtiqueta();
                }

                // Muestra el cuadro de diálogo como un menú desplegable
                String seleccion = (String) JOptionPane.showInputDialog(
                        PanelGrafo.this,
                        "Seleccione la conexión que desea eliminar:",
                        "Eliminar Conexión",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        opcionesDestino,
                        opcionesDestino[0]
                );

                // Si el usuario eligió una opción y no dio a Cancelar
                if (seleccion != null) {
                    for (Conexion c : salientes) {
                        if (c.getDestino().getEtiqueta().equals(seleccion)) {
                            // Guarda los datos antes de borrar para poder deshacer
                            final NodoGrafo origen = nodoSeleccionado;
                            final NodoGrafo destino = c.getDestino();
                            final int peso = c.getPeso();

                            // Borra la conexión
                            origen.eliminarConexionHacia(destino);

                            // Guarda la acción contraria para deshacer
                            historialDeshacer.push(() -> grafo.agregarConexion(origen, destino, peso));

                            repaint();
                            break;
                        }
                    }
                }
            }
        });

        menuContextual.add(itemEliminarConexion);
        menuContextual.add(itemInicio);
        menuContextual.addSeparator();
        menuContextual.add(itemEliminar);
    }

    // ------------------------------------------------------------------
    // Renderizado del grafo
    // ------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dibuja las conexiones existentes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        for (NodoGrafo nodo : grafo.getNodos()) {
            for (Conexion conexion : nodo.getConexionesSalientes()) {
                NodoGrafo destino = conexion.getDestino();
                dibujarAristaDirigida(g2d, nodo.getX(), nodo.getY(), destino.getX(), destino.getY(), conexion.getPeso());
            }
        }

        // Dibuja una línea temporal al arrastrar para conectar
        if (nodoOrigenConexion != null && puntoRatonArrastre != null) {
            g2d.setColor(Color.RED);
            g2d.drawLine(nodoOrigenConexion.getX(), nodoOrigenConexion.getY(), puntoRatonArrastre.x, puntoRatonArrastre.y);
        }

        // Dibuja los nodos con el color según su estado
        for (NodoGrafo nodo : grafo.getNodos()) {

            // Prioridad 1: animación desde hilos
            if (nodo.getEstadoAnimacion() == Modelo.EstadoAnimacion.ACTUAL) {
                g2d.setColor(new Color(34, 197, 94));
            } else if (nodo.getEstadoAnimacion() == Modelo.EstadoAnimacion.VISITADO) {
                g2d.setColor(new Color(245, 165, 36));

                // Prioridad 2: interacción manual del usuario
            } else if (nodo == nodoInicial) {
                g2d.setColor(Color.GREEN);
            } else if (nodo == nodoHorver) {
                g2d.setColor(Color.YELLOW);
            } else {
                g2d.setColor(new Color(173, 216, 230));
            }

            // Relleno
            g2d.fillOval(nodo.getX() - RADIO_NODO, nodo.getY() - RADIO_NODO, RADIO_NODO * 2, RADIO_NODO * 2);

            // Borde
            g2d.setColor(Color.BLACK);
            g2d.drawOval(nodo.getX() - RADIO_NODO, nodo.getY() - RADIO_NODO, RADIO_NODO * 2, RADIO_NODO * 2);

            // Texto central del nodo
            FontMetrics fm = g2d.getFontMetrics();
            int textoAncho = fm.stringWidth(nodo.getEtiqueta());
            int textoAlto = fm.getAscent();
            g2d.drawString(nodo.getEtiqueta(), nodo.getX() - (textoAncho / 2), nodo.getY() + (textoAlto / 4));
        }
    }

    private void dibujarAristaDirigida(Graphics2D g2d, int x1, int y1, int x2, int y2, int peso) {
        int tamañoPunta = 10;

        // Calcula la diferencia de coordenadas y el ángulo original
        int dx = x2 - x1;
        int dy = y2 - y1;
        double angulo = Math.atan2(dy, dx);

        // Calcula el desplazamiento perpendicular para separar la línea del centro del nodo
        int separacion = 8;
        int offsetX = (int) (separacion * Math.cos(angulo + Math.PI / 2));
        int offsetY = (int) (separacion * Math.sin(angulo + Math.PI / 2));

        // Aplica el desplazamiento a los centros virtuales
        int x1Offset = x1 + offsetX;
        int y1Offset = y1 + offsetY;
        int x2Offset = x2 + offsetX;
        int y2Offset = y2 + offsetY;

        // Calcula el destino real para que la punta no quede debajo del círculo
        int xDestino = (int) (x2Offset - RADIO_NODO * Math.cos(angulo));
        int yDestino = (int) (y2Offset - RADIO_NODO * Math.sin(angulo));

        // Dibuja la línea principal ya separada
        g2d.drawLine(x1Offset, y1Offset, xDestino, yDestino);

        // Calcula los vértices de la punta de la flecha
        int xPunta1 = (int) (xDestino - tamañoPunta * Math.cos(angulo - Math.PI / 6));
        int yPunta1 = (int) (yDestino - tamañoPunta * Math.sin(angulo - Math.PI / 6));

        int xPunta2 = (int) (xDestino - tamañoPunta * Math.cos(angulo + Math.PI / 6));
        int yPunta2 = (int) (yDestino - tamañoPunta * Math.sin(angulo + Math.PI / 6));

        // Dibuja la punta del polígono
        Polygon punta = new Polygon();
        punta.addPoint(xDestino, yDestino);
        punta.addPoint(xPunta1, yPunta1);
        punta.addPoint(xPunta2, yPunta2);
        g2d.fillPolygon(punta);

        // Dibuja el peso de la conexión en el centro desplazado
        int xMedio = (x1Offset + xDestino) / 2;
        int yMedio = (y1Offset + yDestino) / 2;

        // Fondo blanco circular para legibilidad
        g2d.setColor(Color.WHITE);
        g2d.fillOval(xMedio - 10, yMedio - 10, 20, 20);

        // Texto del peso
        g2d.setColor(Color.RED);
        g2d.drawString(String.valueOf(peso), xMedio - 4, yMedio + 4);

        // Restaura el color negro
        g2d.setColor(Color.BLACK);
    }

    // ------------------------------------------------------------------
    // Métodos para el estado visual de los nodos
    // ------------------------------------------------------------------
    public void marcarEstadoNodo(NodoGrafo nodo, Modelo.EstadoAnimacion estado) {
        nodo.setEstadoAnimacion(estado);
        repaint();
    }

    public void restablecerEstadosAnimacion() {
        for (NodoGrafo n : grafo.getNodos()) {
            n.setEstadoAnimacion(Modelo.EstadoAnimacion.NORMAL);
        }
        repaint();
    }

    // ------------------------------------------------------------------
    // Clase interna para manejar eventos del ratón
    // ------------------------------------------------------------------
    private class ManejadorRaton extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (edicionBloqueada) {
                return;
            }
            NodoGrafo nodoClickeado = grafo.getNodoEnCoordenadas(e.getX(), e.getY(), RADIO_NODO);

            // Clic derecho -> menú contextual
            if (SwingUtilities.isRightMouseButton(e)) {
                if (nodoClickeado != null) {
                    nodoSeleccionado = nodoClickeado;
                    menuContextual.show(PanelGrafo.this, e.getX(), e.getY());
                }
                return;
            }

            // Clic izquierdo -> crear, mover o conectar
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (nodoClickeado == null) {
                    // Clic en el vacío: crear nodo nuevo
                    String nombre = JOptionPane.showInputDialog(PanelGrafo.this, "Nombre del nuevo nodo:");
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        try {
                            NodoGrafo nuevoNodo = grafo.agregarNodo(nombre, e.getX(), e.getY());
                            if (nuevoNodo != null) {
                                // Guarda la operación contraria para poder deshacerla
                                historialDeshacer.push(() -> grafo.eliminarNodo(nuevoNodo));
                                repaint();
                            }
                        } catch (NombreNodoInvalido ex) {
                            JOptionPane.showMessageDialog(PanelGrafo.this, ex.getMessage(), "Nodo Existente", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                } else {
                    // Clic sobre un nodo existente
                    if (modoConectar) {
                        nodoOrigenConexion = nodoClickeado;
                        puntoRatonArrastre = e.getPoint();
                    } else {
                        nodoSeleccionadoParaMover = nodoClickeado;
                    }
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (edicionBloqueada) {
                return;
            }

            if (nodoSeleccionadoParaMover != null) {
                nodoSeleccionadoParaMover.setX(e.getX());
                nodoSeleccionadoParaMover.setY(e.getY());
                repaint();
            } else if (nodoOrigenConexion != null) {
                puntoRatonArrastre = e.getPoint();
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (edicionBloqueada) {
                // Limpia cualquier estado residual si se soltó el clic mientras estaba bloqueado
                nodoOrigenConexion = null;
                puntoRatonArrastre = null;
                nodoSeleccionadoParaMover = null;
                return;
            }

            if (modoConectar && nodoOrigenConexion != null) {
                NodoGrafo nodoDestino = grafo.getNodoEnCoordenadas(e.getX(), e.getY(), RADIO_NODO);
                if (nodoDestino == null) {
                    JOptionPane.showMessageDialog(PanelGrafo.this, "Debe conectar hacia un nodo existente");
                } else if (nodoOrigenConexion == nodoDestino) {
                    JOptionPane.showMessageDialog(PanelGrafo.this, "No se permiten conexiones hacia el mismo nodo");
                } else {
                    String pesoString = JOptionPane.showInputDialog(PanelGrafo.this, "Ingrese el peso de la conexión:", "1");
                    if (pesoString != null && !pesoString.isEmpty()) {
                        try {
                            int peso = Integer.parseInt(pesoString);
                            if (peso < 1) {
                                throw new PesoInvalido("El peso debe ser positivo");
                            }
                            boolean exito = grafo.agregarConexion(nodoOrigenConexion, nodoDestino, peso);

                            if (exito) {
                                final NodoGrafo origen = nodoOrigenConexion;
                                final NodoGrafo destino = nodoDestino;
                                historialDeshacer.push(() -> origen.eliminarConexionHacia(destino));
                            } else {
                                JOptionPane.showMessageDialog(PanelGrafo.this, "ERROR: Ya existe la conexión.", "Aviso", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(PanelGrafo.this, "El peso de la conexión debe ser un número entero.");
                        } catch (PesoInvalido ex) {
                            JOptionPane.showMessageDialog(PanelGrafo.this, ex.getMessage());
                        }
                    }
                }
            }
            nodoOrigenConexion = null;
            puntoRatonArrastre = null;
            nodoSeleccionadoParaMover = null;

            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            NodoGrafo nodoBajoCursor = grafo.getNodoEnCoordenadas(e.getX(), e.getY(), RADIO_NODO);
            if (nodoHorver != nodoBajoCursor) {
                nodoHorver = nodoBajoCursor;
                repaint();
            }
        }
    }

    // ------------------------------------------------------------------
    // Deshacer
    // ------------------------------------------------------------------
    public void deshacerAccion() {
        if (edicionBloqueada) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (!historialDeshacer.isEmpty()) {
            historialDeshacer.pop().run();
            repaint();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}