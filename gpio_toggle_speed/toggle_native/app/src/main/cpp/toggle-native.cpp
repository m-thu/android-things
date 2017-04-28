#include <android_native_app_glue.h>

#include <pio/gpio.h>
#include <pio/peripheral_manager_client.h>

const char *GPIO_PIN = "BCM17";

void android_main(android_app *app)
{
    app_dummy();

    APeripheralManagerClient *client = APeripheralManagerClient_new();
    AGpio *gpio;

    APeripheralManagerClient_openGpio(client, GPIO_PIN, &gpio);
    AGpio_setActiveType(gpio, AGPIO_ACTIVE_HIGH);
    AGpio_setDirection(gpio, AGPIO_DIRECTION_OUT_INITIALLY_LOW);

    for (;;) {
        AGpio_setValue(gpio, 1);
        AGpio_setValue(gpio, 0);
    }

    while (!app->destroyRequested) {
        android_poll_source *src;

        if (ALooper_pollOnce(0, nullptr, nullptr, reinterpret_cast<void **>(&src)) >= 0) {
            if (src)
                src->process(app, src);
        }
    }

    AGpio_delete(gpio);
    APeripheralManagerClient_delete(client);
}