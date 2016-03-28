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

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.text.NumberFormat;
import java.util.Locale;

import com.pedometrak.R;

public class FragmentOverviewController extends Fragment {

    private TextView mTextView;

    private PieChart mPie;

    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
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
    }

    private void updatePie() {
        // slice for the steps taken today
        PieModel sliceCurrent = new PieModel("", 0, Color.parseColor("#99CC00"));
        mPie.addPieSlice(sliceCurrent);

        // slice for the "missing" steps until reaching the goal
        PieModel sliceGoal = new PieModel("", 10000, Color.parseColor("#CC0000"));
        mPie.addPieSlice(sliceGoal);
        mPie.startAnimation();
    }
}
