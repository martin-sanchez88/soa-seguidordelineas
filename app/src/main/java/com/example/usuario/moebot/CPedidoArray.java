package com.example.usuario.moebot;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class CPedidoArray extends ArrayList<Pedido> implements Parcelable{

    public CPedidoArray(){}

    public String getsecuencia(String Estaciones[]){
    String secuencia="";
    int i = 0, encontro = 0, cantidad = 0;

    cantidad = Estaciones.length;
    for (i= 0; i < cantidad ; i++)
        {
            encontro = this.encontrar_estacion(Estaciones[i]);
            if (encontro == 1)
            {
                secuencia = secuencia + 1;
            }
            else
            {
                secuencia = secuencia + 0;
            }

        }
    return secuencia;
    }

    public int encontrar_estacion(String estacion){
        int encontro = 0, i;
        Pedido ped;
        for( i = 0; i < this.size() ; i++){
            ped = this.get(i); //obtengo el objeto del array
            if (ped.GetEstacion().equals(estacion))
            {
                encontro = 1;
                break;
            }
        }
        return encontro;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int size = this.size();

        dest.writeInt(size);

        for (int i=0; i< size; i++){
            Pedido opedido = this.get(i);
            dest.writeString(opedido.GetEstacion());
            dest.writeString(opedido.GetBebida());
            dest.writeInt(opedido.GetCant());

        }

    }

    public CPedidoArray(Parcel in){

        readfromParcel(in);
    }


    private void readfromParcel( Parcel in){
        this.clear();
        int size = in.readInt();

        for (int i= 0; i< size; i++){
            Pedido opedido = new  Pedido();
            opedido.SetEstacion(in.readString());
            opedido.SetBebida(in.readString());
            opedido.SetCant(in.readInt());
            this.add(opedido);
        }

    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public CPedidoArray createFromParcel(Parcel in)
        {
            return new CPedidoArray(in);
        }
        public Object[] newArray(int arg0)
        {
            return null;
        }
    };

    public int describeContents()
    {
        return 0;
    }

}
