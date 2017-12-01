package com.example.usuario.moebot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Menu extends AppCompatActivity {

    Button BtnAcerca, Btnbluetooth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        BtnAcerca = findViewById(R.id.BtnAcerca);
        BtnAcerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Acerca = new Intent(Menu.this, Acerca.class);
                startActivity(Acerca);
            }
        });


        Btnbluetooth = findViewById(R.id.BtnBluetooth);
        Btnbluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //se determina si esta activado el bluethoot

                    Intent Bluetooth = new Intent(Menu.this,OpcionesBluetooth.class);
                    startActivity(Bluetooth);

            }
        });



    }

}