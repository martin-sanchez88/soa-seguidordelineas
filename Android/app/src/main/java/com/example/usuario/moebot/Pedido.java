package com.example.usuario.moebot;

/**
 * Created by Usuario on 18/11/2017.
 */

public class Pedido {

  //  private int id;
    private String estacion;
    private String bebida;
    private int cant;

    public Pedido(){
        super();
    }
    public Pedido( String estacion, String bebida, int cant){
        SetEstacion(estacion);
        SetBebida(bebida);
        SetCant(cant);
    }


    public String GetEstacion(){
        return this.estacion;
    }

    public void SetEstacion(String estacion){
        this.estacion = estacion;
    }

    public String GetBebida(){
        return this.bebida;

    }
    public void SetBebida(String bebida){
        this.bebida = bebida;
    }

    public void SetCant(int cant){
        this.cant = cant;
    }

    public int GetCant(){
        return this.cant;
    }


}
