package Paneles;
import java.awt.*;
import java.awt.event.*;
import Modelo.*;
import javax.swing.*;
import java.util.*;

public class PanelGrafo extends JPanel {
    // Variables a usar
    private NodoGrafo nodoInicial;
    private NodoGrafo nodoHorver;
    private JPopupMenu menuContextual;
    private NodoGrafo nodoSeleccionado;

    private Stack<Runnable> historialDeshacer = new Stack<>();
    
    private Grafo grafo;
    private final int RADIO_NODO = 20;
    private NodoGrafo nodoSeleccionadoParaMover = null;
    private NodoGrafo nodoOrigenConexion = null;
    private Point puntoRatonArrastre = null;
    private boolean modoConectar;

    public PanelGrafo( Grafo grafo) {
        setBorder(BorderFactory.createTitledBorder("Visualización"));
        this.grafo=grafo;
        inicializarMenuContextual();

        // Ahora el manejador centraliza todos los eventos del mouse
        ManejadorRaton manejador = new ManejadorRaton();
        addMouseListener(manejador);
        addMouseMotionListener(manejador);

        //Configurar confiduración Ctrl + z
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        // Detectar Ctrl + Z
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Deshacer");
        
        // Vincularlo a nuestro método
        actionMap.put("Deshacer", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deshacerAccion();
            }
        });
    }

    public void limpiar() {
        grafo = new Grafo();
        nodoInicial = null;
    repaint();
}

    private void inicializarMenuContextual() {
        menuContextual = new JPopupMenu();
        
        // Opcion para marcar al nodo como el inicio del grafo
        JMenuItem itemInicio = new JMenuItem("Marcar como inicio");
        itemInicio.addActionListener(e -> {    
            if (nodoSeleccionado != null) {
                nodoInicial = nodoSeleccionado;
                repaint();
            }
        });
        
        // Opcion para eliminar el nodo
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
                // Obtenemos la lista de flechas que salen de este nodo
                java.util.List<Conexion> salientes = nodoSeleccionado.getConexionesSalientes();
                
                if (salientes.isEmpty()) {
                    JOptionPane.showMessageDialog(PanelGrafo.this, 
                        "Este nodo no tiene conexiones salientes.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Extraemos los nombres de los nodos destino para mostrarlos en la lista
                String[] opcionesDestino = new String[salientes.size()];
                for (int i = 0; i < salientes.size(); i++) {
                    opcionesDestino[i] = salientes.get(i).getDestino().getEtiqueta();
                }

                // Mostramos un JOptionPane especial que se convierte en menú desplegable
                String seleccion = (String) JOptionPane.showInputDialog(
                        PanelGrafo.this,
                        "Seleccione la conexión que desea eliminar:",
                        "Eliminar Conexión",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        opcionesDestino,
                        opcionesDestino[0]
                );

                // Si el usuario eligió una opción y no le dio a "Cancelar"
                if (seleccion != null) {
                    for (Conexion c : salientes) {
                        if (c.getDestino().getEtiqueta().equals(seleccion)) {
                            // Guardamos los datos antes de borrar para el Ctrl+Z
                            final NodoGrafo origen = nodoSeleccionado;
                            final NodoGrafo destino = c.getDestino();
                            final int peso = c.getPeso();
                            
                            // 1. Borramos la conexión
                            origen.eliminarConexionHacia(destino);
                            
                            // 2. Guardamos en el historial cómo restaurarla (acción contraria)
                            historialDeshacer.push(() -> grafo.agregarConexion(origen, destino, peso));
                            
                            repaint();
                            break; // Salimos del bucle una vez encontrada y borrada
                        }
                    }
                }
            }
        });

        // Asegúrate de agregar el nuevo ítem al menú (ponlo al final de inicializarMenuContextual)
        menuContextual.add(itemEliminarConexion);

        menuContextual.add(itemInicio);
        menuContextual.addSeparator();
        menuContextual.add(itemEliminar);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 1. Dibujar las conexiones existentes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2)); 
        for (NodoGrafo nodo : grafo.getNodos()) { 
            for (Conexion conexion : nodo.getConexionesSalientes()){ 
                NodoGrafo destino = conexion.getDestino(); 
                //g2d.drawLine(nodo.getX(), nodo.getY(), destino.getX(), destino.getY());
                dibujarAristaDirigida(g2d, nodo.getX(), nodo.getY(), destino.getX(), destino.getY(), conexion.getPeso()); 
            }
        }

        // 2. Dibujar una línea temporal al arrastrar para conectar
        if (nodoOrigenConexion != null && puntoRatonArrastre != null) {
            g2d.setColor(Color.RED);
            g2d.drawLine(nodoOrigenConexion.getX(), nodoOrigenConexion.getY(), puntoRatonArrastre.x, puntoRatonArrastre.y); 
        }

        // 3. Dibujar los nodos (Unificamos la lógica de colores y bordes aquí)
        for (NodoGrafo nodo : grafo.getNodos()) {
            
            // Prioridad 1: Animación desde hilos
            if (nodo.getEstadoAnimacion() == Modelo.EstadoAnimacion.ACTUAL) {
                g2d.setColor(new Color(34, 197, 94));   // Verde brillante: se está visitando ahora
            } else if (nodo.getEstadoAnimacion() == Modelo.EstadoAnimacion.VISITADO) {
                g2d.setColor(new Color(245, 165, 36));  // Ámbar: ya fue visitado
            
            // Prioridad 2: Interacción manual del usuario
            } else if (nodo == nodoInicial) {
                g2d.setColor(Color.GREEN); // Nodo de inicio
            } else if (nodo == nodoHorver) {
                g2d.setColor(Color.YELLOW); // Hover con el ratón
            } else {
                g2d.setColor(new Color(173, 216, 230)); // Color base (azul claro)
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

    public void setModoConectar(boolean modo) {
        this.modoConectar = modo;
        // Opcional: limpiar variables residuales si se desactiva el modo
        if (!modo) {
            nodoOrigenConexion = null;
            puntoRatonArrastre = null;
            repaint();
        }
    }

    public Grafo getGrafo() {
        return grafo;
    }

    public void setGrafo(Grafo grafo){
        this.grafo=grafo;
        repaint();
        }

    public NodoGrafo getNodoInicial() {
        return nodoInicial;
    }

    private void dibujarAristaDirigida(Graphics2D g2d, int x1, int y1, int x2, int y2, int peso) {
        int tamañoPunta = 10;
        
        // 1. Calcular la diferencia de coordenadas y el ángulo original
        int dx = x2 - x1;
        int dy = y2 - y1;
        double angulo = Math.atan2(dy, dx);
        
        // 2. Calcular el desplazamiento perpendicular (offset)
        // Separamos la línea 8 píxeles del centro exacto
        int separacion = 8;
        int offsetX = (int) (separacion * Math.cos(angulo + Math.PI / 2));
        int offsetY = (int) (separacion * Math.sin(angulo + Math.PI / 2));
        
        // Aplicar el desplazamiento a los centros virtuales
        int x1Offset = x1 + offsetX;
        int y1Offset = y1 + offsetY;
        int x2Offset = x2 + offsetX;
        int y2Offset = y2 + offsetY;
        
        // 3. Calcular el destino real para que la punta no quede debajo del círculo
        // Usamos los centros desplazados para este cálculo
        int xDestino = (int) (x2Offset - RADIO_NODO * Math.cos(angulo));
        int yDestino = (int) (y2Offset - RADIO_NODO * Math.sin(angulo));
        
        // 4. Dibujar la línea principal ya separada
        g2d.drawLine(x1Offset, y1Offset, xDestino, yDestino);
        
        // 5. Calcular los vértices de la punta de la flecha
        int xPunta1 = (int) (xDestino - tamañoPunta * Math.cos(angulo - Math.PI / 6));
        int yPunta1 = (int) (yDestino - tamañoPunta * Math.sin(angulo - Math.PI / 6));
        
        int xPunta2 = (int) (xDestino - tamañoPunta * Math.cos(angulo + Math.PI / 6));
        int yPunta2 = (int) (yDestino - tamañoPunta * Math.sin(angulo + Math.PI / 6));
        
        // 6. Dibujar la punta (polígono)
        Polygon punta = new Polygon();
        punta.addPoint(xDestino, yDestino);
        punta.addPoint(xPunta1, yPunta1);
        punta.addPoint(xPunta2, yPunta2);
        g2d.fillPolygon(punta);
        
        // 7. Dibujar el peso de la conexión en el nuevo centro desplazado
        int xMedio = (x1Offset + xDestino) / 2;
        int yMedio = (y1Offset + yDestino) / 2;
        
        // Fondo blanco circular para legibilidad
        g2d.setColor(Color.WHITE);
        g2d.fillOval(xMedio - 10, yMedio - 10, 20, 20); 
        
        // Texto del peso
        g2d.setColor(Color.RED);
        g2d.drawString(String.valueOf(peso), xMedio - 4, yMedio + 4);
        
        // Restaurar el color negro
        g2d.setColor(Color.BLACK); 
    }

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

    // =========================================================
    // CLASE INTERNA: Centraliza todo lo relacionado al ratón
    // =========================================================
    private class ManejadorRaton extends MouseAdapter {
        
        @Override
        public void mousePressed(MouseEvent e) {
            NodoGrafo nodoClickeado = grafo.getNodoEnCoordenadas(e.getX(), e.getY(), RADIO_NODO);  
            
            // 1. Clic Derecho -> Menú Contextual (Ya lo tenías)
            if (SwingUtilities.isRightMouseButton(e)) { 
                if (nodoClickeado != null) { 
                    nodoSeleccionado = nodoClickeado; 
                    menuContextual.show(PanelGrafo.this, e.getX(), e.getY()); 
                }
                return; 
            }
            
            // 2. Clic Izquierdo -> Crear, Mover o Conectar
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (nodoClickeado == null) {
                    // Clic en el vacío: Crear nodo nuevo
                    String nombre = JOptionPane.showInputDialog(PanelGrafo.this, "Nombre del nuevo nodo:");
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        NodoGrafo nuevoNodo = grafo.agregarNodo(nombre, e.getX(), e.getY());
                        if (nuevoNodo != null) {
                            // GUARDAR EN EL HISTORIAL: La acción contraria (eliminarlo)
                            historialDeshacer.push(() -> grafo.eliminarNodo(nuevoNodo));
                            repaint(); 
                        }
                    }
                } else {
                    // Clic sobre un nodo existente
                    if (modoConectar) {
                        // Si estamos en modo conectar, iniciamos la línea temporal
                        nodoOrigenConexion = nodoClickeado;
                        puntoRatonArrastre = e.getPoint();
                    } else {
                        // Si no, lo seleccionamos para moverlo
                        nodoSeleccionadoParaMover = nodoClickeado;
                    }
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            // Este evento se dispara continuamente mientras el usuario mueve el ratón sin soltar el clic
            
            if (nodoSeleccionadoParaMover != null) {
                // Actualizar las coordenadas del nodo para que siga al cursor
                nodoSeleccionadoParaMover.setX(e.getX());
                nodoSeleccionadoParaMover.setY(e.getY());
                repaint(); 
            } 
            else if (nodoOrigenConexion != null) {
                // Actualizar el punto final de la línea roja de conexión
                puntoRatonArrastre = e.getPoint();
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
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
                            boolean exito = grafo.agregarConexion(nodoOrigenConexion, nodoDestino, peso);
                            
                            if (exito) {
                                // GUARDAR EN EL HISTORIAL: La acción contraria (borrar la flecha)
                                // Necesitas crear variables finales o usar las que ya tienes para la lambda
                                final NodoGrafo origen = nodoOrigenConexion;
                                final NodoGrafo destino = nodoDestino;
                                
                                historialDeshacer.push(() -> origen.eliminarConexionHacia(destino));
                            } else {
                                JOptionPane.showMessageDialog(PanelGrafo.this, "ERROR: Ya existe la conexión.", "Aviso", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException ex){
                            JOptionPane.showMessageDialog(PanelGrafo.this, "El peso de la conexión debe ser un número entero.");
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

    public boolean tieneNodos() {
        return !grafo.getNodos().isEmpty();
    }

    public void deshacerAccion() {
        if (!historialDeshacer.isEmpty()) {
            // Saca la última acción almacenada y la ejecuta (.run)
            historialDeshacer.pop().run();
            repaint();
        } else {
            // Opcional: Sonido de error si no hay nada que deshacer
            Toolkit.getDefaultToolkit().beep(); 
        }
    }
}