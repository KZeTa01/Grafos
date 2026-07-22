package Modelo;

import java.util.*;

public class NodoGrafo {
    private EstadoAnimacion estadoAnimacion = EstadoAnimacion.NORMAL;
    private int id;
    private String etiqueta;
    private int x,y;
    private List<Conexion> conexionesSalientes;
    
    public NodoGrafo(int id, String etiqueta, int x, int y){    
        this.id=id;
        this.etiqueta=etiqueta;
        this.x=x;
        this.y=y;
        this.conexionesSalientes = new ArrayList<>();
    }
    //Getters y setters
    public int getID(){ return id; }
    
    public String getEtiqueta(){ return etiqueta; }
    public void setEtiqueta(String etiqueta){ this.etiqueta=etiqueta; }
    
    public int getX(){ return x; }
    public void setX(int x){this.x = x;}
    public int getY(){ return y; }
    public void setY(int y){this.y = y;}

    public List<Conexion> getConexionesSalientes(){
        return conexionesSalientes;
    }
    //Agrega una conexion a
    public void agregarConexion(Conexion conexion){
        conexionesSalientes.add(conexion);
    } 
    //Elimina las conexiones que tengan como nodo destino el pasado como parametro (c objeto conexion, temporal)
    public void eliminarConexionHacia(NodoGrafo destino){
        conexionesSalientes.removeIf(c -> c.getDestino().equals(destino));
    }

    //Estados del nodo
    public EstadoAnimacion getEstadoAnimacion() { 
        return estadoAnimacion; 
    }
    
    public void setEstadoAnimacion(EstadoAnimacion estado) { 
        this.estadoAnimacion = estado; 
    }
}
