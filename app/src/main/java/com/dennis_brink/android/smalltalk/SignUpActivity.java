package com.dennis_brink.android.smalltalk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputEditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword, editTextDisplayName;
    private Button btnSignUp;
    private CircleImageView imgAvatar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextDisplayName = findViewById(R.id.editTextSignUpName);
        editTextEmail = findViewById(R.id.editTextSignUpEmail);
        editTextPassword = findViewById(R.id.editTekstSignUpPassword);
        imgAvatar = findViewById(R.id.imgCircleAccount);
        progressBar = findViewById(R.id.progressBarSignUpSignUp);

        btnSignUp = findViewById(R.id.btnSignUpSignUp);
        btnSignUp.setOnClickListener(view -> {

        });

        imgAvatar.setOnClickListener(view -> {

        });

    }
}