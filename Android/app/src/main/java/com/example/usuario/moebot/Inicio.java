package com.example.usuario.moebot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

public class Inicio extends AppCompatActivity {

    ProgressBar PBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);


        PBar =  findViewById(R.id.PBar);
        PBar.setMax(100);
        PBar.setProgress(0);

        final Thread thread = new Thread(){
            @Override
            public void run() {
                try{
                    for (int i = 0; i < 100; i++){
                        PBar.setProgress(i);
                        sleep(20);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    Intent Comienzo = new Intent(Inicio.this,Menu.class);
                    startActivity(Comienzo);
                    finish();
                }
            }

        };
        thread.start();

    }
}
