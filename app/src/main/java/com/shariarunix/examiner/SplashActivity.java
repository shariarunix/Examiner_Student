package com.shariarunix.examiner;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progress_bar);

        Thread threadOne = new Thread(this::showProgress);
        threadOne.start();
        Thread threadTwo = new Thread(() -> {
            try {
                Thread.sleep(1500);

                SharedPreferences sharedPreferences = getSharedPreferences("examinerPref", MODE_PRIVATE);
                boolean userCheck = sharedPreferences.getBoolean("userCheck", false);

                if (userCheck) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        threadTwo.start();
    }

    private void showProgress() {

        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(15);
                progressBar.setProgress(i);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }
}