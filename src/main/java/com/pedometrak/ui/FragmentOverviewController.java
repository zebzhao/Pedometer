/*
 * Copyright 2014 Thomas Hoffmann
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
package com.pedometrak.ui;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pedometrak.LocalDatabaseManager;
import com.pedometrak.network.JsonRequestCallback;
import com.pedometrak.network.ServerConnector;
import com.pedometrak.util.Logger;
import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;

import java.text.NumberFormat;
import java.util.Locale;

import com.pedometrak.R;
import org.json.JSONObject;

public class FragmentOverviewController extends Fragment {

    private TextView mTextView;
    private PieChart mPie;
    private BarChart mBarChart;

    private boolean showSteps = true;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_overview, null);

        mTextView = (TextView) v.findViewById(R.id.steps);
        mBarChart = (BarChart) v.findViewById(R.id.bargraph);
        mPie = (PieChart) v.findViewById(R.id.graph);

        mPie.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                showSteps = !showSteps;
            }
        });
        return v;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return ((ActivityMain) getActivity()).handleItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePie();
        updateBars();
    }

    private void updatePie() {
        // slice for the steps taken today
        PieModel sliceGoal = new PieModel("", 100, Color.parseColor("#CC0000"));
        mPie.addPieSlice(sliceGoal);
        mPie.startAnimation();

        ServerConnector.getInstance(getActivity()).getRank(new JsonRequestCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                PieModel sliceCurrent = new PieModel("", (float) response.optDouble("totalStepsTaken")*100, Color.parseColor("#99CC00"));
                mPie.addPieSlice(sliceCurrent);
                mTextView.setText(((float) response.optDouble("totalStepsTaken")*100) + "%");
            }
            @Override
            public void onError() {
            }
        });
    }

    /**
     * Updates the bar graph..
     */
    private void updateBars() {
        LocalDatabaseManager db = LocalDatabaseManager.getInstance(getActivity());

        float totalDistance = db.queryTotalDistance();
        float totalSteps = db.queryTotalSteps();
        String distanceUnits = " (m)";
        String stepsUnits = "";

        if (totalDistance > 1000) {
            distanceUnits = " (km)";
            totalDistance /= 1000;
        }

        if (totalSteps > 1000) {
            stepsUnits = " (1000s)";
            totalSteps /= 1000;
        }

        mBarChart.clearChart();
        mBarChart.addBar(new BarModel("Average Calories", db.queryAverageCalories(), Color.parseColor("#99CC00")));
        mBarChart.addBar(new BarModel("Total Steps" + stepsUnits, totalSteps, Color.parseColor("#99CC00")));
        mBarChart.addBar(new BarModel("Total Distance" + distanceUnits, totalDistance, Color.parseColor("#99CC00")));
        mBarChart.startAnimation();
    }
}
