#include "MicroBit.h"

MicroBit uBit;

int main() {
  // Initialisation
  uBit.init();

  while (true) {
    // Lecture de la température du capteur interne de la Micro:bit
    int tempInterne = uBit.thermometer.getTemperature();

    // On formate le message.
    // L'ajout de "\r\n" à la fin permet de faire un retour à la ligne dans la
    // console du PC.
    ManagedString message =
        "Temp interne : " + ManagedString(tempInterne) + " C\r\n";

    // Envoi du message sur l'interface série (le câble USB)
    uBit.serial.send(message);

    // Pause d'une seconde
    uBit.sleep(1000);
  }

  release_fiber();
}