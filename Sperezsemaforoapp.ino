#include <SoftwareSerial.h>

// HC-05
// Arduino pin 10 = RX (recibe desde TX del HC-05)
// Arduino pin 11 = TX (envía hacia RX del HC-05)
SoftwareSerial BT(10, 11);

// LEDs
const int LED1_PIN = 4;   // Verde
const int LED2_PIN = 5;   // Amarillo
const int LED3_PIN = 6;   // Rojo

void setup() {
  pinMode(LED1_PIN, OUTPUT);
  pinMode(LED2_PIN, OUTPUT);
  pinMode(LED3_PIN, OUTPUT);

  digitalWrite(LED1_PIN, LOW);
  digitalWrite(LED2_PIN, LOW);
  digitalWrite(LED3_PIN, LOW);

  Serial.begin(9600);
  BT.begin(9600);

  Serial.println("Sistema listo");
}

void loop() {
  if (BT.available() > 0) {
    char cmd = BT.read();

    Serial.print("Comando recibido: ");
    Serial.println(cmd);

    switch (cmd) {
      case '0':
        digitalWrite(LED1_PIN, LOW);
        digitalWrite(LED2_PIN, LOW);
        digitalWrite(LED3_PIN, LOW);
        break;

      case '1':
        digitalWrite(LED1_PIN, HIGH);
        break;

      case '2':
        digitalWrite(LED1_PIN, LOW);
        break;

      case '3':
        digitalWrite(LED2_PIN, HIGH);
        break;

      case '4':
        digitalWrite(LED2_PIN, LOW);
        break;

      case '5':
        digitalWrite(LED3_PIN, HIGH);
        break;

      case '6':
        digitalWrite(LED3_PIN, LOW);
        break;
    }
  }
}