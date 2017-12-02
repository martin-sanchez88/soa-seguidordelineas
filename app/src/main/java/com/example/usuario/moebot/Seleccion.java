package com.example.usuario.moebot;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
/*Import de elementos*/
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/*Import de MediaPlayer*/
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

/* Imports de Sensores*/
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Seleccion extends AppCompatActivity implements SensorEventListener{

    //Elementos del View
    Spinner SpBebidas, SpEstacion;
    Button BtnCargar,BtnLista,BtnLimpiar;
    EditText EtxtCant;

    MediaPlayer Mp;

    //Variables
    CPedidoArray Parray = new CPedidoArray();
    private final static float ACC = 20;
    String [] items_bebidas, items_estacion;
    private String direccionplaca;
    Bundle contenedor = null;

    private SensorManager sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion);

        //Creo el sonido
        Mp = MediaPlayer.create(this,R.raw.sonidobotella);
        Mp.setOnPreparedListener (
                new OnPreparedListener()
                {
                    public void onPrepared(MediaPlayer arg0)
                    {
                        Mp.setVolume(1.0f, 1.0f);
                    }
                }
        );

        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Definición de Layout
        SpEstacion = (Spinner) findViewById(R.id.SPEstacion);
        items_estacion = getResources().getStringArray(R.array.lista_estacion);
        ArrayAdapter <String> adapter_Estacion = new ArrayAdapter <String> (getBaseContext(),android.R.layout.simple_spinner_item,items_estacion);
        adapter_Estacion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpEstacion.setAdapter(adapter_Estacion);

        //Cargo las bebidas en el combo box de Bebidas
        SpBebidas = (Spinner) findViewById(R.id.SPBebidas);
        items_bebidas = getResources().getStringArray(R.array.lista_bebidas);
        ArrayAdapter <String> adapter_bebidas = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,items_bebidas);
        adapter_bebidas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SpBebidas.setAdapter(adapter_bebidas);


        //Informo las cantidades
        EtxtCant = (EditText) findViewById(R.id.EtxtCant);
        EtxtCant.setText(R.string.Default1);

        contenedor = getIntent().getExtras();

        if ( contenedor != null)
        {
            //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC06
            direccionplaca = contenedor.getString("Direccion_Bluethoot");
        }

        //Cargo el pedido
        BtnCargar = (Button) findViewById(R.id.BtnCargar);

        BtnCargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Cargar_pedido(Parray);


            }
        });

        //Asocio al botón al elemento del View y implemento su acción al tocarlo
        BtnLista = (Button)findViewById(R.id.BtnLista);
        BtnLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;

                int tamanio = Parray.size();

                //Se valida que haya por lo menos un pedido realizado
                if (tamanio != 0) {
                    Bundle contenedor = new Bundle();

                    //le cargamos al bundle un objeto parcelable que se almacenara
                    //bajo el nombre de "Pedidos"
                    contenedor.putParcelable("Pedidos", Parray);

                    //Creo el intent para moverme de Activity
                    Intent Lista = new Intent(Seleccion.this, ListaPedidos.class);

                    Lista.putExtras(contenedor);
                    Lista.putExtra("Direccion_Bluethoot", direccionplaca);

                    startActivity(Lista);

                }
                else
                {


                    Toast.makeText(context,R.string.Lista_vacia,duration).show();
                }
            }
        });

        BtnLimpiar = (Button) findViewById(R.id.BtnLimpiar);
        BtnLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Parray.clear();
                Toast.makeText(context,R.string.ListaLimpia,duration).show();
            }
        });

    }


    public void Cargar_pedido(CPedidoArray Parray)
    {
        int  number = Integer.parseInt(EtxtCant.getText().toString());
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        //Se valida que la cantidad sea distinta de 0
        if (number != 0) {
            Pedido opedido = new Pedido(SpEstacion.getSelectedItem().toString(),
                    SpBebidas.getSelectedItem().toString(),
                    number);

            Parray.add(opedido);
            Mp.start();
            Toast.makeText(context,R.string.Pedido_cargado,duration).show();

        }
        else
        {
            Toast.makeText(context,R.string.Error_cant0,duration).show();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Ini_Sensores();

    }

    @Override
    protected void onStop()
    {
        Parar_Sensores();
        super.onStop();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {

        int sensorType = event.sensor.getType();
        float[] values = event.values;

        synchronized (this) {


            switch (event.sensor.getType()) {
                case Sensor.TYPE_PROXIMITY:


                    // Si detecta 0 lo represento
                    if (event.values[0] == 0) {
                        showToast("Muy Cerca");
                      
                    }

                    break;

                case Sensor.TYPE_LIGHT:

                    if (event.values[0] == 0){
                        showToast("Poca Luz");
                       
                    }

                    break;

                case  Sensor.TYPE_ACCELEROMETER:

                    if ((Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC)) {
                        showToast("Se movió el acelerómetro");
                        Mp.setVolume(20.0f,20.0f);
                        Mp.start();
                        Cargar_pedido(Parray);
                    }
            }
        }
    }



    // Metodo para iniciar el acceso a los sensores
    protected void Ini_Sensores() {
        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),   SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Se deregistran los sensores que vamos a utilizar
    // Metodo para parar la escucha de los sensores
    private void Parar_Sensores() {
        sensor.unregisterListener(this, sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensor.unregisterListener(this, sensor.getDefaultSensor(Sensor.TYPE_PROXIMITY));
        sensor.unregisterListener(this, sensor.getDefaultSensor(Sensor.TYPE_LIGHT));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
