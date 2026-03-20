## Question : 
Dans le cas de ce TP, quatre choix étaient possibles.
- Utiliser des cartes C8051F02x de SiliconLabs.
- Utiliser des cartes Arduino Uno.
- Utiliser des cartes STM32 de ST Micro-electronics.
- Utiliser des cartes Micro:bit

**Question 1 :**
- Recherchez les caractéristiques des diverses cartes en question et les micro-contrôleurs utilisés par chacune d’entre elles.

**Réponse :** 
- Le choix de la **Micro:bit** (basée sur un **Nordic nRF52833 / ARM Cortex-M4**) est privilégié pour l'IoT car elle intègre nativement des capteurs (accéléromètre, magnétomètre), une matrice LED et une connectivité Bluetooth/Radio, contrairement aux cartes **Arduino Uno** (8-bit), **C8051** (obsolète) ou **STM32** (plus complexe), facilitant ainsi un prototypage rapide sans câblage externe.

**Question 2 :**
- Recherchez les différentes documentations techniques pour la carte Micro:bit et de ses composants. Est-ce que le site du distributeur (BBC) propose des documentations plus complètes que ceux des fabricants ?

**Réponse :** 
- [Micro:bit Tech Site](https://tech.microbit.org/) (Hardware)
- [Nordic nRF52833](https://www.nordicsemi.com/Products/nRF52833) (CPU/Radio)
- [LSM303AGR](https://www.st.com/en/mems-and-sensors/lsm303agr.html) (Capteurs)

Non, les fabricants (Nordic/ST) fournissent les documentations exhaustives (registres, électrique), tandis que la BBC propose des guides simplifiés.

**Question 3 :**
- Quels sont les outils dont vous aurez besoin pour passer de votre code source à un système fonctionnant avec la carte Micro:bit ?

**Réponse :** 
- **Environnement :** `Docker` (image dédiée) et un IDE (`CLion` ou `VS Code` avec Dev Containers).
- **Gestionnaire de build :** `yotta` installé dans un environnement virtuel Python (`venv`).
- **Chaîne de compilation :** `arm-none-eabi-gcc`, `cmake`, `ninja` et `srecord`.
- **Configuration :** Des correctifs `sed` sur les templates `yotta` pour assurer la compatibilité avec CMake 3.0+.
