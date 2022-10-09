package com.dennis_brink.android.smalltalk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private Button btnSignIn, btnSignUp;
    private TextView textForgotPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupLogo();

        editTextEmail = findViewById(R.id.editTextSignUpEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        textForgotPassword = findViewById(R.id.txtForgotPassword);
        progressBar = findViewById(R.id.progressBarSignIn);

        btnSignIn = findViewById(R.id.btnSignUp);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        textForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    textForgotPassword.setTextColor(Color.parseColor("#808080"));
                    Intent i = new Intent(LoginActivity.this, ResetPwdActivity.class);
                    startActivity(i);
                } catch(Exception e){
                    Log.d("DENNIS_B", "Error " + e.getLocalizedMessage());
                }
                finally{
                    textForgotPassword.setTextColor(Color.parseColor("#000000"));
                }
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(i);
                //finish()
            }
        });

    }

    private void setupLogo(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_logo_padding);
        getSupportActionBar().setTitle("Small Talk");
        getSupportActionBar().setSubtitle("");
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

}