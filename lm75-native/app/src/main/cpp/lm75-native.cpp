#include "lm75.h"

#include <android_native_app_glue.h>

// Tag for debug messages
const char *TAG = "lm75-native";
// Temperature measurement interval
const int INTERVAL = 1000;

void android_main(android_app *app)
{
    app_dummy();

    Lm75 *lm75 = new Lm75();
    lm75->power_up();

    while (!app->destroyRequested) {
        android_poll_source *src;

        __android_log_print(ANDROID_LOG_INFO, TAG, "Temperature: %.1f °C", lm75->get_temp());

        // Wait for INTERVAL milliseconds
        if (ALooper_pollOnce(INTERVAL, nullptr, nullptr, reinterpret_cast<void **>(&src)) >= 0) {
            if (src)
                src->process(app, src);
        }
    }

    lm75->power_down();
    delete lm75;
}