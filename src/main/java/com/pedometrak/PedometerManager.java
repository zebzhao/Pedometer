/*
 * Copyright 2013 Thomas Hoffmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pedometrak;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

import com.pedometrak.util.Logger;

/**
 * Background service which keeps the step-sensor listener alive to always get
 * the number of steps since boot.
 * <p/>
 * This service won't be needed any more if there is a way to read the
 * step-value without waiting for a sensor event
 */
public class PedometerManager extends Service implements SensorEventListener {

    public final static int MICROSECONDS_IN_ONE_SECOND = 1000000;
    public static List<Integer> steps = new ArrayList<>(10000);


    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {
        // won't be called
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (event.values[0] > Integer.MAX_VALUE) {
            if (BuildConfig.DEBUG) {
                Logger.log("probably not a real value: " + event.values[0]);
            }
        } else {
            if (BuildConfig.DEBUG) {
                Logger.log("adding to steps array: " + (int) event.values[0]);
            }
            steps.add((int) event.values[0]);
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Logger.log("PedometerManager onCreate");
        registerSensor();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) Logger.log("PedometerManager onDestroy");

        unregisterSensor();
        steps.clear();
    }

    private void registerSensor() {
        if (BuildConfig.DEBUG) Logger.log("Pedometer sensor registered");
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Enable batching with delay of max 1 seconds
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_NORMAL, MICROSECONDS_IN_ONE_SECOND);
    }

    private void unregisterSensor() {
        if (BuildConfig.DEBUG) Logger.log("Pedometer sensor removed");
        try {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Logger.log(e);
            e.printStackTrace();
        }
    }
}
