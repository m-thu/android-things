LM75 temperature sensor
=======================

### Wiring (Raspberry Pi 3)
```
I2C1 SCL <----> SCL (LM75)
I2C1 SDA <----> SDA (LM75)
VCC      ------ VS  (LM75)
GND      ------ GND (LM75)
[NC]     <----- OS  (LM75)
GND      ------ A0  (LM75)
GND      ------ A1  (LM75)
GND      ------ A2  (LM75)
```

### References
[LM75 datasheet (Maxim)](https://datasheets.maximintegrated.com/en/ds/LM75.pdf)

[LM75 datasheet (TI)](http://www.ti.com/lit/ds/symlink/lm75b.pdf)

[I2C-bus specification and user manual](http://www.nxp.com/documents/user_manual/UM10204.pdf)
