import javax.swing.*;

import Excepciones.FormatoInvalido;
import Modelo.Grafo;
import Paneles.*;

import java.awt.*;
import java.awt.event.*;

public class VentanaPrincipal extends JFrame implements ActionListener{
    JPanel centro,derecho,d1,d2,d22; 
    JSlider jsVelocidad;
    JComboBox cboRecorrido;  
    private boolean modoAristaActivo = false;
    JButton modo,ejecutar,guardar,cargar,limpiar; 
    PanelGrafo panel; 
    PanelResultados inferior; 
    

    public VentanaPrincipal(){
        cargarComponenntes();
        configurarVentana(); 
        
    }
    public void cargarComponenntes(){
        setSize(500,500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setTitle("Grafos");
        setLocationRelativeTo(null);
    }
    public void configurarVentana(){
        //CONFIGURANDO EL PANEL DEL CENTRO
        centro = new JPanel(new GridLayout(1,1)); 
            //Agregando el objeto de tipo PanelGrafo; 
            Grafo modeloGrafo = new Grafo(); // 1. Creas el modelo (la memoria de los datos)
            panel = new PanelGrafo(modeloGrafo); // 2. Se lo pasas al lienzo visual
        //agregando el panel al centro;
        centro.add(panel); 
        add(centro, BorderLayout.CENTER);


        //Configurando el panel derecho
        derecho = new  JPanel(new GridLayout(2,1,15,15)); 
            //panel para algoritmo, velocidad y modo(D1)
                d1 = new JPanel(new GridLayout(5,1,10,10));
                d1.setBorder(BorderFactory.createTitledBorder( BorderFactory.createLineBorder(Color.GRAY, 1),"Configuración"));

                //creando el combo
                    String[] opciones = {"BFS","DFS"};
                    cboRecorrido = new JComboBox<>(opciones);
                //Creando el spinner 
                    jsVelocidad = new JSlider(1,3,1); 
                    jsVelocidad.setPaintTicks(true);      // Muestra las marcas
                    jsVelocidad.setMajorTickSpacing(1);  // Marca grande cada 10
                    jsVelocidad.setMinorTickSpacing(1);   // Marca pequeña cada 5
                    jsVelocidad.setPaintLabels(true); // Muestra los números 
                //Creando el boton "MODOS"
                    modo = new JButton("Modo: crear arista(on)"); 

            modo = new JButton("Modo: Mover Nodos"); // Texto inicial por defecto
            modo.addActionListener(e -> {
                modoAristaActivo = !modoAristaActivo; // Invierte el estado (de false a true, y viceversa)
                panel.setModoConectar(modoAristaActivo); // Le avisa a tu PanelGrafo
                
                // Cambiamos el texto (y opcionalmente el color) para que el usuario sepa en qué modo está
                if (modoAristaActivo) {
                    modo.setText("Modo: Crear Arista (ON)");
                    modo.setBackground(Color.GREEN);
                } else {
                    modo.setText("Modo: Mover Nodos");
                    modo.setBackground(null); // Vuelve al color por defecto
                }
            });



            //Agregando los elementos al panel (d1)
                d1.add(new JLabel("Algoritmo")); 
                d1.add(cboRecorrido);
                d1.add(new JLabel("Velocidad"));
                d1.add(jsVelocidad);
                d1.add(modo);
            //panel para boton de ejecutar, guardar, cargar y limpiar lienzo(d2)
                d2 = new JPanel(new GridLayout(3,1,10,10)); 
                    //Creando los botones; 
                        ejecutar = new JButton("Ejecutar recorrido"); 
                        guardar = new   JButton("Guardar") ; 
                        cargar = new JButton("Cargar"); 
                        limpiar = new JButton("Limpiar lienzo");
                    //creando el pannel d22 para agregar los dos botones juntos(guardar y cargar)                    
                    d22 = new JPanel(new GridLayout(1,2)); 
                    d22.add(guardar);
                    d22.add(cargar);
            //Agregando los elementos  al panel (d2)
                d2.add(ejecutar);
                d2.add(d22);
                d2.add(limpiar);
        //agregando los dos paneles al panel principal
        derecho.add(d1);
        derecho.add(d2);
        //agregando el panel a la pantalla principal; 
        add(derecho,BorderLayout.EAST); 
                
        //CONFIGURANDO EL PANEL INFERIOR DE RESULTADOS
        inferior = new PanelResultados(); 
        add(inferior,BorderLayout.SOUTH);

        limpiar.addActionListener(this);
        cargar.addActionListener(this);

    }       


    public void actionPerformed(ActionEvent e){
        if(e.getSource()==limpiar){
            inferior.limpiar();

        } else if(e.getSource() == cargar){
            JFileChooser selector = new JFileChooser();
            int resultado = selector.showOpenDialog(this);  

            if (resultado == JFileChooser.APPROVE_OPTION) {
                String ruta = selector.getSelectedFile().getAbsolutePath();
            try {
                Grafo grafoCargado = PersistenciaGrafos.cargar(ruta);
                // aquí se lo pasarías al panel del lienzo cuando exista ese método
                inferior.registrarPaso("Grafo cargado correctamente desde " + selector.getSelectedFile().getName());
            } catch (FormatoInvalido ex) {
                    JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Archivo inválido",
                    JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
}
