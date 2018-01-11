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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.samwolfand.oneprefs.Prefs;

import io.multy.R;
import io.multy.storage.RealmManager;
import io.multy.storage.SecurePreferencesHelper;
import io.multy.ui.adapters.PinDotsAdapter;
import io.multy.ui.adapters.PinNumbersAdapter;
import io.multy.util.Constants;

/**
 * Created by Ihar Paliashchuk on 02.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class BaseActivity extends AppCompatActivity implements PinNumbersAdapter.OnFingerPrintClickListener, PinNumbersAdapter.OnNumberClickListener, View.OnClickListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private int count = 6;

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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if (v != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText && !v.getClass().getName().startsWith(Constants.ANDROID_PACKAGE)) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[Constants.ZERO];
            float y = ev.getRawY() + v.getTop() - scrcoords[Constants.ONE];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
            RealmManager.open(this);
//            final String counter = SecurePreferencesHelper.getString(this, Constants.PIN_COUNTER);
//            if (counter == null || counter.equals("")) {
                count = 6;
//                SecurePreferencesHelper.putString(this, Constants.PIN_COUNTER, String.valueOf(6));
//            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLock();
    }

    @Override
    protected void onDestroy() {
        if (Prefs.getBoolean(Constants.PREF_APP_INITIALIZED)) {
            RealmManager.close();
        }
        super.onDestroy();
    }

    private void showLock() {
        if (!Prefs.getBoolean(Constants.PREF_LOCK)) {
            return;
        }

        View fastOperation = findViewById(R.id.fast_operations);
        if (fastOperation != null) {
            fastOperation.setVisibility(View.GONE);
        }

        ViewGroup viewGroup = findViewById(R.id.container_main);
        if (viewGroup != null) {
            View convertView = getLayoutInflater().inflate(R.layout.fragment_pin, viewGroup, false);

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
//        final long time = SecurePreferencesHelper.getLong(this, Constants.PREF_LOCK_DATE);
//        if (time != 0 && time > System.currentTimeMillis()) {
//            final long dif = time - System.currentTimeMillis();
//            Toast.makeText(this, "Please try again in " + (dif / 1000) + " seconds", Toast.LENGTH_SHORT).show();
//            return;
//        }

        stringBuilder.append(String.valueOf(number));

        ImageView dot = (ImageView) dotsLayoutManager.getChildAt(stringBuilder.toString().length() - 1);
        dot.setBackgroundResource(R.drawable.circle_white);

        if (stringBuilder.toString().length() == 6) {
            if (!SecurePreferencesHelper.getString(this, Constants.PREF_PIN).equals(stringBuilder.toString())) {
                if (count != 1) {
                    count--;
//                    SecurePreferencesHelper.putString(this, Constants.PIN_COUNTER, String.valueOf(count));
                    showLock();
                    Toast.makeText(this, count + " number of tries remain", Toast.LENGTH_LONG).show();
                } else {
//                    final String multiplierString = SecurePreferencesHelper.getString(this, Constants.PREF_LOCK_MULTIPLIER);
//                    final int multiplier = multiplierString == null ? 3 : Integer.valueOf(multiplierString) * 2;
//
//                    SecurePreferencesHelper.putString(this, Constants.PREF_LOCK_MULTIPLIER, String.valueOf(multiplier));
//                    SecurePreferencesHelper.putString(this, Constants.PREF_LOCK_DATE, String.valueOf(System.currentTimeMillis() + (multiplier * 1000)));
//                    Toast.makeText(this, "Try again in " + multiplier + " seconds", Toast.LENGTH_LONG).show();
//                    SecurePreferencesHelper.putString(this, Constants.PIN_COUNTER, String.valueOf(6));
                    Toast.makeText(this, count + "You reached maximu, number of tries", Toast.LENGTH_LONG).show();
                    finish();
//                    RealmManager.clear();
//                    Prefs.clear();
//                    this.finish();
//                    System.exit(0);
                }
            } else {
                hideLock();
//                SecurePreferencesHelper.putString(this, Constants.PREF_LOCK_DATE, String.valueOf(0));
                SecurePreferencesHelper.putString(this, Constants.PIN_COUNTER, String.valueOf(6));
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
}
