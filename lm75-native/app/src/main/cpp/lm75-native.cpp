#include "lm75.h"

#include <android_native_app_glue.h>

// Tag for debug messages
const char *TAG = "lm75-native";
// Temperature measurement interval
const int INTERVAL = 1000;

void android_main(android_app *app)
{
    app_dummy();

//    Lm75 *lm75 = new Lm75();
//    lm75->power_up();

    APeripheralManagerClient *client = APeripheralManagerClient_new();
    AI2cDevice *i2c_device;
    const char     *DEFAULT_BUS_NAME      = "I2C1";
    const uint32_t  DEFAULT_SLAVE_ADDRESS = 0x48;

    APeripheralManagerClient_openI2cDevice(client, DEFAULT_BUS_NAME, DEFAULT_SLAVE_ADDRESS, &i2c_device);
    AI2cDevice_writeRegByte(i2c_device, 0x01, 0x00);

    uint16_t tmp;
    AI2cDevice_readRegWord(i2c_device, 0x00, &tmp);
    tmp = __builtin_bswap16(tmp) >> 7;
    if (tmp & 0x100)
        tmp |= 0xfe00;
    __android_log_print(ANDROID_LOG_INFO, TAG, "Temperature: %.1f °C", (int16_t)tmp / 2.f);

    AI2cDevice_delete(i2c_device);
    APeripheralManagerClient_delete(client);

    while (!app->destroyRequested) {
        android_poll_source *src;

//        __android_log_print(ANDROID_LOG_INFO, TAG, "Temperature: %.1f °C", lm75->get_temp());

        // Wait for INTERVAL milliseconds
        if (ALooper_pollOnce(INTERVAL, nullptr, nullptr, reinterpret_cast<void **>(&src)) >= 0) {
            if (src)
                src->process(app, src);
        }
    }

//    lm75->power_down();
//    delete lm75;
}