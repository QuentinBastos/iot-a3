#include "MicroBit.h"

MicroBit uBit;

int main()
{
    uBit.init();

    while(1) {
        // Rouge ON
        uBit.io.P0.setDigitalValue(1);
        uBit.io.P1.setDigitalValue(0);
        uBit.io.P2.setDigitalValue(0);
        uBit.sleep(3000); 

        // Vert ON
        uBit.io.P0.setDigitalValue(0);
        uBit.io.P1.setDigitalValue(0);
        uBit.io.P2.setDigitalValue(1);
        uBit.sleep(3000); 

        // Orange ON
        uBit.io.P0.setDigitalValue(0);
        uBit.io.P1.setDigitalValue(1);
        uBit.io.P2.setDigitalValue(0);
        uBit.sleep(1000); 
    }
}
