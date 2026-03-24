/*
The MIT License (MIT)

Copyright (c) 2016 British Broadcasting Corporation.
This software is provided by Lancaster University by arrangement with the BBC.

Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.
*/

#include "MicroBit.h"

MicroBit uBit;

void onButtonA(MicroBitEvent e)
{
    uBit.display.print("A");
}

void onButtonB(MicroBitEvent e)
{
    uBit.display.print("B");
}

void temperature(MicroBitEvent e)
{
    int temp = uBit.thermometer.getTemperature();
    if (temp >= 27) {
        uBit.display.scroll(temp);
    }
}

int tiltLeftCount = 0;

void onTiltLeft(MicroBitEvent e)
{
    tiltLeftCount++;
    uBit.display.scroll(tiltLeftCount);
}

void onNorthFacing(MicroBitEvent e)
{
    int heading = uBit.compass.heading();
    // Le Nord est entre 315° et 45°
    if (heading < 45 || heading > 315) {
        uBit.display.print("N");
    } else {
        uBit.display.clear(); 
    }
}

int main()
{
    // Initialise the micro:bit runtime.
    uBit.init();

    // Enregistrement des événements lors du clic sur les boutons A et B
    // uBit.messageBus.listen(MICROBIT_ID_BUTTON_A, MICROBIT_BUTTON_EVT_CLICK, onButtonA);
    // uBit.messageBus.listen(MICROBIT_ID_BUTTON_B, MICROBIT_BUTTON_EVT_CLICK, onButtonB);


    // Enregistrement d'un événement pour afficher la température toutes les 5 secondes
    // uBit.messageBus.listen(MICROBIT_ID_THERMOMETER, MICROBIT_THERMOMETER_EVT_UPDATE, temperature);

    // Enregistrement d'un événement pour compter le nombre de fois que le micro:bit est incliné vers la gauche
    // uBit.messageBus.listen(MICROBIT_ID_GESTURE, MICROBIT_ACCELEROMETER_EVT_TILT_LEFT, onTiltLeft);

    // Enregistrement d'un événement pour afficher le nord lorsque le micro:bit est vers le nord.
    uBit.compass.calibrate();
    uBit.messageBus.listen(MICROBIT_ID_COMPASS, MICROBIT_COMPASS_EVT_DATA_UPDATE, onNorthFacing);

    // Message d'accueil sur l'écran LED
    uBit.display.scroll("PRETS?");

    // If main exits, there may still be other fibers running or registered event handlers etc.
    // Simply release this fiber, which will mean we enter the scheduler. Worse case, we then
    // sit in the idle task forever, in a power efficient sleep.
    release_fiber();
}

