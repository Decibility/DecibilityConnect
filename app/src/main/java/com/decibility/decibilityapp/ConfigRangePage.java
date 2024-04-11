package com.decibility.decibilityapp;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class ConfigRangePage extends AppCompatActivity {

    EditText min_volume_input;
    EditText max_volume_input;
    EditText min_frequency_input;
    EditText max_frequency_input;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_range_page);

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // get min volume if stored
        min_volume_input = (EditText) findViewById(R.id.min_volume_input);
        String stored_min_volume_input = sharedPreferences.getString("min_volume_input", "");
        min_volume_input.setText(stored_min_volume_input);

        // get max volume if stored
        max_volume_input = (EditText) findViewById(R.id.max_volume_input);
        String stored_max_volume_input = sharedPreferences.getString("max_volume_input", "");
        max_volume_input.setText(stored_max_volume_input);

        // get min frequency if stored
        min_frequency_input = (EditText) findViewById(R.id.min_frequency_input);
        String stored_min_frequency_input = sharedPreferences.getString("min_frequency_input", "");
        min_frequency_input.setText(stored_min_frequency_input);

        // get max frequency if stored
        max_frequency_input = (EditText) findViewById(R.id.max_frequency_input);
        String stored_max_frequency_input = sharedPreferences.getString("max_frequency_input", "");
        max_frequency_input.setText(stored_max_frequency_input);

        Button config_range_back_btn = findViewById(R.id.config_range_back_btn);
        config_range_back_btn.setOnClickListener(view -> {

            // save values
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // save min volume
            String min_volume_input_to_save = min_volume_input.getText().toString();
            editor.putString("min_volume_input", min_volume_input_to_save);

            // save max volume
            String max_volume_input_to_save = max_volume_input.getText().toString();
            editor.putString("max_volume_input", max_volume_input_to_save);

            // save min frequency
            String min_frequency_input_to_save = min_frequency_input.getText().toString();
            editor.putString("min_frequency_input", min_frequency_input_to_save);

            // save max frequency
            String max_frequency_input_to_save = max_frequency_input.getText().toString();
            editor.putString("max_frequency_input", max_frequency_input_to_save);
            editor.apply();

            // switch to home page
            Intent intent = new Intent(this, HomePage.class);
            startActivity(intent);
        });
    }

    // This callback is called only when there is a saved instance previously saved using onSaveInstanceState().
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        min_volume_input.setText(savedInstanceState.getString("MIN_VOL_KEY"));
        System.out.println("ON RESTORE INSTANCE STATE");
    }

    // Invoked when the activity might be temporarily destroyed; save the instance state here.
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("MIN_VOL_KEY", min_volume_input.getText().toString());

        System.out.println("SAVED INSTANCE STATE");
        // Call superclass to save any view hierarchy.
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();

        System.out.println("ON STOPPED");
    }
}
