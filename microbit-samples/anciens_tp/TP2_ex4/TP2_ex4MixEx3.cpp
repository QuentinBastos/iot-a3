#include "MicroBit.h"
#include "bme280.h"

// Déclaration globale de la carte et du bus I2C
MicroBit uBit;
MicroBitI2C i2c(I2C_SDA0, I2C_SCL0);

int main() {
  // Initialisation de la carte
  uBit.init();

  // Initialisation du capteur BME280 en lui passant la carte et le bus I2C
  bme280 bme(&uBit, &i2c);

  // Variables pour stocker les données brutes
  uint32_t pressure = 0;
  int32_t temp = 0;
  uint16_t humidite = 0;

  while (true) {
    // 1. Lecture des données brutes
    bme.sensor_read(&pressure, &temp, &humidite);

    // 2. Compensation (calcul des vraies valeurs grâce aux données de
    // calibration)
    int tmp = bme.compensate_temperature(temp);
    int pres = bme.compensate_pressure(pressure) / 100; // Conversion en hPa
    int hum = bme.compensate_humidity(humidite);

    // 3. Formatage et affichage de la Température (ex: "Temp:22.50 C")
    ManagedString displayTemp =
        "Temp:" + ManagedString(tmp / 100) + "." +
        (tmp > 0 ? ManagedString(tmp % 100) : ManagedString((-tmp) % 100)) +
        " C\r\n";
    uBit.serial.send(displayTemp); // affichage sur le port serie

    // 4. Formatage et affichage de l'Humidité
    ManagedString displayHum = "Humi:" + ManagedString(hum / 100) + "." +
                               ManagedString(tmp % 100) + " rH\r\n";
    uBit.serial.send(displayHum); // affichage sur le port serie

    // 5. Formatage et affichage de la Pression
    ManagedString displayPres = "Pres:" + ManagedString(pres) + " hPa\r\n";
    uBit.serial.send(displayPres); // affichage sur le port serie

    // Pause de 1 seconde avant la prochaine mesure
    uBit.sleep(1000);
  }

  release_fiber();
}

// Commande pour afficher dans le terminal mac : screen /dev/tty.usbmodemXXXXX
// 115200 Commande pour quitter : Ctrl + A puis Ctrl + option + shift + :
