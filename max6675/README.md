MAX6675 Cold-Junction-Compensated K-Thermocouple-to-Digital Converter
=====================================================================

### Wiring (Raspberry Pi 3)
```
SPI0.SCLK -----> SCK (MAX6675)
SPI0.MISO <----- SO  (MAX6675)
SPI0.CE0  -----> CS  (MAX6675)
3.3 V     ------ VCC (MAX6675)
GND       ------ GND (MAX6675)
```

### References
[MAX6675 datasheet](https://datasheets.maximintegrated.com/en/ds/MAX6675.pdf)
