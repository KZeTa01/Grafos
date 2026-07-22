package Paneles;
import java.awt.*;
import java.awt.event.*;
import Modelo.*;
import javax.swing.*;

public class PanelGrafo extends JPanel {
    // Variables a usar
    private NodoGrafo nodoInicial;
    private NodoGrafo nodoHorver;
    private JPopupMenu menuContextual;
    private NodoGrafo nodoSeleccionado;
    
    private Grafo grafo;
    private final int RADIO_NODO = 20;
    private NodoGrafo nodoSeleccionadoParaMover = null;
    private NodoGrafo nodoOrigenConexion = null;
    private Point puntoRatonArrastre = null;
    private boolean modoConectar;

    public PanelGrafo( Grafo grafo) {
        this.grafo=grafo;
        inicializarMenuContextual();

        // Ahora el manejador centraliza todos los eventos del mouse
        ManejadorRaton manejador = new ManejadorRaton();
        addMouseListener(manejador);
        addMouseMotionListener(manejador);
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
                g2d.drawLine(nodo.getX(), nodo.getY(), destino.getX(), destino.getY()); 
            }
        }

        // 2. Dibujar una línea temporal al arrastrar para conectar
        if (nodoOrigenConexion != null && puntoRatonArrastre != null) {
            g2d.setColor(Color.RED);
            g2d.drawLine(nodoOrigenConexion.getX(), nodoOrigenConexion.getY(), puntoRatonArrastre.x, puntoRatonArrastre.y); 
        }

        // 3. Dibujar los nodos (Unificamos la lógica de colores y bordes aquí)
        for (NodoGrafo nodo : grafo.getNodos()) {
            
            // Determinar color de fondo según el estado
            if (nodo == nodoInicial) {
                g2d.setColor(Color.GREEN);
            } else if (nodo == nodoHorver) {
                g2d.setColor(Color.YELLOW);
            } else {
                g2d.setColor(new Color(173, 216, 230)); // Color por defecto
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

    public NodoGrafo getNodoInicial() {
        return nodoInicial;
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
                        grafo.agregarNodo(nombre, e.getX(), e.getY());
                        repaint(); // Actualizar lienzo
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
                            if (!exito) {
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
}