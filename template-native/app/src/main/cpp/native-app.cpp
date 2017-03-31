#include <android_native_app_glue.h>
#include <android/log.h>

void android_main(android_app *app)
{
    app_dummy();

    __android_log_write(ANDROID_LOG_INFO, "native-app", "Hello world from native app!");

    while (!app->destroyRequested) {
        android_poll_source *src;

        if (ALooper_pollOnce(0, nullptr, nullptr, reinterpret_cast<void **>(&src)) >= 0) {
            if (src)
                src->process(app, src);
        }
    }
}