package com.dennis_brink.android.smalltalk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputEditText;

public class ResetPwdActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail;
    private Button btnSend;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd);

        editTextEmail = findViewById(R.id.editTextResetEmail);
        btnSend = findViewById(R.id.btnForgotPasswordSendMail);
        progressBar = findViewById(R.id.progressBarForgotPassword);

        btnSend.setOnClickListener(view -> {

        });

    }
}