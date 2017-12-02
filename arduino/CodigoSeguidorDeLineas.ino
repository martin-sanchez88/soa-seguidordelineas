#define cantidadParadas 6
/*******************************************************************/
/*****   Variables Asociadas a PINES *******************************/
/******************************************************************/

//Motor A (Derecho)
int IN1 = 13;   // Input1 conectada al pin 13
int IN2 = 12; // Input2 conectada al pin 12
int ENA = 10; // ENA para control de velocidad del motor

//Motor B (Izquierdo)
int IN3 = 3;  // Input3 conectada al pin 3
int IN4 = 2;  // Input4 conectada al pin 2
int ENB = 9; // ENB para control de velocidad del motor

//Sensores de Línea Infrarrojos
int sensora = 7; //Sensor Izquierdo
int sensorb = 6; //Sensor Central<
int sensorc = 5; //Sensor Derecho
int Valora = 0; //Valor de Lectura
int Valorb = 0;
int Valorc = 0;

//Buzzer-Speaker
int speakerPin = 11;

//Ultrasonido
int Trigger = 8; //para el pulso ultrasónico
int Echo = 4; //Para el sensor de rebote ultrasónico

//Otras Variables Globales
long distancia;
long tiempo;
unsigned long millisSpeaker;
unsigned long millisSpeakerAnt = 0;
unsigned long millisEspera;
unsigned long millisEsperaAnt = 0;
unsigned long millisUltrasonido;
unsigned long millisUltrasonidoAnt = 0;
unsigned long millisParada;
unsigned long millisParadaAnt = 0;
unsigned long millisProx;
unsigned long millisProxAnt = 0;
unsigned long millisLum;
unsigned long millisLumAnt = 0;


int nota; //Sonido que hace el buzzer
int esperando;
bool abasteciendo;
bool obstaculizado;
bool paradas[cantidadParadas]; //Para saber en cuales parar
int parada = 0; //Acumulador de paradas detectadas
int direccion = 0;
int contadorDeteccionesDerechas = 0; //Dado que los sensores CNY70 suelen tirar lecturas erráticas, me aseguro con al menos 3 o 4 seguidas antes de cambiar la dirección
int contadorDeteccionesIzquierdas = 0;
/*
  Dirección determina cómo están andando las ruedas para evitar seteos innecesarios.
  0 Parado
  1 Andand Rueda Derecha
  2 Andando Rueda Izquierda
  3 Andando Ambas Ruedas
*/
int contadorRecepcion = 0; //Contador de recepción de paradas en la comunicación BT/Serial
char c; //Caracter recibido por BT

void setup() {
  Serial.begin(9600);//Inicializar la comunicación serial
  /*******************************************************************/
  /*****   Inicialización de PINES **********************************/
  /******************************************************************/

  pinMode (IN1, OUTPUT); //Pines de Motor
  pinMode (IN2, OUTPUT);
  pinMode (IN4, OUTPUT);
  pinMode (IN3, OUTPUT);
  pinMode (ENA, OUTPUT);
  pinMode (ENB, OUTPUT);
  pinMode (Trigger, OUTPUT); /*activación del pin salida para el pulso ultrasónico*/
  pinMode (Echo, INPUT); /*activación del pin entrada del rebote del ultrasonido*/
  pinMode (sensora, INPUT);//Definir el sensor(pin5) como entrada
  pinMode (sensorb, INPUT);//Definir el sensor(pin6) como entrada
  pinMode (sensorc, INPUT);//Definir el sensor(pin7) como entrada
  abasteciendo = true;
  obstaculizado = false;
  esperando = 0;

  for (int i = 0; i < cantidadParadas; i++) {
    paradas[i] = false;
  }
}

void loop() {
  /*******************************************************/
  //Recepción de transmisión por BT
  /*******************************************************/
  if (Serial.available() > 0) {
    c = Serial.read();
    //Verbose para Debuggin por Serial
    //Serial.print("Leido ");
    //Serial.write(c);
    //Serial.print("\n");
  }

  //Comprobación de señales enviadas por BT
  if (c == 'P') { //Si recibe la señal de proximidad
    millisProx=millis();
    if (millisProx-millisProxAnt>=3000){
    //Generar Alarma
    tone(speakerPin, 290, 1500);
    millisProxAnt=millisProx;
    }
    c = 'X'; //Desechamos lo leído para evitar re-lectura.
  }

  if (c == 'L') { //Si recibe la señal de Luminosidad
    millisLum=millis();
    if (millisLum-millisLumAnt>=3000){
    //Provocar Espera pequeña
    digitalWrite (IN2, LOW); //Frena Rueda derecha
    digitalWrite (IN1, LOW);
    digitalWrite (IN4, LOW); //Frena Rueda Izquierda
    digitalWrite (IN3, LOW);
    direccion = 0; //Informe de frenado
    esperando = 3;
        millisProxAnt=millisProx;
    }
    //Podría llamarse una espera superlarga y luego cortarla con otra señal cuando se reestablece la luz
    c = 'X'; //Desechamos lo leído para evitar re-lectura.

  }

  /*--------------Comprobación de Obstáculos--------------*/
  /*--------------Comprobación de Obstáculos--------------*/
  /*--------------Comprobación de Obstáculos--------------*/
  millisUltrasonido = millis(); //Medición del obstáculo cada décima de segundo, por problemas de ecos.
  if (millisUltrasonido - millisUltrasonidoAnt >= 100) { //Si se mide distancia
    millisUltrasonidoAnt = millisUltrasonido;
    //Lectura de Distancia
    digitalWrite(Trigger, LOW); /* Por cuestión de estabilización del sensor*/
    delayMicroseconds(5);
    digitalWrite(Trigger, HIGH); /* envío del pulso ultrasónico*/
    delayMicroseconds(10);
    tiempo = pulseIn(Echo, HIGH); /* Función para medir la longitud del pulso entrante. Mide el tiempo que transcurrido entre el envío
  del pulso ultrasónico y cuando el sensor recibe el rebote, es decir: desde que el pin 4 empieza a recibir el rebote, HIGH, hasta que
  deja de hacerlo, LOW, la longitud del pulso entrante*/
    distancia = int(0.017 * tiempo); /*fórmula para calcular la distancia obteniendo un valor entero*/
 
    if (distancia < 10 && distancia != 0 ) { //Y la distancia es menos de 10 cm (Y distinta de cero porque a veces el sensor se cuelga y da cero sin razón conocida)
      obstaculizado = true; //Obstaculizado -----------------------------
      //Frenar Ruedas
      analogWrite(ENA, 0);
      analogWrite(ENB, 0);
      direccion = 0; //Informe de frenado
      millisSpeaker = millis();
      if (millisSpeaker - millisSpeakerAnt >= 1000) {
        millisSpeakerAnt = millisSpeaker;
        if (nota == 294) {
          nota = 494;
        }
        else
        {
          nota = 294;
        }

        tone(speakerPin, nota, 500);
        Serial.write('X');
      }
    }
    else { //Si la distancia no es menos de 10 no está obstaculizado
      obstaculizado = false;
    }
  }
  else  { //Si no se midió distancia no hay que hacer nada en especial, se conserva el estado
  }

  /*----------------Código Seguidor con Estaciones-------------------*/
  /*----------------Código Seguidor con Estaciones-------------------*/
  /*----------------Código Seguidor con Estaciones-------------------*/
  if (!obstaculizado) { //Si no está obstaculizado
   
    if (!abasteciendo) { //Si no está en estación de abastecimiento
      if (esperando == 0) { //Si no está esperando
        //Funcionamiento de Seguidor de Líneas
        Valora = digitalRead(sensora); //Leer y almacenar el valor del sensor Derecho
        Valorb = digitalRead(sensorb); //Leer y almacenar el valor del sensor Central
        Valorc = digitalRead(sensorc); //Leer y almacenar el valor del sensor Izquierdo
       
        delay(20);//Esperar 20 ms

        if (Valora == 1 && Valorb == 0 && Valorc == 1 && direccion != 3) { //B-N-B, Ambas Ruedas andando
          analogWrite(ENA, 190);
          analogWrite(ENB, 190);
          digitalWrite (IN2, HIGH); //Anda Rueda derecha
          digitalWrite (IN1, LOW);
          delay(50); //Por alguna razón los motores arrancan a destiempo, uso esto para intentar sincronizarlo.
          digitalWrite (IN4, HIGH); //Anda Rueda Izquierda
          digitalWrite (IN3, LOW);
          direccion = 3;
       
          contadorDeteccionesDerechas = 0; //Cuando se detecta ir para adelante, se resetean los cambios de dirección, para ignorar las lecturas erráticas.
          contadorDeteccionesIzquierdas = 0;
        }
        if ((Valora == 1 && Valorb == 0 && Valorc == 0 || Valora == 1 && Valorb == 1 && Valorc == 0) && direccion != 1) { //B-N-N,BBN Gira para Derecha
          contadorDeteccionesIzquierdas++;
          contadorDeteccionesDerechas = 0;
          if (contadorDeteccionesIzquierdas > 2) {
            analogWrite(ENA, 180); //Anda Rueda Izquierda
            analogWrite(ENB, 0);
            digitalWrite (IN2, HIGH);
            digitalWrite (IN1, LOW);
            digitalWrite (IN4, HIGH);
            digitalWrite (IN3, LOW);
            direccion = 1;
  
          }
        }
        if ((Valora == 0 && Valorb == 0 && Valorc == 1 || Valora == 0 && Valorb == 1 && Valorc == 1) && direccion != 2) { //N-N-B,NBB Gira para Izquierda
          contadorDeteccionesDerechas++;
          contadorDeteccionesIzquierdas = 0;
          if (contadorDeteccionesDerechas > 2) {
            analogWrite(ENA, 0);
            analogWrite(ENB, 180); //Anda Rueda Derecha
            digitalWrite (IN2, HIGH);
            digitalWrite (IN1, LOW);
            digitalWrite (IN4, HIGH);
            digitalWrite (IN3, LOW);
            direccion = 2;
        
          }

        }
        if (Valora == 0 && Valorb == 0 && Valorc == 0) { //N-N-N, Estación detectada (O hubo crash), frenar ruedas
          //Millis para detección de paradas, sino con cada una detecta innumerables paradas
          millisParada = millis();
          if (millisParada > millisParadaAnt + 1000) {
            millisParadaAnt = millisParada;
            parada++;
            if (parada % cantidadParadas == 0) { //Si es parada cero
              abasteciendo = true; //Abasteciendo, entra en estado receptor
              contadorRecepcion = 0; //Preparamos el contador para la recepción de paradas
              digitalWrite (IN2, LOW); //Frena Rueda derecha
              digitalWrite (IN1, LOW);
              digitalWrite (IN4, LOW); //Frena Rueda Izquierda
              digitalWrite (IN3, LOW);
              direccion = 0; //Informe de frenado
              //Envío de señal de llegada a Abastecimiento por BT
              Serial.write('0');
            }
            else if (paradas[parada % cantidadParadas] == true) { //Si valía parar en dicha parada
              digitalWrite (IN2, LOW); //Frena Rueda derecha
              digitalWrite (IN1, LOW);
              digitalWrite (IN4, LOW); //Frena Rueda Izquierda
              digitalWrite (IN3, LOW);
              direccion = 0; //Informe de frenado
              esperando = 5;
              //Envío de señal de llegada a Parada por BT
              Serial.write((parada % cantidadParadas) + '0');
            }
          }
        }
      } else { //Si está esperando
        direccion = 0;
        //Un loop fijo de "esperando" segundos para la espera en parada
        nota = 261;
        while (esperando > 0) {
          millisEspera = millis();
          if (millisEspera - millisEsperaAnt >= 1000) {
            millisEsperaAnt = millisEspera;
            tone(speakerPin, nota, 500);
            nota += 40;
            esperando--;
          }
        }
        //Aviso de parada parada%cantidadParadas completada
      }
    } else {//Si está abasteciendo
      // Recepción de listas de paradas
      if (contadorRecepcion < (cantidadParadas - 1)) { //Si aún no recibió todas las paradas
        if (c == '0' || c == '1') { //Si es un 0 o 1, los caracteres de lista de paradas
          contadorRecepcion++;
          paradas[contadorRecepcion] = c - '0';
          c = 'X'; //Para evitar volver a leer el mismo hasta que se asigne en la lectura arriba.
        }
      }
      if (contadorRecepcion == (cantidadParadas - 1)) { //Si ya recibió todas las paradas
        abasteciendo = false; //Deja el modo de abastecimiento
        
      }
    }
  }
}

