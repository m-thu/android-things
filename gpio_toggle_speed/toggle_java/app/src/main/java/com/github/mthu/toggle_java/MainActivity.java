package com.github.mthu.toggle_java;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {
    private final static String TAG      = MainActivity.class.getSimpleName();
    private final static String GPIO_PIN = "BCM17";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        PeripheralManagerService service = new PeripheralManagerService();
//        List<String> gpio = service.getGpioList();
//        Log.d(TAG, "GPIOs: "+gpio);
        // BCM12, BCM13, BCM16, BCM17, BCM18, BCM19, BCM20, BCM21, BCM22, BCM23, BCM24, BCM25, BCM26, BCM27, BCM4, BCM5, BCM6

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Configure GPIO pin as an output
                    PeripheralManagerService service = new PeripheralManagerService();
                    Gpio gpio = service.openGpio(GPIO_PIN);
                    gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
                    gpio.setActiveType(Gpio.ACTIVE_HIGH);

                    // Toggle pin
                    for (;;) {
                        gpio.setValue(true);
                        gpio.setValue(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).run();
    }
}
