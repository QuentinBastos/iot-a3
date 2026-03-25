#include "MicroBit.h"
#include "NeoPixel.h" 

MicroBit uBit;

int main() {
    uBit.init();

    while (true) {
        np.setPixelColor(0, 0, 0, 255);
        np.show();                    
        uBit.sleep(250);
        
        np.setPixelColor(0, 255, 255, 255);
        np.show();
        uBit.sleep(250);
        
        np.setPixelColor(0, 255, 0, 0);
        np.show();
        uBit.sleep(250);
        
    }

    release_fiber(); 
}