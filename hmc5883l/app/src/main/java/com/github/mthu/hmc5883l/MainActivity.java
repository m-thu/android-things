package com.github.mthu.hmc5883l;

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
            Hmc5883l hmc5883l = new Hmc5883l();
            hmc5883l.setAverage(Hmc5883l.AVERAGE_8);
            hmc5883l.setMode(Hmc5883l.MODE_NORMAL);
            hmc5883l.setGain(Hmc5883l.GAIN_1090);

            for (;;) {
                // Perform single measurement
                hmc5883l.setOperatingMode(Hmc5883l.OP_SINGLE);
                // Wait for measurement to complete
                while (!hmc5883l.getRdy())
                    ;
                // Read all three axes
                double[] b = hmc5883l.getXYZ();
                Log.i(TAG, "X: " + b[0] + " mG, Y: " + b[1] + " mG, Z: " + b[2] + " mG");

                Thread.sleep(1_000);
            }
        } catch (IOException|InterruptedException|Hmc5883l.RangeOverflowException e) {
            e.printStackTrace();
        }
    }
}
