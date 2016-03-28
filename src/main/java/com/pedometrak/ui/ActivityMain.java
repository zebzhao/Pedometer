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

package com.pedometrak.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.pedometrak.PedometerManager;
import com.pedometrak.R;

public class ActivityMain extends FragmentActivity {

    @Override
    protected void onCreate(final Bundle b) {
        super.onCreate(b);

        if (b == null) {
            // Create new fragment and transaction
            Fragment newFragment = new FragmentOverviewController();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this
            // fragment,
            // and add the transaction to the back stack
            transaction.replace(android.R.id.content, newFragment);

            // Commit the transaction
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStackImmediate();
        } else {
            stopService(new Intent(this, PedometerManager.class));
            finish();
        }
    }

    public void startWorkoutFragment(View v) {
        startService(new Intent(this, PedometerManager.class));
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new FragmentWorkoutViewController()).addToBackStack(null)
                .commit();
    }

    public void closeWorkoutFragment(View v) {
        stopService(new Intent(this, PedometerManager.class));
        getFragmentManager().popBackStackImmediate();
    }


    public boolean handleItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStackImmediate();
                break;
            case R.id.action_settings:
                getFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, new FragmentSettings()).addToBackStack(null)
                        .commit();
                break;
            case R.id.action_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.about);
                TextView tv = new TextView(this);
                tv.setPadding(30, 30, 30, 30);
                tv.setText(R.string.about_text_links);

                try {
                    tv.append(getString(R.string.about_app_version,
                            getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
                } catch (NameNotFoundException e1) {
                    // should not happen as the app is definitely installed when
                    // seeing the dialog
                    e1.printStackTrace();
                }
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                builder.setView(tv);
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
                break;
        }
        return true;
    }
}