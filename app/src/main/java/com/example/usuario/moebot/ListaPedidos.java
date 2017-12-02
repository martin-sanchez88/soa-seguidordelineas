package com.example.usuario.moebot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
/* Imports de Senrores*/
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Handler;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.UUID;


public class ListaPedidos extends AppCompatActivity implements SensorEventListener{

    //Elementos del layout
    ListView lvpedidos;
    Button BtnDescargar;
    CPedidoArray Parray = null;
    Bundle contenedor;
    BluetoothSocket btSocket = null;
    boolean recibiendo;

    private SensorManager sensor;

    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message


    private BluetoothAdapter btAdapter = null;
    private String direccionplaca;
    private ConnectedThread mConnectedThread;

    // SPP UUID service  - Funciona en la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_pedidos);
        final ViewGroup nullParent = null;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);



        lvpedidos =  findViewById(R.id.LvPedidos);

        View header =  getLayoutInflater().inflate(R.layout.listview_header_row, nullParent);

        contenedor = getIntent().getExtras();
        if ( contenedor != null)
        {
            //Obtengo la lista de pedidos de Activity Selección
            Parray = contenedor.getParcelable("Pedidos");

            //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC06
            direccionplaca= contenedor.getString("Direccion_Bluethoot");

            AdapterPedido adapter = new AdapterPedido(this,R.layout.listview_item_row,Parray);
            lvpedidos.addHeaderView(header);
            lvpedidos.setAdapter(adapter);

        }
        BtnDescargar =  findViewById(R.id.BtnDescargar);
        BtnDescargar.setOnClickListener(BtnDescargarListener);

        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout atraves utilizando indirectamente a este handler
        bluetoothIn = Handler_Msg_Hilo_Principal();
    }

    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC06, creando un
    //socketBluethoot
    public void onResume() {
        super.onResume();
        Ini_Sensores();
        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC06
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();

        direccionplaca= extras.getString("Direccion_Bluethoot");

        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        BluetoothDevice device = btAdapter.getRemoteDevice(direccionplaca);

        //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
        try
        {
            btSocket = createBluetoothSocket(device);
        }
        catch (IOException e)
        {
            MostrarToast( "La creacción del Socket fallo");
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        }
        catch (IOException e)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        //Una establecida la conexion con el Hc06 se crea el hilo secundario, el cual va a recibir
        // los datos de Arduino atraves del bluethoot
        recibiendo = true;
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("P");

    }

    @Override
    //Cuando se ejecuta el evento onPause se cierra el socket Bluethoot, para no seguir recibiendo datos
    public void onPause()
    {
        Parar_Sensores();
        super.onPause();
        recibiendo=false;
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    protected void onStop()
    {

        ////////////////////////

        recibiendo=false;
        //////////////////////
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
        super.onStop();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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

    // Metodo que escucha el cambio de los sensores
    @Override
    public void onSensorChanged(SensorEvent event) {
      

        synchronized (this) {


            switch (event.sensor.getType()) {
                case Sensor.TYPE_PROXIMITY:


                    // Si detecta 0 lo represento
                    if (event.values[0] < 5) {
                        //MostrarToast("Sensor de proximidad en 0");
                        mConnectedThread.write("P");
                    }

                    break;

                case Sensor.TYPE_LIGHT:

                    if (event.values[0] < 10){
                        //MostrarToast("Sensor de Luz en 0");
                        mConnectedThread.write("L");
                    }

                    break;
            }
        }
    }

    //Metodo que crea el socket bluethoot
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void MostrarToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    //Idea, realizar toda la conexión en el Thread (incluyendo la creación del socket)

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //Constructor de la clase del hilo secundario
        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //metodo run del hilo, que va a entrar en una espera activa para recibir los msjs del HC06
        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            //el hilo secundario se queda esperando mensajes del HC06
            while (recibiendo)
            {
                try
                {
                    //se leen los datos del Bluethoot
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }

        }


        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                MostrarToast("La conexion fallo");
                finish();

            }
        }
    }

    //Listener del boton encender que envia  msj para Apagar Led a Arduino atraves del Bluethoot
    private View.OnClickListener BtnDescargarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String secuencia; //Informará la secuencia de mesas a revisar
            String Estaciones[] = null;




            Estaciones = getResources().getStringArray(R.array.lista_estacion);
            secuencia = Parray.getsecuencia(Estaciones);
            if (btSocket!=null)
            {

                mConnectedThread.write(secuencia);

                MostrarToast("Secuencia de mesas enviadas");
            }
        }
    };

    //Handler que sirve que permite mostrar datos en el Layout al hilo secundario
    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler() {
            public void handleMessage(android.os.Message msg)
            {
                //si se recibio un msj del hilo secundario
                if (msg.what == handlerState)
                {
                    //voy concatenando el msj
                    String readMessage = (String) msg.obj;
                    if (readMessage.equalsIgnoreCase("X")){
                        MostrarToast("Obstáculo Detectado");
                    }
                    else if (!readMessage.equalsIgnoreCase("0")) {
                        MostrarToast("Llegó a la Parada " + readMessage);
                    }
                    else{
                        MostrarToast("Moebot en Abastecimiento");
                    }

                }
            }
        };

    }
}
