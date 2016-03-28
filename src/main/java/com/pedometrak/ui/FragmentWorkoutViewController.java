package com.pedometrak.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Chronometer;
import android.widget.TextView;
import com.pedometrak.BuildConfig;
import com.pedometrak.DatabaseManager;
import com.pedometrak.MetricCalculator;
import com.pedometrak.R;
import com.pedometrak.util.Logger;
import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.text.NumberFormat;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FragmentWorkoutViewController extends Fragment implements SensorEventListener {

    private BarChart mBarChart;
    private TextView mTextView;
    private Chronometer mChronometer;

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
    public void onStart() {
        super.onStart();
        mBarChart = (BarChart) getActivity().findViewById(R.id.bargraph);
        mTextView = (TextView) getActivity().findViewById(R.id.stats);
        mChronometer = (Chronometer) getActivity().findViewById(R.id.chronometer);

        final SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometrak", Context.MODE_MULTI_PROCESS);

        float value = prefs.getFloat("stepsize_value", FragmentSettings.DEFAULT_STEP_SIZE);
        String unit = prefs.getString("stepsize_unit", FragmentSettings.DEFAULT_STEP_UNIT);
        float meters = unit.equals("cm") ? value/100f : 0.3048f*value;
        MetricCalculator.changeStrideLength(meters);

        mChronometer.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mChronometer.stop();
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

        mTextView.setText("Steps: " + NumberFormat.getInstance().format(step) + "\n" +
                "Distance Travelled: " + NumberFormat.getInstance().format(dist) + " m\n" +
                "Calories Burned: " + NumberFormat.getInstance().format(cal) + " cal");

        updateBars(step, dist, cal);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register a sensor listener to update the UI if a step is taken
        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_UI, 0);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop sensor here
        try {
            SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Logger.log(e);
            e.printStackTrace();
        }

        DatabaseManager db = DatabaseManager.getInstance(getActivity());
        // TODO: Save current data
        db.close();
    }

    /**
     * Updates the bar graph..
     */
    private void updateBars(int steps, float distance, float calories) {
        mBarChart.clearChart();
        mBarChart.addBar(new BarModel("Steps", steps, Color.parseColor("#99CC00")));
        mBarChart.addBar(new BarModel("Distance (m)", distance, Color.parseColor("#99CC00")));
        mBarChart.addBar(new BarModel("Calories (cal)", calories, Color.parseColor("#99CC00")));
    }
}
