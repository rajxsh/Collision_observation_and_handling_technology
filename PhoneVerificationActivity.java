package com.example.coht;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class PhoneVerificationActivity extends AppCompatActivity {
    private static final String KEY = "com.lifeline.secret";
    private static final String STATE = "com.lifeline.state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        SharedPreferences.Editor editor = getSharedPreferences(KEY, MODE_PRIVATE).edit();
        editor.putString(STATE, "verification");
        editor.apply();
    }

    public void goToDashboard(View view) {
        finish();
        startActivity(new Intent(this, DashboardActivity.class));
    }
}
