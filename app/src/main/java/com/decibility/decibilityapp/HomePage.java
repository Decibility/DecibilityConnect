package com.decibility.decibilityapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomePage extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page); // connect layout to activity

        Button connect_device_btn = findViewById(R.id.connect_device_btn);
        connect_device_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, ConnectDevicePage.class);

                Intent this_intent = getIntent();
                if (this_intent.hasExtra("min_volume_input")) {
                    intent.putExtra("min_volume_input", this_intent.getStringExtra("min_volume_input"));
                }
                if (this_intent.hasExtra("max_volume_input")) {
                    intent.putExtra("max_volume_input", this_intent.getStringExtra("max_volume_input"));
                }
                if (this_intent.hasExtra("min_frequency_input")) {
                    intent.putExtra("min_frequency_input", this_intent.getStringExtra("min_frequency_input"));
                }
                if (this_intent.hasExtra("max_frequency_input")) {
                    intent.putExtra("max_frequency_input", this_intent.getStringExtra("max_frequency_input"));
                }

                startActivity(intent);
            }
        });

        Button config_range_btn = findViewById(R.id.config_range_btn);
        config_range_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(HomePage.this, ConfigRangePage.class);
                Intent this_intent = getIntent();
                if (this_intent.hasExtra("min_volume_input")) {
                    intent1.putExtra("min_volume_input", this_intent.getStringExtra("min_volume_input"));
                }
                if (this_intent.hasExtra("max_volume_input")) {
                    intent1.putExtra("max_volume_input", this_intent.getStringExtra("max_volume_input"));
                }
                if (this_intent.hasExtra("min_frequency_input")) {
                    intent1.putExtra("min_frequency_input", this_intent.getStringExtra("min_frequency_input"));
                }
                if (this_intent.hasExtra("max_frequency_input")) {
                    intent1.putExtra("max_frequency_input", this_intent.getStringExtra("max_frequency_input"));
                }
                startActivity(intent1);
            }
        });

    }
}