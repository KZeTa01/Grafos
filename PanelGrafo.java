import java.awt.*;
import java.awt.event.*;
import Modelo.*;
import javax.swing.*;

public class PanelGrafo extends JPanel implements MouseListener, MouseMotionListener {
    //Variables a usar
    private NodoGrafo nodoInicial;
    private NodoGrafo nodoHorver;
    private JPopupMenu menuContextual;
    private NodoGrafo nodoSeleccionado;
    //
    private Grafo grafo;
    private final int RADIO_NODO = 20;
    private NodoGrafo nodoSeleccionadoParaMover = null;
    private NodoGrafo nodoOrigenConexion = null;
    private Point puntoRatonArrastre = null;
    private boolean modoConectar;

    public PanelGrafo(){
        inicializarMenuContextual();
    }


    private void inicializarMenuContextual(){
        menuContextual = new JPopupMenu();
        // Opcion para marcar al nodo como el inicio del grafo
        JMenuItem itemInicio = new JMenuItem("Marcar como inicio");
        itemInicio.addActionListener(e -> {    //Se usa la función lambda para ahorrar código
            if (nodoSeleccionado != null) {
                nodoInicial = nodoSeleccionado;
                repaint();
            }
        });
        //Opcion para eliminar el nodo
        JMenuItem itemEliminar = new JMenuItem("Eliminar Nodo");
        itemEliminar.addActionListener(e -> {    //Se usa la función lambda para ahorrar código
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
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseClicked'");
    }

    @Override
    public void mousePressed(MouseEvent e) {
        NodoGrafo nodoClickeado = grafo.getNodoEnCoordenadas(e.getX(), e.getY(), RADIO_NODO);  //Se obtiene el nodo que se 
        // encuentra en el radio donde se dio click (extrae las coordenadsa del click para verificar si estuvo dentro del rango)
        if (SwingUtilities.isRightMouseButton(e)) { //Se ejecuta si se presionó con el botón derecho del mouse
            if (nodoClickeado !=null) { //Verifica si se dió click en un nodo
                nodoSeleccionado = nodoClickeado; //Se guarda el nodo clickeado en una variable temporal
                menuContextual.show(this, e.getX(), e.getY()); //Se muestra el menú
            }
            return; 
        }
        //...
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (modoConectar && nodoOrigenConexion != null) {
            NodoGrafo nodoDestino = grafo.getNodoEnCoordenadas(e.getX(), e.getY(), RADIO_NODO); //Guarda el nodo ubicado en el espacio donde se soltó el click en una variable temporal 
            if (nodoDestino == null) { //Nodo no existe
                JOptionPane.showMessageDialog(this, "Debe conectar hacia un nodo existente"); //Valida que se suelte el click en un nodo existente
            } else if (nodoOrigenConexion == nodoDestino) { //Mismo nodo
                JOptionPane.showMessageDialog(this, "No se permiten conexiones hacia el mismo nodo");
            } else{
                String pesoString = JOptionPane.showInputDialog(this, "Ingrese el peso de la conexión:", "1");
                if (pesoString != null && !pesoString.isEmpty()) {
                    try{
                        int peso = Integer.parseInt(pesoString);
                        boolean exito = grafo.agregarConexion(nodoOrigenConexion, nodoDestino, peso);
                        if (!exito) {
                            JOptionPane.showMessageDialog(this, "ERROR: Ya existe la conexión.", "Aviso", JOptionPane.ERROR_MESSAGE);
                        }
                    }catch (NumberFormatException ex){
                        JOptionPane.showMessageDialog(this, "El peso de la conexión debe ser un número entero.");
                    }
                }
            }
        }
        //
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}


    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        NodoGrafo nodoBajoCursor = grafo.getNodoEnCoordenadas(e.getX(), e.getY(), RADIO_NODO);
        if (nodoHorver != nodoBajoCursor) {
            nodoHorver = nodoBajoCursor;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //
        for (NodoGrafo nodo : grafo.getNodos()) {
            if (nodo == nodoInicial) {
                g2d.setColor(Color.GREEN);
            } else if (nodo == nodoHorver) {
                g2d.setColor(Color.YELLOW);
            } else{
                g2d.setColor(new Color(173, 216, 230));
            }

            g2d.fillOval(nodo.getX()-RADIO_NODO, nodo.getY()-RADIO_NODO, RADIO_NODO*2, RADIO_NODO*2);
            
        }

    }



}
