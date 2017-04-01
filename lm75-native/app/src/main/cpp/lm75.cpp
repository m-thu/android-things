#include "lm75.h"

Lm75::Lm75()
{
    Lm75(DEFAULT_BUS_NAME, DEFAULT_SLAVE_ADDRESS);
}

Lm75::Lm75(const char *bus, uint32_t slave)
{
    client = APeripheralManagerClient_new();

    if (client != nullptr) {
        APeripheralManagerClient_openI2cDevice(client, bus, slave, &i2c_device);
        if (i2c_device == nullptr)
            __android_log_assert("i2c_device != nullptr", TAG, "Failed to open I2C device!");
    } else {
        __android_log_assert("client != nullptr", TAG, "");
    }
}

Lm75::~Lm75()
{
    if (i2c_device != nullptr)
        AI2cDevice_delete(i2c_device);
    if (client != nullptr)
        APeripheralManagerClient_delete(client);
}

void Lm75::power_up()
{
    if (i2c_device != nullptr) {
        if (AI2cDevice_writeRegByte(i2c_device, LM75_REG_CONFIG, 0x00) != 0)
            __android_log_assert("writeRegByte != 0", TAG, "Couldn't write register!");
    } else {
        __android_log_assert("i2c_device != nullptr", TAG, "I2C device isn't connected!");
    }
}

void Lm75::power_down()
{
    if (i2c_device != nullptr) {
        if (AI2cDevice_writeRegByte(i2c_device, LM75_REG_CONFIG, LM75_SHUTDOWN) != 0)
            __android_log_assert("writeRegByte != 0", TAG, "Couldn't write register!");
    } else {
        __android_log_assert("i2c_device != nullptr", TAG, "I2C device isn't connected!");
    }
}

float Lm75::get_temp()
{
    uint16_t tmp;

    if (i2c_device != nullptr) {
        if (AI2cDevice_readRegWord(i2c_device, LM75_REG_TEMP, &tmp) != 0)
            __android_log_assert("readRegWord != 0", TAG, "Error reading register!");
        tmp >>= 7;
        if (tmp & 0x100)
            tmp |= 0xfe00;
        return (int16_t)tmp / 2.f;
    } else {
        __android_log_assert("i2c_device != nullptr", TAG, "I2C device isn't connected!");
    }
}