package com.example.har;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static List<Float> acc_tot, gyr_tot, linacc_tot;
    private static final int TIME_STAMP = 100;
    private static final String TAG = "MainActivity";

    private static List<Float> ax,ay,az;
    private static List<Float> gx,gy,gz;
    private static List<Float> lx,ly,lz;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mGyroscope, mLinearAcceleration;

    private float[] results;
    private ActivityClassifier classifier;

    private TextView bikingTextView, downstairsTextView, joggingTextView, sittingTextView, standingTextView, upstairsTextView, walkingTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        acc_tot = new ArrayList<>();
        gyr_tot = new ArrayList<>();
        linacc_tot = new ArrayList<>();

        initLayoutItems();

        ax=new ArrayList<>(); ay=new ArrayList<>(); az=new ArrayList<>();
        gx=new ArrayList<>(); gy=new ArrayList<>(); gz=new ArrayList<>();
        lx=new ArrayList<>(); ly=new ArrayList<>(); lz=new ArrayList<>();

        mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        classifier=new ActivityClassifier(getApplicationContext());

        mSensorManager.registerListener(this,mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this,mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this,mLinearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void initLayoutItems() {
        bikingTextView = findViewById(R.id.biking_TextView);
        downstairsTextView = findViewById(R.id.downstairs_TextView);
        joggingTextView = findViewById(R.id.jogging_TextView);
        sittingTextView  = findViewById(R.id.sitting_TextView);
        standingTextView = findViewById(R.id.standing_TextView);
        upstairsTextView = findViewById(R.id.upstairs_TextView);
        walkingTextView = findViewById(R.id.walking_TextView);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax.add(event.values[0]);
            ay.add(event.values[1]);
            az.add(event.values[2]);

            float accMag = (float)Math.sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1] + event.values[2]*event.values[2]);
            acc_tot.add(accMag);
        } else if(sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gx.add(event.values[0]);
            gy.add(event.values[1]);
            gz.add(event.values[2]);

            float gyrMag = (float)Math.sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1] + event.values[2]*event.values[2]);
            gyr_tot.add(gyrMag);
        } else {
            lx.add(event.values[0]);
            ly.add(event.values[1]);
            lz.add(event.values[2]);

            float linaccMag = (float)Math.sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1] + event.values[2]*event.values[2]);
            linacc_tot.add(linaccMag);
        }

        predictActivity();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void predictActivity() {
        List<Float> data=new ArrayList<>();
        if (ax.size() >= TIME_STAMP && ay.size() >= TIME_STAMP && az.size() >= TIME_STAMP
                && gx.size() >= TIME_STAMP && gy.size() >= TIME_STAMP && gz.size() >= TIME_STAMP
                && lx.size() >= TIME_STAMP && ly.size() >= TIME_STAMP && lz.size() >= TIME_STAMP
                && acc_tot.size() >= TIME_STAMP && gyr_tot.size() >= TIME_STAMP && linacc_tot.size() >= TIME_STAMP) {

            data.addAll(ax.subList(0,TIME_STAMP));
            data.addAll(ay.subList(0,TIME_STAMP));
            data.addAll(az.subList(0,TIME_STAMP));
            data.addAll(gx.subList(0,TIME_STAMP));
            data.addAll(gy.subList(0,TIME_STAMP));
            data.addAll(gz.subList(0,TIME_STAMP));
            data.addAll(lx.subList(0,TIME_STAMP));
            data.addAll(ly.subList(0,TIME_STAMP));
            data.addAll(lz.subList(0,TIME_STAMP));
            data.addAll(acc_tot.subList(0,TIME_STAMP));
            data.addAll(gyr_tot.subList(0,TIME_STAMP));
            data.addAll(linacc_tot.subList(0,TIME_STAMP));

            results = classifier.predictProbabilities(toFloatArray(data));
            Log.i(TAG, "predictActivity: "+ Arrays.toString(results));

            float maxVal = -1;
            int maxIdx = -1;
            for (int i = 0; i < results.length; i++) {
                float curVal = results[i];
                if (curVal > maxVal) {
                    maxVal = curVal;
                    maxIdx = i;
                }
            }

            bikingTextView.setText("Biking: \t" + round(results[0] * 100,2) + "%");
            downstairsTextView.setText("DownStairs: \t" + round(results[1] * 100,2) + "%");
            joggingTextView.setText("Jogging: \t" + round(results[2] * 100,2) + "%");
            sittingTextView.setText("Sitting: \t" + round(results[3] * 100,2) + "%");
            standingTextView.setText("Standing: \t" + round(results[4] * 100,2) + "%");
            upstairsTextView.setText("Upstairs: \t" + round(results[5] * 100,2) + "%");;
            walkingTextView.setText("Walking: \t" + round(results[6] * 100,2) + "%");

            // For instance, highlight the most probable activity by making its text bold
            bikingTextView.setTypeface(null, Typeface.NORMAL);
            downstairsTextView.setTypeface(null, Typeface.NORMAL);
            joggingTextView.setTypeface(null, Typeface.NORMAL);
            sittingTextView.setTypeface(null, Typeface.NORMAL);
            standingTextView.setTypeface(null, Typeface.NORMAL);
            upstairsTextView.setTypeface(null, Typeface.NORMAL);
            walkingTextView.setTypeface(null, Typeface.NORMAL);

            switch (maxIdx) {
                case 0:
                    bikingTextView.setTypeface(null, Typeface.BOLD);
                    break;
                case 1:
                    downstairsTextView.setTypeface(null, Typeface.BOLD);
                    break;
                case 2:
                    joggingTextView.setTypeface(null, Typeface.BOLD);
                    break;
                case 3:
                    sittingTextView.setTypeface(null, Typeface.BOLD);
                    break;
                case 4:
                    standingTextView.setTypeface(null, Typeface.BOLD);
                    break;
                case 5:upstairsTextView.setTypeface(null, Typeface.BOLD);
                    break;
                case 6:
                    walkingTextView.setTypeface(null, Typeface.BOLD);
                    break;
            }

            data.clear();
            ax.clear(); ay.clear(); az.clear();
            gx.clear(); gy.clear(); gz.clear();
            lx.clear(); ly.clear(); lz.clear();
            acc_tot.clear(); gyr_tot.clear(); linacc_tot.clear();
        }

            }

    private float round(float value, int decimal_places) {
        BigDecimal bigDecimal=new BigDecimal(Float.toString(value));
        bigDecimal = bigDecimal.setScale(decimal_places, BigDecimal.ROUND_HALF_UP);
        return bigDecimal.floatValue();
    }

    private float[] toFloatArray(List<Float> data) {
        int i=0;
        float[] array=new float[data.size()];
        for (Float f:data) {
            array[i++] = (f != null ? f: Float.NaN);
        }
        return array;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }
}