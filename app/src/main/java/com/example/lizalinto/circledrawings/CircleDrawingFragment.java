package com.example.lizalinto.circledrawings;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by lizalinto on 4/6/15.
 */
public class CircleDrawingFragment extends Fragment implements SensorEventListener {

    private  CircleDrawingView circleView;
    private SensorManager mSensorManager;
    private Sensor accelerometer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_circle_drawing,container,false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        circleView = (CircleDrawingView)getActivity().findViewById(R.id.circleView);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onResume(){
        super.onResume();
        boolean isRunning = mSensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(circleView, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
