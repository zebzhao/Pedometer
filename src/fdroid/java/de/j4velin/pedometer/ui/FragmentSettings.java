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
package de.j4velin.pedometer.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import de.j4velin.pedometer.DatabaseManager;
import de.j4velin.pedometer.R;
import de.j4velin.pedometer.PedometerManager;

public class FragmentSettings extends PreferenceFragment implements OnPreferenceClickListener {

    final static float DEFAULT_STEP_SIZE = Locale.getDefault() == Locale.US ? 2.5f : 75f;
    final static String DEFAULT_STEP_UNIT = Locale.getDefault() == Locale.US ? "ft" : "cm";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        final SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS);

        Preference stepsize = findPreference("stepsize");
        stepsize.setOnPreferenceClickListener(this);
        stepsize.setSummary(getString(R.string.step_size_summary,
                prefs.getFloat("stepsize_value", DEFAULT_STEP_SIZE),
                prefs.getString("stepsize_unit", DEFAULT_STEP_UNIT)));

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_pause).setVisible(false);
        menu.findItem(R.id.action_split_count).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return ((ActivityMain) getActivity()).optionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        AlertDialog.Builder builder;
        View v;
        final SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_MULTI_PROCESS);

        switch (preference.getTitleRes()) {
            case R.string.step_size:
                builder = new AlertDialog.Builder(getActivity());

                v = getActivity().getLayoutInflater().inflate(R.layout.stepsize, null);

                final RadioGroup unit = (RadioGroup) v.findViewById(R.id.unit);
                final EditText value = (EditText) v.findViewById(R.id.value);

                unit.check(prefs.getString("stepsize_unit", DEFAULT_STEP_UNIT).equals("cm") ? R.id.cm : R.id.ft);

                value.setText(String.valueOf(prefs.getFloat("stepsize_value", DEFAULT_STEP_SIZE)));

                builder.setView(v);
                builder.setTitle(R.string.set_step_size);

                builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            prefs.edit()
                                    .putFloat("stepsize_value", Float.valueOf(value.getText().toString()))
                                    .putString("stepsize_unit", unit.getCheckedRadioButtonId() == R.id.cm ? "cm" : "ft").apply();
                            preference
                                    .setSummary(
                                            getString(R.string.step_size_summary,
                                            Float.valueOf(value.getText().toString()),
                                                    unit.getCheckedRadioButtonId() == R.id.cm ? "cm" : "ft"));
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                break;
        }
        return false;
    }
}
