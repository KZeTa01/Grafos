package Modelo;

public class Conexion {
    private NodoGrafo origen, destino;   //Puntas de la arista (conexion)
    private int peso; //Valor que toma esa arista (conexion)
    public Conexion(NodoGrafo origen, NodoGrafo destino, int peso){
        this.origen=origen;
        this.destino=destino;
        this.peso=peso;
    }
    //Getters, setters
    public NodoGrafo getOrigen(){
        return origen;
    }
    public NodoGrafo getDestino(){
        return destino;
    }
    public int getPeso(){
        return peso;
    }
    public void setPeso(int peso){
        this.peso=peso;
    }

    //ToString - permite ver un resumen de la conexion
    @Override
    public String toString(){
        return origen.getEtiqueta()+"→"+destino.getEtiqueta()+" ("+peso+")";
    }
}
