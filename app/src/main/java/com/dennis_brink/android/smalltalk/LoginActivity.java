package com.dennis_brink.android.smalltalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private Button btnSignIn, btnSignUp;
    private TextView textForgotPassword;
    private ProgressBar progressBar;

    FirebaseAuth auth;
    FirebaseUser fbuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupLogo();

        editTextEmail = findViewById(R.id.editTextSignUpEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        textForgotPassword = findViewById(R.id.txtForgotPassword);
        progressBar = findViewById(R.id.progressBarSignIn);

        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        auth = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(view -> {

            Log.d("DENNIS_B", "btnSignIn");

            Button btn = (Button) view;

            btn.setClickable(false);

            String userEmail = editTextEmail.getText().toString();
            String userPassword = editTextPassword.getText().toString();

            if(userEmail == null || userEmail.isEmpty()){
                Toast.makeText(this, "Please enter an e-mail address", Toast.LENGTH_SHORT).show();
                btn.setClickable(true);
                return;
            }
            if(userPassword == null || userPassword.isEmpty()){
                Toast.makeText(this, "Please enter a password, password cannot be empty", Toast.LENGTH_SHORT).show();
                btn.setClickable(true);
                return;
            }

            signIn(userEmail, userPassword, view);

        });

        textForgotPassword.setOnClickListener(view -> {
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
        });

        btnSignUp.setOnClickListener(view -> {
            Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(i);
            //finish()
        });

    }

    private void setupLogo(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_logo_padding);
        getSupportActionBar().setTitle("Small Talk");
        getSupportActionBar().setSubtitle("");
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

    private void startMainActivity(){
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void signIn(String userEmail, String userPassword, View view) {
        progressBar.setVisibility(View.VISIBLE);
        Button btn = (Button) view;

        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                progressBar.setVisibility(View.INVISIBLE);
                startMainActivity();
            } else {
                // not successful
                progressBar.setVisibility(View.INVISIBLE);
                btn.setClickable(true);
                Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        fbuser = auth.getCurrentUser();
        if(fbuser != null ){
            Log.d("DENNIS_B", "(MainActivity) - onStart(): User " + fbuser.getEmail() + " is logged in. Start MainActivity");
            startMainActivity();
        }
    }
}