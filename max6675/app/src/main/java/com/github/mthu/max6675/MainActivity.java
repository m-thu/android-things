package com.github.mthu.max6675;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Max6675 max6675 = new Max6675();

            for (;;) {
                Log.i(TAG, "Temperature: " + max6675.getTemp() + " °C");
                Thread.sleep(1_000);
            }
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
    }
}
