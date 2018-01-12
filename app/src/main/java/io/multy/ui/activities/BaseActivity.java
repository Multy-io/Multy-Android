/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import net.khirr.library.foreground.Foreground;

import io.multy.R;
import io.multy.storage.RealmManager;
import io.multy.storage.SecurePreferencesHelper;
import io.multy.ui.adapters.PinDotsAdapter;
import io.multy.ui.adapters.PinNumbersAdapter;
import io.multy.util.Constants;

public class BaseActivity extends AppCompatActivity implements PinNumbersAdapter.OnFingerPrintClickListener, PinNumbersAdapter.OnNumberClickListener, View.OnClickListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private int count = 6;
    private Foreground.Listener foregroundListener;
    private RecyclerView.LayoutManager dotsLayoutManager;
    private StringBuilder stringBuilder;
    boolean isLockVisible = false;

    public void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
            if (!isLockVisible) {
                RealmManager.open(this);
            }
            count = 6;
        }

        foregroundListener = new Foreground.Listener() {
            @Override
            public void background() {
                if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
                    RealmManager.close();
                }

                if (Prefs.getBoolean(Constants.PREF_LOCK)) {
                    showLock();
                }
            }

            @Override
            public void foreground() {
                if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED) && !Prefs.getBoolean(Constants.PREF_LOCK)) {
                    RealmManager.open(BaseActivity.this);
                }


            }
        };

        Foreground.Companion.addListener(foregroundListener);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        Foreground.Companion.removeListener(foregroundListener);
        super.onDestroy();
    }

    void showLock() {
        if (!Prefs.getBoolean(Constants.PREF_LOCK)) {
            return;
        }

        if (Prefs.getBoolean(Constants.PREF_SELF_CLICKED)) {
            Prefs.putBoolean(Constants.PREF_SELF_CLICKED, false);
            return;
        }

        View fastOperation = findViewById(R.id.fast_operations);
        if (fastOperation != null) {
            fastOperation.setVisibility(View.GONE);
        }

        ViewGroup viewGroup = findViewById(R.id.container_main);
        if (viewGroup != null) {
            View convertView = getLayoutInflater().inflate(R.layout.fragment_pin, viewGroup, false);

            View previousView = viewGroup.findViewById(R.id.container_pin);
            if (previousView != null) {
                viewGroup.removeViewInLayout(previousView);
            }

            RecyclerView recyclerViewDots = convertView.findViewById(R.id.recycler_view_dots);
            RecyclerView recyclerViewNumbers = convertView.findViewById(R.id.recycler_view_numbers);

            dotsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerViewDots.setAdapter(new PinDotsAdapter());
            recyclerViewDots.setLayoutManager(dotsLayoutManager);
            recyclerViewDots.setHasFixedSize(true);

            recyclerViewNumbers.setAdapter(new PinNumbersAdapter(this, this, this, true));
            recyclerViewNumbers.setLayoutManager(new GridLayoutManager(this, 3));
            recyclerViewNumbers.setHasFixedSize(true);

            stringBuilder = new StringBuilder();
            viewGroup.addView(convertView);

            isLockVisible = true;
            RealmManager.open(this);
        }
    }

    private void hideLock() {
        if (!Prefs.getBoolean(Constants.PREF_LOCK)) {
            return;
        }

        View fastOperation = findViewById(R.id.fast_operations);
        if (fastOperation != null) {
            fastOperation.setVisibility(View.VISIBLE);
        }

        ViewGroup viewGroup = findViewById(R.id.container_main);
        if (viewGroup != null) {
            View locker = findViewById(R.id.container_pin);
            viewGroup.removeViewInLayout(locker);
            isLockVisible = false;
        }
    }

    @Override
    public void onFingerprintClick() {

    }

    @Override
    public void onNumberClick(int number) {
        stringBuilder.append(String.valueOf(number));

        ImageView dot = (ImageView) dotsLayoutManager.getChildAt(stringBuilder.toString().length() - 1);
        dot.setBackgroundResource(R.drawable.circle_white);

        if (stringBuilder.toString().length() >= 6) {
            if (!SecurePreferencesHelper.getString(this, Constants.PREF_PIN).equals(stringBuilder.toString())) {
                if (count != 1) {
                    count--;
                    showLock();
                    Toast.makeText(this, count + " number of tries remain", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, count + "You reached maximum, number of tries", Toast.LENGTH_LONG).show();
                    finish();
                }
            } else {
                hideLock();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (stringBuilder.length() > 0) {
            ImageView dot = (ImageView) dotsLayoutManager.getChildAt(stringBuilder.toString().length() - 1);
            dot.setBackgroundResource(R.drawable.circle_border_white);
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
    }

    @Override
    public void onBackPressed() {
        Prefs.putBoolean(Constants.PREF_SELF_CLICKED, true);
        super.onBackPressed();
    }
}
