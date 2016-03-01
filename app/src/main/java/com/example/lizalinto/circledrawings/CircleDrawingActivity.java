package com.example.lizalinto.circledrawings;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;


public class CircleDrawingActivity extends ActionBarActivity implements SensorEventListener{

    private  CircleDrawingView circleView;
    private SensorManager mSensorManager;
    private Sensor accelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_drawing);

        circleView = (CircleDrawingView)findViewById(R.id.circleView);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    protected void onResume() {
        super.onResume();
        boolean isRunning = mSensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_GAME);
        if (!isRunning)
            Log.i("CircleDrawingActivity", "could not start accelerometer");
        mSensorManager.registerListener(circleView, accelerometer,
                SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_circle_drawing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            ArrayList circles = circleView.mCircles;
            circles.clear();
            circleView.invalidate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
