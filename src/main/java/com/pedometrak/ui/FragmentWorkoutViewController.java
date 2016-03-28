package com.pedometrak.ui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pedometrak.DatabaseManager;
import com.pedometrak.MetricCalculator;
import com.pedometrak.R;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FragmentWorkoutViewController extends Fragment implements SensorEventListener {

    public FragmentWorkoutViewController() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workout_view, container, false);
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {
        // won't happen
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        int step = MetricCalculator.calculateSteps();
        float cal = MetricCalculator.calculateCalories();
        float dist = MetricCalculator.calculateDistance();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register a sensor listener to update the UI if a step is taken
        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_UI, 0);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop sensor here
        try {
            SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatabaseManager db = DatabaseManager.getInstance(getActivity());
        // TODO: Save current data
        db.close();
    }
}
