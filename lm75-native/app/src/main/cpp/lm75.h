#ifndef LM75_H
#define LM75_H

#include <android/log.h>
#include <pio/peripheral_manager_client.h>
#include <pio/i2c_device.h>

#include <cstdint>

class Lm75
{
public:
    Lm75();
    Lm75(const char *bus, uint32_t slave);
    ~Lm75();

    // Power up sensor
    void power_up();
    // Power down sensor
    void power_down();
    // Read temperature
    float get_temp();

private:
    // Default bus name and slave address
    const char     *DEFAULT_BUS_NAME      = "I2C1";
    const uint32_t  DEFAULT_SLAVE_ADDRESS = 0x48;

    // Tag for debug messages
    const char *TAG = "lm75";

    APeripheralManagerClient *client;
    AI2cDevice *i2c_device;

    // LM75 registers //

    enum {
        // Temperature (read only)
        LM75_REG_TEMP = 0x0,
        // Configuration (read/write)
        LM75_REG_CONFIG,
        // T_HYST (r/w)
        LM75_REG_THYST,
        // T_OS (r/w)
        LM75_REG_TOS
    };

    // Command register

    enum {
        // Bit 7..5: always zero
        // Bit 4..3: fault queue
        LM75_FAULTS_1    = 0x0 << 3,
        LM75_FAULTS_2    = 0x1 << 3,
        LM75_FAULTS_4    = 0x2 << 3,
        LM75_FAULTS_6    = 0x3 << 3,
        // Bit 2: o.s. polarity (0: active low, 1: active high)
        LM75_OS_POLARITY = 1 << 2,
        // Bit 1: comparator/interrupt mode (0: comparator, 1: interrupt)
        LM75_CMP_INT     = 1 << 1,
        // Bit 0: shutdown (1: low power shutdown mode)
        LM75_SHUTDOWN    = 1 << 0
    };

};

#endif // LM75_H
