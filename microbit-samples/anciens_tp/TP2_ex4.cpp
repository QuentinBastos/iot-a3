#include "MicroBit.h"

MicroBit uBit;

// L'adresse I2C du BME280 sur 8 bits pour l'écriture
const int BME280_ADDR = 0xEC;

void testerBME280() {
  // Le registre 0xD0 contient l'identifiant unique du BME280
  char cmd[1];
  cmd[0] = 0xD0;

  // 1. On indique au capteur qu'on veut lire ce registre précis
  uBit.i2c.write(BME280_ADDR, cmd, 1);

  // 2. On lit la réponse du capteur (1 octet)
  char reponse[1];
  uBit.i2c.read(BME280_ADDR, reponse, 1);

  // 3. Vérification : le BME280 doit renvoyer 0x60 (soit 96 en décimal)
  if (reponse[0] == 0x60) {
    uBit.display.scroll("BME280 OK!");
  } else {
    uBit.display.scroll("ERREUR I2C");
  }
}

int main() {
  // Initialisation globale de la Micro:bit
  uBit.init();

  // Appel de notre fonction de test
  testerBME280();

  // Libération du thread principal pour laisser tourner les tâches de fond
  release_fiber();
  return 0;
}