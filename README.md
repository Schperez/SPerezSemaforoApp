# SemaforoApp

Aplicación Android desarrollada en Kotlin para controlar 3 LEDs conectados a un Arduino mediante Bluetooth.

## Descripción
La app permite conectarse a un módulo Bluetooth HC-05 emparejado con el nombre **SLAVE** y controlar tres LEDs físicos desde el teléfono.

## Funciones
- Conexión Bluetooth con el módulo SLAVE
- Encendido y apagado individual de 3 LEDs
- Indicador visual de conexión
- Botón para apagar todos los LEDs

## Comandos Bluetooth
- `0` = Apagar todos los LEDs
- `1` = Encender LED 1
- `2` = Apagar LED 1
- `3` = Encender LED 2
- `4` = Apagar LED 2
- `5` = Encender LED 3
- `6` = Apagar LED 3

## Hardware utilizado
- Arduino Uno
- Módulo Bluetooth HC-05
- 3 LEDs
- 3 resistencias
- Protoboard y cables jumper

## Conexiones
### LEDs
- LED 1 (verde) -> pin 4
- LED 2 (amarillo) -> pin 5
- LED 3 (rojo) -> pin 6

### Bluetooth HC-05
- VCC -> 5V
- GND -> GND
- TXD -> pin 10 de Arduino
- RXD -> pin 11 de Arduino

## Tecnologías
- Kotlin
- Jetpack Compose
- Bluetooth clásico (SPP)
- Arduino IDE

## Autor
- Sergio Uriel Pérez Padilla
