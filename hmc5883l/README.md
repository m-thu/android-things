HMC5883L 3-axis digital compass
===============================

### Wiring (Raspberry Pi 3)
```
I2C1 SCL <----> SCL       (HMC5883L)
I2C1 SDA <----> SDA       (HMC5883L)
3.3V     ------ VDD/VDDIO (HMC5883L)
GND      ------ GND       (HMC5883L)
[NC]     <----- DRDY      (HMC5883L)
```

### References
[HMC5883L datasheet](https://cdn-shop.adafruit.com/datasheets/HMC5883L_3-Axis_Digital_Compass_IC.pdf)

[I2C-bus specification and user manual](http://www.nxp.com/documents/user_manual/UM10204.pdf)
