package Paneles;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List; 
import java.io.*; 

public class PanelResultados extends JPanel{

    private JTextPane textPane; //Lo que el usuario ve en la pantalla
    private StyledDocument documento; //El contenido y el formato
    
    //Style define el el formato para cada texto 

    private Style estiloNormal; 
    private Style estiloExito;
    private Style estiloError;

    //Método que define las caracteristicas del JTextPane, sin editar fondo negro
    public PanelResultados(){
        setLayout(new BorderLayout());

        textPane = new JTextPane(); 
        textPane.setEditable(false);
        textPane.setBackground(Color.decode("#0d1117"));
        textPane.setBorder(BorderFactory.createTitledBorder(
    BorderFactory.createLineBorder(Color.blue, 1)));
        
        documento = textPane.getStyledDocument();
        crearEstilos(); 

        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setPreferredSize(new Dimension(0, 180));
        add(scroll, BorderLayout.CENTER);
    }

    
    private void crearEstilos(){
        estiloNormal = textPane.addStyle("normal", null);
        StyleConstants.setForeground(estiloNormal, Color.LIGHT_GRAY);

        estiloExito = textPane.addStyle("exito", null);
        StyleConstants.setForeground(estiloExito, new Color(0, 200, 0));

        estiloError = textPane.addStyle("error", null);
        StyleConstants.setForeground(estiloError, new Color(220, 80, 80));

    }

    public void registrarPaso(String texto) {
        try {
            documento.insertString(documento.getLength(), texto + "\n", estiloNormal);
            textPane.setCaretPosition(documento.getLength()); // auto-scroll hacia abajo
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void registrarResumen(List<Integer> ordenDeVisita, boolean todosAlcanzados) {
        try {
            // Separador
            documento.insertString(documento.getLength(), "---\n", estiloNormal);

            // Orden de visita, ej: 22 -> 24 -> 26 -> 23 -> 25
            StringBuilder sb = new StringBuilder("Orden de visita: ");
            for (int i = 0; i < ordenDeVisita.size(); i++) {
                sb.append(ordenDeVisita.get(i));
                if (i < ordenDeVisita.size() - 1) {
                sb.append(" -> ");
                }
            }
            sb.append("\n");
            documento.insertString(documento.getLength(), sb.toString(), estiloNormal);

            // Mensaje final, en verde o rojo según el resultado
            String mensajeFinal = todosAlcanzados
                ? "Todos los nodos fueron alcanzados.\n"
                : "Algunos nodos no fueron alcanzados.\n";
            Style estiloFinal = todosAlcanzados ? estiloExito : estiloError;
            documento.insertString(documento.getLength(), mensajeFinal, estiloFinal);

            textPane.setCaretPosition(documento.getLength());
        }catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void limpiar(){
        try {
            documento.remove(0, documento.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

}