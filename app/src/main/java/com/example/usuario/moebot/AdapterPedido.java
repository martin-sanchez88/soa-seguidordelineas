package com.example.usuario.moebot;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class AdapterPedido extends ArrayAdapter<Pedido>{

    Context Mycontext;
    int MylayoutResourceid;
    CPedidoArray Mydata;

    public  AdapterPedido ( Context context, int layoutResourceid, CPedidoArray data){
        super(context,layoutResourceid,data);
        this.Mycontext = context;
        this.MylayoutResourceid = layoutResourceid;
        this.Mydata = data;
    }

    public View getView(int position , View convertView, ViewGroup parent){
        View row = convertView;
        PedidoHolder holder = null;

        if (row == null)
        {
            LayoutInflater inflater = ((Activity)Mycontext).getLayoutInflater();
            row = inflater.inflate(MylayoutResourceid,parent,false);

            holder = new PedidoHolder();
            holder.Bebida = (TextView) row.findViewById(R.id.TvBebida);
            holder.Estacion = (TextView) row.findViewById(R.id.TvEstacion);
            holder.Cant = (TextView) row.findViewById(R.id.TvCantidad);
            row.setTag(holder);
        }
        else
        {
        holder = (PedidoHolder) row.getTag();
        }

        Pedido opedido = Mydata.get(position);
        holder.Estacion.setText(opedido.GetEstacion());
        holder.Bebida.setText(opedido.GetBebida());
        holder.Cant.setText(String.valueOf(opedido.GetCant()));

        return row;
    }

    static class PedidoHolder{
        TextView Estacion;
        TextView Bebida;
        TextView Cant;

    }
}
